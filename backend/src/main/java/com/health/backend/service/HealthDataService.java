package com.health.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.health.backend.domain.HealthData;
import com.health.backend.dto.PageResponse;
import com.health.backend.repository.HealthDataRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class HealthDataService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
}
