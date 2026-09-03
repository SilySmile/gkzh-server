ALTER TABLE `lottery_record`
ADD COLUMN `biz_type` varchar(32) DEFAULT 'career_week' COMMENT '活动类型：career_week/job_week' AFTER `lottery_id`;

UPDATE `lottery_record` SET `biz_type` = 'career_week';
