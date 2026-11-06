-- zycck 测试数据清理（仅清理职业猜猜看产生的用户记录）
-- 不删除职业大类、职业/题目配置、游戏配置、学校和活动实例。
-- 使用前请先执行预览查询；确认无误后执行 COMMIT，发现范围不对执行 ROLLBACK。

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_zycck_test_games;
CREATE TEMPORARY TABLE tmp_zycck_test_games (
  game_id BIGINT PRIMARY KEY
);

-- 同时覆盖已有 zycck 业务记录和活动配置中登记为 zycck 的游戏。
INSERT IGNORE INTO tmp_zycck_test_games(game_id)
SELECT game_id FROM gkzh_zycck_record WHERE game_type = 'zycck' AND game_id IS NOT NULL;
INSERT IGNORE INTO tmp_zycck_test_games(game_id)
SELECT game_id FROM gkzh_activity_game WHERE game_type = 'zycck';

-- 预览影响数量
SELECT
  (SELECT COUNT(*) FROM gkzh_zycck_record WHERE game_type = 'zycck') AS zycck_records,
  (SELECT COUNT(*) FROM gkzh_game_participation p JOIN tmp_zycck_test_games g ON g.game_id = p.game_id) AS game_participations,
  (SELECT COUNT(*) FROM gkzh_zycck_record WHERE game_type = 'zycck' AND status = 'finished') AS finished_records;

-- 删除顺序：活动参与记录 -> zycck 业务记录，避免保留孤立统计数据。
DELETE p
FROM gkzh_game_participation p
JOIN tmp_zycck_test_games g ON g.game_id = p.game_id;

DELETE FROM gkzh_zycck_record WHERE game_type = 'zycck';

DROP TEMPORARY TABLE IF EXISTS tmp_zycck_test_games;

-- 确认删除请执行 COMMIT；仅查看范围请改为 ROLLBACK。
-- COMMIT;
ROLLBACK;
