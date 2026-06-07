package com.health.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FastApiPushResponse(
    @JsonProperty("selectedKnowledgeIds") List<Long> selectedKnowledgeIds,
    String reason
) {}
