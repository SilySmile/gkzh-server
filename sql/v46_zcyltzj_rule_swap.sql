-- 骰子局九宫格规则格位置调整：规则一由下中（第 8 格）移到下左（第 7 格），
-- 与骰子点数 6 交换位置；规则二保持下右（第 9 格）。可重复执行。
SET @has_left := (SELECT COUNT(*) FROM gkzh_zcyltzj_rule_cell WHERE cell_no = 7);
UPDATE gkzh_zcyltzj_rule_cell
SET cell_no = 7, update_time = NOW()
WHERE cell_no = 8 AND @has_left = 0;

-- 同步字段注释为新的位置说明，避免数据库注释仍写着“8下中”。
ALTER TABLE gkzh_zcyltzj_rule_cell
  MODIFY COLUMN cell_no TINYINT NOT NULL COMMENT '九宫格位置：7下左，9下右';
