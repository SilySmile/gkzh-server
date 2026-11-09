-- 职面未来（zmwl）基础结构与 8 个未来发展方向；可重复执行。
-- 规范：所有业务表与字段必须提供中文 COMMENT；本脚本只创建 gkzh_zmwl_* 表，
-- 不修改其他游戏数据。方向内容来自《职面未来.docx》，线上可在 Web“所需配置”继续维护。
-- 适用 MySQL 5.7（不使用 CHECK 约束、窗口函数和 JSON 函数索引）。

CREATE TABLE IF NOT EXISTS gkzh_zmwl_direction (
  direction_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '发展方向编号（自增主键）',
  direction_code VARCHAR(32) NOT NULL COMMENT '发展方向编码，全小写下划线，例如 employment，用于记录快照与统计',
  name VARCHAR(50) NOT NULL COMMENT '发展方向中文名，例如“就业工作”',
  short_intro VARCHAR(255) DEFAULT NULL COMMENT '卡片上的简短介绍，例如“走进职场，开启职业生涯”',
  tag_name VARCHAR(50) DEFAULT NULL COMMENT '方向标签称号，例如“职场启航者”，展示在个人选择卡上',
  interpretation TEXT COMMENT '方向解读长文案，展示在个人选择卡“方向解读”区域',
  message TEXT COMMENT '寄语长文案，展示在个人选择卡“寄语”区域',
  icon_url VARCHAR(255) DEFAULT NULL COMMENT '方向图标图片地址；为空时学生端使用内置兜底图标',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '学生端选项展示顺序，数值越小越靠前',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用；停用方向不出现在学生端，也不能被选择',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(direction_id),
  UNIQUE KEY uk_zmwl_direction_code(direction_code),
  KEY idx_zmwl_direction_sort(sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职面未来发展发展方向配置';

CREATE TABLE IF NOT EXISTS gkzh_zmwl_setting (
  setting_id TINYINT NOT NULL COMMENT '配置编号，固定为1（单行配置）',
  allow_reselect CHAR(1) NOT NULL DEFAULT '0' COMMENT '确认后是否允许重新选择：0不允许（只读），1允许覆盖',
  enabled CHAR(1) NOT NULL DEFAULT '1' COMMENT '是否启用游戏：0停用，1启用；停用时学生端拒绝进入',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(setting_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职面未来规则参数（单行配置）';

CREATE TABLE IF NOT EXISTS gkzh_zmwl_record (
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
  game_type VARCHAR(32) NOT NULL DEFAULT 'zmwl' COMMENT '游戏类型，固定为zmwl',
  status VARCHAR(16) NOT NULL DEFAULT 'participating' COMMENT '参与状态：participating进行中，finished已完成',
  direction_code VARCHAR(32) DEFAULT NULL COMMENT '所选发展方向编码快照；未确认时为空',
  direction_name VARCHAR(50) DEFAULT NULL COMMENT '所选发展方向中文名快照，后台改名称不影响历史记录',
  tag_name VARCHAR(50) DEFAULT NULL COMMENT '所选方向标签称号快照，例如“世界探索者”',
  interpretation TEXT COMMENT '方向解读快照，确认选择时写入，后台改文案不影响历史记录',
  message TEXT COMMENT '寄语快照，确认选择时写入，后台改文案不影响历史记录',
  icon_url VARCHAR(255) DEFAULT NULL COMMENT '所选方向图标地址快照',
  scan_time DATETIME DEFAULT NULL COMMENT '扫码进入游戏时间',
  start_time DATETIME DEFAULT NULL COMMENT '点击“开启探索”进入选择页时间',
  finish_time DATETIME DEFAULT NULL COMMENT '确认选择时间',
  duration_ms BIGINT DEFAULT NULL COMMENT '从进入选择页到确认选择的用时（毫秒）',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(record_id),
  UNIQUE KEY uk_zmwl_record_scope(school_id,instance_id,game_id,user_id),
  KEY idx_zmwl_record_instance(instance_id,school_id),
  KEY idx_zmwl_record_user(user_id),
  KEY idx_zmwl_record_direction(direction_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职面未来参与记录';

-- 规则参数只插入一次；后台保存后重复执行脚本不会覆盖。
INSERT IGNORE INTO gkzh_zmwl_setting(setting_id,allow_reselect,enabled,create_time,update_time)
VALUES(1,'0','1',NOW(),NOW());

-- 8 个未来发展方向；编码唯一，重复执行不会产生重复数据，也不会覆盖后台修改。
INSERT IGNORE INTO gkzh_zmwl_direction(direction_code,name,short_intro,tag_name,interpretation,message,sort_order,status,create_time,update_time) VALUES
('employment','就业工作','走进职场，开启职业生涯','职场启航者',
 '你希望毕业后尽快进入职场，将大学期间积累的知识和能力运用到实际工作中。你期待通过工作积累经验、提升能力，在实践中逐渐找到自己的职业方向。',
 '职场是一段全新的旅程，每一次尝试和积累，都可能成为未来成长的起点。',1,'0',NOW(),NOW()),
('domestic_study','国内升学','继续深造，提升学历与能力','学海探索者',
 '你希望继续留在校园，通过考研、读博等方式深入学习专业知识，提升自己的学历和专业能力。你期待在更深入的学习中拓展视野，为未来发展积累更多可能。',
 '学习的道路没有终点，每一次知识的积累，都是在为未来创造更多选择。坚持自己的目标，也别忘了享受成长的过程。',2,'0',NOW(),NOW()),
('overseas_study','出国留学','走向世界，探索更多可能','世界探索者',
 '你希望走出熟悉的环境，到不同的国家和地区学习、生活，接触多元文化与教育方式。你期待在新的环境中开阔眼界、提升能力，获得更加丰富的人生体验。',
 '世界很大，值得去看看。勇敢走出舒适圈，在不同的文化和经历中发现更多可能，也遇见更好的自己。',3,'0',NOW(),NOW()),
('public_exam','考公考编','参加公务员或事业单位考试','稳步前行者',
 '你希望通过公务员、事业单位等考试，进入相对稳定的职业发展环境。你比较重视工作的稳定性、职业保障和长期发展，希望通过自己的努力获得理想的工作机会。',
 '每一份理想都值得认真准备。明确目标、脚踏实地，在一次次积累中靠近自己期待的未来。',4,'0',NOW(),NOW()),
('entrepreneurship','自主创业','尝试自己的想法，开创事业','梦想创造者',
 '你希望不局限于传统的就业路径，而是尝试将自己的想法转化为实际行动，通过创业或自主经营开辟属于自己的发展空间。你期待拥有更多自主性，在实践中探索机会、创造价值。',
 '每一个想法都有可能成为新的起点。保持热情，也要做好充分准备，在不断尝试和调整中寻找属于自己的方向。',5,'0',NOW(),NOW()),
('self_employment','自主择业','根据个人意愿，选择适合自己的发展道路','自由规划者',
 '希望根据自己的兴趣、能力和个人意愿，自主选择适合自己的发展道路。无论是进入不同类型的企业、选择灵活就业，还是尝试多样化的职业形式，你都期待拥有更多自主选择的空间，按照自己的想法规划未来。',
 '未来的道路不止一种，适合自己的才值得认真探索。了解自身优势，关注外部机会，勇敢尝试不同可能，在一次次选择与实践中走出属于自己的道路。',6,'0',NOW(),NOW()),
('military','入伍参军','投身军营，在军旅生活中实现人生价值','热血追梦者',
 '你希望投身军营，在军旅生活中锻炼自我、磨炼意志，在纪律与挑战中提升个人能力，并通过实际行动实现人生价值。这条道路不仅意味着身份的转变，也意味着一段充满责任与成长的全新旅程。',
 '每一份坚定的选择，都值得认真对待。保持热爱与勇气，提前了解入伍政策和相关要求，做好充分准备，在新的旅程中书写属于自己的青春篇章。',7,'0',NOW(),NOW()),
('undecided','暂未确定','还在探索，希望再给自己一些时间','未来探索者',
 '你目前还没有明确具体的发展方向，可能正在不同选择之间思考，也可能希望通过更多体验和了解，再决定未来的道路。',
 '不必因为暂时没有答案而焦虑。允许自己慢慢探索，多了解、多体验，每一次尝试都能帮助你更接近真正想要的生活。',8,'0',NOW(),NOW());
