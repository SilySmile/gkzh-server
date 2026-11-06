-- 职业飞行棋（zyfxq）游戏登记与后台菜单；可重复执行。
-- 菜单挂载到“游戏配置”（2235），权限点与 Web 控制器 @PreAuthorize 保持一致。
-- 当前最大菜单号为 2444、最大 config_id 为 6，本脚本使用 2445-2451 与自增 config_id。
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='participate_portrait')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN participate_portrait char(1) NOT NULL DEFAULT ''1'' COMMENT ''是否参与人物画像''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='view_type')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN view_type varchar(32) NOT NULL DEFAULT ''generic'' COMMENT ''活动查看模板类型''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 游戏编码 zyfxq：活动与二维码使用的唯一编码，必须与 gkzh_game_config.route 一致。
INSERT INTO gkzh_game_type(game_type,game_name,front_component,result_type,status,create_time,update_time)
VALUES('zyfxq','职业飞行棋','pages/zyfxq/start','complete','0',NOW(),NOW())
ON DUPLICATE KEY UPDATE game_name=VALUES(game_name),front_component=VALUES(front_component),result_type=VALUES(result_type),status='0',update_time=NOW();

INSERT INTO gkzh_game_config(game_type,category,route,game_name,description,config_json,participate_portrait,view_type,status,create_time,update_time)
SELECT 'zyfxq','choice','zyfxq','职业飞行棋',
       '19 格环形棋盘，单骰掷点，踩到答题格答 20 秒内单选题，答对继续掷骰、答错后退 2 格，3 分钟内抵达终点通关',
       '{}','0','zyfxq','0',NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM gkzh_game_config WHERE game_type='zyfxq');

UPDATE gkzh_game_config
SET category='choice',route='zyfxq',game_name='职业飞行棋',participate_portrait='0',view_type='zyfxq',status='0',update_time=NOW()
WHERE game_type='zyfxq';

INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2445,'职业飞行棋',2235,95,'zyfxq','','','','1',0,'M','0','0','','job','admin',NOW(),'admin',NOW(),'zyfxq游戏配置目录'),
(2446,'所需配置',2445,1,'config','zyfxq/config/index','','',1,0,'C','0','0','zyfxq:config:list','edit','admin',NOW(),'admin',NOW(),'19格棋盘、规则参数与职业问答/情景任务卡题库维护'),
(2447,'参与记录',2445,2,'records','zyfxq/records/index','','',1,0,'C','0','0','zyfxq:record:list','documentation','admin',NOW(),'admin',NOW(),'按活动、结果、学生属性和参与日期筛选及删除'),
(2448,'统计分析',2445,3,'statistics','zyfxq/statistics/index','','',1,0,'C','0','0','zyfxq:statistics:list','chart','admin',NOW(),'admin',NOW(),'结果输出学校端：参与、平均耗时、掷骰次数、通过率与题目统计'),
(2449,'配置保存',2445,10,'#','','','',1,0,'F','1','0','zyfxq:config:edit','#','admin',NOW(),'admin',NOW(),''),
(2450,'记录删除',2445,11,'#','','','',1,0,'F','1','0','zyfxq:record:remove','#','admin',NOW(),'admin',NOW(),'删除整条参与记录、答题明细及活动参与数据'),
(2451,'统计导出',2445,12,'#','','','',1,0,'F','1','0','zyfxq:statistics:export','#','admin',NOW(),'admin',NOW(),'导出统计汇总、题目统计和学生明细');

-- 管理员角色始终授权；其他已有角色同步继承，避免上线后菜单不可见。
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,2445),(1,2446),(1,2447),(1,2448),(1,2449),(1,2450),(1,2451);
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2445 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2446 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2447 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2448 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2449 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2450 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2451 FROM sys_role;
