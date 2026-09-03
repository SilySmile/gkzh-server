-- 本地/线上架构同步（MySQL 5.7+，可重复执行）
-- 新增到已有表的必填字段均提供 DEFAULT，避免阻断线上历史数据。
DROP PROCEDURE IF EXISTS gkzh_sync_online_schema;
DELIMITER $$
CREATE PROCEDURE gkzh_sync_online_schema()
BEGIN
  -- 随机文本核销码：旧数据先写入 PENDING，再立即补齐随机码。
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'lottery_record' AND column_name = 'redemption_code'
  ) THEN
    ALTER TABLE lottery_record ADD COLUMN redemption_code varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '随机文本核销码';
  ELSEIF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'lottery_record' AND column_name = 'redemption_code'
      AND (is_nullable = 'YES' OR COALESCE(column_default, '') <> 'PENDING')
  ) THEN
    ALTER TABLE lottery_record MODIFY COLUMN redemption_code varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '随机文本核销码';
  END IF;

  UPDATE lottery_record
  SET redemption_code = CONCAT('GKZH-', UPPER(SUBSTRING(REPLACE(UUID(), '-', ''), 1, 10)))
  WHERE redemption_code IS NULL OR redemption_code = '' OR redemption_code = 'PENDING';

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'lottery_record' AND index_name = 'uk_lottery_record_redemption_code'
  ) THEN
    ALTER TABLE lottery_record ADD UNIQUE KEY uk_lottery_record_redemption_code (redemption_code);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'gkzh_prize_redemption') THEN
    CREATE TABLE gkzh_prize_redemption (
      redemption_id bigint(20) NOT NULL AUTO_INCREMENT,
      lottery_record_id bigint(20) NOT NULL,
      school_id bigint(20) NOT NULL DEFAULT 0,
      student_id bigint(20) DEFAULT NULL,
      staff_id bigint(20) DEFAULT NULL,
      status char(1) NOT NULL DEFAULT '0',
      redeem_time datetime DEFAULT NULL,
      remark varchar(500) DEFAULT NULL,
      create_time datetime DEFAULT NULL,
      update_time datetime DEFAULT NULL,
      PRIMARY KEY (redemption_id), UNIQUE KEY uk_redemption_record (lottery_record_id),
      KEY idx_redemption_school_status (school_id,status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户奖品核销状态';
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'gkzh_school_staff') THEN
    CREATE TABLE gkzh_school_staff (
      staff_id bigint(20) NOT NULL AUTO_INCREMENT,
      user_name varchar(64) DEFAULT NULL,
      password varchar(255) DEFAULT NULL,
      user_id bigint(20) DEFAULT NULL,
      school_id bigint(20) NOT NULL DEFAULT 0,
      staff_name varchar(64) NOT NULL DEFAULT '',
      status char(1) NOT NULL DEFAULT '0',
      can_redeem tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许奖品核销：0否 1是',
      create_by varchar(64) DEFAULT '', create_time datetime DEFAULT NULL,
      update_by varchar(64) DEFAULT '', update_time datetime DEFAULT NULL,
      PRIMARY KEY (staff_id), UNIQUE KEY uk_staff_user_name (user_name), KEY idx_staff_school (school_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校工作人员账号';
  ELSE
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_school_staff' AND column_name='user_name') THEN
      ALTER TABLE gkzh_school_staff ADD COLUMN user_name varchar(64) DEFAULT NULL COMMENT '工作人员独立登录账号' AFTER staff_id;
      UPDATE gkzh_school_staff SET user_name=CONCAT('staff_', staff_id) WHERE user_name IS NULL OR user_name='';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_school_staff' AND column_name='password') THEN
      ALTER TABLE gkzh_school_staff ADD COLUMN password varchar(255) DEFAULT NULL COMMENT '工作人员独立登录密码' AFTER user_name;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_school_staff' AND column_name='can_redeem') THEN
      ALTER TABLE gkzh_school_staff ADD COLUMN can_redeem tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许奖品核销：0否 1是' AFTER status;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='gkzh_school_staff' AND index_name='uk_staff_user_name') THEN
      ALTER TABLE gkzh_school_staff ADD UNIQUE KEY uk_staff_user_name (user_name);
    END IF;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'gkzh_prize_redemption_log') THEN
    CREATE TABLE gkzh_prize_redemption_log (
      log_id bigint(20) NOT NULL AUTO_INCREMENT,
      redemption_id bigint(20) DEFAULT NULL,
      lottery_record_id bigint(20) NOT NULL,
      school_id bigint(20) DEFAULT NULL,
      staff_id bigint(20) DEFAULT NULL,
      action varchar(32) NOT NULL DEFAULT 'SCAN',
      before_status char(1) DEFAULT NULL,
      after_status char(1) DEFAULT NULL,
      remark varchar(500) DEFAULT NULL,
      create_time datetime DEFAULT NULL,
      PRIMARY KEY (log_id), KEY idx_redemption_log_record (lottery_record_id), KEY idx_redemption_log_school (school_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖品核销审计流水';
  END IF;
END$$
DELIMITER ;
CALL gkzh_sync_online_schema();
DROP PROCEDURE gkzh_sync_online_schema;
