-- 记录核销操作人的真实身份，工作人员使用 staff_id，Web 后台管理员使用 admin_user_id。
ALTER TABLE `gkzh_prize_redemption`
  ADD COLUMN `admin_user_id` bigint(20) DEFAULT NULL COMMENT '后台核销管理员用户ID' AFTER `staff_id`,
  ADD KEY `idx_redemption_admin_user` (`admin_user_id`);

ALTER TABLE `gkzh_prize_redemption_log`
  ADD COLUMN `admin_user_id` bigint(20) DEFAULT NULL COMMENT '后台操作管理员用户ID' AFTER `staff_id`,
  ADD KEY `idx_redemption_log_admin_user` (`admin_user_id`);
