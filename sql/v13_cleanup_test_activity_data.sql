-- =============================================================
-- GKZH 测试活动数据清理脚本（MySQL 5.7+）
--
-- 用途：测试活动创建、扫码、游戏、抽奖及核销后，按“活动实例 ID”清理测试数据。
--
-- 安全设计：
--   1. 第一次调用为预览，不删除任何数据；
--   2. 仅传入 p_execute = 1 才实际删除；
--   3. 不删除学生、工作人员、学校、活动定义、游戏类型、游戏配置、霍兰德题库；
--   4. 仅删除该实例关联的抽奖活动；若该抽奖活动仍被其他实例使用，则保留。
--
-- 使用示例：
--   -- 预览影响范围（不会删除）
--   CALL gkzh_cleanup_test_activity(123, 0);
--
--   -- 确认删除活动实例 123 的测试数据
--   CALL gkzh_cleanup_test_activity(123, 1);
--
--   -- 使用完成后删除临时存储过程
--   DROP PROCEDURE IF EXISTS gkzh_cleanup_test_activity;
-- =============================================================

DROP PROCEDURE IF EXISTS gkzh_cleanup_test_activity;

DELIMITER $$

CREATE PROCEDURE gkzh_cleanup_test_activity(
    IN p_instance_id BIGINT,
    IN p_execute TINYINT
)
main: BEGIN
    DECLARE v_instance_count INT DEFAULT 0;
    DECLARE v_game_count INT DEFAULT 0;
    DECLARE v_lottery_count INT DEFAULT 0;

    -- 任一步骤异常时回滚，避免留下半清理的数据。
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        DROP TEMPORARY TABLE IF EXISTS tmp_cleanup_games;
        DROP TEMPORARY TABLE IF EXISTS tmp_cleanup_lotteries;
        RESIGNAL;
    END;

    IF p_instance_id IS NULL OR p_instance_id <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '请传入有效的测试活动实例 ID（instance_id）';
    END IF;

    SELECT COUNT(*) INTO v_instance_count
    FROM gkzh_activity_week_instance
    WHERE instance_id = p_instance_id;

    IF v_instance_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '未找到该活动实例，未执行任何删除';
    END IF;

    DROP TEMPORARY TABLE IF EXISTS tmp_cleanup_games;
    DROP TEMPORARY TABLE IF EXISTS tmp_cleanup_lotteries;

    CREATE TEMPORARY TABLE tmp_cleanup_games (
        game_id BIGINT PRIMARY KEY
    );

    INSERT INTO tmp_cleanup_games (game_id)
    SELECT game_id
    FROM gkzh_activity_game
    WHERE instance_id = p_instance_id;

    CREATE TEMPORARY TABLE tmp_cleanup_lotteries (
        lottery_id BIGINT PRIMARY KEY
    );

    INSERT IGNORE INTO tmp_cleanup_lotteries (lottery_id)
    SELECT lottery_id
    FROM gkzh_activity_week_school
    WHERE instance_id = p_instance_id
      AND lottery_id IS NOT NULL;

    SELECT COUNT(*) INTO v_game_count FROM tmp_cleanup_games;
    SELECT COUNT(*) INTO v_lottery_count FROM tmp_cleanup_lotteries;

    -- 预览输出：先确认影响范围，再用 p_execute = 1 执行删除。
    SELECT
        p_instance_id AS instance_id,
        (SELECT title FROM gkzh_activity_week_instance WHERE instance_id = p_instance_id) AS instance_title,
        v_game_count AS games_to_delete,
        (SELECT COUNT(*) FROM gkzh_game_participation WHERE instance_id = p_instance_id) AS game_participations_to_delete,
        (SELECT COUNT(*) FROM xycc_user_selection us JOIN tmp_cleanup_games g ON g.game_id = us.game_id) AS xycc_results_to_delete,
        (SELECT COUNT(*) FROM gkzh_activity_participation_record WHERE activity_id = p_instance_id) AS participation_records_to_delete,
        (SELECT COUNT(*) FROM gkzh_student_checkin WHERE activity_id = p_instance_id) AS checkin_records_to_delete,
        (SELECT COUNT(*) FROM lottery_record WHERE activity_id = p_instance_id) AS lottery_records_to_delete,
        (SELECT COUNT(*) FROM gkzh_lottery_record WHERE instance_id = p_instance_id) AS framework_lottery_records_to_delete,
        v_lottery_count AS linked_lotteries_to_check,
        p_execute AS execute_delete;

    IF IFNULL(p_execute, 0) <> 1 THEN
        SELECT '预览完成：以上仅为统计，未删除任何数据。确认后执行 CALL gkzh_cleanup_test_activity(活动实例ID, 1);' AS message;
        LEAVE main;
    END IF;

    START TRANSACTION;

    -- 1. 奖品核销流水及核销状态（依赖抽奖记录，必须先删）。
    DELETE log_item
    FROM gkzh_prize_redemption_log log_item
    INNER JOIN lottery_record record_item ON record_item.record_id = log_item.lottery_record_id
    WHERE record_item.activity_id = p_instance_id;

    DELETE redemption
    FROM gkzh_prize_redemption redemption
    INNER JOIN lottery_record record_item ON record_item.record_id = redemption.lottery_record_id
    WHERE record_item.activity_id = p_instance_id;

    -- 2. 用户产生的结果和流水。
    DELETE us
    FROM xycc_user_selection us
    INNER JOIN tmp_cleanup_games game_item ON game_item.game_id = us.game_id;

    DELETE FROM gkzh_game_participation WHERE instance_id = p_instance_id;
    DELETE FROM gkzh_activity_participation_record WHERE activity_id = p_instance_id;
    DELETE FROM gkzh_student_checkin WHERE activity_id = p_instance_id;
    DELETE FROM lottery_record WHERE activity_id = p_instance_id;
    DELETE FROM gkzh_lottery_record WHERE instance_id = p_instance_id;

    -- 3. 活动配置，从最下层游戏开始删除。
    DELETE FROM gkzh_lottery_rule WHERE instance_id = p_instance_id;
    DELETE FROM gkzh_activity_game WHERE instance_id = p_instance_id;
    DELETE FROM gkzh_activity_area WHERE instance_id = p_instance_id;
    DELETE FROM gkzh_activity_week_school WHERE instance_id = p_instance_id;
    DELETE FROM gkzh_activity_week_instance WHERE instance_id = p_instance_id;

    -- 4. 仅删除已不被其他活动实例引用、且没有剩余抽奖记录的测试抽奖活动及奖品。
    DELETE prize
    FROM lottery_prize prize
    INNER JOIN tmp_cleanup_lotteries lottery_item ON lottery_item.lottery_id = prize.lottery_id
    WHERE NOT EXISTS (
        SELECT 1 FROM gkzh_activity_week_school school_item
        WHERE school_item.lottery_id = prize.lottery_id
    )
      AND NOT EXISTS (
        SELECT 1 FROM lottery_record record_item
        WHERE record_item.lottery_id = prize.lottery_id
    );

    DELETE lottery_item
    FROM lottery lottery_item
    INNER JOIN tmp_cleanup_lotteries cleanup_item ON cleanup_item.lottery_id = lottery_item.lottery_id
    WHERE NOT EXISTS (
        SELECT 1 FROM gkzh_activity_week_school school_item
        WHERE school_item.lottery_id = lottery_item.lottery_id
    )
      AND NOT EXISTS (
        SELECT 1 FROM lottery_record record_item
        WHERE record_item.lottery_id = lottery_item.lottery_id
    );

    COMMIT;

    DROP TEMPORARY TABLE IF EXISTS tmp_cleanup_games;
    DROP TEMPORARY TABLE IF EXISTS tmp_cleanup_lotteries;

    SELECT CONCAT('已删除活动实例 ', p_instance_id, ' 的测试数据。学生、工作人员、活动定义、游戏配置及题库未删除。') AS message;
END$$

DELIMITER ;
