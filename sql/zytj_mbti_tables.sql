-- ----------------------------
-- 职愿探究-MBTI商品表
-- ----------------------------
DROP TABLE IF EXISTS `gkzh_mbti_product`;
CREATE TABLE `gkzh_mbti_product` (
  `product_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `product_image` VARCHAR(500) DEFAULT NULL COMMENT '商品图片URL（完整路径）',
  `mbti_dimension` CHAR(1) NOT NULL COMMENT 'MBTI维度：E/I/S/N/T/F/J/P',
  `column_index` INT(11) NOT NULL COMMENT '列序号：1=E/I, 2=S/N, 3=T/F, 4=J/P',
  `sort_order` INT(11) DEFAULT 0 COMMENT '同列内排序（数字越小越靠前）',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态：0=正常 1=停用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`product_id`),
  KEY `idx_column_sort` (`column_index`, `sort_order`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职愿探究-MBTI商品表';
-- ----------------------------
-- 职愿探究-学生选择记录表
-- ----------------------------
DROP TABLE IF EXISTS `gkzh_mbti_student_choice`;
CREATE TABLE `gkzh_mbti_student_choice` (
  `choice_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '选择ID',
  `student_id` BIGINT(20) NOT NULL COMMENT '学生ID',
  `student_name` VARCHAR(100) DEFAULT NULL COMMENT '学生姓名（冗余字段，方便查询）',
  `student_no` VARCHAR(50) DEFAULT NULL COMMENT '学号（冗余字段，方便查询）',
  `activity_id` BIGINT(20) NOT NULL COMMENT '活动ID',
  `choice_code` CHAR(4) NOT NULL COMMENT '生成的MBTI代码（如ESTJ）',
  `product_ids` VARCHAR(100) DEFAULT NULL COMMENT '选择的商品ID（逗号分隔，如1,5,8,12）',
  `choice_time` INT(11) DEFAULT NULL COMMENT '用时（秒）',
  `is_redeemed` CHAR(1) DEFAULT '0' COMMENT '是否已兑换盲盒：0=否 1=是',
  `redeem_time` DATETIME DEFAULT NULL COMMENT '兑换时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`choice_id`),
  KEY `idx_student_activity` (`student_id`, `activity_id`),
  KEY `idx_choice_code` (`choice_code`),
  KEY `idx_redeemed` (`is_redeemed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职愿探究-学生选择记录表';
