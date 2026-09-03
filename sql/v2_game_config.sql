-- =============================================================
-- 游戏配置模块
-- =============================================================
CREATE TABLE IF NOT EXISTS `gkzh_game_config` (
  `config_id`       bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '游戏配置ID',
  `game_type`       varchar(32)   NOT NULL COMMENT '游戏类型编码',
  `category`        varchar(32)   DEFAULT NULL COMMENT '游戏类别：choice/answer/cooperation',
  `route`           varchar(50)   DEFAULT NULL COMMENT '具体游戏路由编码',
  `game_name`       varchar(100)  NOT NULL COMMENT '游戏名称',
  `description`     varchar(500)  DEFAULT NULL COMMENT '游戏说明',
  `config_json`     text          COMMENT '游戏配置JSON',
  `status`          char(1)       NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_by`       varchar(64)   DEFAULT '' COMMENT '创建者',
  `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`       varchar(64)   DEFAULT '' COMMENT '更新者',
  `update_time`     datetime      DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  KEY `idx_game_config_type` (`game_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏配置表';

INSERT INTO `gkzh_game_config`
  (`game_type`, `category`, `route`, `game_name`, `description`, `config_json`, `status`, `create_time`)
VALUES
  ('mind-window', 'choice', 'mind-window', '心愿橱窗', '心愿橱窗选择类游戏', '{}', '0', NOW())
ON DUPLICATE KEY UPDATE
  `game_name` = VALUES(`game_name`),
  `category` = VALUES(`category`),
  `route` = VALUES(`route`),
  `description` = VALUES(`description`),
  `update_time` = NOW();
