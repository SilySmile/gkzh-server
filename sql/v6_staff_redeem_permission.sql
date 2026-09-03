-- 工作人员核销权限控制。已有工作人员默认允许核销，后台可单独关闭。
SET @has_can_redeem := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'gkzh_school_staff' AND column_name = 'can_redeem');
SET @sql := IF(@has_can_redeem = 0, 'ALTER TABLE gkzh_school_staff ADD COLUMN can_redeem tinyint(1) NOT NULL DEFAULT 1 COMMENT ''是否允许奖品核销：0否 1是'' AFTER status', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
