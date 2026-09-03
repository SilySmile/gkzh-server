-- 将工作人员从后台 sys_user 中彻底分离，工作人员仅用于用户端登录
ALTER TABLE `gkzh_school_staff`
  DROP INDEX `uk_staff_user`,
  MODIFY COLUMN `user_id` bigint(20) NULL COMMENT '历史字段，不再使用',
  ADD COLUMN `user_name` varchar(64) NULL COMMENT '工作人员独立登录账号' AFTER `staff_id`,
  ADD COLUMN `password` varchar(255) NULL COMMENT '工作人员独立登录密码' AFTER `user_name`,
  ADD UNIQUE KEY `uk_staff_user_name` (`user_name`);

UPDATE `gkzh_school_staff` SET `user_name` = CONCAT('staff_', `staff_id`) WHERE `user_name` IS NULL;
-- 旧账号密码为空时请由后台重置密码后再登录；保留可空以兼容历史数据。
