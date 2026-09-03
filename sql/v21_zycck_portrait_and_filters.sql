-- zycck 配置标记与后台筛选字段；可重复执行。
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='participate_portrait')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN participate_portrait char(1) NOT NULL DEFAULT ''1'' COMMENT ''是否参与人物画像''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_record' AND column_name='department_id')=0,
  'ALTER TABLE gkzh_zycck_record ADD COLUMN department_id bigint DEFAULT NULL, ADD COLUMN major varchar(100) DEFAULT NULL, ADD COLUMN gender char(1) DEFAULT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
CREATE INDEX idx_zycck_record_filters ON gkzh_zycck_record(school_id,department_id,major,gender);
