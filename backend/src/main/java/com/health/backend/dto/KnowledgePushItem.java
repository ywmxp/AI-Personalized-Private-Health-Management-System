package com.health.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 知识推送记录（含关联知识标题和标签，用于前端推送列表展示）
 */
public record KnowledgePushItem(
    @JsonProperty("pushId") Long pushId,
    @JsonProperty("knowledgeId") Long knowledgeId,
    String title,
    @JsonProperty("relateTag") String relateTag,
    @JsonProperty("pushTime") String pushTime,
    @JsonProperty("isRead") Integer isRead
) {}
