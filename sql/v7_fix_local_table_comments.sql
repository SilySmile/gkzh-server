-- 修复工作人员/奖品核销表历史乱码注释，仅修改元数据，不修改业务数据。
ALTER TABLE gkzh_prize_redemption COMMENT='用户奖品核销状态',
  MODIFY COLUMN redemption_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '核销ID',
  MODIFY COLUMN lottery_record_id bigint(20) NOT NULL COMMENT '中奖记录ID',
  MODIFY COLUMN school_id bigint(20) NOT NULL DEFAULT 0 COMMENT '学校ID',
  MODIFY COLUMN student_id bigint(20) DEFAULT NULL COMMENT '学生ID',
  MODIFY COLUMN staff_id bigint(20) DEFAULT NULL COMMENT '核销工作人员ID',
  MODIFY COLUMN status char(1) NOT NULL DEFAULT '0' COMMENT '0待核销 1已核销 2已撤销 3已过期',
  MODIFY COLUMN redeem_time datetime DEFAULT NULL COMMENT '核销时间',
  MODIFY COLUMN remark varchar(500) DEFAULT NULL COMMENT '核销备注';

ALTER TABLE gkzh_prize_redemption_log COMMENT='奖品核销审计流水',
  MODIFY COLUMN log_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  MODIFY COLUMN redemption_id bigint(20) DEFAULT NULL COMMENT '核销ID',
  MODIFY COLUMN lottery_record_id bigint(20) NOT NULL COMMENT '中奖记录ID',
  MODIFY COLUMN school_id bigint(20) DEFAULT NULL COMMENT '学校ID',
  MODIFY COLUMN staff_id bigint(20) DEFAULT NULL COMMENT '操作工作人员ID';

ALTER TABLE gkzh_school_staff COMMENT='学校工作人员账号',
  MODIFY COLUMN staff_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作人员ID',
  MODIFY COLUMN status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用';
