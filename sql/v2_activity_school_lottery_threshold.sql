ALTER TABLE `gkzh_activity_week_school`
ADD COLUMN `min_finish_count` int(11) DEFAULT 0 COMMENT '抽奖最低完成游戏数' AFTER `school_id`,
ADD COLUMN `lottery_id` bigint(20) DEFAULT NULL COMMENT '绑定抽奖活动ID' AFTER `min_finish_count`;
ADD COLUMN `max_draw_count` int(11) DEFAULT 1 COMMENT '每个用户最多抽奖次数' AFTER `lottery_id`;
