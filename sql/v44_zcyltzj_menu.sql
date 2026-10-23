-- 职场压力·骰子局（zcyltzj）游戏登记与后台菜单；可重复执行。
-- 菜单挂载到“游戏配置”（2235），权限点与 Web 控制器 @PreAuthorize 保持一致。
-- 旧环境若尚未执行 v22，先补齐人物画像标记字段，避免登记游戏配置时缺列。
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_game_config' AND column_name='participate_portrait')=0,
  'ALTER TABLE gkzh_game_config ADD COLUMN participate_portrait char(1) NOT NULL DEFAULT ''1'' COMMENT ''是否参与人物画像''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO gkzh_game_type(game_type,game_name,front_component,result_type,status,create_time,update_time)
VALUES('zcyltzj','职场压力·骰子局','pages/zcyltzj/start','complete','0',NOW(),NOW())
ON DUPLICATE KEY UPDATE game_name=VALUES(game_name),front_component=VALUES(front_component),result_type=VALUES(result_type),status='0',update_time=NOW();

INSERT INTO gkzh_game_config(game_type,category,route,game_name,description,config_json,participate_portrait,status,create_time,update_time)
SELECT 'zcyltzj','choice','zcyltzj','职场压力·骰子局','掷骰子抽取职场压力场景并完成六道选择题，生成压力接受度结果','{}','0','0',NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM gkzh_game_config WHERE game_type='zcyltzj');

UPDATE gkzh_game_config SET category='choice',route='zcyltzj',game_name='职场压力·骰子局',participate_portrait='0',status='0',update_time=NOW() WHERE game_type='zcyltzj';

INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2420,'职场压力·骰子局',2235,40,'zcyltzj','','','','1',0,'M','0','0','','star','admin',NOW(),'admin',NOW(),'zcyltzj游戏配置目录'),
(2421,'所需配置',2420,1,'config','zcyltzj/config/index','','',1,0,'C','0','0','zcyltzj:config:list','edit','admin',NOW(),'admin',NOW(),'六个点数名称与每套试题'),
(2422,'参与记录',2420,2,'records','zcyltzj/records/index','','',1,0,'C','0','0','zcyltzj:record:list','documentation','admin',NOW(),'admin',NOW(),'按活动、点数和学生属性筛选及删除'),
(2423,'统计分析',2420,3,'statistics','zcyltzj/statistics/index','','',1,0,'C','0','0','zcyltzj:statistics:list','chart','admin',NOW(),'admin',NOW(),'统计汇总、颜色分布和 Excel 导出'),
(2424,'配置保存',2420,10,'#','','','',1,0,'F','1','0','zcyltzj:config:edit','#','admin',NOW(),'admin',NOW(),''),
(2425,'记录删除',2420,11,'#','','','',1,0,'F','1','0','zcyltzj:record:remove','#','admin',NOW(),'admin',NOW(),'删除整条参与记录及活动参与数据'),
(2426,'统计导出',2420,12,'#','','','',1,0,'F','1','0','zcyltzj:statistics:export','#','admin',NOW(),'admin',NOW(),'导出统计汇总、学生数据和答题明细');

INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,2420),(1,2421),(1,2422),(1,2423),(1,2424),(1,2425),(1,2426);
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2420 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2421 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2422 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2423 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2424 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2425 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2426 FROM sys_role;
