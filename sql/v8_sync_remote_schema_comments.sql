-- 线上库 gkzh_528 结构同步：仅补齐本地已有字段的注释，不影响表数据。
ALTER TABLE `gkzh_prize_redemption`
  MODIFY `redemption_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '核销ID',
  MODIFY `lottery_record_id` bigint(20) NOT NULL COMMENT '中奖记录ID',
  MODIFY `school_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '学校ID',
  MODIFY `student_id` bigint(20) DEFAULT NULL COMMENT '学生ID',
  MODIFY `staff_id` bigint(20) DEFAULT NULL COMMENT '核销工作人员ID',
  MODIFY `status` char(1) NOT NULL DEFAULT '0' COMMENT '0待核销 1已核销 2已撤销 3已过期',
  MODIFY `redeem_time` datetime DEFAULT NULL COMMENT '核销时间',
  MODIFY `remark` varchar(500) DEFAULT NULL COMMENT '核销备注';

ALTER TABLE `gkzh_prize_redemption_log`
  MODIFY `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  MODIFY `redemption_id` bigint(20) DEFAULT NULL COMMENT '核销ID',
  MODIFY `lottery_record_id` bigint(20) NOT NULL COMMENT '中奖记录ID',
  MODIFY `school_id` bigint(20) DEFAULT NULL COMMENT '学校ID',
  MODIFY `staff_id` bigint(20) DEFAULT NULL COMMENT '操作工作人员ID',
  MODIFY `action` varchar(32) NOT NULL DEFAULT 'SCAN' COMMENT 'SCAN/REDEEM/REPEAT/REVOKE';

ALTER TABLE `gkzh_school_staff`
  MODIFY `staff_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作人员ID',
  MODIFY `user_name` varchar(64) DEFAULT NULL COMMENT '工作人员独立登录账号',
  MODIFY `password` varchar(255) DEFAULT NULL COMMENT '工作人员独立登录密码',
  MODIFY `user_id` bigint(20) DEFAULT NULL COMMENT '历史字段，不再使用',
  MODIFY `school_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '学校ID',
  MODIFY `staff_name` varchar(64) NOT NULL DEFAULT '' COMMENT '工作人员姓名',
  MODIFY `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  MODIFY `can_redeem` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否允许奖品核销：0否 1是';
