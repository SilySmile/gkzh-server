-- 抽奖记录绑定活动实例，避免同一抽奖配置在不同活动之间串记录。
ALTER TABLE `lottery_record`
  ADD COLUMN `activity_id` bigint(20) DEFAULT NULL COMMENT '活动实例ID' AFTER `lottery_id`,
  ADD KEY `idx_lottery_record_activity_user` (`activity_id`,`user_id`);

-- 已有活动周记录通过参与记录中的 module_id（中奖记录ID）回填活动实例。
UPDATE `lottery_record` r
JOIN `gkzh_activity_participation_record` p
  ON p.module_id = r.record_id AND p.participation_type = 3
SET r.activity_id = p.activity_id
WHERE r.activity_id IS NULL;
