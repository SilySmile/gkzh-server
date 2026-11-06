-- 未来职业猜猜看（zycck）游戏配置登记、画像标记及后台菜单。
-- 可重复执行；菜单挂载到“游戏配置”父菜单（2235）。
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='participate_portrait')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN participate_portrait char(1) NOT NULL DEFAULT ''1'' COMMENT ''是否参与人物画像''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO gkzh_game_type(game_type,game_name,front_component,result_type,status,create_time,update_time)
VALUES('zycck','未来职业猜猜看','pages/zycck/start','complete','0',NOW(),NOW())
ON DUPLICATE KEY UPDATE game_name=VALUES(game_name),front_component=VALUES(front_component),result_type=VALUES(result_type),status='0',update_time=NOW();
INSERT INTO gkzh_game_config(game_type,category,route,game_name,description,config_json,participate_portrait,status,create_time,update_time)
SELECT 'zycck','choice','zycck','未来职业猜猜看','单人选择类职业探索游戏','{}','1','0',NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM gkzh_game_config WHERE game_type='zycck');
UPDATE gkzh_game_config SET category='choice',route='zycck',game_name='未来职业猜猜看',participate_portrait='1',status='0',update_time=NOW() WHERE game_type='zycck';

INSERT INTO gkzh_zycck_category(code,name,description,draw_mode,sort_order,status,create_time,update_time) VALUES
('technology','科技与创新','关注技术、研发与创新场景。','fixed',1,'0',NOW(),NOW()),
('health','健康与生命','关注医疗、健康与生命服务场景。','fixed',2,'0',NOW(),NOW()),
('culture','文化与传播','关注文化、内容与传播场景。','fixed',3,'0',NOW(),NOW()),
('management','组织与管理','关注组织协作、管理与公共服务场景。','fixed',4,'0',NOW(),NOW()),
('nature','自然与生活','关注自然、农业与生活服务场景。','fixed',5,'0',NOW(),NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name),description=VALUES(description),sort_order=VALUES(sort_order),status='0',update_time=NOW();

INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2400,'职业猜猜看',2235,30,'zycck','','','','1',0,'M','0','0','','job','admin',NOW(),'admin',NOW(),'zycck游戏配置目录'),
(2401,'所需配置',2400,1,'config','zycck/config/index','','',1,0,'C','0','0','zycck:config:list','edit','admin',NOW(),'admin',NOW(),'五类职业、题目及抽题方式'),
(2402,'参与记录',2400,2,'records','zycck/records/index','','',1,0,'C','0','0','zycck:record:list','documentation','admin',NOW(),'admin',NOW(),'按活动和学生属性筛选'),
(2403,'统计分析',2400,3,'statistics','zycck/statistics/index','','',1,0,'C','0','0','zycck:statistics:list','chart','admin',NOW(),'admin',NOW(),'统计及PDF导出'),
(2404,'配置新增',2400,10,'#','','','',1,0,'F','1','0','zycck:config:add','#','admin',NOW(),'admin',NOW(),''),
(2405,'配置修改',2400,11,'#','','','',1,0,'F','1','0','zycck:config:edit','#','admin',NOW(),'admin',NOW(),''),
(2406,'统计导出',2400,12,'#','','','',1,0,'F','1','0','zycck:statistics:export','#','admin',NOW(),'admin',NOW(),'');
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,2400),(1,2401),(1,2402),(1,2403),(1,2404),(1,2405),(1,2406);
UPDATE sys_menu SET parent_id=2235, menu_name='职业猜猜看', path='zycck', component='', menu_type='M', perms='', update_time=NOW() WHERE menu_id=2400;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2400 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2401 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2402 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2403 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2404 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2405 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2406 FROM sys_role;
