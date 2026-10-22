-- 抽奖奖品与中奖记录增加“是否需核销”字段（可重复执行）。
-- 奖品默认需核销；中奖记录保存抽奖时的快照，避免后续修改奖品影响历史记录。
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'lottery_prize' AND column_name = 'need_redemption') = 0,
  'ALTER TABLE lottery_prize ADD COLUMN need_redemption CHAR(1) NOT NULL DEFAULT ''1'' COMMENT ''是否需核销：1-需核销，0-无需核销'' AFTER prize_type',
  'SELECT 1');
PREPARE lottery_need_stmt FROM @sql;
EXECUTE lottery_need_stmt;
DEALLOCATE PREPARE lottery_need_stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'lottery_record' AND column_name = 'need_redemption') = 0,
  'ALTER TABLE lottery_record ADD COLUMN need_redemption CHAR(1) NOT NULL DEFAULT ''1'' COMMENT ''中奖时奖品是否需核销：1-需核销，0-无需核销'' AFTER prize_title',
  'SELECT 1');
PREPARE lottery_need_stmt FROM @sql;
EXECUTE lottery_need_stmt;
DEALLOCATE PREPARE lottery_need_stmt;

-- 谢谢参与不进入核销流程；其余奖品历史记录默认需核销。
UPDATE lottery_prize SET need_redemption = '0' WHERE prize_type = 3;
UPDATE lottery_record r
JOIN lottery_prize p ON p.prize_id = r.prize_id
SET r.need_redemption = '0'
WHERE p.prize_type = 3;

-- 历史空值兜底为需核销，保证旧记录仍可按原核销流程处理。
UPDATE lottery_record SET need_redemption = '1' WHERE need_redemption IS NULL OR need_redemption = '';
