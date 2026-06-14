package com.health.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.health.backend.dto.AiPlanData;
import com.health.backend.dto.AiPlanRequest;
import com.health.backend.dto.AiProfileData;
import com.health.backend.dto.AiProfileRequest;
import com.health.backend.dto.AiPushData;
import com.health.backend.dto.AiPushRequest;
import com.health.backend.dto.ApiResponse;
import com.health.backend.security.JwtUser;
import com.health.backend.service.AiProxyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiProxyService aiProxyService;

    public AiController(AiProxyService aiProxyService) {
        this.aiProxyService = aiProxyService;
    }

    @PostMapping("/profile")
    public ApiResponse<AiProfileData> generateProfile(
        @AuthenticationPrincipal JwtUser currentUser,
        @Valid @RequestBody AiProfileRequest request
    ) {
        AiProfileData data = aiProxyService.generateProfile(currentUser.userId(), request.timeRange());
        return ApiResponse.success(data);
    }

    @PostMapping("/plan")
    public ApiResponse<AiPlanData> generatePlan(
        @AuthenticationPrincipal JwtUser currentUser,
        @Valid @RequestBody AiPlanRequest request
    ) {
        AiPlanData data = aiProxyService.generatePlan(currentUser.userId(), request.healthNeed());
        return ApiResponse.success(data);
    }

    @PostMapping("/knowledge-push")
    public ApiResponse<AiPushData> pushKnowledge(
        @AuthenticationPrincipal JwtUser currentUser,
        @Valid @RequestBody AiPushRequest request
    ) {
        AiPushData data = aiProxyService.pushKnowledge(currentUser.userId(), request.profileId());
        return ApiResponse.success(data);
    }
}
