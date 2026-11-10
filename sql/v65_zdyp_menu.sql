-- 职得一拼（zdyp）游戏登记与后台菜单；可重复执行。
-- 菜单挂载到“游戏配置”（2235），权限点与 Web 控制器 @PreAuthorize 保持一致。
-- 当前最大菜单号为 2472、最大 config_id 为 10，本脚本使用 2473-2479 与自增 config_id。
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='participate_portrait')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN participate_portrait char(1) NOT NULL DEFAULT ''1'' COMMENT ''是否参与人物画像''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='view_type')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN view_type varchar(32) NOT NULL DEFAULT ''generic'' COMMENT ''活动查看模板类型''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 游戏编码 zdyp：活动与二维码使用的唯一编码，必须与 gkzh_game_config.route 一致。
INSERT INTO gkzh_game_type(game_type,game_name,front_component,result_type,status,create_time,update_time)
VALUES('zdyp','职得一拼','pages/zdyp/start','complete','0',NOW(),NOW())
ON DUPLICATE KEY UPDATE game_name=VALUES(game_name),front_component=VALUES(front_component),result_type=VALUES(result_type),status='0',update_time=NOW();

INSERT INTO gkzh_game_config(game_type,category,route,game_name,description,config_json,participate_portrait,view_type,status,create_time,update_time)
SELECT 'zdyp','choice','zdyp','职得一拼',
       '45秒内从10套新就业形态岗位中任选一套，把工作内容、核心技能、职业缺点、发展前景四张卡片拼回对应象限，还原完整岗位信息',
       '{}','0','zdyp','0',NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM gkzh_game_config WHERE game_type='zdyp');

UPDATE gkzh_game_config
SET category='choice',route='zdyp',game_name='职得一拼',participate_portrait='0',view_type='zdyp',status='0',update_time=NOW()
WHERE game_type='zdyp';

INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2473,'职得一拼',2235,99,'zdyp','','','','1',0,'M','0','0','','component','admin',NOW(),'admin',NOW(),'zdyp游戏配置目录'),
(2474,'所需配置',2473,1,'config','zdyp/config/index','','',1,0,'C','0','0','zdyp:config:list','edit','admin',NOW(),'admin',NOW(),'10套岗位、40张维度卡片与规则参数维护'),
(2475,'参与记录',2473,2,'records','zdyp/records/index','','',1,0,'C','0','0','zdyp:record:list','documentation','admin',NOW(),'admin',NOW(),'按活动、岗位、行为分型、学生属性和参与日期筛选及删除'),
(2476,'统计分析',2473,3,'statistics','zdyp/statistics/index','','',1,0,'C','0','0','zdyp:statistics:list','chart','admin',NOW(),'admin',NOW(),'学校端统计：通关率、维度错误、首填偏好与行为分型占比'),
(2477,'配置保存',2473,10,'#','','','',1,0,'F','1','0','zdyp:config:edit','#','admin',NOW(),'admin',NOW(),''),
(2478,'记录删除',2473,11,'#','','','',1,0,'F','1','0','zdyp:record:remove','#','admin',NOW(),'admin',NOW(),'删除整条参与记录及活动参与数据'),
(2479,'统计导出',2473,12,'#','','','',1,0,'F','1','0','zdyp:statistics:export','#','admin',NOW(),'admin',NOW(),'导出统计汇总、行为分型、维度错误、职业分布和学生明细');

-- 管理员角色始终授权；其他已有角色同步继承，避免上线后菜单不可见。
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,2473),(1,2474),(1,2475),(1,2476),(1,2477),(1,2478),(1,2479);
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2473 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2474 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2475 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2476 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2477 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2478 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2479 FROM sys_role;
