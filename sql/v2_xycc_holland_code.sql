CREATE TABLE IF NOT EXISTS `xycc_holland_code` (
  `code`          varchar(1)    NOT NULL COMMENT '霍兰德编码：R/I/A/S/E/C',
  `name`          varchar(20)   NOT NULL COMMENT '类型名称',
  `full_name`     varchar(50)   DEFAULT NULL COMMENT '英文名称',
  `summary`       text          COMMENT '特点解释',
  `traits`        varchar(255)  DEFAULT NULL COMMENT '核心特点',
  `life`          text          COMMENT '生活事例',
  `work`          varchar(500)  DEFAULT NULL COMMENT '典型工作',
  `sort_order`    int(11)       DEFAULT 0 COMMENT '排序',
  `status`        char(1)       DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_time`   datetime      DEFAULT NULL COMMENT '创建时间',
  `update_time`   datetime      DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿橱窗霍兰德编码解释表';

INSERT INTO `xycc_holland_code`
  (`code`, `name`, `full_name`, `summary`, `traits`, `life`, `work`, `sort_order`, `status`, `create_time`)
VALUES
  ('R', '技能型', 'Realistic', '喜欢操作机械、修理仪器等需要技术的活动；喜欢用实际行动代替言语表达，重视现在胜于重视未来；喜欢具体明确、需要动手操作的工作环境；喜欢从事机械、电子、建筑、农事等方面的工作。通常情绪稳定、忍耐力强，给人以诚实、谦和、踏实的印象。', '情绪稳定、忍耐力强、诚实、谦和、踏实', '喜欢修理各种家用电器、设备等；喜欢上实验课、劳技课等，因为可以操作实验设备、仪器；喜欢玩各种组装、拼装类玩具；喜欢打理花草、制作家具、缝制衣物、烹饪；喜欢户外运动、体育活动等。', '工程、机械、制造、建筑、运动、户外作业、技术维护', 1, '0', NOW()),
  ('I', '研究型', 'Investigative', '喜欢研究且解决抽象的问题，喜欢运用心智能力去观察、分析、推理，喜欢与符号、概念、文字、抽象思考有关的活动；喜欢从事理化、生物等需要动脑的研究性工作；在工作中表现出优异的科学能力。通常个性独立、温和、谨慎、理性、有逻辑。', '独立、温和、谨慎、理性、有逻辑', '脑袋里常常有各种各样的“为什么”；与各类娱乐杂志相比，更喜欢阅读科学、哲学等知识类书籍、材料；喜欢参加在脑力上更有挑战的活动和游戏，如下棋、推理游戏；平时可能话不多，但不人云亦云。', '科研、实验、数据分析、医疗、技术研发、学术研究', 2, '0', NOW()),
  ('A', '艺术型', 'Artistic', '喜欢借助文字、声音、动作或色彩来表达内心想法和对美的感受；喜欢自由自在的、富有创意的工作环境，对美的事物具有敏锐的直觉，个性热情、冲动，有丰富的想象力和创造力，乐于独立思考和创作。', '热情、有想象力、有创造力、敏锐、乐于创作', '喜欢欣赏各种形式的艺术作品；乐意参加文艺演出；没事写写自娱自乐的小文章、拍拍各类照片、听听音乐会、看看画展等。', '设计、艺术、音乐、写作、影视、广告创意、表演', 3, '0', NOW()),
  ('S', '社会型', 'Social', '喜欢从事与人接触的活动。对人慷慨、仁慈，喜欢倾听和关心别人，能敏锐觉察别人的感受。在团队中，乐于与人合作，喜欢和大家一起完成工作。他们关心人胜于关心物，关心他人的福祉；喜欢助人的工作，个性温暖、友善、乐于助人，容易与人相处。', '温暖、友善、乐于助人、善于合作、有同理心', '小时候喜欢扮演老师，常常教育、指导其他的小朋友；喜欢参加公益服务类活动，在帮助他人的过程中感到很快乐；喜欢和谐友善的工作和生活环境，同学中有争执或冲突，往往充当“和事佬”。', '教育、咨询、社会工作、医疗护理、培训、客户服务', 4, '0', NOW()),
  ('E', '经营型', 'Enterprising', '喜欢以言语说服或影响他人，领导他人；喜欢销售、管理、法律、政治方面的工作；做事有组织、有计划，喜欢立刻采取行动，领导人们达成工作目标。通常精力充沛，生活紧凑，善于表达，希望拥有权力。', '精力充沛、善于表达、有组织、有计划、有领导力', '从小担任各种学生干部，在各项活动中表现出色；喜欢看金融学电视和书籍，对商业活动很感兴趣；大学后对创业感兴趣，与小伙伴们组建团队开展创业实践；热衷参加演讲比赛、辩论赛等活动；喜欢做销售类的兼职。', '管理、销售、市场、创业、商务、公关、项目运营', 5, '0', NOW()),
  ('C', '常规型', 'Conventional', '喜欢秩序、规则、数据和细节，偏好结构化、稳定、清晰流程的工作。', '严谨、有条理、可靠、注重细节', '喜欢按计划完成任务，善于整理资料、记录数据，喜欢稳定、清晰、可预期的工作环境。', '财务、行政、数据管理、档案、审计、运营支持', 6, '0', NOW())
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `full_name` = VALUES(`full_name`),
  `summary` = VALUES(`summary`),
  `traits` = VALUES(`traits`),
  `life` = VALUES(`life`),
  `work` = VALUES(`work`),
  `sort_order` = VALUES(`sort_order`),
  `status` = VALUES(`status`),
  `update_time` = NOW();
