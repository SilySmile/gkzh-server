-- zycck 打印限制、任务快照字段、取消权限及菜单调整（MySQL 5.7+，可重复执行）。
-- 信标配置统一由 Web“信标管理”维护；新环境建表见 v32，已有环境补表见 v37，独立菜单见 v38。

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='user_id')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN user_id BIGINT DEFAULT NULL COMMENT ''平台用户编号'' AFTER record_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='school_id')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN school_id BIGINT DEFAULT NULL COMMENT ''学校编号'' AFTER user_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='school_name')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN school_name VARCHAR(100) DEFAULT NULL COMMENT ''学校名称'' AFTER school_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='student_id')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN student_id BIGINT DEFAULT NULL COMMENT ''学生编号'' AFTER school_name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='student_no')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN student_no VARCHAR(100) DEFAULT NULL COMMENT ''学号'' AFTER student_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='student_name')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN student_name VARCHAR(100) DEFAULT NULL COMMENT ''学生姓名'' AFTER student_no', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='instance_id')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN instance_id BIGINT DEFAULT NULL COMMENT ''活动实例编号'' AFTER student_name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='activity_name')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN activity_name VARCHAR(255) DEFAULT NULL COMMENT ''活动名称'' AFTER instance_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='game_id')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN game_id BIGINT DEFAULT NULL COMMENT ''活动游戏编号'' AFTER activity_name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='game_name')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN game_name VARCHAR(255) DEFAULT NULL COMMENT ''游戏名称'' AFTER game_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='beacon_uuid')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN beacon_uuid VARCHAR(100) DEFAULT NULL COMMENT ''打印时检测到的信标 UUID'' AFTER retry_count', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='beacon_major')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN beacon_major INT DEFAULT NULL COMMENT ''信标 Major'' AFTER beacon_uuid', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='beacon_minor')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN beacon_minor INT DEFAULT NULL COMMENT ''信标 Minor'' AFTER beacon_major', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='beacon_rssi')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN beacon_rssi INT DEFAULT NULL COMMENT ''信标 RSSI'' AFTER beacon_minor', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='beacon_distance')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN beacon_distance DECIMAL(8,2) DEFAULT NULL COMMENT ''信标估算距离（米）'' AFTER beacon_rssi', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND column_name='cancel_time')=0, 'ALTER TABLE gkzh_zycck_print_task ADD COLUMN cancel_time DATETIME DEFAULT NULL COMMENT ''取消时间'' AFTER print_time', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE gkzh_zycck_print_task t
JOIN gkzh_zycck_record r ON r.record_id=t.record_id
LEFT JOIN gkzh_student st ON st.student_id=COALESCE(r.student_id,
    (SELECT st2.student_id FROM gkzh_student st2 WHERE st2.user_id=r.user_id
     AND st2.school_id=r.school_id AND st2.del_flag='0' ORDER BY st2.student_id LIMIT 1))
LEFT JOIN gkzh_school sc ON sc.school_id=r.school_id
LEFT JOIN gkzh_activity_week_instance ai ON ai.instance_id=r.instance_id
LEFT JOIN gkzh_activity_game ag ON ag.game_id=r.game_id
SET t.user_id=COALESCE(t.user_id,r.user_id), t.school_id=COALESCE(t.school_id,r.school_id),
    t.school_name=COALESCE(t.school_name,sc.title), t.student_id=COALESCE(t.student_id,r.student_id,st.student_id),
    t.student_no=COALESCE(t.student_no,st.student_no), t.student_name=COALESCE(t.student_name,st.student_name),
    t.instance_id=COALESCE(t.instance_id,r.instance_id), t.activity_name=COALESCE(t.activity_name,ai.title),
    t.game_id=COALESCE(t.game_id,r.game_id), t.game_name=COALESCE(t.game_name,ag.title)
WHERE t.user_id IS NULL OR t.school_name IS NULL OR t.student_no IS NULL OR t.student_name IS NULL
   OR t.activity_name IS NULL OR t.game_name IS NULL;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND index_name='idx_zycck_print_task_user_status')=0, 'CREATE INDEX idx_zycck_print_task_user_status ON gkzh_zycck_print_task(user_id,status)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_print_task' AND index_name='idx_zycck_print_task_student')=0, 'CREATE INDEX idx_zycck_print_task_student ON gkzh_zycck_print_task(student_no,student_name)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- “系统管理 → 打印管理 → 打印机管理/打印任务”。
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark) VALUES
(2415,'打印管理',1,10,'print-management','','','',1,0,'M','0','0','','printer','admin',NOW(),'admin',NOW(),'云打印管理目录'),
(2416,'取消打印任务',2408,3,'#','','','',1,0,'F','1','0','zycck:print-task:cancel','#','admin',NOW(),'admin',NOW(),'');
UPDATE sys_menu SET parent_id=1,order_num=10,menu_name='打印管理',path='print-management',component='',menu_type='M',perms='',icon='printer',update_time=NOW() WHERE menu_id=2415;
UPDATE sys_menu SET parent_id=2415,order_num=1,menu_name='打印机管理',path='printers',component='zycck/printers/index',menu_type='C',update_time=NOW() WHERE menu_id=2407;
UPDATE sys_menu SET parent_id=2415,order_num=2,menu_name='打印任务',path='print-tasks',component='zycck/print-tasks/index',menu_type='C',update_time=NOW() WHERE menu_id=2408;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2415 FROM sys_role;
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT role_id,2416 FROM sys_role;
