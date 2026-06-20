package com.health.backend.dto;

public record HealthDataImportConflictItem(
    HealthDataComparisonItem existingRecord,
    HealthDataComparisonItem incomingRecord
) {
}
