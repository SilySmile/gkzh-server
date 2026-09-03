INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2250,CONVERT(0xE5B7A5E4BD9CE4BABAE59198E7AEA1E79086 USING utf8mb4),2084,'3','staff-account','staff/account/index','','StaffAccount',1,0,'C','0','0','school:school:list','peoples','admin',NOW(),'',NULL,CONVERT(0xE5ADA6E6A0A1E5B7A5E4BD9CE4BABAE59198E8B4A6E58FB7E9858DE7BDAE USING utf8mb4))
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), parent_id=VALUES(parent_id), order_num=VALUES(order_num), path=VALUES(path), component=VALUES(component), perms=VALUES(perms), remark=VALUES(remark), update_time=NOW();
INSERT IGNORE INTO `sys_role_menu` (`role_id`,`menu_id`) SELECT `role_id`,2250 FROM `sys_role` WHERE `role_id` <> 1;
INSERT IGNORE INTO `sys_role_menu` (`role_id`,`menu_id`) SELECT `role_id`,2250 FROM `sys_role` WHERE `role_id`=1;
