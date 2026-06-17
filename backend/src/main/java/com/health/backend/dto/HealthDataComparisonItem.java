package com.health.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.health.backend.domain.HealthData;

public record HealthDataComparisonItem(
    String dataType,
    String dataValue,
    String unit,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime recordTime
) {
    public static HealthDataComparisonItem from(HealthData data) {
        return new HealthDataComparisonItem(
            data.getDataType(),
            data.getDataValue(),
            data.getUnit(),
            data.getRecordTime()
        );
    }
}
