-- 为每条中奖记录生成独立、不可猜测的文本核销码
ALTER TABLE lottery_record ADD COLUMN redemption_code varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '随机文本核销码';
UPDATE lottery_record
SET redemption_code = CONCAT('GKZH-', UPPER(SUBSTRING(REPLACE(UUID(), '-', ''), 1, 10)))
WHERE redemption_code IS NULL OR redemption_code = '' OR redemption_code = 'PENDING';
ALTER TABLE lottery_record ADD UNIQUE KEY uk_lottery_record_redemption_code (redemption_code);
