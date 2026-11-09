-- 职面未来（zmwl）游戏登记与后台菜单；可重复执行。
-- 菜单挂载到“游戏配置”（2235），权限点与 Web 控制器 @PreAuthorize 保持一致。
-- 当前最大菜单号为 2458、最大 config_id 为 8，本脚本使用 2459-2465 与自增 config_id。
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='participate_portrait')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN participate_portrait char(1) NOT NULL DEFAULT ''1'' COMMENT ''是否参与人物画像''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='view_type')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN view_type varchar(32) NOT NULL DEFAULT ''generic'' COMMENT ''活动查看模板类型''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 游戏编码 zmwl：活动与二维码使用的唯一编码，必须与 gkzh_game_config.route 一致。
INSERT INTO gkzh_game_type(game_type,game_name,front_component,result_type,status,create_time,update_time)
VALUES('zmwl','职面未来','pages/zmwl/start','complete','0',NOW(),NOW())
ON DUPLICATE KEY UPDATE game_name=VALUES(game_name),front_component=VALUES(front_component),result_type=VALUES(result_type),status='0',update_time=NOW();

INSERT INTO gkzh_game_config(game_type,category,route,game_name,description,config_json,participate_portrait,view_type,status,create_time,update_time)
SELECT 'zmwl','choice','zmwl','职面未来',
       '从就业工作、国内升学、出国留学、考公考编、自主创业、自主择业、入伍参军、暂未确定 8 个方向中选择一条，生成个人未来选择卡',
       '{}','0','zmwl','0',NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM gkzh_game_config WHERE game_type='zmwl');

UPDATE gkzh_game_config
SET category='choice',route='zmwl',game_name='职面未来',participate_portrait='0',view_type='zmwl',status='0',update_time=NOW()
WHERE game_type='zmwl';

INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2459,'职面未来',2235,97,'zmwl','','','','1',0,'M','0','0','','star','admin',NOW(),'admin',NOW(),'zmwl游戏配置目录'),
(2460,'所需配置',2459,1,'config','zmwl/config/index','','',1,0,'C','0','0','zmwl:config:list','edit','admin',NOW(),'admin',NOW(),'8个未来发展方向与规则参数维护'),
(2461,'参与记录',2459,2,'records','zmwl/records/index','','',1,0,'C','0','0','zmwl:record:list','documentation','admin',NOW(),'admin',NOW(),'按活动、方向、结果、学生属性和参与日期筛选及删除'),
(2462,'统计分析',2459,3,'statistics','zmwl/statistics/index','','',1,0,'C','0','0','zmwl:statistics:list','chart','admin',NOW(),'admin',NOW(),'学校端统计：参与、完成、方向偏好占比与院系分布'),
(2463,'配置保存',2459,10,'#','','','',1,0,'F','1','0','zmwl:config:edit','#','admin',NOW(),'admin',NOW(),''),
(2464,'记录删除',2459,11,'#','','','',1,0,'F','1','0','zmwl:record:remove','#','admin',NOW(),'admin',NOW(),'删除整条参与记录及活动参与数据'),
(2465,'统计导出',2459,12,'#','','','',1,0,'F','1','0','zmwl:statistics:export','#','admin',NOW(),'admin',NOW(),'导出统计汇总、方向分布、院系偏好和学生明细');

-- 管理员角色始终授权；其他已有角色同步继承，避免上线后菜单不可见。
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,2459),(1,2460),(1,2461),(1,2462),(1,2463),(1,2464),(1,2465);
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2459 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2460 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2461 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2462 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2463 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2464 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2465 FROM sys_role;
