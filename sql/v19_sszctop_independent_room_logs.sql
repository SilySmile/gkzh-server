-- 职场TOP日志独立化：房间销毁后仍保留日志快照。
-- 兼容 MySQL 5.7：通过 information_schema 判断字段/索引后再执行 ALTER，脚本可重复执行。
DELIMITER $$
CREATE PROCEDURE gkzh_migrate_sszctop_room_log_snapshot()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'gkzh_sszctop_room_log' AND column_name = 'room_status') THEN
        ALTER TABLE gkzh_sszctop_room_log ADD COLUMN room_status varchar(16) DEFAULT NULL COMMENT '记录产生时的房间状态快照';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'gkzh_sszctop_room_log' AND column_name = 'student_name') THEN
        ALTER TABLE gkzh_sszctop_room_log ADD COLUMN student_name varchar(100) DEFAULT NULL COMMENT '日志对应用户姓名快照';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'gkzh_sszctop_room_log' AND column_name = 'student_no') THEN
        ALTER TABLE gkzh_sszctop_room_log ADD COLUMN student_no varchar(50) DEFAULT NULL COMMENT '日志对应用户学号快照';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'gkzh_sszctop_room_log' AND index_name = 'idx_sszctop_log_activity_user') THEN
        ALTER TABLE gkzh_sszctop_room_log ADD INDEX idx_sszctop_log_activity_user(instance_id, user_id, log_id);
    END IF;
END$$
DELIMITER ;
CALL gkzh_migrate_sszctop_room_log_snapshot();
DROP PROCEDURE gkzh_migrate_sszctop_room_log_snapshot;

-- 将已有房间/成员中的展示信息一次性复制到日志快照；后续查询不再关联这两张会被销毁的表。
UPDATE gkzh_sszctop_room_log l
LEFT JOIN gkzh_sszctop_room_member m ON m.room_id = l.room_id AND m.user_id = l.user_id
SET l.student_name = COALESCE(l.student_name, m.student_name),
    l.student_no = COALESCE(l.student_no, m.student_no)
WHERE l.user_id IS NOT NULL
  AND (l.student_name IS NULL OR l.student_no IS NULL);

-- 旧日志没有状态快照时仅补充一次；空房销毁后的新日志会直接写入 destroyed 快照。
UPDATE gkzh_sszctop_room_log l
LEFT JOIN gkzh_sszctop_room r ON r.room_id = l.room_id
SET l.room_status = COALESCE(l.room_status, r.status)
WHERE l.room_status IS NULL;
