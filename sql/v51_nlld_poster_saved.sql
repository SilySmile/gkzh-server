-- 能量雷达参与记录增加“保存截图”统计字段；可重复执行。
-- 每场活动游戏（学校+活动实例+游戏+学生）只有一条参与记录，因此保存截图也只记一次，
-- 重复点击只保留首次保存时间，不新增记录。

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema=DATABASE() AND table_name='gkzh_nlld_record' AND column_name='poster_saved')=0,
  'ALTER TABLE gkzh_nlld_record ADD COLUMN poster_saved char(1) NOT NULL DEFAULT ''0'' COMMENT ''是否点击保存截图：0未保存，1已保存'' AFTER result_type',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema=DATABASE() AND table_name='gkzh_nlld_record' AND column_name='poster_save_time')=0,
  'ALTER TABLE gkzh_nlld_record ADD COLUMN poster_save_time datetime DEFAULT NULL COMMENT ''首次点击保存截图时间'' AFTER poster_saved',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
