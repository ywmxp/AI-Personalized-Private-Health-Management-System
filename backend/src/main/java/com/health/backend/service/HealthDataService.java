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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.health.backend.domain.HealthData;
import com.health.backend.dto.PageResponse;
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
        HealthData data = new HealthData();
        data.setUserId(userId);
        data.setDataType(dataType);
        data.setDataValue(dataValue);
        data.setUnit(unit);
        data.setRecordTime(recordTime != null ? recordTime : LocalDateTime.now());
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
            PageRequest.of(page - 1, size));

        return new PageResponse<>(result.getContent(), page, size, result.getTotalElements());
    }

    /** 批量导入健康数据 CSV */
    @Transactional
    public int importCsv(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "CSV文件不能为空");
        }

        List<HealthData> records = parseCsv(userId, file);
        if (records.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "CSV文件未包含有效数据行");
        }

        healthDataRepository.saveAll(records);
        return records.size();
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
            throw csvLineError(lineNumber, "列数必须为4");
        }

        String dataType = columns.get(0).trim();
        String dataValue = columns.get(1).trim();
        String unit = columns.get(2).trim();
        String recordTimeText = columns.get(3).trim();

        if (dataType.isEmpty()) {
            throw csvLineError(lineNumber, "dataType不能为空");
        }
        if (dataValue.isEmpty()) {
            throw csvLineError(lineNumber, "dataValue不能为空");
        }
        if (recordTimeText.isEmpty()) {
            throw csvLineError(lineNumber, "recordTime不能为空");
        }

        LocalDateTime recordTime;
        try {
            recordTime = LocalDateTime.parse(recordTimeText, DT_FMT);
        } catch (DateTimeParseException e) {
            throw csvLineError(lineNumber, "recordTime格式必须为yyyy-MM-dd HH:mm:ss");
        }

        HealthData data = new HealthData();
        data.setUserId(userId);
        data.setDataType(dataType);
        data.setDataValue(dataValue);
        data.setUnit(unit.isEmpty() ? null : unit);
        data.setRecordTime(recordTime);
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

    private BusinessException csvLineError(int lineNumber, String message) {
        return new BusinessException(ErrorCode.VALIDATION_ERROR, "第" + lineNumber + "行：" + message);
    }
}
