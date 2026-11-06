-- 职业猜猜看活动反馈：MySQL 5.7+，可重复执行。
-- 上线顺序：先在目标数据库执行本脚本，再部署服务端、Web 和小程序。
-- 清单历史从本次升级开始完整记录；旧版已加入后又删空的情况无法还原。
SET @zycck_feedback_sql := IF((SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema=DATABASE() AND table_name='gkzh_zycck_record' AND column_name='exploration_added')=0,
  'ALTER TABLE gkzh_zycck_record ADD COLUMN exploration_added CHAR(1) NOT NULL DEFAULT ''0'' COMMENT ''是否曾加入探索清单：0未加入，1曾加入；删除后保留'' AFTER exploration_career_ids', 'SELECT 1');
PREPARE zycck_feedback_stmt FROM @zycck_feedback_sql;
EXECUTE zycck_feedback_stmt;
DEALLOCATE PREPARE zycck_feedback_stmt;

-- 仅回填当前非空清单，不覆盖新版本已经保存的“曾加入”标记。
UPDATE gkzh_zycck_record SET exploration_added='1'
WHERE exploration_added='0' AND
  CASE WHEN JSON_VALID(exploration_career_ids) THEN JSON_LENGTH(exploration_career_ids) ELSE 0 END > 0;

CREATE TABLE IF NOT EXISTS gkzh_zycck_activity_feedback (
  record_id BIGINT NOT NULL COMMENT '参与记录主键，关联学校、活动实例、游戏和学生，每份记录一份反馈',
  questionnaire_version VARCHAR(20) NOT NULL DEFAULT '1' COMMENT '问卷版本，用于区分后续版本的题目与选项',
  question1_answers JSON NOT NULL COMMENT '第1题新认识的稳定选项编码数组，1至3项，NONE与其余选项互斥',
  question1_other VARCHAR(500) NOT NULL DEFAULT '' COMMENT '第1题其他补充文字，选择OTHER时必填',
  show_question2 CHAR(1) NOT NULL DEFAULT '0' COMMENT '第2题是否适用：0未曾加入清单，1曾加入清单',
  question2_answer VARCHAR(40) NOT NULL DEFAULT '' COMMENT '第2题加入清单主要原因的单选编码，不适用时为空',
  question2_other VARCHAR(500) NOT NULL DEFAULT '' COMMENT '第2题其他补充文字，选择OTHER时必填',
  question3_answers JSON NOT NULL COMMENT '第3题后续活动需求的稳定选项编码数组，1至3项，NONE与其余选项互斥',
  question3_other VARCHAR(500) NOT NULL DEFAULT '' COMMENT '第3题其他补充文字，选择OTHER时必填',
  submitted_time DATETIME NOT NULL COMMENT '最近一次成功提交反馈的时间',
  PRIMARY KEY (record_id),
  KEY idx_zycck_feedback_submitted (submitted_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职业猜猜看活动反馈，按参与记录关联活动供后续总结报告分析';
