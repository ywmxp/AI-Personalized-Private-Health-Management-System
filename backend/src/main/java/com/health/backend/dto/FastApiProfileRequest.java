package com.health.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FastApiProfileRequest(
    @JsonProperty("userId") Long userId,
    @JsonProperty("healthData") List<FastApiHealthDataItem> healthData,
    @JsonProperty("timeRange") String timeRange
) {}
