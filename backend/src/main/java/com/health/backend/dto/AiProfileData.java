package com.health.backend.dto;

import java.util.List;

public record AiProfileData(
    Long profileId,
    List<String> healthTags,
    String riskLevel,
    String analysis
) {}
