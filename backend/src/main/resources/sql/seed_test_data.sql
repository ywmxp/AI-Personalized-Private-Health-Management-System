-- ============================================
-- AI Health 测试数据种子脚本
-- 先在前端注册一个普通用户，然后执行此脚本
-- PowerShell:
--   Get-Content backend\src\main\resources\sql\seed_test_data.sql | docker exec -i ai-health-mysql mysql -uhealth_app -phealth_app_123 ai_health
-- ============================================

-- 查询现有用户ID，选一个作为测试数据归属
-- SELECT user_id, username FROM user;

SET @uid = 1;

-- ===== 健康数据（近30天） =====
INSERT INTO health_data (user_id, data_type, data_value, unit, record_time, create_time, update_time) VALUES
(@uid, 'WEIGHT', '70.5', 'kg', '2026-05-16 08:30:00', NOW(), NOW()),
(@uid, 'WEIGHT', '70.2', 'kg', '2026-05-18 08:30:00', NOW(), NOW()),
(@uid, 'WEIGHT', '69.8', 'kg', '2026-05-21 08:30:00', NOW(), NOW()),
(@uid, 'WEIGHT', '69.5', 'kg', '2026-05-25 08:30:00', NOW(), NOW()),
(@uid, 'WEIGHT', '70.0', 'kg', '2026-05-28 08:30:00', NOW(), NOW()),
(@uid, 'WEIGHT', '69.7', 'kg', '2026-06-01 08:30:00', NOW(), NOW()),
(@uid, 'WEIGHT', '70.1', 'kg', '2026-06-03 08:30:00', NOW(), NOW()),
(@uid, 'WEIGHT', '69.9', 'kg', '2026-06-06 08:30:00', NOW(), NOW()),
(@uid, 'WEIGHT', '70.3', 'kg', '2026-06-09 08:30:00', NOW(), NOW()),
(@uid, 'WEIGHT', '70.5', 'kg', '2026-06-12 08:30:00', NOW(), NOW()),

(@uid, 'BLOOD_PRESSURE', '118/76', 'mmHg', '2026-05-16 09:00:00', NOW(), NOW()),
(@uid, 'BLOOD_PRESSURE', '120/80', 'mmHg', '2026-05-20 09:00:00', NOW(), NOW()),
(@uid, 'BLOOD_PRESSURE', '122/78', 'mmHg', '2026-05-25 09:00:00', NOW(), NOW()),
(@uid, 'BLOOD_PRESSURE', '119/75', 'mmHg', '2026-06-01 09:00:00', NOW(), NOW()),
(@uid, 'BLOOD_PRESSURE', '121/79', 'mmHg', '2026-06-08 09:00:00', NOW(), NOW()),
(@uid, 'BLOOD_PRESSURE', '118/77', 'mmHg', '2026-06-14 09:00:00', NOW(), NOW()),

(@uid, 'BLOOD_GLUCOSE', '5.2', 'mmol/L', '2026-05-16 07:30:00', NOW(), NOW()),
(@uid, 'BLOOD_GLUCOSE', '5.5', 'mmol/L', '2026-05-22 07:30:00', NOW(), NOW()),
(@uid, 'BLOOD_GLUCOSE', '5.1', 'mmol/L', '2026-05-28 07:30:00', NOW(), NOW()),
(@uid, 'BLOOD_GLUCOSE', '5.4', 'mmol/L', '2026-06-05 07:30:00', NOW(), NOW()),
(@uid, 'BLOOD_GLUCOSE', '5.3', 'mmol/L', '2026-06-12 07:30:00', NOW(), NOW()),

(@uid, 'SLEEP_HOURS', '6.5', '小时', '2026-05-17 07:00:00', NOW(), NOW()),
(@uid, 'SLEEP_HOURS', '7.0', '小时', '2026-05-20 07:00:00', NOW(), NOW()),
(@uid, 'SLEEP_HOURS', '6.8', '小时', '2026-05-23 07:00:00', NOW(), NOW()),
(@uid, 'SLEEP_HOURS', '7.2', '小时', '2026-05-27 07:00:00', NOW(), NOW()),
(@uid, 'SLEEP_HOURS', '7.5', '小时', '2026-06-01 07:00:00', NOW(), NOW()),
(@uid, 'SLEEP_HOURS', '7.0', '小时', '2026-06-05 07:00:00', NOW(), NOW()),
(@uid, 'SLEEP_HOURS', '6.9', '小时', '2026-06-09 07:00:00', NOW(), NOW()),
(@uid, 'SLEEP_HOURS', '7.3', '小时', '2026-06-13 07:00:00', NOW(), NOW()),

(@uid, 'EXERCISE_MINUTES', '30', '分钟', '2026-05-16 18:30:00', NOW(), NOW()),
(@uid, 'EXERCISE_MINUTES', '45', '分钟', '2026-05-19 18:30:00', NOW(), NOW()),
(@uid, 'EXERCISE_MINUTES', '40', '分钟', '2026-05-22 18:30:00', NOW(), NOW()),
(@uid, 'EXERCISE_MINUTES', '50', '分钟', '2026-05-25 18:30:00', NOW(), NOW()),
(@uid, 'EXERCISE_MINUTES', '35', '分钟', '2026-05-28 18:30:00', NOW(), NOW()),
(@uid, 'EXERCISE_MINUTES', '45', '分钟', '2026-06-01 18:30:00', NOW(), NOW()),
(@uid, 'EXERCISE_MINUTES', '55', '分钟', '2026-06-05 18:30:00', NOW(), NOW()),
(@uid, 'EXERCISE_MINUTES', '40', '分钟', '2026-06-09 18:30:00', NOW(), NOW()),
(@uid, 'EXERCISE_MINUTES', '45', '分钟', '2026-06-13 18:30:00', NOW(), NOW());

-- ===== 健康知识 =====
INSERT INTO knowledge (title, content, relate_tag, source, create_time) VALUES
('每日饮水指南', '成年人每天建议饮水1500-2000ml，少量多次饮用效果更佳。晨起一杯温水有助唤醒身体，餐前半小时饮水有助于消化。运动前后注意补充水分。', '饮水', '系统内置', NOW()),
('改善睡眠质量的10个方法', '保持规律作息、睡前1小时远离电子屏幕、保持卧室温度18-22°C、适度运动都有助于改善睡眠。睡前可进行冥想或深呼吸练习，避免晚餐过饱。', '睡眠不足', '系统内置', NOW()),
('科学运动建议', '每周至少150分钟中等强度有氧运动，如快走、慢跑、游泳、骑行。结合2-3次力量训练效果更佳。运动前后注意热身和拉伸，避免运动损伤。', '运动达标', '系统内置', NOW()),
('均衡饮食指南', '每天摄入谷薯类250-400g、蔬菜300-500g、水果200-350g、畜禽肉40-75g、蛋类40-50g。少油少盐，每日盐摄入不超过6g，油不超过25-30g。', '饮食', '系统内置', NOW()),
('血压管理小贴士', '低盐饮食是控制血压的关键，每日食盐摄入量控制在6g以下。多吃富含钾的食物如香蕉、土豆、菠菜。保持规律运动和良好心态。定期监测血压。', '血压偏高', '系统内置', NOW()),
('血糖控制建议', '少食多餐，控制碳水总量。选择低GI食物如全谷物、豆类。餐后适当散步有助于血糖控制。定期监测空腹和餐后血糖。', '血糖偏高', '系统内置', NOW());

-- ===== 提醒 =====
INSERT INTO reminder (user_id, reminder_type, reminder_time, status, create_time, update_time) VALUES
(@uid, 'MEDICATION', '08:30:00', 1, NOW(), NOW()),
(@uid, 'EXERCISE', '18:00:00', 1, NOW(), NOW()),
(@uid, 'SLEEP', '22:30:00', 0, NOW(), NOW()),
(@uid, 'WATER', '14:00:00', 1, NOW(), NOW());

SELECT 'Seed data inserted successfully!' AS result;
