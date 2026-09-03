-- 学校工作人员、奖品核销与审计流水
CREATE TABLE IF NOT EXISTS `gkzh_school_staff` (
  `staff_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作人员ID',
  `user_name` varchar(64) NOT NULL COMMENT '工作人员独立登录账号',
  `password` varchar(255) NOT NULL COMMENT '工作人员独立登录密码',
  `school_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '学校ID',
  `staff_name` varchar(64) NOT NULL DEFAULT '' COMMENT '工作人员姓名',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `uk_staff_user_name` (`user_name`),
  KEY `idx_staff_school` (`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校工作人员账号';

CREATE TABLE IF NOT EXISTS `gkzh_prize_redemption` (
  `redemption_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '核销ID',
  `lottery_record_id` bigint(20) NOT NULL COMMENT '中奖记录ID',
  `school_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '学校ID',
  `student_id` bigint(20) DEFAULT NULL COMMENT '学生ID',
  `staff_id` bigint(20) DEFAULT NULL COMMENT '核销工作人员ID',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '0待核销 1已核销 2已撤销 3已过期',
  `redeem_time` datetime DEFAULT NULL COMMENT '核销时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '核销备注',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`redemption_id`),
  UNIQUE KEY `uk_redemption_record` (`lottery_record_id`),
  KEY `idx_redemption_school_status` (`school_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户奖品核销状态';

CREATE TABLE IF NOT EXISTS `gkzh_prize_redemption_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `redemption_id` bigint(20) DEFAULT NULL COMMENT '核销ID',
  `lottery_record_id` bigint(20) NOT NULL COMMENT '中奖记录ID',
  `school_id` bigint(20) DEFAULT NULL COMMENT '学校ID',
  `staff_id` bigint(20) DEFAULT NULL COMMENT '操作工作人员ID',
  `action` varchar(32) NOT NULL DEFAULT 'SCAN' COMMENT 'SCAN/REDEEM/REPEAT/REVOKE',
  `before_status` char(1) DEFAULT NULL,
  `after_status` char(1) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `idx_redemption_log_record` (`lottery_record_id`),
  KEY `idx_redemption_log_school` (`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖品核销审计流水';
