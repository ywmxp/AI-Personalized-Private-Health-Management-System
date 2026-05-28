package com.health.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FastApiPushRequest(
    @JsonProperty("profileId") Long profileId,
    @JsonProperty("healthTags") List<String> healthTags,
    @JsonProperty("knowledgeItems") List<FastApiKnowledgeItem> knowledgeItems
) {}
