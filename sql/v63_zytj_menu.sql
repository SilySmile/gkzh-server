-- 职愿探究（zytj）游戏登记与后台菜单；可重复执行。
-- 菜单挂载到“游戏配置”（2235），权限点与 Web 控制器 @PreAuthorize 保持一致。
-- 当前最大菜单号为 2465、最大 config_id 为 9，本脚本使用 2466-2472 与自增 config_id。

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='participate_portrait')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN participate_portrait char(1) NOT NULL DEFAULT ''1'' COMMENT ''是否参与人物画像''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='view_type')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN view_type varchar(32) NOT NULL DEFAULT ''generic'' COMMENT ''活动查看模板类型''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 游戏编码 zytj：活动与二维码使用的唯一编码，必须与 gkzh_game_config.route 一致。
INSERT INTO gkzh_game_type(game_type,game_name,front_component,result_type,status,create_time,update_time)
VALUES('zytj','职愿探究','pages/zytj/start','complete','0',NOW(),NOW())
ON DUPLICATE KEY UPDATE game_name=VALUES(game_name),front_component=VALUES(front_component),result_type=VALUES(result_type),status='0',update_time=NOW();

-- MBTI 是人物画像的核心输入，因此 participate_portrait 置 1。
INSERT INTO gkzh_game_config(game_type,category,route,game_name,description,config_json,participate_portrait,view_type,status,create_time,update_time)
SELECT 'zytj','choice','zytj','职愿探究',
       '30 秒内从四列橱窗中各选 1 件物品，按列序生成四位人格类型代码，输出职业探索卡',
       '{}','1','zytj','0',NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM gkzh_game_config WHERE game_type='zytj');

UPDATE gkzh_game_config
SET category='choice',route='zytj',game_name='职愿探究',participate_portrait='1',view_type='zytj',status='0',update_time=NOW()
WHERE game_type='zytj';

INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2466,'职愿探究',2235,98,'zytj','','','','1',0,'M','0','0','','component','admin',NOW(),'admin',NOW(),'zytj游戏配置目录'),
(2467,'所需配置',2466,1,'config','zytj/config/index','','',1,0,'C','0','0','zytj:config:list','edit','admin',NOW(),'admin',NOW(),'8个橱窗物品、16型人格对照与规则参数维护'),
(2468,'参与记录',2466,2,'records','zytj/records/index','','',1,0,'C','0','0','zytj:record:list','documentation','admin',NOW(),'admin',NOW(),'按活动、人格类型、学生属性和参与日期筛选及删除'),
(2469,'统计分析',2466,3,'statistics','zytj/statistics/index','','',1,0,'C','0','0','zytj:statistics:list','chart','admin',NOW(),'admin',NOW(),'学校端统计：参与、提交、16型分布与院系分布'),
(2470,'配置保存',2466,10,'#','','','',1,0,'F','1','0','zytj:config:edit','#','admin',NOW(),'admin',NOW(),''),
(2471,'记录删除',2466,11,'#','','','',1,0,'F','1','0','zytj:record:remove','#','admin',NOW(),'admin',NOW(),'删除整条参与记录及活动参与数据'),
(2472,'统计导出',2466,12,'#','','','',1,0,'F','1','0','zytj:statistics:export','#','admin',NOW(),'admin',NOW(),'导出统计汇总、型别分布、院系分布和学生明细');

-- 管理员角色始终授权；其他已有角色同步继承，避免上线后菜单不可见。
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,2466),(1,2467),(1,2468),(1,2469),(1,2470),(1,2471),(1,2472);
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2466 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2467 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2468 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2469 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2470 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2471 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2472 FROM sys_role;
