package com.health.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FastApiHealthDataItem(
    @JsonProperty("dataType") String dataType,
    @JsonProperty("dataValue") String dataValue,
    @JsonProperty("recordTime") String recordTime
) {}
