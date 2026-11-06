-- 简历称重（jlcz）游戏登记与后台菜单；可重复执行。
-- 菜单挂载到“游戏配置”（2235），权限点与 Web 控制器 @PreAuthorize 保持一致。
-- 当前最大菜单号为 2451、最大 config_id 为 7，本脚本使用 2452-2458 与自增 config_id。
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='participate_portrait')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN participate_portrait char(1) NOT NULL DEFAULT ''1'' COMMENT ''是否参与人物画像''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='view_type')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN view_type varchar(32) NOT NULL DEFAULT ''generic'' COMMENT ''活动查看模板类型''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 游戏编码 jlcz：活动与二维码使用的唯一编码，必须与 gkzh_game_config.route 一致。
INSERT INTO gkzh_game_type(game_type,game_name,front_component,result_type,status,create_time,update_time)
VALUES('jlcz','简历称重','pages/jlcz/start','complete','0',NOW(),NOW())
ON DUPLICATE KEY UPDATE game_name=VALUES(game_name),front_component=VALUES(front_component),result_type=VALUES(result_type),status='0',update_time=NOW();

INSERT INTO gkzh_game_config(game_type,category,route,game_name,description,config_json,participate_portrait,view_type,status,create_time,update_time)
SELECT 'jlcz','choice','jlcz','简历称重',
       '对照简历勾选四大类共 20 个砝码，挂到天平右侧与 80g 标准砝码比较；总重达到 80g 即通关，未通关可重新称重',
       '{}','0','jlcz','0',NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM gkzh_game_config WHERE game_type='jlcz');

UPDATE gkzh_game_config
SET category='choice',route='jlcz',game_name='简历称重',participate_portrait='0',view_type='jlcz',status='0',update_time=NOW()
WHERE game_type='jlcz';

INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2452,'简历称重',2235,96,'jlcz','','','','1',0,'M','0','0','','star','admin',NOW(),'admin',NOW(),'jlcz游戏配置目录'),
(2453,'所需配置',2452,1,'config','jlcz/config/index','','',1,0,'C','0','0','jlcz:config:list','edit','admin',NOW(),'admin',NOW(),'20个砝码与天平规则参数维护'),
(2454,'参与记录',2452,2,'records','jlcz/records/index','','',1,0,'C','0','0','jlcz:record:list','documentation','admin',NOW(),'admin',NOW(),'按活动、结果、学生属性和参与日期筛选及删除'),
(2455,'统计分析',2452,3,'statistics','jlcz/statistics/index','','',1,0,'C','0','0','jlcz:statistics:list','chart','admin',NOW(),'admin',NOW(),'学校端统计：参与、通关率、平均重量、砝码选择率与缺口'),
(2456,'配置保存',2452,10,'#','','','',1,0,'F','1','0','jlcz:config:edit','#','admin',NOW(),'admin',NOW(),''),
(2457,'记录删除',2452,11,'#','','','',1,0,'F','1','0','jlcz:record:remove','#','admin',NOW(),'admin',NOW(),'删除整条参与记录、砝码明细及活动参与数据'),
(2458,'统计导出',2452,12,'#','','','',1,0,'F','1','0','jlcz:statistics:export','#','admin',NOW(),'admin',NOW(),'导出统计汇总、砝码选择率、类别统计和学生明细');

-- 管理员角色始终授权；其他已有角色同步继承，避免上线后菜单不可见。
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,2452),(1,2453),(1,2454),(1,2455),(1,2456),(1,2457),(1,2458);
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2452 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2453 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2454 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2455 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2456 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2457 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2458 FROM sys_role;
