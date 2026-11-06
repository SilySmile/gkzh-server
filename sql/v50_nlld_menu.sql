-- 能量雷达（nlld）游戏登记与后台菜单；可重复执行。
-- 菜单挂载到“游戏配置”（2235），权限点与 Web 控制器 @PreAuthorize 保持一致。
-- 旧环境若缺少 view_type / participate_portrait 字段，先补齐，避免登记游戏配置时缺列。
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='participate_portrait')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN participate_portrait char(1) NOT NULL DEFAULT ''1'' COMMENT ''是否参与人物画像''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='view_type')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN view_type varchar(32) NOT NULL DEFAULT ''generic'' COMMENT ''活动查看模板类型''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO gkzh_game_type(game_type,game_name,front_component,result_type,status,create_time,update_time)
VALUES('nlld','能量雷达','pages/nlld/start','complete','0',NOW(),NOW())
ON DUPLICATE KEY UPDATE game_name=VALUES(game_name),front_component=VALUES(front_component),result_type=VALUES(result_type),status='0',update_time=NOW();

INSERT INTO gkzh_game_config(game_type,category,route,game_name,description,config_json,participate_portrait,view_type,status,create_time,update_time)
SELECT 'nlld','choice','nlld','能量雷达','6 关各从 5 件物品中选择 2 件，生成职业价值观 TOP2 或多元开放型画像','{}','0','nlld','0',NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM gkzh_game_config WHERE game_type='nlld');

UPDATE gkzh_game_config
SET category='choice',route='nlld',game_name='能量雷达',participate_portrait='0',view_type='nlld',status='0',update_time=NOW()
WHERE game_type='nlld';

INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2434,'能量雷达',2235,60,'nlld','','','','1',0,'M','0','0','','star','admin',NOW(),'admin',NOW(),'nlld游戏配置目录'),
(2435,'所需配置',2434,1,'config','nlld/config/index','','',1,0,'C','0','0','nlld:config:list','edit','admin',NOW(),'admin',NOW(),'6关30件物品与15个维度解读维护'),
(2436,'参与记录',2434,2,'records','nlld/records/index','','',1,0,'C','0','0','nlld:record:list','documentation','admin',NOW(),'admin',NOW(),'按活动、TOP维度、结果类型和学生属性筛选及删除'),
(2437,'统计分析',2434,3,'statistics','nlld/statistics/index','','',1,0,'C','0','0','nlld:statistics:list','chart','admin',NOW(),'admin',NOW(),'维度偏好、TOP2/多元开放型分布与Excel导出'),
(2438,'配置保存',2434,10,'#','','','',1,0,'F','1','0','nlld:config:edit','#','admin',NOW(),'admin',NOW(),''),
(2439,'记录删除',2434,11,'#','','','',1,0,'F','1','0','nlld:record:remove','#','admin',NOW(),'admin',NOW(),'删除整条参与记录及活动参与数据'),
(2440,'统计导出',2434,12,'#','','','',1,0,'F','1','0','nlld:statistics:export','#','admin',NOW(),'admin',NOW(),'导出统计汇总、维度偏好和学生明细');

INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,2434),(1,2435),(1,2436),(1,2437),(1,2438),(1,2439),(1,2440);
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2434 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2435 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2436 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2437 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2438 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2439 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2440 FROM sys_role;
