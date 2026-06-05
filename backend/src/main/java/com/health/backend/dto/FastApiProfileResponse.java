package com.health.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FastApiProfileResponse(
    @JsonProperty("healthTags") List<String> healthTags,
    @JsonProperty("riskLevel") String riskLevel,
    String analysis
) {}
