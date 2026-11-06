-- 职场压力·骰子局（zcyltzj）基础结构、六个点数与初始题库；可重复执行。
-- 规范：所有业务表和字段必须提供中文 COMMENT，便于数据库运维和后续接手。
-- 本脚本只创建 gkzh_zcyltzj_* 表并写入初始内容，不修改其他游戏数据。

CREATE TABLE IF NOT EXISTS gkzh_zcyltzj_point (
  point_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '点数配置编号',
  point_no TINYINT NOT NULL COMMENT '骰子点数：1-6',
  point_name VARCHAR(50) NOT NULL COMMENT '点数名称，例如“早八生存”',
  theme VARCHAR(100) DEFAULT NULL COMMENT '该点数对应的压力主题',
  projection VARCHAR(255) DEFAULT NULL COMMENT '该点数对应的职场投射说明',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序，数值越小越靠前',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(point_id),
  UNIQUE KEY uk_zcyltzj_point_no(point_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='骰子局点数配置';

CREATE TABLE IF NOT EXISTS gkzh_zcyltzj_question (
  question_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '试题编号',
  point_no TINYINT NOT NULL COMMENT '所属骰子点数：1-6',
  question_no INT NOT NULL COMMENT '点数内题号，从1开始',
  content VARCHAR(500) NOT NULL COMMENT '题干',
  option_a VARCHAR(300) NOT NULL COMMENT 'A选项文字',
  option_b VARCHAR(300) NOT NULL COMMENT 'B选项文字',
  option_c VARCHAR(300) NOT NULL COMMENT 'C选项文字',
  option_d VARCHAR(300) NOT NULL COMMENT 'D选项文字',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序，数值越小越靠前',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(question_id),
  UNIQUE KEY uk_zcyltzj_question(point_no,question_no),
  KEY idx_zcyltzj_question_point(point_no,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='骰子局点数试题';

CREATE TABLE IF NOT EXISTS gkzh_zcyltzj_record (
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
  game_type VARCHAR(32) NOT NULL DEFAULT 'zcyltzj' COMMENT '游戏类型，固定为zcyltzj',
  status VARCHAR(16) NOT NULL DEFAULT 'participating' COMMENT '参与状态：participating进行中，finished已完成',
  stage VARCHAR(16) NOT NULL DEFAULT 'entered' COMMENT '业务阶段：entered已扫码，answering已掷骰子答题中，finished已完成',
  point_no TINYINT DEFAULT NULL COMMENT '本局掷出的骰子点数：1-6',
  point_name VARCHAR(50) DEFAULT NULL COMMENT '本局点数名称快照',
  question_snapshot_json JSON DEFAULT NULL COMMENT '本局点数、试题和选项快照JSON',
  answers_json JSON DEFAULT NULL COMMENT '学生答案JSON，保存每题题号与选项',
  red_count INT NOT NULL DEFAULT 0 COMMENT '红色（压力高）答案数量',
  green_count INT NOT NULL DEFAULT 0 COMMENT '绿色（压力低）答案数量',
  pressure_value INT DEFAULT NULL COMMENT '压力值（红色答案占比），0-100',
  color VARCHAR(10) DEFAULT NULL COMMENT '结果颜色：red红色，green绿色，yellow黄色',
  scan_time DATETIME NOT NULL COMMENT '扫码进入游戏时间',
  start_time DATETIME DEFAULT NULL COMMENT '掷骰子并开始答题的时间',
  finish_time DATETIME DEFAULT NULL COMMENT '完成答题的时间',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(record_id),
  UNIQUE KEY uk_zcyltzj_record_scope(school_id,instance_id,game_id,user_id),
  KEY idx_zcyltzj_record_instance(instance_id,game_id),
  KEY idx_zcyltzj_record_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='骰子局学生参与记录';

-- 六个骰子点数的名称、主题与职场投射；INSERT IGNORE 保留后台已修改的名称。
INSERT IGNORE INTO gkzh_zcyltzj_point(point_no,point_name,theme,projection,sort_order,status,create_time,update_time) VALUES
(1,'早八生存','早起通勤/固定班次','作息规律性、通勤耐受度、对固定班次的接受度',1,'0',NOW(),NOW()),
(2,'小组作业','团队搭便车/分工不均','搭便车容忍度、冲突处理、责任边界',2,'0',NOW(),NOW()),
(3,'DDL冲刺','多任务并行/极限交付','时间管理、抗压交付、多任务优先级',3,'0',NOW(),NOW()),
(4,'公开表达','汇报/答辩/被质询','当众表达、临场应变、向上沟通',4,'0',NOW(),NOW()),
(5,'期末连考','密集考核/连续高压','高压周期耐受、连续考核心态、工作强度边界',5,'0',NOW(),NOW()),
(6,'毕业去向','求职被拒/方向未定/同辈比较','求职韧性、职业决策、同辈压力、家庭期待冲突',6,'0',NOW(),NOW());

-- 每个点数 6 道题；INSERT IGNORE 以（点数, 题号）为唯一键，重复执行不会覆盖后台修改。
INSERT IGNORE INTO gkzh_zcyltzj_question(point_no,question_no,content,option_a,option_b,option_c,option_d,sort_order,status,create_time,update_time) VALUES
(1,1,'你能接受一周有几天需要早起赶早八？','0天','1-2天','3-4天','5天全早八',1,'0',NOW(),NOW()),
(1,2,'如果未来工作要求每天8点前到岗，你的感受是？','完全能接受','有点困但能撑','靠咖啡续命','直接崩溃',2,'0',NOW(),NOW()),
(1,3,'早八前一天晚上你通常几点睡？','11点前','12点左右','凌晨1点后','通宵',3,'0',NOW(),NOW()),
(1,4,'你能接受通勤单程多久？','30分钟内','1小时','1.5小时','2小时以上',4,'0',NOW(),NOW()),
(1,5,'如果工作经常临时调早班，你会？','提前调整作息','偶尔抱怨但配合','很抗拒','直接考虑换工作',5,'0',NOW(),NOW()),
(1,6,'早八和晚睡冲突时，你更倾向？','早睡保早八','偶尔熬夜','熬夜但早起','选晚睡放弃早八',6,'0',NOW(),NOW()),

(2,1,'队友划水比例到多少你能接受？','0%','20%','50%','80%以上',1,'0',NOW(),NOW()),
(2,2,'如果同事全程不干活，你会？','沟通分工','自己多做点','找领导反映','直接摆烂',2,'0',NOW(),NOW()),
(2,3,'你更接受哪种团队模式？','明确分工各做各','一起讨论共同做','我兜底但功劳公平','谁急谁做',3,'0',NOW(),NOW()),
(2,4,'团队讨论有人一直跑题，你会？','拉回正题','忍着听完','私下提醒','直接退出讨论',4,'0',NOW(),NOW()),
(2,5,'你能接受团队项目占绩效考核多大比重？','10%以内','20%-30%','40%-50%','60%以上',5,'0',NOW(),NOW()),
(2,6,'项目截止前队友失联，你会？','提前确认进度','自己补上','群里催到回','放弃这部分',6,'0',NOW(),NOW()),

(3,1,'你通常提前多久开始赶DDL？','一周以上','提前三天','提前一天','截止前两小时',1,'0',NOW(),NOW()),
(3,2,'如果领导前一天才派活，你的感受是？','完全能接受','有点慌但能完成','极度焦虑','直接拒绝',2,'0',NOW(),NOW()),
(3,3,'你能接受一学期/一季度有多少次“极限赶工”？','0次','1-2次','3-5次','越多越刺激',3,'0',NOW(),NOW()),
(3,4,'多个任务撞在同一天，你会？','提前排优先级','按截止时间赶','先做简单的','哪个都不想做',4,'0',NOW(),NOW()),
(3,5,'赶工时有人约你出去玩，你会？','做完再去','拒绝约局','玩完回来通宵','直接去玩',5,'0',NOW(),NOW()),
(3,6,'交付后发现有错，你的反应是？','检查过就行','有点懊恼','想撤回重交','焦虑到睡不着',6,'0',NOW(),NOW()),

(4,1,'你能接受在多大人数面前发言？','小组5人内','全班30人','百人报告厅','完全不想上台',1,'0',NOW(),NOW()),
(4,2,'如果工作要定期向领导汇报，你的感受是？','从容表达','紧张但能应对','大脑空白','想原地消失',2,'0',NOW(),NOW()),
(4,3,'汇报被连环追问，你会？','从容回答','紧张但能应对','大脑空白','想逃避',3,'0',NOW(),NOW()),
(4,4,'课堂/会议被突然点名，你会？','直接回答','想几秒再说','希望别人救场','低头装没听见',4,'0',NOW(),NOW()),
(4,5,'你能接受每季度几次公开汇报？','0次','1-2次','3-5次','越多越锻炼',5,'0',NOW(),NOW()),
(4,6,'演讲时忘词了，你会？','看提纲继续','停顿一下接上','慌乱跳过','直接结束',6,'0',NOW(),NOW()),

(5,1,'你能接受几天内考完所有期末？','两周分散考','一周考完','三天考完','一天连考三门',1,'0',NOW(),NOW()),
(5,2,'如果工作进入连续考核周，你的状态是？','提前准备稳过','熬夜突击','只求达标','直接想请假',2,'0',NOW(),NOW()),
(5,3,'高压周期你的睡眠是？','正常7小时','5-6小时','3-4小时','通宵',3,'0',NOW(),NOW()),
(5,4,'同时有三项考核要交，你会？','提前排期','按截止顺序做','先做最急的','直接摆烂一项',4,'0',NOW(),NOW()),
(5,5,'一项考核结果不理想，你会？','调整心态做下一项','难过但继续','影响后面发挥','不想继续',5,'0',NOW(),NOW()),
(5,6,'你能接受高压周期每天工作几小时？','8小时以内','9-10小时','11-12小时','12小时以上',6,'0',NOW(),NOW()),

(6,1,'你能接受毕业时职业方向还没定吗？','完全接受，慢慢来','有点焦虑但正常','非常焦虑','完全不能接受',1,'0',NOW(),NOW()),
(6,2,'投简历连续被拒，你会？','继续投','调整方向再投','怀疑自己','不想再投',2,'0',NOW(),NOW()),
(6,3,'身边同学都定了去向，你还没定，你的感受是？','按自己节奏','有点压力','很慌','彻底崩溃',3,'0',NOW(),NOW()),
(6,4,'家人催你考公/考研/进大厂，你会？','按自己规划沟通','听听建议再定','应付着拖','直接吵起来',4,'0',NOW(),NOW()),
(6,5,'你能接受第一份工作不对口吗？','完全接受','可以试试','尽量对口','必须对口',5,'0',NOW(),NOW()),
(6,6,'毕业前一个月还没offer，你会？','继续投继续面','降低预期','焦虑到失眠','放弃找工作',6,'0',NOW(),NOW());
