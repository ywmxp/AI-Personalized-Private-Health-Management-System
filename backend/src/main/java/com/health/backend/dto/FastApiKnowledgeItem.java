package com.health.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FastApiKnowledgeItem(
    @JsonProperty("knowledgeId") Long knowledgeId,
    String title,
    @JsonProperty("relateTag") String relateTag
) {}
