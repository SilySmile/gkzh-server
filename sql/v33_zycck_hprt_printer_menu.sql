-- 未来职业猜猜看：汉印云打印机与打印任务后台菜单。
-- 可重复执行；依赖 v22 创建的“职业猜猜看”目录（2400）和 v32 打印表。
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2407,'打印机管理',2400,4,'printers','zycck/printers/index','','',1,0,'C','0','0','zycck:printer:list','printer','admin',NOW(),'admin',NOW(),'汉印云打印机绑定、同步及状态维护'),
(2408,'打印任务',2400,5,'print-tasks','zycck/print-tasks/index','','',1,0,'C','0','0','zycck:print-task:list','list','admin',NOW(),'admin',NOW(),'汉印云打印任务查询及重打'),
(2409,'打印机同步',2407,1,'#','','','',1,0,'F','1','0','zycck:printer:sync','#','admin',NOW(),'admin',NOW(),''),
(2410,'打印机绑定',2407,2,'#','','','',1,0,'F','1','0','zycck:printer:bind','#','admin',NOW(),'admin',NOW(),''),
(2411,'打印机修改',2407,3,'#','','','',1,0,'F','1','0','zycck:printer:edit','#','admin',NOW(),'admin',NOW(),''),
(2412,'打印机解绑',2407,4,'#','','','',1,0,'F','1','0','zycck:printer:remove','#','admin',NOW(),'admin',NOW(),''),
(2413,'任务状态刷新',2408,1,'#','','','',1,0,'F','1','0','zycck:print-task:query','#','admin',NOW(),'admin',NOW(),''),
(2414,'打印任务重打',2408,2,'#','','','',1,0,'F','1','0','zycck:print-task:reprint','#','admin',NOW(),'admin',NOW(),'');

UPDATE sys_menu SET parent_id=2400, order_num=4, menu_name='打印机管理', path='printers', component='zycck/printers/index', menu_type='C', perms='zycck:printer:list', update_time=NOW() WHERE menu_id=2407;
UPDATE sys_menu SET parent_id=2400, order_num=5, menu_name='打印任务', path='print-tasks', component='zycck/print-tasks/index', menu_type='C', perms='zycck:print-task:list', update_time=NOW() WHERE menu_id=2408;

INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2407 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2408 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2409 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2410 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2411 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2412 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2413 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2414 FROM sys_role;
