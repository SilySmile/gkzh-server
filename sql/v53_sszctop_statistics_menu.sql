-- ============================================================================
-- 谁是职场TOP（sszctop）统计分析后台菜单与导出权限
-- 说明：菜单挂到 2300“谁是职场TOP”下，权限点与 SszctopStatisticsController 一致。
-- 脚本可重复执行；已存在的菜单和角色授权不会被重复插入。
-- ============================================================================
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2441,'统计分析',2300,5,'statistics','sszctop/statistics/index','','',1,0,'C','0','0','sszctop:statistics:list','chart','admin',NOW(),'admin',NOW(),'七维度完成情况、结果分布与学生明细'),
(2442,'统计导出',2300,12,'#','','','',1,0,'F','1','0','sszctop:statistics:export','#','admin',NOW(),'admin',NOW(),'导出统计汇总、维度统计与学生明细');

-- 先授权给超级管理员，再补齐现有全部角色，保证新菜单对所有角色可见。
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,2441),(1,2442);
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2441 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2442 FROM sys_role;
