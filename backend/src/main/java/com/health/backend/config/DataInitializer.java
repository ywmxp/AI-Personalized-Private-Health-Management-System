package com.health.backend.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.health.backend.domain.Knowledge;
import com.health.backend.repository.KnowledgeRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final KnowledgeRepository knowledgeRepository;

    public DataInitializer(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
    }

    @Override
    public void run(String... args) {
        if (knowledgeRepository.count() > 0) return;

        knowledgeRepository.save(create("每日饮水指南",
            "成年人每天建议饮水1500-2000ml，少量多次饮用效果更佳。晨起一杯温水有助唤醒身体，餐前半小时饮水有助于消化。运动前后注意补充水分。",
            "饮水"));
        knowledgeRepository.save(create("改善睡眠质量的10个方法",
            "保持规律作息、睡前1小时远离电子屏幕、保持卧室温度18-22°C、适度运动都有助于改善睡眠。睡前可进行冥想或深呼吸练习，避免晚餐过饱。",
            "睡眠不足"));
        knowledgeRepository.save(create("科学运动建议",
            "每周至少150分钟中等强度有氧运动，如快走、慢跑、游泳、骑行。结合2-3次力量训练效果更佳。运动前后注意热身和拉伸，避免运动损伤。",
            "运动达标"));
        knowledgeRepository.save(create("均衡饮食指南",
            "每天摄入谷薯类250-400g、蔬菜300-500g、水果200-350g、畜禽肉40-75g、蛋类40-50g。少油少盐，每日盐摄入不超过6g，油不超过25-30g。",
            "饮食"));
        knowledgeRepository.save(create("血压管理小贴士",
            "低盐饮食是控制血压的关键，每日食盐摄入量控制在6g以下。多吃富含钾的食物如香蕉、土豆、菠菜。保持规律运动和良好心态。定期监测血压。",
            "血压偏高"));
        knowledgeRepository.save(create("血糖控制建议",
            "少食多餐，控制碳水总量。选择低GI食物如全谷物、豆类。餐后适当散步有助于血糖控制。定期监测空腹和餐后血糖。",
            "血糖偏高"));
    }

    private Knowledge create(String title, String content, String tag) {
        Knowledge k = new Knowledge();
        k.setTitle(title);
        k.setContent(content);
        k.setRelateTag(tag);
        k.setSource("系统内置");
        k.setCreateTime(LocalDateTime.now());
        return k;
    }
}
