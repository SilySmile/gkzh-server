ALTER TABLE `gkzh_student`
  ADD COLUMN `education` varchar(30) NULL DEFAULT NULL COMMENT '学历' AFTER `grade`,
  ADD COLUMN `graduation_time` varchar(7) NULL DEFAULT NULL COMMENT '毕业时间（YYYY-MM）' AFTER `education`;
