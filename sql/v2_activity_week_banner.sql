ALTER TABLE `gkzh_activity_week_instance`
ADD COLUMN `banner_url` varchar(500) DEFAULT NULL COMMENT '顶部 Banner 图 URL' AFTER `title`;
