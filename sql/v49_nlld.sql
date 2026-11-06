-- 能量雷达（nlld）基础结构、15 个价值观维度、6 关 30 件物品与解读文案；可重复执行。
-- 规范：所有业务表和字段必须提供中文 COMMENT；本脚本只创建 gkzh_nlld_* 表，
-- 不修改其他游戏数据。内容来自《能量雷达(2).docx》，后台可继续维护。

CREATE TABLE IF NOT EXISTS gkzh_nlld_dimension (
  dimension_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '维度编号',
  dimension_code VARCHAR(32) NOT NULL COMMENT '维度编码：intellect、achievement、creativity、autonomy、variety、salary、security、prestige、aesthetics、environment、management、colleague、supervisor、altruism、lifestyle',
  dimension_name VARCHAR(20) NOT NULL COMMENT '维度中文名，例如智力激发、安全稳定',
  dimension_group VARCHAR(32) NOT NULL COMMENT '维度分组：内在价值、外在与保障、人际影响与生活方式',
  summary TEXT COMMENT '该维度的“渴望……”解读文案',
  career_reference TEXT COMMENT '该维度的“职业参考：……”文案',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序，数值越小越靠前，也用于 TOP2 同分排序',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(dimension_id),
  UNIQUE KEY uk_nlld_dimension_code(dimension_code),
  UNIQUE KEY uk_nlld_dimension_name(dimension_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='能量雷达职业价值观维度配置';

CREATE TABLE IF NOT EXISTS gkzh_nlld_round (
  round_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关卡编号',
  round_no TINYINT NOT NULL COMMENT '第几关，取值1-6',
  title VARCHAR(50) NOT NULL COMMENT '关卡名称，例如抢修海岛供水系统',
  scene TEXT COMMENT '任务情景说明',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序，数值越小越靠前',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(round_id),
  UNIQUE KEY uk_nlld_round_no(round_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='能量雷达关卡配置';

CREATE TABLE IF NOT EXISTS gkzh_nlld_option (
  option_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '物品编号',
  round_id BIGINT NOT NULL COMMENT '所属关卡编号',
  option_label CHAR(1) NOT NULL COMMENT '选项字母：A、B、C、D、E',
  option_name VARCHAR(100) NOT NULL COMMENT '物品名称，例如居民应急供水分配器',
  option_desc VARCHAR(1000) DEFAULT NULL COMMENT '物品说明，即括号内的描述',
  dimension_id BIGINT NOT NULL COMMENT '对应价值观维度编号',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '关内显示顺序，数值越小越靠前',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(option_id),
  UNIQUE KEY uk_nlld_option_label(round_id,option_label),
  KEY idx_nlld_option_dimension(dimension_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='能量雷达关卡物品选项配置';

CREATE TABLE IF NOT EXISTS gkzh_nlld_record (
  record_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '参与记录编号',
  school_id BIGINT NOT NULL COMMENT '学校编号',
  instance_id BIGINT NOT NULL COMMENT '活动周实例编号',
  game_id BIGINT NOT NULL COMMENT '活动游戏编号',
  user_id BIGINT NOT NULL COMMENT '平台用户编号',
  student_id BIGINT DEFAULT NULL COMMENT '学生编号',
  student_no VARCHAR(50) DEFAULT NULL COMMENT '学号快照',
  student_name VARCHAR(100) DEFAULT NULL COMMENT '学生姓名快照',
  department_id BIGINT DEFAULT NULL COMMENT '专业（二级部门）编号，其上一级为院系',
  major VARCHAR(100) DEFAULT NULL COMMENT '专业名称快照',
  gender CHAR(1) DEFAULT NULL COMMENT '性别快照：0男，1女，其他值表示未知',
  game_type VARCHAR(32) NOT NULL DEFAULT 'nlld' COMMENT '游戏类型，固定为nlld',
  status VARCHAR(16) NOT NULL DEFAULT 'participating' COMMENT '参与状态：participating进行中，finished已完成',
  stage VARCHAR(16) NOT NULL DEFAULT 'entered' COMMENT '业务阶段：entered已扫码，choosing选择中，finished已完成',
  answered_rounds TINYINT NOT NULL DEFAULT 0 COMMENT '已提交关数，取值0-6',
  top1_dimension_id BIGINT DEFAULT NULL COMMENT 'TOP1维度编号快照；多元开放型为空',
  top1_dimension_name VARCHAR(20) DEFAULT NULL COMMENT 'TOP1维度名称快照；多元开放型为空',
  top2_dimension_id BIGINT DEFAULT NULL COMMENT 'TOP2维度编号快照；多元开放型为空',
  top2_dimension_name VARCHAR(20) DEFAULT NULL COMMENT 'TOP2维度名称快照；多元开放型为空',
  result_type VARCHAR(16) DEFAULT NULL COMMENT '结果类型：top2两个维度并列最高分，open多元开放型；未结束为空',
  score_json JSON DEFAULT NULL COMMENT '15个维度得分与结果类型快照，供报告和统计稳定展示',
  scan_time DATETIME NOT NULL COMMENT '扫码进入游戏时间',
  finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(record_id),
  UNIQUE KEY uk_nlld_record_scope(school_id,instance_id,game_id,user_id),
  KEY idx_nlld_record_instance(instance_id,game_id),
  KEY idx_nlld_record_result(result_type,status),
  KEY idx_nlld_record_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='能量雷达学生参与记录';

CREATE TABLE IF NOT EXISTS gkzh_nlld_answer (
  answer_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '选择明细编号',
  record_id BIGINT NOT NULL COMMENT '参与记录编号',
  round_no TINYINT NOT NULL COMMENT '第几关，取值1-6',
  option_id BIGINT NOT NULL COMMENT '物品编号',
  option_label CHAR(1) NOT NULL COMMENT '选项字母快照：A-E',
  option_name VARCHAR(100) NOT NULL COMMENT '物品名称快照',
  dimension_id BIGINT NOT NULL COMMENT '维度编号快照',
  dimension_code VARCHAR(32) NOT NULL COMMENT '维度编码快照',
  create_time DATETIME DEFAULT NULL COMMENT '提交时间',
  PRIMARY KEY(answer_id),
  UNIQUE KEY uk_nlld_answer_option(record_id,round_no,option_id),
  KEY idx_nlld_answer_record(record_id),
  KEY idx_nlld_answer_dimension(dimension_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='能量雷达学生选择明细';

-- 15 个维度；INSERT IGNORE 保证重复执行不覆盖后台修改。
INSERT IGNORE INTO gkzh_nlld_dimension(dimension_id,dimension_code,dimension_name,dimension_group,summary,career_reference,sort_order,status,create_time,update_time) VALUES
(1,'intellect','智力激发','内在价值','渴望持续动脑、学习新知、探究抽象与复杂问题，工作要能不断提供智力上的挑战。','职业参考：科研、算法与技术开发、咨询、数据分析、医生、法律等需要持续钻研的岗位更容易满足；长期重复、流程固化的事务性工作会让高分者迅速倦怠。选 offer 时重点看岗位是否有持续的学习曲线与复杂问题空间。',1,'0',NOW(),NOW()),
(2,'achievement','成就满足','内在价值','看重目标达成、把事情做成的完整闭环，以及由此获得的结果反馈与自我超越感。','职业参考：目标清晰、绩效可量化、有晋升通道的方向更适配，如销售、项目管理、创业、竞技性强的技术岗；职责边界模糊、成果难以归属的"打杂型"岗位难以满足高分者。要注意区分"成就"与"声望"——前者靠把事做成，后者靠外界怎么看。',2,'0',NOW(),NOW()),
(3,'creativity','创造发明','内在价值','需要在工作中表达新想法、产出原创成果，而不是照章执行既有方案。','职业参考：艺术、设计、内容创作、产品研发、广告策划、架构设计等原创性领域更有活力；高度标准化、只要求执行的岗位会让高分者感到"被框住"。转行咨询中常见的"稳定但无聊"型倦怠，往往与这项被长期压制有关。',3,'0',NOW(),NOW()),
(4,'autonomy','独立自主','内在价值','希望按自己的方式安排工作，自主决定方法、节奏与优先级，反感被事无巨细地监督。','职业参考：自由职业、独立咨询、学术研究、远程与结果导向型岗位适配度高；层级森严、打卡盯人的组织环境会构成持续消耗。高分者要同时评估自律能力与风险承受力——独立的另一面是无人兜底，它常与"安全感"形成内在冲突。',4,'0',NOW(),NOW()),
(5,'variety','多样变化','内在价值','偏好任务常换常新、节奏多变，排斥日复一日高度重复的单调劳动。','职业参考：记者、活动策划、急诊医疗、综合管理、平台型轮岗机制更契合；流水线、单一职能长期重复操作容易引发倦怠。若职业路径本身偏专精（如某些技术深岗），可通过项目轮换、副业或阶段性任务变化来补偿。',5,'0',NOW(),NOW()),
(6,'salary','经济报酬','外在与保障','重视薪资、奖金、福利等物质回报水平，以及努力与回报之间的对等关系。','职业参考：销售提成、金融、热门技术岗、创业型公司的高弹性回报结构更能满足；薪资天花板低、增长曲线平缓的职业需要慎重。把经济报酬排前列完全正当——关键是用可验证的行业薪酬数据决策，而不是被口头承诺或"谈钱庸俗"的观念干扰。',6,'0',NOW(),NOW()),
(7,'security','安全稳定','外在与保障','看重工作稳定、雇用关系牢靠、未来前景可预期，不愿承受频繁变动与失业风险。','职业参考：体制内、教育、大型成熟机构、长期合同制岗位适配度高；创业公司、强周期行业与纯佣金岗位压力较大。高分者并非"没有追求"，而是在用确定性换发展速度——转行决策时务必把"稳定"显性计入平衡单，而不是被"年轻人就该闯"的声音带走。',7,'0',NOW(),NOW()),
(8,'prestige','声望地位','外在与保障','看重职业的社会地位、他人尊重与认可度，希望自己从事的工作"说出去体面"。','职业参考：医生、律师、高校教职、知名机构与头部平台岗位更能满足；声望很大程度由行业与平台背书，择业时可重点比较行业社会评价与机构品牌。需要警惕的是：声望满足的是"别人眼中的我"，若与兴趣、能力长期错配，"体面的辛苦"最容易被合理化拖延。',8,'0',NOW(),NOW()),
(9,'aesthetics','美的追求','外在与保障','希望工作中能接触或创造美的事物，看重环境、产品与成果的审美品质。','职业参考：设计、建筑、文创、花艺、景观、视觉陈列、内容审美向岗位天然契合；即便不从事创意行业，也可以选择空间品质好、产品有审美追求的组织。美感长期被忽视会表现为说不清原因的烦躁——在 offer 比较时，办公空间与产品调性值得作为一项明确指标实地感受。',9,'0',NOW(),NOW()),
(10,'environment','工作环境','外在与保障','看重物理工作条件：舒适、安全、便利，包括通勤、设施与劳动保护。','职业参考：对环境敏感者（含部分健康状况、家庭照护责任）应把通勤时长、办公条件、作业安全写进 offer 评估清单，权重不亚于薪资；野外、工地、倒班、高强度差旅岗位需要诚实评估耐受度。环境是"日常八小时"的组成部分，它对幸福感的侵蚀是慢性的，不宜只在签约时忍一忍。',10,'0',NOW(),NOW()),
(11,'management','管理权力','人际影响与生活方式','希望影响他人、统筹资源、承担领导与决策职责，从"带人成事"中获得满足。','职业参考：管理培训生、项目负责人、团队主管、创业路径适配；纯个人贡献者路线即使高薪也可能让高分者觉得"没在往上走"。需要区分"想赢"（成就）与"想带人"（管理）——后者要真实评估沟通、冲突处理与承担责任的意愿，管理岗的压力来自人而非任务。',11,'0',NOW(),NOW()),
(12,'colleague','同事关系','人际影响与生活方式','看重与共事伙伴的人际氛围，希望和友善、合拍、能互相支持的人一起工作。','职业参考：高分者在团队协作密集、文化温暖的组织中表现最好，孤立、内卷、互相防备的氛围是其离职首要诱因。面试阶段的团队感受、在职者口碑与离职率值得重点打听；独立作业型岗位（远程、单兵）则要主动建立同侪支持网络来补偿。',12,'0',NOW(),NOW()),
(13,'supervisor','上司关系','人际影响与生活方式','看重与直接主管的相处质量，希望上级公正、讲道理、愿意支持下属成长。','职业参考：研究反复表明，直接上级是工作满意度与离职决策的关键变量——"加入公司，离开经理"。高分者择业时应把"未来主管是谁、管理风格如何"列为必尽调项，争取终面与未来主管深谈；遇到长期不公正的上级，换组或换岗比硬扛更符合自身价值结构。',13,'0',NOW(),NOW()),
(14,'altruism','利他主义','人际影响与生活方式','希望工作能直接帮助他人、对社会有正向贡献，从"被需要"中获得意义感。','职业参考：医疗、教育、心理咨询、社会工作、公益、公共服务等助人领域意义感最强；企业中也可选择用户价值明确的产品、客户成功、ESG 等岗位。高分者要警惕"意义感透支"：助人行业普遍薪资偏低、情绪劳动重，需同时为经济报酬与职业耗竭设置防护边界。',14,'0',NOW(),NOW()),
(15,'lifestyle','生活方式','人际影响与生活方式','希望工作允许自己按期望的方式生活：兼顾家庭、爱好、休息与个人节奏，不让职业吞掉生活。','职业参考：弹性工时、远程友好、加班文化弱的岗位与行业更契合；"高薪高压＋生活全部让渡"的路径即使短期可承受，长期也会动摇排序。这一项在成家、育儿、照护老人等阶段通常自动前移，做重大选择时建议按"三年后生活形态"反推',15,'0',NOW(),NOW());

-- 6 个关卡；题目与情景来自需求文档。
INSERT IGNORE INTO gkzh_nlld_round(round_id,round_no,title,scene,sort_order,status,create_time,update_time) VALUES
(1,1,'抢修海岛供水系统','海岛供水系统突发故障，居民用水受到影响。你加入了抢修团队，需要尽快恢复供水。请从以下 5 件物品中选择 2 件，帮助团队完成抢修。',1,'0',NOW(),NOW()),
(2,2,'探索神秘洞穴','抢修供水系统时，队员发现水源附近有异常发光的矿物。你和探险小队循着线索来到海岛深处，发现一处神秘洞穴。为了查明矿物特征并完成勘测，请从 5 件探索装备中选择 2 件。',2,'0',NOW(),NOW()),
(3,3,'建设海岛旅游中心','洞穴勘测带来了新的发现，海岛团队决定发展旅游项目，计划建设一座旅游中心。你需要参与建筑设计、施工协调和设施建设。请从以下 5 件物品中选择 2 件，帮助团队推进项目。',3,'0',NOW(),NOW()),
(4,4,'建设海岛科研基地','随着旅游中心规划逐步推进，团队也决定建设科研基地，进一步研究岛上的特殊资源。基地需要安装实验设备、保障供电并完成初期调试。请从以下 5 件物品中选择 2 件，帮助团队完成建设任务。',4,'0',NOW(),NOW()),
(5,5,'修复海岛社区','科研基地建设期间，台风突然袭击海岛，社区的房屋、道路和供水设施受损。你加入灾后修复团队，需要尽快恢复居民的正常生活。请从以下 5 件物品中选择 2 件，帮助团队完成修复任务。',5,'0',NOW(),NOW()),
(6,6,'筹备海岛文化节','经过大家的努力，海岛逐步恢复正常。为了庆祝重建与新发现，居民决定举办首届海岛文化节。距离开幕只剩一天，团队需要搭建舞台、准备节目、调试设备并完成彩排。请从以下 5 件物品中选择 2 件，帮助团队完成筹备。',6,'0',NOW(),NOW());

-- 30 件物品；每关 5 件，A-E 各一件，每个维度在 6 关中合计出现 2 次。
INSERT IGNORE INTO gkzh_nlld_option(option_id,round_id,option_label,option_name,option_desc,dimension_id,sort_order,status,create_time,update_time) VALUES
(1,1,'A','居民应急供水分配器','优先保障缺水居民的饮水需求',14,1,'0',NOW(),NOW()),
(2,1,'B','管道压力监测装置','监测管道压力，防止爆裂和二次事故',7,2,'0',NOW(),NOW()),
(3,1,'C','维修工作台','改善管道抢修时的照明、通风和作业条件',10,3,'0',NOW(),NOW()),
(4,1,'D','多人协作通信终端','同步维修进度，协调不同小组',12,4,'0',NOW(),NOW()),
(5,1,'E','任务补给箱','抢修期间，根据个人承担的工作任务和贡献等级，提供不同额度的即时物资补给，可领取额外的食物、饮水及个人工作用品',6,5,'0',NOW(),NOW()),
(6,2,'A','矿物采样与实验工具箱','收集不同类型的发光矿物，并利用工具设计简易实验装置，测试矿物在不同条件下的反应，探索其潜在用途。',3,1,'0',NOW(),NOW()),
(7,2,'B','岩层与矿物分析仪','分析矿物成分、研究异常发光现象，寻找未知能源的线索',1,2,'0',NOW(),NOW()),
(8,2,'C','独立导航与操作终端','自主规划探索路线，决定勘测顺序和具体行动方案',4,3,'0',NOW(),NOW()),
(9,2,'D','多区域探索任务地图','在洞穴勘测、海岸采样和山地调查等不同任务间灵活切换',5,4,'0',NOW(),NOW()),
(10,2,'E','任务成果记录仪','记录已完成的勘测目标、发现的线索和阶段性成果',2,5,'0',NOW(),NOW()),
(11,3,'A','建筑设计工作站','根据海岛地形设计建筑布局、观景平台和绿化方案，解决施工中的空间规划问题。',9,1,'0',NOW(),NOW()),
(12,3,'B','施工机械协同控制台','集中控制和协调挖掘机、起重机及运输设备的作业顺序，避免设备相互干扰，提高建筑材料运输和现场施工的效率。',11,2,'0',NOW(),NOW()),
(13,3,'C','重点工程评优申报材料','整理旅游中心的建设成果与设计亮点，提交海岛重点工程评审，争取项目表彰与专业认可。',8,3,'0',NOW(),NOW()),
(14,3,'D','施工环境监测与调控设备','监测施工区域的温度、湿度、粉尘和通风状况，并辅助改善作业环境，减少恶劣环境对施工效率和人员状态的影响。',10,4,'0',NOW(),NOW()),
(15,3,'E','施工排班表','在确保工程进度的前提下，允许团队协商安排施工时段和轮班，为个人休息与生活留出时间。',15,5,'0',NOW(),NOW()),
(16,4,'A','应急供电与储能设备','为实验室仪器和施工设备提供备用电力，避免因海岛供电不稳定导致施工中断或实验数据丢失。',7,1,'0',NOW(),NOW()),
(17,4,'B','专家远程会诊终端','连接科研基地的项目负责人和资深专家，及时获得实验室布局、设备安装和技术故障处理方面的指导。',13,2,'0',NOW(),NOW()),
(18,4,'C','科研设备采购卡','可兑换一定额度的采购资金，用于购买基地建设和设备调试所缺少的零件、线缆及安装材料，帮助团队补齐物资缺口，按时完成基地建设。',6,3,'0',NOW(),NOW()),
(19,4,'D','实验设备原型组装零件','利用现有零件组装和改造实验设备，解决标准设备无法适配海岛科研条件的问题，让基地尽快具备实验能力。',3,4,'0',NOW(),NOW()),
(20,4,'E','基地验收与调试检测仪','检查供电系统、实验仪器和基础设施是否达到运行标准，记录调试结果，帮助团队尽快通过验收并启动首轮实验。',2,5,'0',NOW(),NOW()),
(21,5,'A','社区应急物资配送车','将饮用水、食品和急救用品送到受灾居民家中及临时安置点，保障居民在修复期间的基本生活需求。',14,1,'0',NOW(),NOW()),
(22,5,'B','多人协同抢修工具车','配备道路清障、房屋修补和管道维修工具，供不同小组共同使用，方便成员配合完成紧急抢修。',12,2,'0',NOW(),NOW()),
(23,5,'C','设施抢修工具包','提供高性能管道修复器材、应急电路维修工具和关键设施专用零件，帮助团队优先修复供水、电力等重要设施。选择该物品后，你将负责其中一项关键设施的抢修任务，完成后有机会获得社区公开表彰。',8,3,'0',NOW(),NOW()),
(24,5,'D','多功能抢修设备','可根据任务快速更换清障、切割和管道维修模块，让团队灵活应对道路堵塞、房屋损坏和供水中断等不同问题。',5,4,'0',NOW(),NOW()),
(25,5,'E','灾后抢修任务指挥台','根据受灾程度安排抢修顺序，分配人员、车辆和工具，优先处理供水中断、道路阻塞等影响较大的问题。',11,5,'0',NOW(),NOW()),
(26,6,'A','舞台布景设计与搭建组件','用于设计并搭建舞台背景、装饰结构和主题展示区，让文化节现场更美观、更有特色。',9,1,'0',NOW(),NOW()),
(27,6,'B','舞台故障诊断终端','分析音响、灯光和播放系统的联动数据，帮助定位复杂故障原因、比较解决方案并验证效果，确保演出顺利进行。',1,2,'0',NOW(),NOW()),
(28,6,'C','节目创作与道具制作箱','提供手工材料、基础制作工具和可改造道具，供使用者自主设计演出环节、制作表演道具，完成分配的节目准备任务。',4,3,'0',NOW(),NOW()),
(29,6,'D','现场补给装备包','配备饮水、便携餐食和轻便休息用品，方便工作人员根据轮班安排补充体力、短暂休息，减少连续工作对个人生活节奏的影响。',15,4,'0',NOW(),NOW()),
(30,6,'E','总导演彩排批注与对讲设备','包含节目流程表、批注板和对讲设备，方便总导演现场指出问题、给出修改意见，并协调团队及时调整节目顺序和舞台走位。',13,5,'0',NOW(),NOW());

-- 汇总核对（只读）：每个维度应各出现 2 次。
-- SELECT d.dimension_name, COUNT(o.option_id) AS option_count
-- FROM gkzh_nlld_dimension d LEFT JOIN gkzh_nlld_option o ON o.dimension_id = d.dimension_id
-- GROUP BY d.dimension_id ORDER BY d.sort_order;
