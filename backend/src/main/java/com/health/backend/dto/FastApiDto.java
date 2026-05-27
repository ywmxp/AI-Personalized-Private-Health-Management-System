package com.health.backend.dto;

import java.util.List;

// ---- Internal FastAPI request bodies (snake_case JSON keys via @JsonProperty) ----

import com.fasterxml.jackson.annotation.JsonProperty;

public record FastApiHealthDataItem(
    @JsonProperty("dataType") String dataType,
    @JsonProperty("dataValue") String dataValue,
    @JsonProperty("recordTime") String recordTime
) {}

public record FastApiProfileRequest(
    @JsonProperty("userId") Long userId,
    @JsonProperty("healthData") List<FastApiHealthDataItem> healthData,
    @JsonProperty("timeRange") String timeRange
) {}

public record FastApiProfileResponse(
    @JsonProperty("healthTags") List<String> healthTags,
    @JsonProperty("riskLevel") String riskLevel,
    String analysis
) {}

public record FastApiPlanRequest(
    @JsonProperty("userId") Long userId,
    @JsonProperty("healthData") List<FastApiHealthDataItem> healthData,
    @JsonProperty("healthNeed") String healthNeed
) {}

public record FastApiPlanResponse(
    @JsonProperty("planName") String planName,
    @JsonProperty("dietSuggest") String dietSuggest,
    @JsonProperty("sportSuggest") String sportSuggest,
    @JsonProperty("sleepSuggest") String sleepSuggest
) {}

public record FastApiKnowledgeItem(
    @JsonProperty("knowledgeId") Long knowledgeId,
    String title,
    @JsonProperty("relateTag") String relateTag
) {}

public record FastApiPushRequest(
    @JsonProperty("profileId") Long profileId,
    @JsonProperty("healthTags") List<String> healthTags,
    @JsonProperty("knowledgeItems") List<FastApiKnowledgeItem> knowledgeItems
) {}

public record FastApiPushResponse(
    @JsonProperty("selectedKnowledgeIds") List<Long> selectedKnowledgeIds,
    String reason
) {}
