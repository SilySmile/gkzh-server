-- 简历称重（jlcz）基础结构、20 个砝码与规则参数；可重复执行。
-- 规范：所有业务表与字段必须提供中文 COMMENT；本脚本只创建 gkzh_jlcz_* 表，
-- 不修改其他游戏数据。砝码内容来自《简历称重.docx》，线上可在 Web 继续维护。
-- 适用 MySQL 5.7（不使用 CHECK 约束、窗口函数和 JSON 函数索引）。

CREATE TABLE IF NOT EXISTS gkzh_jlcz_weight (
  weight_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '砝码编号（自增主键）',
  weight_no TINYINT NOT NULL COMMENT '挂钩号，取值 1-20，与线下简历选装卡编号一致',
  category_code VARCHAR(16) NOT NULL COMMENT '砝码大类编码：core硬通货，soft软实力，basic基本功，bonus加分项',
  category_name VARCHAR(50) NOT NULL COMMENT '砝码大类中文名，例如“硬通货（核心竞争力）”',
  label VARCHAR(50) NOT NULL COMMENT '砝码标签，例如“靶心岗位”',
  icon VARCHAR(32) DEFAULT NULL COMMENT '砝码图标字符（emoji），例如“🎯”',
  image_url VARCHAR(255) DEFAULT NULL COMMENT '砝码图片地址；为空时学生端使用 icon 字符展示',
  weight_g INT NOT NULL COMMENT '砝码重量（克），当前取 10 或 5',
  condition_text VARCHAR(500) DEFAULT NULL COMMENT '学生自查标准说明，用于卡片展开和结果建议',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '同大类内显示顺序，数值越小越靠前',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用；停用砝码不出现在学生端且不计分',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(weight_id),
  UNIQUE KEY uk_jlcz_weight_no(weight_no),
  KEY idx_jlcz_weight_category(category_code,sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历称重砝码配置';

CREATE TABLE IF NOT EXISTS gkzh_jlcz_setting (
  setting_id TINYINT NOT NULL COMMENT '配置编号，固定为1（单行配置）',
  left_weight_g INT NOT NULL DEFAULT 80 COMMENT '天平左侧标准砝码重量（克），线下固定为 80g',
  pass_weight_g INT NOT NULL DEFAULT 80 COMMENT '通关总重量阈值（克），总重达到该值即通关',
  time_limit_seconds INT NOT NULL DEFAULT 0 COMMENT '称重限时（秒），0 表示不限时；超过后提交按超时处理',
  allow_retry CHAR(1) NOT NULL DEFAULT '1' COMMENT '未通关是否允许重新称重：0禁止，1允许',
  enabled CHAR(1) NOT NULL DEFAULT '1' COMMENT '是否启用游戏：0停用，1启用；停用时学生端拒绝进入',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(setting_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历称重规则参数（单行配置）';

CREATE TABLE IF NOT EXISTS gkzh_jlcz_record (
  record_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '参与记录编号',
  school_id BIGINT NOT NULL COMMENT '学校编号',
  instance_id BIGINT NOT NULL COMMENT '活动周实例编号',
  game_id BIGINT NOT NULL COMMENT '活动游戏编号',
  user_id BIGINT NOT NULL COMMENT '平台用户编号',
  student_id BIGINT DEFAULT NULL COMMENT '学生编号',
  student_no VARCHAR(50) DEFAULT NULL COMMENT '学号快照',
  student_name VARCHAR(100) DEFAULT NULL COMMENT '学生姓名快照',
  department_id BIGINT DEFAULT NULL COMMENT '专业（二级部门）编号，其父部门才是院系',
  major VARCHAR(100) DEFAULT NULL COMMENT '专业名称快照',
  gender VARCHAR(16) DEFAULT NULL COMMENT '性别快照：男、女，其他值表示未知',
  game_type VARCHAR(32) NOT NULL DEFAULT 'jlcz' COMMENT '游戏类型，固定为jlcz',
  status VARCHAR(16) NOT NULL DEFAULT 'participating' COMMENT '参与状态：participating进行中，finished已完成',
  result VARCHAR(16) DEFAULT NULL COMMENT '称重结果：pass通关，fail未通关；未提交为空',
  total_weight INT NOT NULL DEFAULT 0 COMMENT '本次勾选砝码总重量（克）',
  selected_count INT NOT NULL DEFAULT 0 COMMENT '本次勾选砝码数量',
  category_json TEXT COMMENT '四大类小计重量快照 JSON，例如 {"core":30,"soft":10}',
  selected_json TEXT COMMENT '本次勾选砝码快照 JSON，保存砝码编号、名称与重量，后台改配置不影响历史',
  attempt_no INT NOT NULL DEFAULT 1 COMMENT '第几次称重；未通关重新称重时累加',
  start_time DATETIME DEFAULT NULL COMMENT '开始称重时间',
  finish_time DATETIME DEFAULT NULL COMMENT '提交称重时间',
  duration_ms BIGINT DEFAULT NULL COMMENT '本次称重用时（毫秒），从开始称重到提交',
  scan_time DATETIME DEFAULT NULL COMMENT '扫码进入游戏时间',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(record_id),
  UNIQUE KEY uk_jlcz_record_scope(school_id,instance_id,game_id,user_id),
  KEY idx_jlcz_record_instance(instance_id,school_id),
  KEY idx_jlcz_record_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历称重参与记录';

CREATE TABLE IF NOT EXISTS gkzh_jlcz_choice (
  choice_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '选择明细编号',
  record_id BIGINT NOT NULL COMMENT '参与记录编号',
  attempt_no INT NOT NULL DEFAULT 1 COMMENT '第几次称重，与参与记录 attempt_no 对应',
  weight_id BIGINT NOT NULL COMMENT '砝码编号（提交时的配置编号）',
  weight_no TINYINT NOT NULL COMMENT '挂钩号快照，取值 1-20',
  weight_label VARCHAR(50) NOT NULL COMMENT '砝码标签快照',
  weight_g INT NOT NULL COMMENT '砝码重量快照（克）',
  category_code VARCHAR(16) NOT NULL COMMENT '砝码大类编码快照',
  create_time DATETIME DEFAULT NULL COMMENT '选择时间',
  PRIMARY KEY(choice_id),
  KEY idx_jlcz_choice_record(record_id,attempt_no),
  KEY idx_jlcz_choice_weight(weight_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历称重砝码选择明细';

-- 规则参数只插入一次；后台保存后重复执行脚本不会覆盖。
INSERT IGNORE INTO gkzh_jlcz_setting(setting_id,left_weight_g,pass_weight_g,time_limit_seconds,allow_retry,enabled,create_time,update_time)
VALUES(1,80,80,0,'1','1',NOW(),NOW());

-- 20 个砝码；挂钩号唯一，重复执行不会产生重复数据，也不会覆盖后台修改。
INSERT IGNORE INTO gkzh_jlcz_weight(weight_no,category_code,category_name,label,icon,weight_g,condition_text,sort_order,status,create_time,update_time) VALUES
(1,'core','硬通货（核心竞争力）','靶心岗位','🎯',10,'简历上明确写出求职岗位，且专业或经历与之相关。',1,'0',NOW(),NOW()),
(2,'core','硬通货（核心竞争力）','绩点王者','📊',10,'成绩排名前30%，或曾获校级及以上奖学金。',2,'0',NOW(),NOW()),
(3,'core','硬通货（核心竞争力）','大厂履历','💼',10,'拥有行业内知名企业/机构的实习经历。',3,'0',NOW(),NOW()),
(4,'core','硬通货（核心竞争力）','证书护体','📜',10,'持有与岗位直接相关的权威证书（如CPA、法考、四六级高分等高含金量证书）。',4,'0',NOW(),NOW()),
(5,'core','硬通货（核心竞争力）','数据牛人','📈',10,'实习/项目经历中包含具体量化成果（数字、百分比）。',5,'0',NOW(),NOW()),
(6,'soft','软实力（发展潜力）','创新先锋','💡',5,'经历中有“从0到1”“优化流程”“提出新方案”等描述。',6,'0',NOW(),NOW()),
(7,'soft','软实力（发展潜力）','团队担当','👑',5,'在项目或社团中担任过负责人、队长、组长等角色。',7,'0',NOW(),NOW()),
(8,'soft','软实力（发展潜力）','技能满点','🔧',5,'罗列的软件、工具技能与岗位要求高度匹配。',8,'0',NOW(),NOW()),
(9,'soft','软实力（发展潜力）','高光时刻','🏆',5,'获得过校级以上竞赛奖项、荣誉或发表过文章。',9,'0',NOW(),NOW()),
(10,'soft','软实力（发展潜力）','表达力强','🗣️',5,'经历描述使用了动宾结构（如“负责策划…活动”），清晰流畅。',10,'0',NOW(),NOW()),
(11,'basic','基本功（职业素养）','格式规范','📝',5,'排版清晰整洁，没有错别字，字体统一。',11,'0',NOW(),NOW()),
(12,'basic','基本功（职业素养）','针对性强','🎯',5,'自我评价非套话，能结合岗位要求具体描述自身优势。',12,'0',NOW(),NOW()),
(13,'basic','基本功（职业素养）','形象得体','🖼️',5,'附有职业照/正式证件照，形象阳光、专业。',13,'0',NOW(),NOW()),
(14,'basic','基本功（职业素养）','一页纸','📄',5,'简历篇幅完美控制在一页A4纸以内。',14,'0',NOW(),NOW()),
(15,'basic','基本功（职业素养）','信息齐全','📞',5,'姓名、电话、邮箱等联系方式完整且准确。',15,'0',NOW(),NOW()),
(16,'bonus','加分项（额外亮点）','作品链接','🔗',5,'附有作品集链接、个人博客或GitHub主页。',16,'0',NOW(),NOW()),
(17,'bonus','加分项（额外亮点）','语言优势','🌍',5,'除英语外，掌握第二外语或英语口语流利（需有证明）。',17,'0',NOW(),NOW()),
(18,'bonus','加分项（额外亮点）','持续学习','📚',5,'简历中体现了额外学习/培训经历（如网课证书、读书笔记）。',18,'0',NOW(),NOW()),
(19,'bonus','加分项（额外亮点）','行业热情','❤️',5,'有个人行业分析、竞品报告等能体现热情的材料。',19,'0',NOW(),NOW()),
(20,'bonus','加分项（额外亮点）','推荐背书','🤝',5,'有前实习单位上级的推荐信或老师的高度评价（文字体现）。',20,'0',NOW(),NOW());
