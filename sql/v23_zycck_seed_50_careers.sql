-- zycck 首批题库：五个职业大类各 10 个职业/题目，共 50 组。
-- 题目与职业一体化；每个职业只对应一条题目。脚本可重复执行。
DROP TEMPORARY TABLE IF EXISTS tmp_zycck_seed;
CREATE TEMPORARY TABLE tmp_zycck_seed(category_code VARCHAR(32) NOT NULL, career_name VARCHAR(100) NOT NULL, intro VARCHAR(500) NOT NULL);
INSERT INTO tmp_zycck_seed VALUES
('technology','人工智能工程师','设计、训练和部署人工智能模型。'),('technology','软件开发工程师','负责软件系统的设计、编码、测试和维护。'),('technology','数据分析师','通过数据处理和分析支持业务决策。'),('technology','产品经理','围绕用户需求规划产品并协调团队交付。'),('technology','网络安全工程师','保护系统、网络和数据免受安全威胁。'),('technology','芯片设计工程师','完成集成电路的架构、设计与验证。'),('technology','机器人研发工程师','研发机器人结构、控制和智能算法。'),('technology','云计算架构师','规划云平台架构并保障系统稳定运行。'),('technology','交互设计师','设计数字产品的交互流程和使用体验。'),('technology','生物技术研发员','开展生物技术实验并推动成果转化。'),
('health','临床医生','通过问诊、检查和治疗守护患者健康。'),('health','注册护士','执行护理计划并提供连续的临床照护。'),('health','药剂师','审核处方并指导公众安全合理用药。'),('health','康复治疗师','通过训练和治疗帮助患者恢复功能。'),('health','公共卫生专员','开展疾病预防、健康教育和人群监测。'),('health','营养师','根据个体情况制定科学的膳食建议。'),('health','医学影像技师','操作影像设备协助医生完成检查诊断。'),('health','心理咨询师','运用专业方法支持来访者改善心理状态。'),('health','兽医','诊断和治疗动物疾病并开展防疫工作。'),('health','医疗项目经理','协调医疗项目资源、流程和交付质量。'),
('culture','新闻记者','采访事实并通过文字、图片或视频传播信息。'),('culture','出版编辑','策划、审校和完善书稿及出版内容。'),('culture','纪录片导演','通过影像记录真实人物、事件和社会议题。'),('culture','博物馆策展人','围绕主题组织展览并讲述文化内容。'),('culture','高校教师','开展教学、科研和学生培养工作。'),('culture','翻译','在不同语言之间准确传递信息和文化。'),('culture','品牌文案','为品牌创作清晰、有感染力的传播内容。'),('culture','摄影师','使用影像记录人物、空间和重要瞬间。'),('culture','文化遗产保护员','运用专业方法保护和修复文化遗产。'),('culture','出版发行经理','负责出版物的市场推广和渠道运营。'),
('management','企业经理','通过规划、决策和协作推动组织目标实现。'),('management','人力资源经理','负责人才招聘、培养、激励和组织发展。'),('management','项目经理','管理项目范围、进度、成本和交付质量。'),('management','综合管理类公务员','承担机关综合协调、文秘会务和政策研究。'),('management','金融分析师','通过研究、估值和报告支持金融决策。'),('management','市场营销经理','制定营销策略并推动品牌和业务增长。'),('management','供应链经理','统筹采购、生产、物流和库存协同。'),('management','城市规划师','研究城市发展并编制空间规划方案。'),('management','政策研究员','围绕公共议题开展调查、分析和政策建议。'),('management','客户成功经理','帮助客户持续获得产品和服务价值。'),
('nature','农业技术推广员','把农业技术转化为田间可执行的方案。'),('nature','景观设计师','设计公园、社区和公共空间的景观环境。'),('nature','环境工程师','运用工程技术解决污染治理和环境保护问题。'),('nature','食品研发工程师','研发安全、美味且符合需求的新食品。'),('nature','林业工程师','开展森林培育、保护和资源管理工作。'),('nature','海洋科学研究员','研究海洋环境、资源和生态变化。'),('nature','生态保护员','开展自然保护区巡护、监测和修复。'),('nature','动物科学技术员','服务畜牧养殖、动物营养和繁育管理。'),('nature','花艺设计师','运用植物和色彩设计花艺作品与空间。'),('nature','户外活动指导员','组织户外活动并保障参与者安全和体验。');

INSERT INTO gkzh_zycck_career_question(category_id,career_name,one_line_intro,main_work,day_example,why_exists,option_a,option_b,option_c,option_d,option_a_career_id,option_b_career_id,option_c_career_id,option_d_career_id,correct_option_key,explanation,draw_candidate,sort_order,status,create_time,update_time)
SELECT c.category_id,s.career_name,s.intro,s.intro,CONCAT('围绕',s.career_name,'的典型任务开展工作。'),CONCAT('社会需要',s.career_name,'解决真实问题。'),s.career_name,
 (SELECT s2.career_name FROM tmp_zycck_seed s2 WHERE s2.category_code=s.category_code ORDER BY s2.career_name LIMIT 1 OFFSET 1),
 (SELECT s3.career_name FROM tmp_zycck_seed s3 WHERE s3.category_code=s.category_code ORDER BY s3.career_name LIMIT 1 OFFSET 2),
 (SELECT s4.career_name FROM tmp_zycck_seed s4 WHERE s4.category_code=s.category_code ORDER BY s4.career_name LIMIT 1 OFFSET 3),
 0,0,0,0,
 'A',CONCAT('正确答案对应的职业是：',s.career_name,'。'), '0',1,'0',NOW(),NOW()
FROM tmp_zycck_seed s JOIN gkzh_zycck_category c ON c.code=s.category_code
ON DUPLICATE KEY UPDATE one_line_intro=VALUES(one_line_intro),main_work=VALUES(main_work),day_example=VALUES(day_example),why_exists=VALUES(why_exists),option_a=VALUES(option_a),option_b=VALUES(option_b),option_c=VALUES(option_c),option_d=VALUES(option_d),correct_option_key='A',explanation=VALUES(explanation),status='0',update_time=NOW();

UPDATE gkzh_zycck_career_question q JOIN (SELECT category_id,MIN(career_question_id) first_id FROM gkzh_zycck_career_question GROUP BY category_id) x ON x.category_id=q.category_id SET q.draw_candidate=IF(q.career_question_id=x.first_id,'1','0'),q.update_time=NOW();

UPDATE gkzh_zycck_career_question q JOIN gkzh_zycck_category c ON c.category_id=q.category_id
JOIN gkzh_zycck_career_question a ON a.category_id=q.category_id AND a.career_name=q.career_name
JOIN gkzh_zycck_career_question b ON b.category_id=q.category_id AND b.career_name=(SELECT s2.career_name FROM tmp_zycck_seed s2 WHERE s2.category_code=c.code ORDER BY s2.career_name LIMIT 1 OFFSET 1)
JOIN gkzh_zycck_career_question d ON d.category_id=q.category_id AND d.career_name=(SELECT s3.career_name FROM tmp_zycck_seed s3 WHERE s3.category_code=c.code ORDER BY s3.career_name LIMIT 1 OFFSET 2)
JOIN gkzh_zycck_career_question e ON e.category_id=q.category_id AND e.career_name=(SELECT s4.career_name FROM tmp_zycck_seed s4 WHERE s4.category_code=c.code ORDER BY s4.career_name LIMIT 1 OFFSET 3)
SET q.option_a_career_id=a.career_question_id,q.option_b_career_id=b.career_question_id,q.option_c_career_id=d.career_question_id,q.option_d_career_id=e.career_question_id,q.update_time=NOW();
