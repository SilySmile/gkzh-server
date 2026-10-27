-- 职业角斗场（zyjdc）游戏登记与后台菜单；可重复执行。
-- 菜单挂载到“游戏配置”（2235），权限点与 Web 控制器 @PreAuthorize 保持一致。
-- 旧环境若缺少 view_type / participate_portrait 字段，先补齐，避免登记游戏配置时缺列。
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='participate_portrait')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN participate_portrait char(1) NOT NULL DEFAULT ''1'' COMMENT ''是否参与人物画像''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='view_type')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN view_type varchar(32) NOT NULL DEFAULT ''generic'' COMMENT ''活动查看模板类型''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO gkzh_game_type(game_type,game_name,front_component,result_type,status,create_time,update_time)
VALUES('zyjdc','职业角斗场','pages/zyjdc/start','complete','0',NOW(),NOW())
ON DUPLICATE KEY UPDATE game_name=VALUES(game_name),front_component=VALUES(front_component),result_type=VALUES(result_type),status='0',update_time=NOW();

INSERT INTO gkzh_game_config(game_type,category,route,game_name,description,config_json,participate_portrait,view_type,status,create_time,update_time)
SELECT 'zyjdc','choice','zyjdc','职业角斗场','选择动物角色进行4×4斗兽棋对弈，3分钟内吃光对方或剩余最大棋子等级高者获胜','{}','0','zyjdc','0',NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM gkzh_game_config WHERE game_type='zyjdc');

UPDATE gkzh_game_config
SET category='choice',route='zyjdc',game_name='职业角斗场',participate_portrait='0',view_type='zyjdc',status='0',update_time=NOW()
WHERE game_type='zyjdc';

INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2427,'职业角斗场',2235,50,'zyjdc','','','','1',0,'M','0','0','','star','admin',NOW(),'admin',NOW(),'zyjdc游戏配置目录'),
(2428,'所需配置',2427,1,'config','zyjdc/config/index','','',1,0,'C','0','0','zyjdc:config:list','edit','admin',NOW(),'admin',NOW(),'8个动物角色内容与头像维护'),
(2429,'参与记录',2427,2,'records','zyjdc/records/index','','',1,0,'C','0','0','zyjdc:record:list','documentation','admin',NOW(),'admin',NOW(),'按活动、角色、结果和学生属性筛选及删除'),
(2430,'统计分析',2427,3,'statistics','zyjdc/statistics/index','','',1,0,'C','0','0','zyjdc:statistics:list','chart','admin',NOW(),'admin',NOW(),'角色偏好、胜负分布与Excel导出'),
(2431,'配置保存',2427,10,'#','','','',1,0,'F','1','0','zyjdc:config:edit','#','admin',NOW(),'admin',NOW(),''),
(2432,'记录删除',2427,11,'#','','','',1,0,'F','1','0','zyjdc:record:remove','#','admin',NOW(),'admin',NOW(),'删除整条参与记录及活动参与数据'),
(2433,'统计导出',2427,12,'#','','','',1,0,'F','1','0','zyjdc:statistics:export','#','admin',NOW(),'admin',NOW(),'导出统计汇总、角色分布和学生明细');

INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,2427),(1,2428),(1,2429),(1,2430),(1,2431),(1,2432),(1,2433);
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2427 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2428 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2429 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2430 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2431 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2432 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2433 FROM sys_role;
