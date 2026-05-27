CREATE TABLE IF NOT EXISTS `user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `password_hash` VARCHAR(100) NOT NULL,
  `birth_date` DATE NULL,
  `gender` VARCHAR(20) NULL,
  `height` DECIMAL(5,2) NULL,
  `role` VARCHAR(20) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  KEY `idx_user_role` (`role`),
  KEY `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `login_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NULL,
  `login_ip` VARCHAR(50) NULL,
  `login_time` DATETIME NOT NULL,
  `login_result` TINYINT NOT NULL,
  `failure_reason` VARCHAR(100) NULL,
  PRIMARY KEY (`log_id`),
  KEY `idx_login_log_user_time` (`user_id`, `login_time`),
  KEY `idx_login_log_time` (`login_time`),
  KEY `idx_login_log_result` (`login_result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
