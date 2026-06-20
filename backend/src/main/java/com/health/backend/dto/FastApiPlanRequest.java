package com.health.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FastApiPlanRequest(
    @JsonProperty("userId") Long userId,
    @JsonProperty("healthData") List<FastApiHealthDataItem> healthData,
    @JsonProperty("healthNeed") String healthNeed
) {}
