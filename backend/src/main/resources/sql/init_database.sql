-- 创建数据库
CREATE DATABASE IF NOT EXISTS ai_health 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci;

-- 使用数据库
USE ai_health;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
  user_id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  phone VARCHAR(11) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  age INT,
  gender VARCHAR(10),
  height DECIMAL(5,2),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建健康数据表
CREATE TABLE IF NOT EXISTS health_data (
  data_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  data_type VARCHAR(20),
  data_value VARCHAR(20),
  record_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(user_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建提醒表
CREATE TABLE IF NOT EXISTS reminder (
  reminder_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  reminder_type VARCHAR(20),
  reminder_time TIME,
  status TINYINT DEFAULT 1,
  FOREIGN KEY (user_id) REFERENCES user(user_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建AI咨询记录表
CREATE TABLE IF NOT EXISTS ai_consult (
  consult_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  consult_content TEXT,
  ai_response TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(user_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;