-- ============================================================================
-- 心愿橱窗（xycc）统计分析后台菜单与导出权限
-- 说明：菜单挂到 2007“心愿橱窗”下，权限点与 XyccStatisticsController 一致。
-- 脚本可重复执行；已存在的菜单和角色授权不会被重复插入。
-- ============================================================================
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2443,'统计分析',2007,9,'statistics','xycc/statistics/index','','',1,0,'C','0','0','xycc:statistics:list','chart','admin',NOW(),'admin',NOW(),'编码分布、职业方向与学生明细'),
(2444,'统计导出',2007,12,'#','','','',1,0,'F','1','0','xycc:statistics:export','#','admin',NOW(),'admin',NOW(),'导出统计汇总、编码分布与学生明细');

-- 先授权给超级管理员，再补齐现有全部角色。
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,2443),(1,2444);
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2443 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2444 FROM sys_role;
