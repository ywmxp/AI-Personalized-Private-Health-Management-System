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
import com.health.backend.repository.UserRepository;

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
    private final UserRepository userRepository;

    public AiProxyService(RestTemplate restTemplate,
                          ObjectMapper objectMapper,
                          HealthDataRepository healthDataRepository,
                          HealthProfileRepository healthProfileRepository,
                          HealthPlanRepository healthPlanRepository,
                          KnowledgeRepository knowledgeRepository,
                          KnowledgePushRepository knowledgePushRepository,
                          UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.healthDataRepository = healthDataRepository;
        this.healthProfileRepository = healthProfileRepository;
        this.healthPlanRepository = healthPlanRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.knowledgePushRepository = knowledgePushRepository;
        this.userRepository = userRepository;
    }

    // ==================== Profile ====================

    public AiProfileData generateProfile(Long userId, String timeRange) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = resolveStart(timeRange);

        List<HealthData> records = healthDataRepository
            .findByUserIdAndRecordTimeBetweenOrderByRecordTimeAsc(userId, start, end);

        if (records.isEmpty()) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "所选时段没有健康数据可供分析");
        }

        FastApiProfileResponse resp;
        try {
            List<FastApiHealthDataItem> items = records.stream()
                .map(r -> new FastApiHealthDataItem(r.getDataType(), r.getDataValue(),
                    r.getRecordTime().format(DT_FMT)))
                .toList();
            FastApiProfileRequest req = new FastApiProfileRequest(userId, items, timeRange);
            resp = post("/api/ai/profile", req, FastApiProfileResponse.class);
        } catch (Exception e) {
            resp = mockProfile(userId, records);
        }

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

    // ==================== Plan ====================

    public AiPlanData generatePlan(Long userId, String healthNeed) {
        FastApiPlanResponse resp;
        try {
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minusDays(30);
            List<HealthData> records = healthDataRepository
                .findByUserIdAndRecordTimeBetweenOrderByRecordTimeAsc(userId, start, end);
            List<FastApiHealthDataItem> items = records.stream()
                .map(r -> new FastApiHealthDataItem(r.getDataType(), r.getDataValue(),
                    r.getRecordTime().format(DT_FMT)))
                .toList();
            FastApiPlanRequest req = new FastApiPlanRequest(userId, items, healthNeed);
            resp = post("/api/ai/plan", req, FastApiPlanResponse.class);
        } catch (Exception e) {
            resp = mockPlan(userId, healthNeed);
        }

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

    // ==================== Knowledge Push ====================

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

        FastApiPushResponse resp;
        try {
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
            resp = post("/api/ai/knowledge-push", req, FastApiPushResponse.class);
        } catch (Exception e) {
            resp = mockPush();
        }

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

    // ==================== Internal ====================

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

    // ==================== Mock fallback ====================

    private FastApiProfileResponse mockProfile(Long userId, List<HealthData> records) {
        var user = userRepository.findById(userId).orElse(null);
        String gender = user != null && user.getGender() != null ? user.getGender().name() : "UNKNOWN";
        var height = user != null && user.getHeight() != null ? user.getHeight() : null;
        var birthDate = user != null ? user.getBirthDate() : null;

        List<String> tags = new ArrayList<>();
        List<String> riskReasons = new ArrayList<>();

        // 体重分析（结合身高算 BMI）
        var weightRecord = records.stream().filter(r -> "WEIGHT".equals(r.getDataType()))
            .reduce((first, second) -> second).orElse(null);
        if (weightRecord != null && height != null) {
            double w = Double.parseDouble(weightRecord.getDataValue());
            double h = height.doubleValue() / 100.0;
            double bmi = w / (h * h);
            if (bmi < 18.5) { tags.add("体重偏低"); riskReasons.add("BMI " + String.format("%.1f", bmi) + "，体重偏低"); }
            else if (bmi < 24) tags.add("体重正常");
            else if (bmi < 28) { tags.add("体重偏高"); riskReasons.add("BMI " + String.format("%.1f", bmi) + "，超重"); }
            else { tags.add("体重超标"); riskReasons.add("BMI " + String.format("%.1f", bmi) + "，肥胖"); }
        } else if (weightRecord != null) {
            tags.add("体重数据");
        }

        // 睡眠分析
        var sleepRecords = records.stream().filter(r -> "SLEEP_HOURS".equals(r.getDataType())).toList();
        if (!sleepRecords.isEmpty()) {
            double avgSleep = sleepRecords.stream().mapToDouble(r -> Double.parseDouble(r.getDataValue())).average().orElse(0);
            if (avgSleep < 6) { tags.add("睡眠不足"); riskReasons.add("平均睡眠仅 " + String.format("%.1f", avgSleep) + " 小时"); }
            else if (avgSleep < 7) tags.add("睡眠偏少");
            else tags.add("睡眠充足");
        }

        // 运动分析
        var exRecords = records.stream().filter(r -> "EXERCISE_MINUTES".equals(r.getDataType())).toList();
        if (!exRecords.isEmpty()) {
            double avgEx = exRecords.stream().mapToDouble(r -> Double.parseDouble(r.getDataValue())).average().orElse(0);
            if (avgEx >= 30) tags.add("运动达标");
            else { tags.add("运动不足"); riskReasons.add("平均运动仅 " + String.format("%.0f", avgEx) + " 分钟/天"); }
        }

        // 血压分析
        var bpRecords = records.stream().filter(r -> "BLOOD_PRESSURE".equals(r.getDataType())).toList();
        if (!bpRecords.isEmpty()) {
            boolean hasHigh = bpRecords.stream().anyMatch(r -> {
                String[] parts = r.getDataValue().split("/");
                return parts.length == 2 && (Integer.parseInt(parts[0]) > 140 || Integer.parseInt(parts[1]) > 90);
            });
            if (hasHigh) { tags.add("血压偏高"); riskReasons.add("存在血压偏高记录"); }
            else tags.add("血压正常");
        }

        if (tags.isEmpty()) tags.add("数据待完善");

        String riskLevel = riskReasons.size() >= 2 ? "MEDIUM" : riskReasons.isEmpty() ? "LOW" : "MEDIUM";
        String genderLabel = "MALE".equals(gender) ? "男性" : "FEMALE".equals(gender) ? "女性" : "用户";

        StringBuilder analysis = new StringBuilder();
        if (height != null && birthDate != null) {
            int age = java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
            analysis.append(String.format("作为%d岁%s（身高%.0fcm），", age, genderLabel, height));
        } else if (height != null) {
            analysis.append(String.format("作为%s（身高%.0fcm），", genderLabel, height));
        }
        if (riskReasons.isEmpty()) {
            analysis.append("您近期健康指标整体良好，请继续保持当前的生活方式。");
        } else {
            analysis.append(String.join("；", riskReasons)).append("。建议针对性改善，保持健康生活习惯。");
        }

        return new FastApiProfileResponse(tags, riskLevel, analysis.toString());
    }

    private FastApiPlanResponse mockPlan(Long userId, String healthNeed) {
        var user = userRepository.findById(userId).orElse(null);
        double h = user != null && user.getHeight() != null ? user.getHeight().doubleValue() : 170;
        int age = user != null && user.getBirthDate() != null
            ? java.time.Period.between(user.getBirthDate(), java.time.LocalDate.now()).getYears() : 30;

        String key = healthNeed.contains("睡眠") ? "改善睡眠" :
            healthNeed.contains("减重") || healthNeed.contains("体重") ? "减重管理" :
            healthNeed.contains("血压") ? "控制血压" :
            healthNeed.contains("血糖") ? "控制血糖" :
            healthNeed.contains("运动") || healthNeed.contains("体质") ? "增强体质" : "综合健康管理";

        return switch (key) {
            case "改善睡眠" -> new FastApiPlanResponse(key + "专属计划",
                "晚餐宜清淡，避免辛辣和高油食物。晚餐建议在19:00前完成，睡前可饮用温牛奶帮助放松。减少咖啡因摄入。",
                String.format("每天30-45分钟中等强度运动（身高%.0fcm，建议心率控制在110-130次/分），下午4-6点最佳。避免睡前2小时内剧烈运动。", h),
                "固定22:30前上床，早上6:30起床。睡前1小时远离手机。卧室保持18-22°C，使用遮光窗帘。");
            case "减重管理" -> new FastApiPlanResponse(key + "专属计划",
                String.format("控制每日总热量摄入（建议约%d千卡），增加蔬菜水果比例。减少精制碳水和含糖饮料。每餐七分饱即可。", (int)(25 * h / 100 * h / 100)),
                "每周至少5天有氧运动40分钟以上，如快走、慢跑、游泳。结合力量训练提升基础代谢。",
                "保证7-8小时睡眠，睡眠不足会影响代谢和食欲激素。周末避免过度补觉。");
            default -> new FastApiPlanResponse(key + "专属计划",
                String.format("均衡饮食（建议每日约%d千卡），多吃蔬菜水果和优质蛋白。少油少盐，控制加工食品摄入。", (int)(30 * h / 100 * h / 100)),
                String.format("每周150分钟中等强度有氧运动（身高%.0fcm，%d岁），结合2-3次力量训练。选择喜欢的运动方式更易坚持。", h, age),
                "保持规律作息，每晚7-8小时优质睡眠。睡前冥想或深呼吸10分钟有助放松。");
        };
    }

    private FastApiPushResponse mockPush() {
        List<Knowledge> all = knowledgeRepository.findAll();
        if (all.isEmpty()) return new FastApiPushResponse(List.of(), "暂无知识可推送");
        List<Long> ids = all.stream().limit(2).map(Knowledge::getKnowledgeId).toList();
        return new FastApiPushResponse(ids, "基于健康画像推荐");
    }
}
