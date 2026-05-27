package com.health.backend.dto;

import java.util.List;

// ---- Frontend requests (API doc format) ----

public record AiProfileRequest(String timeRange) {}

public record AiPlanRequest(String healthNeed) {}

public record AiPushRequest(Long profileId) {}

// ---- Frontend responses (wrapped in ApiResponse.data) ----

public record AiProfileData(
    Long profileId,
    List<String> healthTags,
    String riskLevel,
    String analysis
) {}

public record AiPlanData(
    Long planId,
    String planName,
    String dietSuggest,
    String sportSuggest,
    String sleepSuggest
) {}

public record AiPushData(List<Long> pushIds) {}
