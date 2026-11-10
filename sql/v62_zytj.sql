-- 职愿探究（zytj）基础结构、8 个橱窗物品与 16 型人格对照；可重复执行。
-- 规范：所有业务表与字段必须提供中文 COMMENT；本脚本只创建 gkzh_zytj_* 表，
-- 不修改其他游戏数据，也不删除历史遗留表 gkzh_mbti_product / gkzh_mbti_student_choice。
-- 内容来自《职愿探究-MBTI.docx》与《线上开发规则——职愿探究.xlsx》，
-- 线上可在 Web“职愿探究 → 所需配置”继续维护文案与图片。
-- 适用 MySQL 5.7（不使用 CHECK 约束、窗口函数和 JSON 函数索引）。

CREATE TABLE IF NOT EXISTS gkzh_zytj_item (
  item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '物品编号（自增主键）',
  item_code CHAR(1) NOT NULL COMMENT '物品对应的性格维度代码：E/I/S/N/T/F/J/P，四列各两个',
  item_name VARCHAR(50) NOT NULL COMMENT '物品中文名，例如“派对气球”',
  dimension_code VARCHAR(4) NOT NULL COMMENT '所属维度：EI外向内向、SN实感直觉、TF思考情感、JP判断感知',
  column_index TINYINT NOT NULL COMMENT '所在列序号1-4，决定该物品归属的性格维度：1=EI、2=SN、3=TF、4=JP',
  row_index TINYINT NOT NULL DEFAULT 1 COMMENT '所在行序号1-2，学生端2行4列橱窗按此排布',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '同列内展示顺序，数值越小越靠前',
  image_url VARCHAR(255) DEFAULT NULL COMMENT '物品图片地址；为空时学生端显示名称占位',
  metaphor VARCHAR(500) DEFAULT NULL COMMENT '隐喻说明，解释该物品为什么代表这个性格维度',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用；停用物品不出现在学生端，也不能被选择',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(item_id),
  UNIQUE KEY uk_zytj_item_code(item_code),
  KEY idx_zytj_item_column(column_index,row_index,sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职愿探究橱窗物品（每列一个性格维度）';

CREATE TABLE IF NOT EXISTS gkzh_zytj_type (
  type_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '人格类型编号（自增主键）',
  type_code CHAR(4) NOT NULL COMMENT '四位人格类型代码，例如ENFP；由四列所选物品的维度代码按列序拼成',
  feature VARCHAR(500) DEFAULT NULL COMMENT '性格特点，展示在职业探索卡“性格特点”区域',
  career_field VARCHAR(500) DEFAULT NULL COMMENT '适合职业领域，展示在职业探索卡“适合职业领域”区域',
  typical_careers VARCHAR(500) DEFAULT NULL COMMENT '典型职业，展示在职业探索卡“典型职业”区域',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '后台列表展示顺序，数值越小越靠前',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用；当前不拦截结果展示，仅用于后台维护',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(type_id),
  UNIQUE KEY uk_zytj_type_code(type_code),
  KEY idx_zytj_type_sort(sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职愿探究16型人格对照表';

CREATE TABLE IF NOT EXISTS gkzh_zytj_setting (
  setting_id TINYINT NOT NULL COMMENT '配置编号，固定为1（单行配置）',
  time_limit INT NOT NULL DEFAULT 30 COMMENT '单局限时（秒），从进入选择页开始计时；0表示不限时',
  allow_reselect CHAR(1) NOT NULL DEFAULT '0' COMMENT '提交后是否允许重新探究：0不允许（只读），1允许覆盖并按最新一次统计',
  enabled CHAR(1) NOT NULL DEFAULT '1' COMMENT '是否启用游戏：0停用，1启用；停用时学生端拒绝进入',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(setting_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职愿探究规则参数（单行配置）';

CREATE TABLE IF NOT EXISTS gkzh_zytj_record (
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
  game_type VARCHAR(32) NOT NULL DEFAULT 'zytj' COMMENT '游戏类型，固定为zytj',
  status VARCHAR(16) NOT NULL DEFAULT 'participating' COMMENT '参与状态：participating进行中，finished已提交',
  type_code CHAR(4) DEFAULT NULL COMMENT '人格类型代码快照，例如ENFP；未提交时为空',
  feature VARCHAR(500) DEFAULT NULL COMMENT '性格特点快照，提交时写入，后台改文案不影响历史记录',
  career_field VARCHAR(500) DEFAULT NULL COMMENT '适合职业领域快照，提交时写入',
  typical_careers VARCHAR(500) DEFAULT NULL COMMENT '典型职业快照，提交时写入',
  item_codes VARCHAR(32) DEFAULT NULL COMMENT '按列序选择物品的维度代码快照，例如“E,N,T,P”',
  item_ids VARCHAR(64) DEFAULT NULL COMMENT '按列序选择物品编号快照，例如“11,13,14,17”',
  time_limit INT DEFAULT NULL COMMENT '本局限时快照（秒），0表示不限时',
  is_timeout CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否超时提交：0否，1是；超时由服务端补全未选列后提交',
  attempt_no INT NOT NULL DEFAULT 1 COMMENT '探究次数，允许重新探究时每次覆盖并自增',
  scan_time DATETIME DEFAULT NULL COMMENT '扫码进入游戏时间',
  start_time DATETIME DEFAULT NULL COMMENT '进入选择页并开始计时的时间',
  finish_time DATETIME DEFAULT NULL COMMENT '确认选择时间',
  duration_ms BIGINT DEFAULT NULL COMMENT '从进入选择页到确认选择的用时（毫秒）',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(record_id),
  UNIQUE KEY uk_zytj_record_scope(school_id,instance_id,game_id,user_id),
  KEY idx_zytj_record_instance(instance_id,school_id),
  KEY idx_zytj_record_user(user_id),
  KEY idx_zytj_record_type(type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职愿探究参与记录';

-- 规则参数只插入一次；后台保存后重复执行脚本不会覆盖。
-- 需求口径：30 秒内每列选 1 件；提交后默认只读，可在后台开启“允许重新探究”。
INSERT IGNORE INTO gkzh_zytj_setting(setting_id,time_limit,allow_reselect,enabled,create_time,update_time)
VALUES(1,30,'0','1',NOW(),NOW());

-- 8 个橱窗物品：2 行 4 列，每列一个维度。图片为设计稿拆图后按内容哈希发布的版本目录。
-- 编码唯一，重复执行不会产生重复数据，也不会覆盖后台修改的名称、图片与状态。
INSERT IGNORE INTO gkzh_zytj_item
  (item_code,item_name,dimension_code,column_index,row_index,sort_order,image_url,metaphor,status,create_time,update_time)
VALUES
  ('E','派对气球','EI',1,1,0,'https://zhiye.sxgkzh.cn/imgs/zytj/5331b0a2e482/p2-item-party-balloon.png',
   '代表喜欢热闹、社交场合，倾向于从与人互动中获取能量，符合外向型特征','0',NOW(),NOW()),
  ('I','单人帐篷','EI',1,2,1,'https://zhiye.sxgkzh.cn/imgs/zytj/5331b0a2e482/p2-item-tent.png',
   '暗示享受独自的空间，更愿意在安静环境中恢复精力，是内向型的象征','0',NOW(),NOW()),
  ('N','水晶球','SN',2,1,0,'https://zhiye.sxgkzh.cn/imgs/zytj/5331b0a2e482/p2-item-crystal-ball.png',
   '常与预测未来、灵感等抽象概念联系，代表关注可能性、未来趋势，符合直觉型特点','0',NOW(),NOW()),
  ('S','地质锤','SN',2,2,1,'https://zhiye.sxgkzh.cn/imgs/zytj/5331b0a2e482/p2-item-geologist-hammer.png',
   '用于实地勘探、收集具体信息，体现注重实际、细节和经验，是实感型的代表','0',NOW(),NOW()),
  ('T','逻辑拼图','TF',3,1,0,'https://zhiye.sxgkzh.cn/imgs/zytj/5331b0a2e482/p2-item-logic-puzzle.png',
   '需要运用逻辑分析、推理来完成，体现理性思考、客观判断的特质，对应思考型','0',NOW(),NOW()),
  ('F','手工编织围巾','TF',3,2,1,'https://zhiye.sxgkzh.cn/imgs/zytj/5331b0a2e482/p2-item-knit-scarf.png',
   '制作过程饱含情感投入，注重他人感受，象征着情感型的价值取向','0',NOW(),NOW()),
  ('P','沙滩躺椅','JP',4,1,0,'https://zhiye.sxgkzh.cn/imgs/zytj/5331b0a2e482/p2-item-beach-chair.png',
   '传达出放松、随性的生活态度，喜欢灵活应变，是感知型的隐喻','0',NOW(),NOW()),
  ('J','台历','JP',4,2,1,'https://zhiye.sxgkzh.cn/imgs/zytj/5331b0a2e482/p2-item-calendar.png',
   '有明确日程规划，有计划、有条理地安排时间，倾向于做出决定并坚持，属于判断型特征','0',NOW(),NOW());

-- 16 型人格对照表，内容逐字来自《职愿探究-MBTI.docx》第 4 节。
INSERT IGNORE INTO gkzh_zytj_type(type_code,feature,career_field,typical_careers,sort_order,status,create_time,update_time) VALUES
  ('ISTJ','实际、有条理、注重细节、坚守传统','金融、法律、行政后勤','审计师、法务专员、办公室主任',1,'0',NOW(),NOW()),
  ('ISFJ','忠诚、体贴、善于照顾他人、注重秩序','教育、医疗、服务行业','小学教师、护士、客服主管',2,'0',NOW(),NOW()),
  ('INFJ','富有洞察力、创造力、理想主义、善于规划','科研、文化艺术、咨询','心理学家、作家、战略规划师',3,'0',NOW(),NOW()),
  ('INTJ','理性、独立、目标导向、擅长逻辑分析','科技、金融投资、管理','软件架构师、投资分析师、企业高管',4,'0',NOW(),NOW()),
  ('ISTP','冷静、灵活、擅长实操、喜欢探索','机械维修、体育竞技、执法','汽车修理工、运动员、警察',5,'0',NOW(),NOW()),
  ('ISFP','敏感、艺术感强、注重当下体验','艺术设计、美容美发、手工艺','插画师、美发师、陶艺师',6,'0',NOW(),NOW()),
  ('INFP','富有同情心、追求自我价值、充满创意','教育咨询、写作、公益','生涯规划师、自由撰稿人、公益项目策划',7,'0',NOW(),NOW()),
  ('INTP','好奇、逻辑思维卓越、喜欢理论研究','科研、编程、学术','物理学家、程序员、大学教授',8,'0',NOW(),NOW()),
  ('ESTP','外向、勇敢、适应力强、喜欢挑战','销售、市场营销、演艺','销售经理、市场推广专员、演员',9,'0',NOW(),NOW()),
  ('ESFP','热情、友善、善于社交、热爱生活','公关、娱乐、旅游','公关专员、主持人、导游',10,'0',NOW(),NOW()),
  ('ENFP','乐观、富有想象力、善于激励他人','广告创意、培训、人力资源','广告策划师、培训师、招聘主管',11,'0',NOW(),NOW()),
  ('ENTP','机智、善于辩论、思维敏捷、敢于创新','创业、科技研发、咨询顾问','创业者、技术研发工程师、管理咨询顾问',12,'0',NOW(),NOW()),
  ('ESTJ','果断、组织能力强、注重效率、遵守规则','企业管理、军事、教育管理','总经理、军官、中学校长',13,'0',NOW(),NOW()),
  ('ESFJ','乐于助人、责任心强、擅长协调、注重人际关系','人力资源、社区服务、餐饮管理','人力资源经理、社区工作者、餐厅经理',14,'0',NOW(),NOW()),
  ('ENFJ','领导力强、善于沟通、富有感染力、目标明确','政治、教育、企业管理','政治家、大学辅导员、企业总裁',15,'0',NOW(),NOW()),
  ('ENTJ','自信、有远见、决策力强、善于战略规划','商业、政治、大型项目管理','商业巨头、政府',16,'0',NOW(),NOW());
