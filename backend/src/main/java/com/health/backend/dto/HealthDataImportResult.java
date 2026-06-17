package com.health.backend.dto;

public record HealthDataImportResult(
    int importedCount,
    int overwrittenCount,
    int skippedDuplicateCount
) {
}
