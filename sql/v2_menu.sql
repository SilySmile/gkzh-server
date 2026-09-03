-- =============================================================
-- 活动配置、游戏配置菜单（使用原系统 sys_menu 架构）
-- =============================================================

-- 顶层：游戏配置
INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
  (2235, '游戏配置', 0, 8, 'game-config', NULL, '', 'GameConfigRoot', 1, 0, 'M', '0', '0', '', 'education', 'admin', NOW(), '', NULL, '游戏配置目录')
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `parent_id` = VALUES(`parent_id`),
  `order_num` = VALUES(`order_num`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `query` = VALUES(`query`),
  `route_name` = VALUES(`route_name`),
  `menu_type` = VALUES(`menu_type`),
  `visible` = VALUES(`visible`),
  `status` = VALUES(`status`),
  `perms` = VALUES(`perms`),
  `icon` = VALUES(`icon`),
  `update_time` = NOW();

-- 游戏配置 -> 配置列表
INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
  (2236, '配置列表', 2235, 1, 'config', 'game/config/index', '', 'GameConfig', 1, 0, 'C', '0', '0', 'game:config:list', 'education', 'admin', NOW(), '', NULL, '游戏配置列表')
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `parent_id` = VALUES(`parent_id`),
  `order_num` = VALUES(`order_num`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `query` = VALUES(`query`),
  `route_name` = VALUES(`route_name`),
  `menu_type` = VALUES(`menu_type`),
  `visible` = VALUES(`visible`),
  `status` = VALUES(`status`),
  `perms` = VALUES(`perms`),
  `icon` = VALUES(`icon`),
  `update_time` = NOW();

-- 将原有“心愿橱窗”及子菜单移动到“游戏配置”下面
UPDATE `sys_menu`
SET `parent_id` = 2235,
    `order_num` = 2,
    `update_time` = NOW()
WHERE `menu_id` = 2007;

-- 游戏配置 -> 编码解释
INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
  (2238, '编码解释', 2007, 6, 'holland', 'xycc/holland/index', '', 'HollandCode', 1, 0, 'C', '0', '0', 'xycc:holland:list', 'education', 'admin', NOW(), '', NULL, '心愿橱窗编码解释')
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `parent_id` = VALUES(`parent_id`),
  `order_num` = VALUES(`order_num`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `query` = VALUES(`query`),
  `route_name` = VALUES(`route_name`),
  `menu_type` = VALUES(`menu_type`),
  `visible` = VALUES(`visible`),
  `status` = VALUES(`status`),
  `perms` = VALUES(`perms`),
  `icon` = VALUES(`icon`),
  `update_time` = NOW();

-- 给非超管角色分配新菜单权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, 2235 FROM `sys_role` WHERE `role_id` <> 1;
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, 2236 FROM `sys_role` WHERE `role_id` <> 1;
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, 2237 FROM `sys_role` WHERE `role_id` <> 1;
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, 2007 FROM `sys_role` WHERE `role_id` <> 1;
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, 2238 FROM `sys_role` WHERE `role_id` <> 1;

-- 活动管理 -> 活动配置
INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
  (2237, '活动配置', 2006, 2, 'week', 'activity/week/index', '', 'ActivityWeek', 1, 0, 'C', '0', '0', 'activity:week:list', 'education', 'admin', NOW(), '', NULL, '活动配置')
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `parent_id` = VALUES(`parent_id`),
  `order_num` = VALUES(`order_num`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `query` = VALUES(`query`),
  `route_name` = VALUES(`route_name`),
  `menu_type` = VALUES(`menu_type`),
  `visible` = VALUES(`visible`),
  `status` = VALUES(`status`),
  `perms` = VALUES(`perms`),
  `icon` = VALUES(`icon`),
  `update_time` = NOW();
