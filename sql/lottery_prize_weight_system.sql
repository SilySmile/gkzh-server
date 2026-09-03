-- 基于权重系统的抽奖奖品表结构

-- 修改奖品表，将probability改为weight
ALTER TABLE `lottery_prize` 
CHANGE COLUMN `probability` `weight` int(11) NOT NULL DEFAULT 1 COMMENT '奖品权重（数值越大中奖概率越高）';

-- 添加新的字段用于更灵活的配置
ALTER TABLE `lottery_prize` 
ADD COLUMN `prize_type` tinyint(1) DEFAULT 1 COMMENT '奖品类型（1-实物奖品，2-虚拟奖品，3-谢谢参与）',
ADD COLUMN `is_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用（0-禁用，1-启用）';

-- 更新现有数据，将概率转换为权重
UPDATE `lottery_prize` SET 
`weight` = CASE 
    WHEN `probability` = 0.05 THEN 5
    WHEN `probability` = 0.10 THEN 10
    WHEN `probability` = 0.15 THEN 15
    WHEN `probability` = 0.20 THEN 20
    WHEN `probability` = 0.25 THEN 25
    WHEN `probability` = 0.50 THEN 50
    ELSE 10
END,
`prize_type` = CASE 
    WHEN `title` LIKE '%谢谢%' OR `title` LIKE '%参与%' THEN 3
    WHEN `title` LIKE '%优惠券%' OR `title` LIKE '%积分%' THEN 2
    ELSE 1
END;

-- 插入测试数据（使用权重系统）
INSERT INTO `lottery_prize` (`prize_id`, `activity_id`, `title`, `image_url`, `stock`, `weight`, `prize_type`, `sort_order`, `create_by`, `create_time`) VALUES
(12, 1, '特等奖', '/static/lottery/fangche.png', 1, 1, 1, 0, 'admin', NOW()),
(13, 1, '一等奖', '/static/lottery/huwaishoubiao.png', 2, 3, 1, 1, 'admin', NOW()),
(14, 1, '二等奖', '/static/lottery/jiguancejuyi.png', 5, 8, 1, 2, 'admin', NOW()),
(15, 1, '三等奖', '/static/lottery/jiguancejuyi.png', 10, 15, 1, 3, 'admin', NOW()),
(16, 1, '四等奖', '/static/lottery/huwaishoubiao.png', 20, 25, 1, 4, 'admin', NOW()),
(17, 1, '五等奖', '/static/lottery/fangche.png', 50, 40, 1, 5, 'admin', NOW()),
(18, 1, '谢谢参与', '/static/lottery/thanks.png', -1, 100, 3, 6, 'admin', NOW());

-- 抽奖奖品权重系统数据库设计
-- 作者: gkzh
-- 日期: 2025-01-17

-- 1. 抽奖活动表
CREATE TABLE IF NOT EXISTS `lottery_activity` (
  `activity_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `title` varchar(100) NOT NULL COMMENT '活动标题',
  `description` text COMMENT '活动描述',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` char(1) DEFAULT '0' COMMENT '活动状态（0未开始 1进行中 2已结束）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动表';

-- 2. 抽奖奖品表（简化版权重系统）
CREATE TABLE IF NOT EXISTS `lottery_prize` (
  `prize_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '奖品ID',
  `activity_id` bigint(20) NOT NULL COMMENT '所属活动ID',
  `title` varchar(100) NOT NULL COMMENT '奖品名称',
  `image_url` varchar(255) DEFAULT NULL COMMENT '奖品图片URL',
  `stock` int(11) DEFAULT 0 COMMENT '库存数量',
  `weight` int(11) NOT NULL DEFAULT 1 COMMENT '权重值（数值越大中奖概率越高）',
  `prize_type` char(1) DEFAULT '1' COMMENT '奖品类型（1实物奖品 2虚拟奖品 3谢谢参与）',
  `is_enabled` char(1) DEFAULT '0' COMMENT '启用状态（0启用 1禁用）',
  `sort_order` int(11) DEFAULT 0 COMMENT '显示顺序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`prize_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_weight` (`weight`),
  KEY `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖奖品表';

-- 3. 抽奖记录表
CREATE TABLE IF NOT EXISTS `lottery_record` (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
  `prize_id` bigint(20) NOT NULL COMMENT '中奖奖品ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) NOT NULL COMMENT '用户姓名',
  `prize_name` varchar(100) NOT NULL COMMENT '奖品名称',
  `weight` int(11) NOT NULL COMMENT '中奖权重',
  `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽奖时间',
  `status` char(1) DEFAULT '0' COMMENT '状态（0未领取 1已领取 2已过期）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_draw_time` (`draw_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖记录表';

-- 添加奖品类型字典数据
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (11, '奖品类型', 'lottery_prize_type', '0', 'admin', NOW(), '', NULL, '抽奖奖品类型列表');

-- 添加奖品类型字典数据项
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES 
(30, 1, '实物奖品', '1', 'lottery_prize_type', '', 'primary', 'Y', '0', 'admin', NOW(), '', NULL, '实物奖品'),
(31, 2, '虚拟奖品', '2', 'lottery_prize_type', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '虚拟奖品'),
(32, 3, '谢谢参与', '3', 'lottery_prize_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '谢谢参与');

-- 插入示例数据
INSERT INTO `lottery_activity` (`activity_id`, `title`, `description`, `start_time`, `end_time`, `status`) VALUES
(1, '新年抽奖活动', '新年抽奖活动，丰厚奖品等你来拿！', '2025-01-01 00:00:00', '2025-12-31 23:59:59', '1');

INSERT INTO `lottery_prize` (`activity_id`, `title`, `image_url`, `stock`, `weight`, `prize_type`, `is_enabled`, `sort_order`) VALUES
(1, 'iPhone 15', '/static/lottery/iphone.png', 1, 1, '1', '0', 1),
(1, 'AirPods Pro', '/static/lottery/airpods.png', 3, 5, '1', '0', 2),
(1, '小米手环', '/static/lottery/miband.png', 10, 20, '1', '0', 3),
(1, '50元话费', '/static/lottery/phone_bill.png', 50, 50, '2', '0', 4),
(1, '20元话费', '/static/lottery/phone_bill.png', 100, 100, '2', '0', 5),
(1, '谢谢参与', '/static/lottery/thanks.png', 999999, 200, '3', '0', 6); 