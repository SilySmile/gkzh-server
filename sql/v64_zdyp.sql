-- 职得一拼（zdyp）基础结构与 10 套岗位内容；可重复执行。
-- 规范：所有业务表与字段必须提供中文 COMMENT；本脚本只创建 gkzh_zdyp_* 表，不修改其他游戏数据。
-- 内容来自《game/职得一拼/职业速览拼图.docx》，线上可在 Web“所需配置”继续维护名称与文案。
-- 适用 MySQL 5.7（不使用 CHECK 约束、窗口函数和 JSON 函数索引）。

CREATE TABLE IF NOT EXISTS gkzh_zdyp_career (
  career_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '岗位编号（自增主键）',
  career_code VARCHAR(64) NOT NULL COMMENT '岗位编码，全小写下划线，用于记录快照与统计',
  career_name VARCHAR(100) NOT NULL COMMENT '岗位中文名，例如“大数据工程技术人员”',
  intro TEXT COMMENT '岗位定义，解析页展示，说明该岗位做什么',
  industry_value TEXT COMMENT '行业价值，解析页展示，说明该岗位在行业中的作用',
  newcomer_advice TEXT COMMENT '新生入门建议，解析页展示，说明在校期间如何准备',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '学生端职业列表展示顺序，数值越小越靠前',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用；停用岗位不出现在学生端，也不能被选择',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(career_id),
  UNIQUE KEY uk_zdyp_career_code(career_code),
  KEY idx_zdyp_career_sort(sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职得一拼岗位配置（每套岗位对应一组拼图卡片）';

CREATE TABLE IF NOT EXISTS gkzh_zdyp_card (
  card_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '卡片编号（自增主键）',
  career_id BIGINT NOT NULL COMMENT '所属岗位编号，对应 gkzh_zdyp_career.career_id',
  dimension_code VARCHAR(32) NOT NULL COMMENT '卡片维度编码：content 工作内容，skill 核心技能，weakness 职业缺点，prospect 发展前景',
  card_text TEXT NOT NULL COMMENT '卡片正文，学生端直接展示该文案',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '同一岗位内卡片的默认顺序，仅用于后台展示',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用；停用卡片不参与拼图',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(card_id),
  UNIQUE KEY uk_zdyp_card(career_id,dimension_code),
  KEY idx_zdyp_card_career(career_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职得一拼岗位卡片（每个岗位四个维度各一张）';

CREATE TABLE IF NOT EXISTS gkzh_zdyp_setting (
  setting_id TINYINT NOT NULL COMMENT '配置编号，固定为1（单行配置）',
  time_limit INT NOT NULL DEFAULT 45 COMMENT '单次闯关限时（秒），0 表示不限时；到点未填满按超时处理',
  enabled CHAR(1) NOT NULL DEFAULT '1' COMMENT '是否启用游戏：0停用，1启用；停用时学生端拒绝进入',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(setting_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职得一拼规则参数（单行配置）';

CREATE TABLE IF NOT EXISTS gkzh_zdyp_record (
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
  game_type VARCHAR(32) NOT NULL DEFAULT 'zdyp' COMMENT '游戏类型，固定为zdyp',
  status VARCHAR(16) NOT NULL DEFAULT 'participating' COMMENT '参与状态：participating进行中，finished已通关',
  career_id BIGINT DEFAULT NULL COMMENT '所选岗位编号；未开始时为空',
  career_code VARCHAR(64) DEFAULT NULL COMMENT '所选岗位编码快照',
  career_name VARCHAR(100) DEFAULT NULL COMMENT '所选岗位中文名快照，后台改名不影响历史记录',
  attempt_no INT NOT NULL DEFAULT 0 COMMENT '已发起的闯关次数，重闯时累加',
  first_attempt_correct CHAR(1) NOT NULL DEFAULT '0' COMMENT '首次尝试是否一次全对：0否，1是；用于行为分型与首次通关率',
  total_wrong INT NOT NULL DEFAULT 0 COMMENT '全部尝试累计放错的卡槽次数',
  timeout_count INT NOT NULL DEFAULT 0 COMMENT '超时尝试次数',
  first_attempt_ms BIGINT DEFAULT NULL COMMENT '首次尝试用时（毫秒），用于急躁粗放型判定与平均用时',
  best_ms BIGINT DEFAULT NULL COMMENT '通关尝试的最短用时（毫秒）',
  behavior_type VARCHAR(32) DEFAULT NULL COMMENT '首次通关时写入的行为分型编码：balanced 全面均衡型，advantage 优势偏好型，weak 信息薄弱型，rushed 急躁粗放型',
  behavior_name VARCHAR(50) DEFAULT NULL COMMENT '行为分型中文名快照',
  conclusion TEXT COMMENT '个人结论快照，首次通关时写入，后台改文案不影响历史报告',
  suggestion TEXT COMMENT '提升建议快照，首次通关时写入',
  scan_time DATETIME DEFAULT NULL COMMENT '扫码进入游戏时间',
  start_time DATETIME DEFAULT NULL COMMENT '首次点击开始闯关时间',
  finish_time DATETIME DEFAULT NULL COMMENT '首次通关时间',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(record_id),
  UNIQUE KEY uk_zdyp_record_scope(school_id,instance_id,game_id,user_id),
  KEY idx_zdyp_record_instance(instance_id,school_id),
  KEY idx_zdyp_record_user(user_id),
  KEY idx_zdyp_record_career(career_code),
  KEY idx_zdyp_record_behavior(behavior_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职得一拼参与记录（一校一活动一游戏一用户一条）';

CREATE TABLE IF NOT EXISTS gkzh_zdyp_attempt (
  attempt_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '闯关尝试编号',
  record_id BIGINT NOT NULL COMMENT '所属参与记录编号',
  attempt_no INT NOT NULL COMMENT '第几次闯关，从1开始累加',
  career_id BIGINT DEFAULT NULL COMMENT '本局所选岗位编号',
  correct_count INT NOT NULL DEFAULT 0 COMMENT '本次放对的卡槽数量（0-4）',
  wrong_count INT NOT NULL DEFAULT 0 COMMENT '本次放错的卡槽数量（0-4）',
  filled_count INT NOT NULL DEFAULT 0 COMMENT '本次实际提交的卡槽数量，少于4表示超时未填满',
  is_timeout CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否超时：0否，1是；由服务端按开始时间加限时判定',
  is_pass CHAR(1) NOT NULL DEFAULT '0' COMMENT '本次是否通关：0否，1是（4个卡槽全部放对）',
  duration_ms BIGINT DEFAULT NULL COMMENT '本次从开始到提交的用时（毫秒）',
  first_slot_code VARCHAR(32) DEFAULT NULL COMMENT '本局第一个被填入的卡槽编码，用于首填偏好统计',
  submit_time DATETIME DEFAULT NULL COMMENT '提交时间',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY(attempt_id),
  UNIQUE KEY uk_zdyp_attempt(record_id,attempt_no),
  KEY idx_zdyp_attempt_record(record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职得一拼闯关尝试记录';

CREATE TABLE IF NOT EXISTS gkzh_zdyp_answer (
  answer_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '作答明细编号',
  attempt_id BIGINT NOT NULL COMMENT '所属闯关尝试编号',
  record_id BIGINT NOT NULL COMMENT '所属参与记录编号，便于按记录直接统计',
  slot_code VARCHAR(32) NOT NULL COMMENT '卡槽编码：content、skill、weakness、prospect',
  card_id BIGINT DEFAULT NULL COMMENT '放入该卡槽的卡片编号；超时未填时为空',
  card_dimension VARCHAR(32) DEFAULT NULL COMMENT '放入卡片所属维度，用于判断是否放对',
  is_correct CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否放对：0否，1是',
  place_order INT NOT NULL DEFAULT 0 COMMENT '本局放置顺序，从1开始，用于首填偏好与操作轨迹',
  elapsed_ms BIGINT DEFAULT NULL COMMENT '从本局开始到放入该卡槽的用时（毫秒）',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY(answer_id),
  UNIQUE KEY uk_zdyp_answer(attempt_id,slot_code),
  KEY idx_zdyp_answer_record(record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职得一拼作答明细（每次尝试的四个卡槽放置）';

-- 规则参数只插入一次；后台保存后重复执行脚本不会覆盖。
INSERT IGNORE INTO gkzh_zdyp_setting(setting_id,time_limit,enabled,create_time,update_time)
VALUES(1,45,'1',NOW(),NOW());

-- 10 套岗位；编码唯一，重复执行不会产生重复数据，也不会覆盖后台修改。
INSERT IGNORE INTO gkzh_zdyp_career(career_code,career_name,intro,industry_value,newcomer_advice,sort_order,status,create_time,update_time) VALUES
('big_data_engineer','大数据工程技术人员',
 '大数据工程技术人员负责行业海量数据的采集、清洗、整理与分析，搭建基础数据模型，输出可用于决策的数据报告。',
 '数字经济核心刚需岗位，全行业数字化转型都需要数据支撑，岗位缺口大，可晋升数据分析师或数据运营主管。',
 '先把统计基础和表格、可视化工具练熟，再用真实业务数据做小项目，避免只会工具不懂业务。',1,'0',NOW(),NOW()),
('blockchain_engineer','区块链工程技术人员',
 '区块链工程技术人员参与区块链系统的基础搭建、功能测试与运行维护，把数据上链、信息存证等能力落到具体业务场景。',
 '属于国家数字基建重点赛道，在金融、政务、供应链等领域有长期应用需求，政策红利明显、岗位相对稳定。',
 '先理解分布式与密码学的基本概念，再通过开源项目熟悉链上开发流程，关注真实落地场景而不是概念热度。',2,'0',NOW(),NOW()),
('intelligent_manufacturing_engineer','智能制造工程技术人员',
 '智能制造工程技术人员协助智能产线调试、生产流程优化与设备运维，推动传统制造向数字化、智能化升级。',
 '中国制造转型升级的核心岗位，政策强力扶持、制造业刚需，晋升路径清晰：技术员到工程师再到项目技术负责人。',
 '在校期间多进实验室和实训车间，把机械基础、数据统计与现场问题处理结合起来，积累真实产线经验。',3,'0',NOW(),NOW()),
('network_security_evaluator','网络安全等级保护测评师',
 '网络安全等级保护测评师负责网络系统安全检查、漏洞排查与等级保护测评，输出安全记录与防护优化建议。',
 '数字化时代政企单位全覆盖的刚需岗位，含金量高、替代性低，职业稳定性与成长性兼备。',
 '系统学习网络安全与合规知识，考取相关认证，并通过靶场演练积累漏洞排查和报告撰写的实操经验。',4,'0',NOW(),NOW()),
('cloud_network_operator','云网智能运维员',
 '云网智能运维员负责云端平台与网络设备的日常监控、运行维护和故障预警，保障线上业务持续稳定运行。',
 '上云趋势下的全民刚需岗位，各行各业全面普及，就业面广，可成长为云网工程师或运维主管。',
 '先掌握网络与云平台的基础操作，再通过模拟故障演练熟悉排查流程，养成记录台账和复盘的习惯。',5,'0',NOW(),NOW()),
('genai_application_specialist','生成式人工智能系统应用员',
 '生成式人工智能系统应用员运用生成式 AI 工具完成提示词优化、内容生成与校对，推动 AI 能力在各业务中落地。',
 '国家正式收录的 AI 新职业，全行业通用，适配所有专业，是未来十年核心的通用新业态岗位。',
 '不要停留在套用工具，要练习提示词设计与内容校对，把 AI 输出与专业知识结合，形成可复用的工作方法。',6,'0',NOW(),NOW()),
('vr_engineer','虚拟现实工程技术人员',
 '虚拟现实工程技术人员负责 VR、AR 虚拟场景搭建与沉浸式内容制作，调试交互效果并适配文旅、教育、展览等场景。',
 '元宇宙与数字文旅产业的重点扶持岗位，属于工信部重点数字经济职业，政企项目需求旺盛。',
 '打好三维空间与建模基础，熟练使用主流虚拟内容制作工具，多参与课程项目和比赛积累完整作品。',7,'0',NOW(),NOW()),
('industrial_internet_operator','工业互联网运维员',
 '工业互联网运维员负责工业互联网平台的日常运维、设备数据对接与网络状态监测，保障平台稳定、安全运行。',
 '智能制造核心刚需岗位，属于国家工业数字化重点布局方向，岗位稳定、缺口大，就业覆盖面广。',
 '熟悉工业现场与网络基础，练习设备数据对接和故障排查，同时补充工业数字化规则方面的知识。',8,'0',NOW(),NOW()),
('digital_twin_technician','数字孪生应用技术员',
 '数字孪生应用技术员搭建实体场景的数字孪生模型，完成数据同步、场景仿真与动态监测，支撑城市、工业与建筑行业。',
 '新型智慧城市与智能制造的核心支撑岗位，2026 年官方新增职业，未来 5 至 10 年高速发展、人才稀缺。',
 '同步学习空间建模与数据读取，尽早接触仿真软件和真实项目，弥补前沿岗位实操资源少的短板。',9,'0',NOW(),NOW()),
('user_growth_operator','用户增长运营师',
 '用户增长运营师依托数字化平台分析用户行为，策划拉新、留存与促活方案，推动用户规模与价值稳步增长。',
 '全网行业通用的刚需岗位，2026 年最新国标认证职业，数字化企业全员刚需，就业灵活、适配全行业。',
 '从数据分析和内容策划入手，做一两个真实的运营小项目，用数据复盘替代主观判断，形成可展示的成果。',10,'0',NOW(),NOW());

-- 40 张卡片：每个岗位固定四个维度各一张，维度编码与记录、统计口径一致。
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'content','负责行业海量数据采集、清洗、整理、分析，搭建基础数据模型，挖掘数据价值，输出数据报告，为企业决策与数字化运营提供支撑。',1,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='big_data_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'skill','具备数据整理能力、逻辑分析能力、基础统计思维、办公数据工具操作、报表可视化输出能力，细心严谨、擅长归纳。',2,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='big_data_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'weakness','日常工作重复性高、数据处理枯燥，长期久坐用眼负荷大；行业迭代快，需要持续学习新技术，容易出现技能跟不上行业更新的压力。',3,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='big_data_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'prospect','数字经济核心刚需岗位，工信部重点扶持职业，全行业数字化转型必备，岗位缺口极大，可晋升数据分析师、数据运营主管。',4,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='big_data_engineer';

INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'content','参与区块链系统基础搭建、功能测试、运行维护、应用落地，协助完成数据上链、信息存证、安全核查等基础技术配套工作。',1,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='blockchain_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'skill','具备基础网络认知、区块链底层逻辑理解、严谨排查能力、基础技术文档撰写、问题记录与复盘能力。',2,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='blockchain_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'weakness','技术门槛逐年抬高，底层知识晦涩难懂；行业热度波动大，部分领域落地场景有限，新手容易出现“学完无对口场景”的尴尬问题。',3,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='blockchain_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'prospect','国家数字基建重点赛道，金融、政务、供应链多领域广泛应用，属于长期政策红利型新兴技术岗位，稳定性强。',4,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='blockchain_engineer';

INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'content','协助智能产线调试、生产流程优化、智能制造设备运维、生产数据统计，推进传统制造向数字化、智能化生产升级。',1,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='intelligent_manufacturing_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'skill','具备机械基础认知、流程梳理能力、设备运维意识、数据统计、现场问题观察与基础处理能力。',2,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='intelligent_manufacturing_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'weakness','一线实操环境相对枯燥，部分岗位需要倒班、现场值守；实操经验要求高，应届生上手慢，初期成长周期较长。',3,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='intelligent_manufacturing_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'prospect','中国制造转型升级核心岗位，政策强力扶持，制造业刚需、就业稳定，晋升路径：技术员到工程师再到项目技术负责人。',4,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='intelligent_manufacturing_engineer';

INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'content','负责企业、单位网络系统安全检查、漏洞排查、等级保护测评、安全记录归档，制定基础网络安全防护优化建议。',1,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='network_security_evaluator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'skill','具备网络安全常识、风险识别能力、细致排查能力、文档撰写、规范落地与合规检查思维。',2,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='network_security_evaluator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'weakness','合规条款繁杂、记忆内容多，工作容错率极低；重大漏洞排查压力大，应急值守、加班情况较多，职业心理压力偏高。',3,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='network_security_evaluator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'prospect','数字化时代刚需岗位，政企单位全覆盖需求，岗位含金量高、替代性低，职业稳定性与成长性兼备。',4,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='network_security_evaluator';

INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'content','负责云端平台、网络设备日常监控、运行维护、故障预警、基础排查，保障企业云服务、线上业务持续稳定运行。',1,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='cloud_network_operator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'skill','具备基础云网认知、设备监控能力、应急反应能力、台账整理、问题复盘与日常运维执行力。',2,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='cloud_network_operator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'weakness','需要 7×24 小时轮值应急，突发故障需即时处理；日常工作偏值守、重复性强，前期技术成长速度较慢。',3,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='cloud_network_operator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'prospect','上云趋势下全民刚需岗位，各行各业全面普及，就业面极广，可成长为云网工程师、运维主管。',4,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='cloud_network_operator';

INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'content','熟练运用生成式 AI 工具，优化提示词、生成并校对图文、文案、方案、素材内容，完成 AI 内容二次优化与落地应用。',1,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='genai_application_specialist';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'skill','具备 AI 工具实操能力、创意优化、内容校对、逻辑打磨、审美筛选、人机协同创作能力。',2,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='genai_application_specialist';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'weakness','岗位替代性风险高，基础 AI 工作极易被迭代；行业标准尚未完全统一，新手容易停留在“只会套用工具”，难以形成核心竞争力。',3,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='genai_application_specialist';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'prospect','国家正式收录 AI 新职业，全行业通用能力，适配所有专业就业赋能，未来十年核心通用新业态岗位。',4,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='genai_application_specialist';

INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'content','负责 VR、AR 虚拟场景搭建、沉浸式内容制作、交互效果调试，优化虚拟视觉体验，适配文旅、教育、展览等数字化场景落地应用。',1,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='vr_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'skill','具备三维空间认知、场景建模基础、视觉审美、交互逻辑思维，熟练使用主流虚拟内容制作工具，具备内容优化能力。',2,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='vr_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'weakness','设备与软件更新快，学习成本持续偏高；行业项目制居多，工作节奏不稳定，淡旺季明显，就业稳定性弱于传统技术岗。',3,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='vr_engineer';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'prospect','元宇宙、数字文旅产业重点扶持岗位，属于工信部重点数字经济职业，政企项目需求旺盛，可晋升虚拟内容主创、项目技术统筹。',4,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='vr_engineer';

INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'content','负责工业互联网平台日常运维、设备数据对接、网络状态监测、基础故障排查，保障工业数字化平台稳定、安全、高效运行。',1,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='industrial_internet_operator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'skill','具备基础网络认知、数据整理能力、设备运维思维、问题排查能力，熟悉工业数字化基础规则，具备细心严谨的工作素养。',2,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='industrial_internet_operator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'weakness','对接工业现场，环境相对受限；岗位细分细致，长期单一模块工作容易技能固化，横向拓展难度较大。',3,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='industrial_internet_operator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'prospect','智能制造核心刚需岗位，国家工业数字化重点布局方向，岗位稳定、缺口大，属于长期红利型新职业，就业覆盖面极广。',4,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='industrial_internet_operator';

INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'content','搭建实体场景的数字孪生模型，完成数据同步、场景仿真、动态监测，助力城市、工业、建筑行业实现数字化模拟与智能管控。',1,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='digital_twin_technician';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'skill','具备空间建模认知、数据读取整理、仿真逻辑理解、基础软件操作能力，拥有严谨的数字化思维与复盘能力。',2,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='digital_twin_technician';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'weakness','属于前沿新兴岗位，行业落地不成熟，优质岗位集中在大城市；新手实操资源少，自学难度大，入门成长周期长。',3,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='digital_twin_technician';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'prospect','新型智慧城市、智能制造核心支撑岗位，2026 年最新官方新增职业，未来 5 至 10 年高速发展，晋升通道清晰、人才稀缺。',4,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='digital_twin_technician';

INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'content','依托数字化平台，分析用户行为数据，策划拉新、留存、促活运营方案，优化产品服务体验，实现用户规模与价值稳步增长。',1,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='user_growth_operator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'skill','具备基础数据分析、用户思维、方案策划、热点洞察、复盘优化能力，擅长精细化运营与问题迭代。',2,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='user_growth_operator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'weakness','工作数据压力大、KPI 考核严格，流量波动不可控；行业内卷严重，工作重复性高，个人差异化优势难建立。',3,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='user_growth_operator';
INSERT IGNORE INTO gkzh_zdyp_card(career_id,dimension_code,card_text,sort_order,status,create_time,update_time)
SELECT career_id,'prospect','全网行业通用刚需岗位，2026 年最新国标认证职业，数字化企业全员刚需，就业灵活、适配全行业，成长性极强。',4,'0',NOW(),NOW() FROM gkzh_zdyp_career WHERE career_code='user_growth_operator';
