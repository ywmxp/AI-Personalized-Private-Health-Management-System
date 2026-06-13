package com.health.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.backend.domain.HealthData;
import com.health.backend.domain.HealthPlan;
import com.health.backend.domain.HealthProfile;
import com.health.backend.domain.Knowledge;
import com.health.backend.domain.KnowledgePush;
import com.health.backend.dto.AiPlanData;
import com.health.backend.dto.AiProfileData;
import com.health.backend.dto.AiPushData;
import com.health.backend.dto.FastApiHealthDataItem;
import com.health.backend.dto.FastApiKnowledgeItem;
import com.health.backend.dto.FastApiPlanRequest;
import com.health.backend.dto.FastApiPlanResponse;
import com.health.backend.dto.FastApiProfileRequest;
import com.health.backend.dto.FastApiProfileResponse;
import com.health.backend.dto.FastApiPushRequest;
import com.health.backend.dto.FastApiPushResponse;
import com.health.backend.exception.BusinessException;
import com.health.backend.exception.ErrorCode;
import com.health.backend.repository.HealthDataRepository;
import com.health.backend.repository.HealthPlanRepository;
import com.health.backend.repository.HealthProfileRepository;
import com.health.backend.repository.KnowledgePushRepository;
import com.health.backend.repository.KnowledgeRepository;

@Service
public class AiProxyService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${AI_SERVICE_HOST:127.0.0.1}")
    private String aiHost;

    @Value("${AI_SERVICE_PORT:30000}")
    private int aiPort;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final HealthDataRepository healthDataRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final HealthPlanRepository healthPlanRepository;
    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgePushRepository knowledgePushRepository;

    public AiProxyService(RestTemplate restTemplate,
                          ObjectMapper objectMapper,
                          HealthDataRepository healthDataRepository,
                          HealthProfileRepository healthProfileRepository,
                          HealthPlanRepository healthPlanRepository,
                          KnowledgeRepository knowledgeRepository,
                          KnowledgePushRepository knowledgePushRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.healthDataRepository = healthDataRepository;
        this.healthProfileRepository = healthProfileRepository;
        this.healthPlanRepository = healthPlanRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.knowledgePushRepository = knowledgePushRepository;
    }

    // ---- Profile ----

    public AiProfileData generateProfile(Long userId, String timeRange) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = resolveStart(timeRange);

        List<HealthData> records = healthDataRepository
            .findByUserIdAndRecordTimeBetweenOrderByRecordTimeAsc(userId, start, end);

        List<FastApiHealthDataItem> items = records.stream()
            .map(r -> new FastApiHealthDataItem(r.getDataType(), r.getDataValue(),
                r.getRecordTime().format(DT_FMT)))
            .toList();

        FastApiProfileRequest req = new FastApiProfileRequest(userId, items, timeRange);
        FastApiProfileResponse resp = post("/api/ai/profile", req, FastApiProfileResponse.class);

        HealthProfile profile = new HealthProfile();
        profile.setUserId(userId);
        profile.setTimeRange(timeRange);
        try {
            profile.setHealthTags(objectMapper.writeValueAsString(resp.healthTags()));
        } catch (JsonProcessingException e) {
            profile.setHealthTags("[]");
        }
        profile.setRiskLevel(resp.riskLevel());
        profile.setAnalysis(resp.analysis());
        profile = healthProfileRepository.save(profile);

        return new AiProfileData(profile.getProfileId(), resp.healthTags(), resp.riskLevel(), resp.analysis());
    }

    // ---- Plan ----

    public AiPlanData generatePlan(Long userId, String healthNeed) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);

        List<HealthData> records = healthDataRepository
            .findByUserIdAndRecordTimeBetweenOrderByRecordTimeAsc(userId, start, end);

        List<FastApiHealthDataItem> items = records.stream()
            .map(r -> new FastApiHealthDataItem(r.getDataType(), r.getDataValue(),
                r.getRecordTime().format(DT_FMT)))
            .toList();

        FastApiPlanRequest req = new FastApiPlanRequest(userId, items, healthNeed);
        FastApiPlanResponse resp = post("/api/ai/plan", req, FastApiPlanResponse.class);

        HealthPlan plan = new HealthPlan();
        plan.setUserId(userId);
        plan.setPlanName(resp.planName());
        plan.setHealthNeed(healthNeed);
        plan.setDietSuggest(resp.dietSuggest());
        plan.setSportSuggest(resp.sportSuggest());
        plan.setSleepSuggest(resp.sleepSuggest());
        plan.setIsValid(1);
        plan.setFeedbackStatus("NONE");
        plan = healthPlanRepository.save(plan);

        return new AiPlanData(plan.getPlanId(), resp.planName(),
            resp.dietSuggest(), resp.sportSuggest(), resp.sleepSuggest());
    }

    // ---- Knowledge Push ----

    public AiPushData pushKnowledge(Long userId, Long profileId) {
        HealthProfile profile;
        if (profileId == null || profileId == 0) {
            profile = healthProfileRepository.findFirstByUserIdOrderByCreateTimeDesc(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "未找到健康画像，请先生成健康画像"));
        } else {
            profile = healthProfileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "画像不存在"));
        }

        List<String> healthTags;
        try {
            healthTags = objectMapper.readValue(profile.getHealthTags(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            healthTags = List.of();
        }

        List<Knowledge> allKnowledge = knowledgeRepository.findAll();
        List<FastApiKnowledgeItem> knowledgeItems = allKnowledge.stream()
            .map(k -> new FastApiKnowledgeItem(k.getKnowledgeId(), k.getTitle(), k.getRelateTag()))
            .toList();

        FastApiPushRequest req = new FastApiPushRequest(profileId, healthTags,
            profile.getRiskLevel(), profile.getAnalysis(), knowledgeItems);
        FastApiPushResponse resp = post("/api/ai/knowledge-push", req, FastApiPushResponse.class);

        List<Long> pushIds = new ArrayList<>();
        for (Long knowledgeId : resp.selectedKnowledgeIds()) {
            KnowledgePush push = new KnowledgePush();
            push.setUserId(userId);
            push.setKnowledgeId(knowledgeId);
            push.setIsRead(0);
            push = knowledgePushRepository.save(push);
            pushIds.add(push.getPushId());
        }

        return new AiPushData(pushIds);
    }

    // ---- Internal helpers ----

    private String baseUrl() {
        return "http://" + aiHost + ":" + aiPort;
    }

    private <T, R> R post(String path, T requestBody, Class<R> responseType) {
        String url = baseUrl() + path;
        try {
            return restTemplate.postForObject(url, requestBody, responseType);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "AI service error: " + e.getMessage());
        }
    }

    private LocalDateTime resolveStart(String timeRange) {
        LocalDateTime end = LocalDateTime.now();
        return switch (timeRange) {
            case "LAST_7_DAYS" -> end.minusDays(7);
            case "LAST_30_DAYS" -> end.minusDays(30);
            case "LAST_90_DAYS" -> end.minusDays(90);
            default -> end.minusDays(7);
        };
    }
}
