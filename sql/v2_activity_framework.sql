-- =============================================================
-- GKZH V2 活动 / 区域 / 游戏 框架
-- 说明：
-- 1. 本脚本只新增表，不删除或修改现有业务表。
-- 2. 本脚本可重复执行（使用 CREATE TABLE IF NOT EXISTS）。
-- 3. 旧 gkzh_activity.module_config 后续通过独立脚本迁移到本框架。
-- =============================================================

-- 活动定义：生涯活动、就业活动等长期模板
CREATE TABLE IF NOT EXISTS `gkzh_activity_week_definition` (
  `definition_id`   bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '活动定义ID',
  `biz_type`        varchar(32)   NOT NULL COMMENT '业务类型：career_week=生涯活动，job_week=就业活动',
  `name`            varchar(100)  NOT NULL COMMENT '活动名称',
  `description`     varchar(500)  DEFAULT NULL COMMENT '活动描述',
  `status`          char(1)       NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`       varchar(64)   DEFAULT '' COMMENT '创建者',
  `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`       varchar(64)   DEFAULT '' COMMENT '更新者',
  `update_time`     datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`          varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`definition_id`),
  UNIQUE KEY `uk_activity_week_biz_type` (`biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动定义表';

-- 活动实例：每次运营创建一个实例
CREATE TABLE IF NOT EXISTS `gkzh_activity_week_instance` (
  `instance_id`     bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '活动实例ID',
  `definition_id`   bigint(20)    NOT NULL COMMENT '活动定义ID',
  `biz_type`        varchar(32)   NOT NULL COMMENT '业务类型：career_week/job_week',
  `title`           varchar(200)  NOT NULL COMMENT '活动实例名称',
  `banner_url`      varchar(500)  DEFAULT NULL COMMENT '顶部 Banner 图 URL',
  `start_time`      datetime      NOT NULL COMMENT '开始时间',
  `end_time`        datetime      NOT NULL COMMENT '结束时间',
  `status`          char(1)       NOT NULL DEFAULT '0' COMMENT '状态：0未开始 1进行中 2已结束 3停用',
  `create_by`       varchar(64)   DEFAULT '' COMMENT '创建者',
  `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`       varchar(64)   DEFAULT '' COMMENT '更新者',
  `update_time`     datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`          varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`instance_id`),
  KEY `idx_week_instance_definition_id` (`definition_id`),
  KEY `idx_week_instance_biz_type` (`biz_type`),
  KEY `idx_week_instance_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动实例表';

-- 活动实例与学校关系
CREATE TABLE IF NOT EXISTS `gkzh_activity_week_school` (
  `id`              bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键',
  `instance_id`     bigint(20)    NOT NULL COMMENT '活动实例ID',
  `school_id`       bigint(20)    NOT NULL COMMENT '学校ID',
  `min_finish_count` int(11)      DEFAULT 0 COMMENT '抽奖最低完成游戏数',
  `lottery_id`      bigint(20)    DEFAULT NULL COMMENT '绑定抽奖活动ID',
  `max_draw_count`  int(11)       DEFAULT 1 COMMENT '每个用户最多抽奖次数',
  `status`          char(1)       NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_week_school` (`instance_id`, `school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动实例学校关系表';

-- 区域：活动实例下的一级分类，只做展示分类
CREATE TABLE IF NOT EXISTS `gkzh_activity_area` (
  `area_id`         bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '区域ID',
  `instance_id`     bigint(20)    NOT NULL COMMENT '活动实例ID',
  `school_id`       bigint(20)    DEFAULT NULL COMMENT '学校ID，为空表示公共区域',
  `title`           varchar(100)  NOT NULL COMMENT '区域名称',
  `sort_order`      int(11)       NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status`          char(1)       NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_by`       varchar(64)   DEFAULT '' COMMENT '创建者',
  `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`       varchar(64)   DEFAULT '' COMMENT '更新者',
  `update_time`     datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`          varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`area_id`),
  KEY `idx_area_instance_id` (`instance_id`),
  KEY `idx_area_school_id` (`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动区域表';

-- 游戏：区域下的二级内容
CREATE TABLE IF NOT EXISTS `gkzh_activity_game` (
  `game_id`         bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '游戏ID',
  `area_id`         bigint(20)    NOT NULL COMMENT '区域ID',
  `instance_id`     bigint(20)    NOT NULL COMMENT '活动实例ID',
  `game_type`       varchar(32)   NOT NULL COMMENT '游戏类型，对应 gkzh_game_type.game_type',
  `title`           varchar(200)  NOT NULL COMMENT '游戏名称',
  `config`          text          COMMENT '游戏配置JSON',
  `rule_id`         bigint(20)    DEFAULT NULL COMMENT '关联规则ID，可为空',
  `required_flag`   char(1)       NOT NULL DEFAULT '0' COMMENT '是否必玩：0否 1是',
  `sort_order`      int(11)       NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status`          char(1)       NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `qr_code`         varchar(500)  DEFAULT NULL COMMENT '游戏二维码内容',
  `create_by`       varchar(64)   DEFAULT '' COMMENT '创建者',
  `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`       varchar(64)   DEFAULT '' COMMENT '更新者',
  `update_time`     datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`          varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`game_id`),
  KEY `idx_game_area_id` (`area_id`),
  KEY `idx_game_instance_id` (`instance_id`),
  KEY `idx_game_type` (`game_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动游戏表';

-- 游戏类型注册表：游戏可持续新增，核心框架不感知具体业务
CREATE TABLE IF NOT EXISTS `gkzh_game_type` (
  `game_type`       varchar(32)   NOT NULL COMMENT '游戏类型编码',
  `game_name`       varchar(100)  NOT NULL COMMENT '游戏名称',
  `front_component` varchar(255)  DEFAULT NULL COMMENT '学生端组件标识或路径',
  `result_type`     varchar(32)   DEFAULT 'complete' COMMENT '结果类型：complete=完成即通关，score=答题打分',
  `status`          char(1)       NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
  `update_time`     datetime      DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`game_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏类型注册表';

-- 学生游戏参与记录
CREATE TABLE IF NOT EXISTS `gkzh_game_participation` (
  `participation_id` bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '参与记录ID',
  `instance_id`      bigint(20)   NOT NULL COMMENT '活动实例ID',
  `game_id`          bigint(20)   NOT NULL COMMENT '游戏ID',
  `area_id`          bigint(20)   DEFAULT NULL COMMENT '区域ID',
  `school_id`        bigint(20)   DEFAULT NULL COMMENT '学校ID',
  `student_id`       bigint(20)   DEFAULT NULL COMMENT '学生ID',
  `user_id`          bigint(20)   DEFAULT NULL COMMENT '用户ID',
  `scan_time`        datetime     DEFAULT NULL COMMENT '扫码进入时间',
  `start_time`       datetime     DEFAULT NULL COMMENT '开始游戏时间',
  `finish_time`      datetime     DEFAULT NULL COMMENT '完成时间',
  `status`           char(1)      NOT NULL DEFAULT '0' COMMENT '状态：0未完成 1已完成 2未通过',
  `result_json`      text         COMMENT '游戏结果JSON',
  `create_time`      datetime     DEFAULT NULL COMMENT '创建时间',
  `update_time`      datetime     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`participation_id`),
  KEY `idx_game_part_instance_id` (`instance_id`),
  KEY `idx_game_part_game_id` (`game_id`),
  KEY `idx_game_part_student_id` (`student_id`),
  KEY `idx_game_part_school_id` (`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生游戏参与记录表';

-- 抽奖规则：按活动实例配置，判断完成游戏数量
CREATE TABLE IF NOT EXISTS `gkzh_lottery_rule` (
  `rule_id`                 bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `instance_id`             bigint(20)    NOT NULL COMMENT '活动实例ID',
  `required_completed_games` int(11)      NOT NULL DEFAULT 0 COMMENT '抽奖所需完成游戏数',
  `max_draw_per_student`    int(11)       NOT NULL DEFAULT 1 COMMENT '每人最多抽奖次数',
  `status`                  char(1)       NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `config`                  text          COMMENT '奖品池/权重等配置JSON',
  `create_by`               varchar(64)   DEFAULT '' COMMENT '创建者',
  `create_time`             datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`               varchar(64)   DEFAULT '' COMMENT '更新者',
  `update_time`             datetime      DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`rule_id`),
  KEY `idx_lottery_rule_instance_id` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动抽奖规则表';

-- 抽奖记录
CREATE TABLE IF NOT EXISTS `gkzh_lottery_record` (
  `record_id`       bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '抽奖记录ID',
  `rule_id`         bigint(20)    NOT NULL COMMENT '抽奖规则ID',
  `instance_id`     bigint(20)    NOT NULL COMMENT '活动实例ID',
  `student_id`      bigint(20)    DEFAULT NULL COMMENT '学生ID',
  `user_id`         bigint(20)    DEFAULT NULL COMMENT '用户ID',
  `prize_id`        bigint(20)    DEFAULT NULL COMMENT '奖品ID',
  `prize_title`     varchar(200)  DEFAULT NULL COMMENT '奖品名称快照',
  `draw_time`       datetime      DEFAULT NULL COMMENT '抽奖时间',
  `status`          char(1)       NOT NULL DEFAULT '0' COMMENT '状态：0正常 1作废',
  `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_lottery_record_rule_id` (`rule_id`),
  KEY `idx_lottery_record_instance_id` (`instance_id`),
  KEY `idx_lottery_record_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动抽奖记录表';

-- 初始化两个活动定义
INSERT INTO `gkzh_activity_week_definition`
  (`biz_type`, `name`, `description`, `status`, `create_time`)
VALUES
  ('career_week', '生涯活动', '生涯活动，区域与游戏独立配置', '0', NOW()),
  ('job_week', '就业活动', '就业活动，区域与游戏独立配置', '0', NOW())
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `description` = VALUES(`description`),
  `update_time` = NOW();

-- 初始化已有游戏类型，后续可持续增加
INSERT INTO `gkzh_game_type`
  (`game_type`, `game_name`, `result_type`, `status`, `create_time`)
VALUES
  ('choice', '选择', 'choice', '0', NOW()),
  ('answer', '答题', 'answer', '0', NOW()),
  ('cooperation', '合作', 'cooperation', '0', NOW())
ON DUPLICATE KEY UPDATE
  `game_name` = VALUES(`game_name`),
  `result_type` = VALUES(`result_type`),
  `update_time` = NOW();
