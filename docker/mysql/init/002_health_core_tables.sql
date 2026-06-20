CREATE TABLE IF NOT EXISTS `health_data` (
  `data_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `data_type` VARCHAR(30) NOT NULL,
  `data_value` VARCHAR(50) NOT NULL,
  `unit` VARCHAR(20) NULL,
  `record_time` DATETIME NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`data_id`),
  KEY `idx_health_data_user_time` (`user_id`, `record_time`),
  KEY `idx_health_data_user_type_time` (`user_id`, `data_type`, `record_time`),
  CONSTRAINT `fk_health_data_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `reminder` (
  `reminder_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `reminder_type` VARCHAR(30) NOT NULL,
  `reminder_time` TIME NOT NULL,
  `status` TINYINT NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`reminder_id`),
  KEY `idx_reminder_user` (`user_id`),
  CONSTRAINT `fk_reminder_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `health_profile` (
  `profile_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `health_tags` JSON NOT NULL,
  `risk_level` VARCHAR(20) NOT NULL,
  `analysis` TEXT NOT NULL,
  `time_range` VARCHAR(50) NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`profile_id`),
  KEY `idx_health_profile_user_time` (`user_id`, `create_time`),
  CONSTRAINT `fk_health_profile_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `health_plan` (
  `plan_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `plan_name` VARCHAR(50) NOT NULL,
  `health_need` VARCHAR(50) NOT NULL,
  `diet_suggest` TEXT NULL,
  `sport_suggest` TEXT NULL,
  `sleep_suggest` TEXT NULL,
  `is_valid` TINYINT NOT NULL,
  `feedback_status` VARCHAR(20) NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`plan_id`),
  KEY `idx_health_plan_user_time` (`user_id`, `create_time`),
  CONSTRAINT `fk_health_plan_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
