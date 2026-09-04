-- zycck 结构兜底脚本（MySQL 5.7，可重复执行）。
-- 用于处理服务已更新但目标数据库尚未增加 has_question 的环境。
SET @has_question_exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'gkzh_zycck_career_question'
    AND column_name = 'has_question'
);
SET @sql := IF(
  @has_question_exists = 0,
  'ALTER TABLE gkzh_zycck_career_question ADD COLUMN has_question CHAR(1) NOT NULL DEFAULT ''1'' AFTER career_name',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE gkzh_zycck_career_question
  MODIFY option_a VARCHAR(200) DEFAULT NULL,
  MODIFY option_b VARCHAR(200) DEFAULT NULL,
  MODIFY option_c VARCHAR(200) DEFAULT NULL,
  MODIFY option_d VARCHAR(200) DEFAULT NULL,
  MODIFY option_a_career_id BIGINT DEFAULT NULL,
  MODIFY option_b_career_id BIGINT DEFAULT NULL,
  MODIFY option_c_career_id BIGINT DEFAULT NULL,
  MODIFY option_d_career_id BIGINT DEFAULT NULL,
  MODIFY correct_option_key CHAR(1) DEFAULT NULL;

UPDATE gkzh_zycck_career_question
SET has_question = CASE
      WHEN option_a IS NULL AND option_b IS NULL AND option_c IS NULL AND option_d IS NULL THEN '0'
      ELSE '1'
    END,
    draw_candidate = CASE
      WHEN option_a IS NULL AND option_b IS NULL AND option_c IS NULL AND option_d IS NULL THEN '0'
      ELSE draw_candidate
    END;

SELECT DATABASE() AS current_database;
SHOW COLUMNS FROM gkzh_zycck_career_question LIKE 'has_question';
