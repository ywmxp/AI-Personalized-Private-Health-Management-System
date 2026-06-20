package com.health.backend.dto;

public record AiPlanData(
    Long planId,
    String planName,
    String dietSuggest,
    String sportSuggest,
    String sleepSuggest
) {}
