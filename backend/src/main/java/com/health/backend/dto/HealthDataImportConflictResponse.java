package com.health.backend.dto;

import java.util.List;

public record HealthDataImportConflictResponse(
    int duplicateCount,
    List<HealthDataImportConflictItem> duplicates
) {
}
