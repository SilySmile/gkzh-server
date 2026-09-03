-- zycck 基础结构；可重复执行。配置/记录均按活动实例隔离。
CREATE TABLE IF NOT EXISTS gkzh_zycck_category (
  category_id BIGINT NOT NULL AUTO_INCREMENT, code VARCHAR(32) NOT NULL, name VARCHAR(64) NOT NULL,
  draw_mode VARCHAR(16) NOT NULL DEFAULT 'fixed', sort_order INT NOT NULL DEFAULT 0,
  status CHAR(1) NOT NULL DEFAULT '0', create_time DATETIME DEFAULT NULL, update_time DATETIME DEFAULT NULL,
  PRIMARY KEY(category_id), UNIQUE KEY uk_zycck_category_code(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS gkzh_zycck_career_question (
  question_id BIGINT NOT NULL AUTO_INCREMENT, category_id BIGINT NOT NULL, career_id BIGINT NOT NULL,
  career_name VARCHAR(100) NOT NULL, title VARCHAR(500) NOT NULL, image_url VARCHAR(500) DEFAULT NULL,
  option_a VARCHAR(200) NOT NULL, option_b VARCHAR(200) NOT NULL, option_c VARCHAR(200) NOT NULL, option_d VARCHAR(200) NOT NULL,
  explanation VARCHAR(2000) DEFAULT NULL, status CHAR(1) NOT NULL DEFAULT '0', create_time DATETIME DEFAULT NULL, update_time DATETIME DEFAULT NULL,
  PRIMARY KEY(question_id), UNIQUE KEY uk_zycck_career(category_id,career_id), KEY idx_zycck_question_category(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS gkzh_zycck_record (
  record_id BIGINT NOT NULL AUTO_INCREMENT, school_id BIGINT NOT NULL, instance_id BIGINT NOT NULL, game_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL, game_type VARCHAR(32) NOT NULL DEFAULT 'zycck', status VARCHAR(16) NOT NULL DEFAULT 'participating',
  stage VARCHAR(32) NOT NULL DEFAULT 'scanned', current_question_no INT NOT NULL DEFAULT 0,
  question_snapshot JSON DEFAULT NULL, answer_snapshot JSON DEFAULT NULL, exploration_snapshot JSON DEFAULT NULL,
  list_snapshot JSON DEFAULT NULL, config_version VARCHAR(64) DEFAULT NULL,
  scan_time DATETIME NOT NULL, start_time DATETIME DEFAULT NULL, finish_time DATETIME DEFAULT NULL,
  create_time DATETIME DEFAULT NULL, update_time DATETIME DEFAULT NULL,
  PRIMARY KEY(record_id), UNIQUE KEY uk_zycck_record_scope(school_id,instance_id,game_id,user_id),
  KEY idx_zycck_record_instance(instance_id,game_id), KEY idx_zycck_record_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
