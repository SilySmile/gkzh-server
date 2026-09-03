-- 活动管理相关表结构

-- 1. 活动表
DROP TABLE IF EXISTS `lottery_activity`;
CREATE TABLE `lottery_activity` (
  `activity_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `title` varchar(200) NOT NULL COMMENT '活动名称',
  `description` text COMMENT '活动描述',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `activity_type` tinyint(1) DEFAULT '1' COMMENT '活动类型（1-校园活动，2-学术讲座，3-文化活动，4-其他）',
  `location` varchar(200) DEFAULT NULL COMMENT '活动地点',
  `organizer` varchar(100) DEFAULT NULL COMMENT '活动主办方',
  `co_organizer` varchar(100) DEFAULT NULL COMMENT '活动协办方',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '活动联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `qr_code` varchar(500) DEFAULT NULL COMMENT '活动二维码',
  `poster` varchar(500) DEFAULT NULL COMMENT '活动海报',
  `module_config` text COMMENT '活动环节配置（JSON格式）',
  `participant_config` text COMMENT '参与人配置（JSON格式）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`activity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- 2. 活动环节表
DROP TABLE IF EXISTS `activity_module`;
CREATE TABLE `activity_module` (
  `module_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '环节ID',
  `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
  `module_name` varchar(100) NOT NULL COMMENT '环节名称',
  `module_type` tinyint(1) NOT NULL COMMENT '环节类型（1-签到签退，2-抽奖，3-心愿橱窗，4-问卷调查）',
  `description` varchar(500) DEFAULT NULL COMMENT '环节描述',
  `start_time` datetime DEFAULT NULL COMMENT '环节开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '环节结束时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '环节状态（0-禁用，1-启用）',
  `sort_order` int(11) DEFAULT '0' COMMENT '环节顺序',
  `config` text COMMENT '环节配置（JSON格式）',
  `required` tinyint(1) DEFAULT '0' COMMENT '是否必选环节（0-否，1-是）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`module_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_module_type` (`module_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='活动环节表';

-- 3. 活动参与人表
DROP TABLE IF EXISTS `activity_participant`;
CREATE TABLE `activity_participant` (
  `participant_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '参与人ID',
  `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
  `participant_type` tinyint(1) NOT NULL COMMENT '参与人类型（1-学校，2-院系，3-专业，4-个人）',
  `target_id` bigint(20) NOT NULL COMMENT '参与人ID（学校ID、院系ID、专业ID、学生ID）',
  `target_name` varchar(100) NOT NULL COMMENT '参与人名称',
  `target_code` varchar(50) DEFAULT NULL COMMENT '参与人代码',
  `status` tinyint(1) DEFAULT '0' COMMENT '参与状态（0-未参与，1-已参与）',
  `participate_time` varchar(50) DEFAULT NULL COMMENT '参与时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`participant_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_participant_type` (`participant_type`),
  KEY `idx_target_id` (`target_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='活动参与人表';

-- 4. 活动参与记录表
DROP TABLE IF EXISTS `activity_participation_record`;
CREATE TABLE `activity_participation_record` (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
  `module_id` bigint(20) DEFAULT NULL COMMENT '环节ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) NOT NULL COMMENT '用户姓名',
  `user_code` varchar(50) DEFAULT NULL COMMENT '用户学号/工号',
  `participation_type` tinyint(1) NOT NULL COMMENT '参与类型（1-签到，2-签退，3-抽奖，4-心愿，5-问卷）',
  `participation_time` datetime NOT NULL COMMENT '参与时间',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `device_info` varchar(200) DEFAULT NULL COMMENT '设备信息',
  `location` varchar(200) DEFAULT NULL COMMENT '位置信息',
  `result` varchar(500) DEFAULT NULL COMMENT '参与结果',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态（0-无效，1-有效）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`record_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_module_id` (`module_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_participation_type` (`participation_type`),
  KEY `idx_participation_time` (`participation_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='活动参与记录表';

-- 插入测试数据
INSERT INTO `lottery_activity` (`activity_id`, `title`, `description`, `start_time`, `end_time`, `status`, `activity_type`, `location`, `organizer`, `contact_person`, `contact_phone`, `create_by`, `create_time`) VALUES
(1, '2024年校园文化节', '一年一度的校园文化节，包含文艺演出、抽奖活动、心愿墙等环节', '2024-12-20 09:00:00', '2024-12-20 18:00:00', 1, 1, '学校大礼堂', '学生处', '张老师', '13800138000', 'admin', NOW()),
(2, '学术讲座：人工智能发展', '邀请知名专家进行人工智能专题讲座', '2024-12-25 14:00:00', '2024-12-25 16:00:00', 1, 2, '学术报告厅', '计算机学院', '李老师', '13900139000', 'admin', NOW());

INSERT INTO `activity_module` (`module_id`, `activity_id`, `module_name`, `module_type`, `description`, `start_time`, `end_time`, `status`, `sort_order`, `required`, `create_by`, `create_time`) VALUES
(1, 1, '活动签到', 1, '学生签到环节', '2024-12-20 09:00:00', '2024-12-20 09:30:00', 1, 1, 1, 'admin', NOW()),
(2, 1, '文艺演出', 4, '文艺演出环节', '2024-12-20 10:00:00', '2024-12-20 12:00:00', 1, 2, 0, 'admin', NOW()),
(3, 1, '抽奖活动', 2, '幸运抽奖环节', '2024-12-20 14:00:00', '2024-12-20 15:00:00', 1, 3, 0, 'admin', NOW()),
(4, 1, '心愿墙', 3, '心愿墙环节', '2024-12-20 15:00:00', '2024-12-20 17:00:00', 1, 4, 0, 'admin', NOW()),
(5, 1, '活动签退', 1, '学生签退环节', '2024-12-20 17:30:00', '2024-12-20 18:00:00', 1, 5, 1, 'admin', NOW()),
(6, 2, '讲座签到', 1, '讲座签到环节', '2024-12-25 13:30:00', '2024-12-25 14:00:00', 1, 1, 1, 'admin', NOW()),
(7, 2, '问卷调查', 4, '讲座反馈问卷', '2024-12-25 16:00:00', '2024-12-25 16:30:00', 1, 2, 0, 'admin', NOW());

INSERT INTO `activity_participant` (`participant_id`, `activity_id`, `participant_type`, `target_id`, `target_name`, `target_code`, `status`, `create_by`, `create_time`) VALUES
(1, 1, 1, 1, '清华大学', 'THU', 0, 'admin', NOW()),
(2, 1, 2, 1, '计算机学院', 'CS', 0, 'admin', NOW()),
(3, 1, 2, 2, '机械工程学院', 'ME', 0, 'admin', NOW()),
(4, 2, 2, 1, '计算机学院', 'CS', 0, 'admin', NOW()); 