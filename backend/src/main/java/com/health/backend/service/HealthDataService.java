package com.health.backend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.health.backend.domain.HealthData;
import com.health.backend.dto.PageResponse;
import com.health.backend.dto.HealthDataComparisonItem;
import com.health.backend.dto.HealthDataConflictResponse;
import com.health.backend.dto.HealthDataImportConflictItem;
import com.health.backend.dto.HealthDataImportConflictResponse;
import com.health.backend.dto.HealthDataImportDuplicateAction;
import com.health.backend.dto.HealthDataImportResult;
import com.health.backend.exception.BusinessException;
import com.health.backend.exception.ErrorCode;
import com.health.backend.repository.HealthDataRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class HealthDataService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> CSV_HEADER = List.of("dataType", "dataValue", "unit", "recordTime");

    private final HealthDataRepository healthDataRepository;

    public HealthDataService(HealthDataRepository healthDataRepository) {
        this.healthDataRepository = healthDataRepository;
    }

    /** 录入健康数据 */
    @Transactional
    public HealthData create(Long userId, String dataType, String dataValue, String unit, LocalDateTime recordTime) {
        return create(userId, dataType, dataValue, unit, recordTime, false);
    }

    /** 录入健康数据 */
    @Transactional
    public HealthData create(
        Long userId,
        String dataType,
        String dataValue,
        String unit,
        LocalDateTime recordTime,
        boolean overwrite
    ) {
        HealthDataValidator.ValidatedHealthData validated =
            HealthDataValidator.validateForCreate(dataType, dataValue, unit, recordTime);

        Optional<HealthData> existingRecord = healthDataRepository
            .findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(
                userId, validated.dataType(), validated.recordTime());
        if (existingRecord.isPresent()) {
            if (!overwrite) {
                throw duplicateHealthData(existingRecord.get(), validated);
            }
            HealthData data = existingRecord.get();
            data.setDataValue(validated.dataValue());
            data.setUnit(validated.unit());
            return healthDataRepository.save(data);
        }

        HealthData data = new HealthData();
        data.setUserId(userId);
        data.setDataType(validated.dataType());
        data.setDataValue(validated.dataValue());
        data.setUnit(validated.unit());
        data.setRecordTime(validated.recordTime());
        return healthDataRepository.save(data);
    }

    /** 查询健康数据列表（分页 + 筛选） */
    @Transactional(readOnly = true)
    public PageResponse<HealthData> query(Long userId, String dataType,
                                           LocalDateTime startTime, LocalDateTime endTime,
                                           int page, int size) {
        Specification<HealthData> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            if (dataType != null && !dataType.isBlank()) {
                predicates.add(cb.equal(root.get("dataType"), dataType));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordTime"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("recordTime"), endTime));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<HealthData> result = healthDataRepository.findAll(spec,
            PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Order.desc("recordTime"), Sort.Order.desc("dataId"))));

        return new PageResponse<>(result.getContent(), page, size, result.getTotalElements());
    }

    /** 批量导入健康数据 CSV */
    @Transactional
    public int importCsv(Long userId, MultipartFile file) {
        return importCsv(userId, file, null).importedCount();
    }

    /** 批量导入健康数据 CSV */
    @Transactional
    public HealthDataImportResult importCsv(
        Long userId,
        MultipartFile file,
        HealthDataImportDuplicateAction duplicateAction
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "CSV文件不能为空");
        }

        List<HealthData> records = deduplicateCsvRecords(parseCsv(userId, file));
        if (records.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "CSV文件未包含有效数据行");
        }

        List<HealthData> newRecords = new ArrayList<>();
        List<HealthData> overwriteRecords = new ArrayList<>();
        List<HealthDataImportConflictItem> conflicts = new ArrayList<>();

        for (HealthData incomingRecord : records) {
            Optional<HealthData> existingRecord = healthDataRepository
                .findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(
                    userId,
                    incomingRecord.getDataType(),
                    incomingRecord.getRecordTime());
            if (existingRecord.isEmpty()) {
                newRecords.add(incomingRecord);
                continue;
            }

            HealthData existingData = existingRecord.get();
            conflicts.add(new HealthDataImportConflictItem(
                HealthDataComparisonItem.from(existingData),
                HealthDataComparisonItem.from(incomingRecord)));

            if (duplicateAction == HealthDataImportDuplicateAction.OVERWRITE) {
                existingData.setDataValue(incomingRecord.getDataValue());
                existingData.setUnit(incomingRecord.getUnit());
                overwriteRecords.add(existingData);
            }
        }

        if (!conflicts.isEmpty() && duplicateAction == null) {
            throw new BusinessException(
                ErrorCode.DUPLICATE_HEALTH_DATA_IMPORT,
                "CSV中存在重复健康数据，请选择覆盖或跳过重复项",
                new HealthDataImportConflictResponse(conflicts.size(), conflicts)
            );
        }

        if (!newRecords.isEmpty()) {
            healthDataRepository.saveAll(newRecords);
        }
        if (!overwriteRecords.isEmpty()) {
            healthDataRepository.saveAll(overwriteRecords);
        }

        return new HealthDataImportResult(
            newRecords.size(),
            overwriteRecords.size(),
            duplicateAction == HealthDataImportDuplicateAction.SKIP ? conflicts.size() : 0
        );
    }

    /** 删除健康数据 */
    @Transactional
    public void delete(Long dataId, Long userId) {
        HealthData data = healthDataRepository.findById(dataId)
            .orElseThrow(() -> new RuntimeException("数据不存在"));
        if (!data.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该数据");
        }
        healthDataRepository.delete(data);
    }

    /** 健康趋势（按日期聚合） */
    @Transactional(readOnly = true)
    public Map<String, Object> getTrends(Long userId, String dataType,
                                          LocalDateTime startTime, LocalDateTime endTime) {
        List<HealthData> records = healthDataRepository
            .findByUserIdAndRecordTimeBetweenOrderByRecordTimeAsc(userId, startTime, endTime);

        List<Map<String, String>> points = records.stream()
            .filter(r -> r.getDataType().equals(dataType))
            .map(r -> Map.of("date", r.getRecordTime().toLocalDate().toString(), "value", r.getDataValue()))
            .toList();

        return Map.of("dataType", dataType, "points", points);
    }

    private List<HealthData> parseCsv(Long userId, MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "CSV文件不能为空");
            }

            List<String> header = parseCsvLine(removeBom(headerLine));
            if (!CSV_HEADER.equals(header)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "CSV表头必须为dataType,dataValue,unit,recordTime");
            }

            List<HealthData> records = new ArrayList<>();
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                records.add(parseCsvRecord(userId, line, lineNumber));
            }
            return records;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "CSV文件读取失败");
        }
    }

    private HealthData parseCsvRecord(Long userId, String line, int lineNumber) {
        List<String> columns = parseCsvLine(line);
        if (columns.size() != CSV_HEADER.size()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "第" + lineNumber + "行：列数必须为4");
        }

        String dataType = columns.get(0).trim();
        String dataValue = columns.get(1).trim();
        String unit = columns.get(2).trim();
        String recordTimeText = columns.get(3).trim();

        LocalDateTime recordTime;
        try {
            recordTime = LocalDateTime.parse(recordTimeText, DT_FMT);
        } catch (DateTimeParseException e) {
            throw new BusinessException(
                ErrorCode.VALIDATION_ERROR,
                "第" + lineNumber + "行：recordTime格式必须为yyyy-MM-dd HH:mm:ss"
            );
        }

        HealthDataValidator.ValidatedHealthData validated =
            HealthDataValidator.validateForCsv(dataType, dataValue, unit, recordTime, lineNumber);

        HealthData data = new HealthData();
        data.setUserId(userId);
        data.setDataType(validated.dataType());
        data.setDataValue(validated.dataValue());
        data.setUnit(validated.unit());
        data.setRecordTime(validated.recordTime());
        return data;
    }

    private List<String> parseCsvLine(String line) {
        return Arrays.asList(line.split(",", -1));
    }

    private String removeBom(String line) {
        if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        }
        return line;
    }

    private List<HealthData> deduplicateCsvRecords(List<HealthData> records) {
        Map<String, HealthData> deduplicated = new LinkedHashMap<>();
        for (HealthData record : records) {
            deduplicated.put(buildDuplicateKey(record.getDataType(), record.getRecordTime()), record);
        }
        return new ArrayList<>(deduplicated.values());
    }

    private BusinessException duplicateHealthData(
        HealthData existingRecord,
        HealthDataValidator.ValidatedHealthData incomingRecord
    ) {
        return new BusinessException(
            ErrorCode.DUPLICATE_HEALTH_DATA,
            "该时间已存在同类型健康数据，请确认是否覆盖原记录",
            new HealthDataConflictResponse(
                HealthDataComparisonItem.from(existingRecord),
                new HealthDataComparisonItem(
                    incomingRecord.dataType(),
                    incomingRecord.dataValue(),
                    incomingRecord.unit(),
                    incomingRecord.recordTime()))
        );
    }

    private String buildDuplicateKey(String dataType, LocalDateTime recordTime) {
        return dataType + "|" + recordTime.format(DT_FMT);
    }
}
