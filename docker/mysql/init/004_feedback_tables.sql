CREATE TABLE IF NOT EXISTS `feedback` (
  `feedback_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `plan_id` BIGINT NOT NULL,
  `score` INT NOT NULL,
  `content` TEXT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`feedback_id`),
  KEY `idx_feedback_user_time` (`user_id`, `create_time`),
  KEY `idx_feedback_plan` (`plan_id`),
  CONSTRAINT `fk_feedback_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_feedback_plan`
    FOREIGN KEY (`plan_id`) REFERENCES `health_plan` (`plan_id`),
  CONSTRAINT `chk_feedback_score`
    CHECK (`score` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
