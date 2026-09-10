-- 删除未注册学生及其在各业务表中的绑定记录
--
-- 未注册判定（与系统当前逻辑一致）：
--   gkzh_student.password IS NULL 或 TRIM(password) = ''
--
-- 使用方式：
--   1) 先执行：CALL gkzh_purge_unregistered_users(0);  -- 仅预览，不删除
--   2) 确认预览结果后执行：CALL gkzh_purge_unregistered_users(1); -- 正式删除并提交
--   3) 清理完成后可执行：DROP PROCEDURE IF EXISTS gkzh_purge_unregistered_users;
--
-- 说明：
--   * 自动扫描当前数据库所有基础表中名为 user_id / student_id 的字段。
--   * 会清理职业猜猜看、活动参与、签到、抽奖等所有直接绑定到这些 ID 的记录。
--   * gkzh_student 与 sys_user 在扫描清理后最后删除，避免产生孤立记录。
--   * 这是物理删除，请先备份数据库；正式执行前务必先调用参数 0 预览。

DROP PROCEDURE IF EXISTS gkzh_purge_unregistered_users;

DELIMITER //
CREATE PROCEDURE gkzh_purge_unregistered_users(IN p_execute TINYINT)
BEGIN
    DECLARE v_done TINYINT DEFAULT 0;
    DECLARE v_table_name VARCHAR(128);
    DECLARE v_has_user_id TINYINT;
    DECLARE v_has_student_id TINYINT;
    DECLARE v_condition VARCHAR(1200);
    DECLARE v_sql TEXT;

    DECLARE cur_tables CURSOR FOR
        SELECT c.table_name,
               MAX(c.column_name = 'user_id'),
               MAX(c.column_name = 'student_id')
          FROM information_schema.columns c
         WHERE c.table_schema = DATABASE()
           AND c.table_name NOT IN ('gkzh_student', 'sys_user')
           AND c.table_name NOT LIKE 'tmp\\_%'
           AND c.table_name NOT LIKE 'gkzh_purge\\_%'
           AND EXISTS (
                 SELECT 1
                   FROM information_schema.tables t
                  WHERE t.table_schema = c.table_schema
                    AND t.table_name = c.table_name
                    AND t.table_type = 'BASE TABLE'
           )
         GROUP BY c.table_name;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        SET FOREIGN_KEY_CHECKS = 1;
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_execute IS NULL OR p_execute NOT IN (0, 1) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '参数只能是 0（预览）或 1（执行删除）';
    END IF;

    DROP TEMPORARY TABLE IF EXISTS tmp_gkzh_unregistered_students;
    CREATE TEMPORARY TABLE tmp_gkzh_unregistered_students (
        student_id BIGINT NOT NULL PRIMARY KEY,
        user_id BIGINT NULL,
        KEY idx_tmp_unregistered_user_id (user_id)
    ) ENGINE = InnoDB;

    INSERT INTO tmp_gkzh_unregistered_students (student_id, user_id)
    SELECT s.student_id, s.user_id
      FROM gkzh_student s
     WHERE (s.password IS NULL OR TRIM(s.password) = '')
       AND COALESCE(s.del_flag, '0') <> '2';

    DROP TEMPORARY TABLE IF EXISTS tmp_gkzh_unregistered_users;
    CREATE TEMPORARY TABLE tmp_gkzh_unregistered_users (
        user_id BIGINT NOT NULL PRIMARY KEY
    ) ENGINE = InnoDB;

    -- 同一个系统账号如果仍被其他已注册学生使用，则保留该系统账号。
    INSERT INTO tmp_gkzh_unregistered_users (user_id)
    SELECT DISTINCT u.user_id
      FROM tmp_gkzh_unregistered_students u
     WHERE u.user_id IS NOT NULL
       AND NOT EXISTS (
             SELECT 1
               FROM gkzh_student registered_student
              WHERE registered_student.user_id = u.user_id
                AND registered_student.password IS NOT NULL
                AND TRIM(registered_student.password) <> ''
                AND COALESCE(registered_student.del_flag, '0') <> '2'
       );

    DROP TEMPORARY TABLE IF EXISTS tmp_gkzh_purge_preview;
    CREATE TEMPORARY TABLE tmp_gkzh_purge_preview (
        table_name VARCHAR(128) NOT NULL PRIMARY KEY,
        matched_rows BIGINT NOT NULL DEFAULT 0
    ) ENGINE = InnoDB;

    START TRANSACTION;
    SET FOREIGN_KEY_CHECKS = 0;

    -- 逐表统计将被清理的绑定记录，供预览和执行后的核对使用。
    OPEN cur_tables;
    table_loop: LOOP
        FETCH cur_tables INTO v_table_name, v_has_user_id, v_has_student_id;
        IF v_done = 1 THEN
            LEAVE table_loop;
        END IF;

        IF v_has_user_id = 1 AND v_has_student_id = 1 THEN
            SET v_condition = '(`user_id` IN (SELECT user_id FROM tmp_gkzh_unregistered_users) OR `student_id` IN (SELECT student_id FROM tmp_gkzh_unregistered_students))';
        ELSEIF v_has_user_id = 1 THEN
            SET v_condition = '(`user_id` IN (SELECT user_id FROM tmp_gkzh_unregistered_users))';
        ELSE
            SET v_condition = '(`student_id` IN (SELECT student_id FROM tmp_gkzh_unregistered_students))';
        END IF;

        SET @gkzh_purge_matched_rows = 0;
        SET v_sql = CONCAT(
            'SELECT COUNT(*) INTO @gkzh_purge_matched_rows FROM `',
            REPLACE(v_table_name, '`', '``'),
            '` WHERE ', v_condition
        );
        SET @gkzh_purge_sql = v_sql;
        PREPARE stmt_count FROM @gkzh_purge_sql;
        EXECUTE stmt_count;
        DEALLOCATE PREPARE stmt_count;

        INSERT INTO tmp_gkzh_purge_preview (table_name, matched_rows)
        VALUES (v_table_name, @gkzh_purge_matched_rows);
    END LOOP;
    CLOSE cur_tables;

    SELECT '未注册学生数量' AS item, COUNT(*) AS amount
      FROM tmp_gkzh_unregistered_students
    UNION ALL
    SELECT '将删除的系统账号数量', COUNT(*)
      FROM tmp_gkzh_unregistered_users;

    SELECT student_id, user_id
      FROM tmp_gkzh_unregistered_students
     ORDER BY student_id;

    SELECT table_name, matched_rows
      FROM tmp_gkzh_purge_preview
     WHERE matched_rows > 0
     ORDER BY matched_rows DESC, table_name;

    IF p_execute = 1 THEN
        -- 先删除所有业务绑定记录，再删除学生档案和系统账号。
        SET v_done = 0;
        OPEN cur_tables;
        delete_loop: LOOP
            FETCH cur_tables INTO v_table_name, v_has_user_id, v_has_student_id;
            IF v_done = 1 THEN
                LEAVE delete_loop;
            END IF;

            IF v_has_user_id = 1 AND v_has_student_id = 1 THEN
                SET v_condition = '(`user_id` IN (SELECT user_id FROM tmp_gkzh_unregistered_users) OR `student_id` IN (SELECT student_id FROM tmp_gkzh_unregistered_students))';
            ELSEIF v_has_user_id = 1 THEN
                SET v_condition = '(`user_id` IN (SELECT user_id FROM tmp_gkzh_unregistered_users))';
            ELSE
                SET v_condition = '(`student_id` IN (SELECT student_id FROM tmp_gkzh_unregistered_students))';
            END IF;

            SET v_sql = CONCAT(
                'DELETE FROM `', REPLACE(v_table_name, '`', '``'),
                '` WHERE ', v_condition
            );
            SET @gkzh_purge_sql = v_sql;
            PREPARE stmt_delete FROM @gkzh_purge_sql;
            EXECUTE stmt_delete;
            DEALLOCATE PREPARE stmt_delete;
        END LOOP;
        CLOSE cur_tables;

        DELETE s
          FROM gkzh_student s
          JOIN tmp_gkzh_unregistered_students u ON u.student_id = s.student_id;

        DELETE su
          FROM sys_user su
          JOIN tmp_gkzh_unregistered_users u ON u.user_id = su.user_id;

        COMMIT;
        SET FOREIGN_KEY_CHECKS = 1;

        SELECT '清理完成' AS result,
               (SELECT COUNT(*) FROM tmp_gkzh_unregistered_students) AS deleted_students,
               (SELECT COUNT(*) FROM tmp_gkzh_unregistered_users) AS deleted_users;
    ELSE
        ROLLBACK;
        SET FOREIGN_KEY_CHECKS = 1;
        SELECT '仅预览，未删除任何数据' AS result;
    END IF;

    DROP TEMPORARY TABLE IF EXISTS tmp_gkzh_purge_preview;
    DROP TEMPORARY TABLE IF EXISTS tmp_gkzh_unregistered_users;
    DROP TEMPORARY TABLE IF EXISTS tmp_gkzh_unregistered_students;
END//
DELIMITER ;

-- 先预览：CALL gkzh_purge_unregistered_users(0);
-- 确认无误后删除：CALL gkzh_purge_unregistered_users(1);
