package com.health.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FastApiPlanResponse(
    @JsonProperty("planName") String planName,
    @JsonProperty("dietSuggest") String dietSuggest,
    @JsonProperty("sportSuggest") String sportSuggest,
    @JsonProperty("sleepSuggest") String sleepSuggest
) {}
