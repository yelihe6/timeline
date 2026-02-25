-- 时间线数据库表结构（参考）
CREATE DATABASE IF NOT EXISTS time DEFAULT CHARACTER SET utf8mb4;

USE time;

CREATE TABLE IF NOT EXISTS `user` (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  operation_count INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS events (
  event_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  year INT NOT NULL,
  month INT NOT NULL,
  day INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES `user`(user_id)
);
