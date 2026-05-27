CREATE TABLE IF NOT EXISTS `knowledge` (
  `knowledge_id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `relate_tag` VARCHAR(50) NULL,
  `source` VARCHAR(100) NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`knowledge_id`),
  KEY `idx_knowledge_relate_tag` (`relate_tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `knowledge_push` (
  `push_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `knowledge_id` BIGINT NOT NULL,
  `push_time` DATETIME NOT NULL,
  `is_read` TINYINT NOT NULL,
  PRIMARY KEY (`push_id`),
  KEY `idx_knowledge_push_user_time` (`user_id`, `push_time`),
  KEY `idx_knowledge_push_knowledge` (`knowledge_id`),
  CONSTRAINT `fk_knowledge_push_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_knowledge_push_knowledge`
    FOREIGN KEY (`knowledge_id`) REFERENCES `knowledge` (`knowledge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
