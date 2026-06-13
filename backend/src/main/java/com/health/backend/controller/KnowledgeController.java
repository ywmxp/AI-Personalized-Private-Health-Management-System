package com.health.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.health.backend.domain.Knowledge;
import com.health.backend.dto.ApiResponse;
import com.health.backend.dto.KnowledgePushItem;
import com.health.backend.dto.PageResponse;
import com.health.backend.security.JwtUser;
import com.health.backend.service.KnowledgeService;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    /** 知识库列表（分页 + 关键词/标签筛选） */
    @GetMapping
    public ApiResponse<PageResponse<Knowledge>> listKnowledge(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String tag,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(knowledgeService.listKnowledge(keyword, tag, page, size));
    }

    /** 我的推送列表 */
    @GetMapping("/pushes")
    public ApiResponse<PageResponse<KnowledgePushItem>> listPushes(
        @AuthenticationPrincipal JwtUser currentUser,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(knowledgeService.listPushes(currentUser.userId(), page, size));
    }

    /** 标记推送为已读 */
    @PatchMapping("/pushes/{pushId}/read")
    public ApiResponse<Void> markRead(
        @AuthenticationPrincipal JwtUser currentUser,
        @PathVariable Long pushId
    ) {
        knowledgeService.markRead(pushId, currentUser.userId());
        return ApiResponse.success(null);
    }
}
