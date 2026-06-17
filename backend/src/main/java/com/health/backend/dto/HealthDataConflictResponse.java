package com.health.backend.dto;

public record HealthDataConflictResponse(
    HealthDataComparisonItem existingRecord,
    HealthDataComparisonItem incomingRecord
) {
}
