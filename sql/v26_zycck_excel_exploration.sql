-- 按 Excel 最终结构调整：有 A-D 选项的记录为试题，没有选项的记录为探索内容。
-- 试题职业保留在职业库中但不进入探索墙；各大类保留 Excel 中的实际职业数量。
UPDATE gkzh_zycck_career_question
SET has_question='0', draw_candidate='0'
WHERE option_a IS NULL AND option_b IS NULL AND option_c IS NULL AND option_d IS NULL;

UPDATE gkzh_zycck_category SET draw_mode='fixed', update_time=NOW() WHERE code='digital_product';
UPDATE gkzh_zycck_category SET draw_mode='random', update_time=NOW() WHERE code='digital_content';
UPDATE gkzh_zycck_category SET draw_mode='fixed', update_time=NOW() WHERE code='digital_marketing';
UPDATE gkzh_zycck_category SET draw_mode='random', update_time=NOW() WHERE code='professional_service';
UPDATE gkzh_zycck_category SET draw_mode='random', update_time=NOW() WHERE code='freelance_skill';

INSERT INTO gkzh_zycck_career_question(category_id,career_name,has_question,one_line_intro,main_work,day_example,why_exists,draw_candidate,sort_order,status,create_time,update_time)
SELECT c.category_id,'数字非遗传承人','0','用数字方式让传统文化走近年轻人','将传统技艺、民俗文化等内容通过短视频、直播、数字展示等方式传播，让更多年轻人了解和关注传统文化。','文化整理｜整理传统文化素材\n内容创作｜设计数字传播内容\n线上分享｜通过平台传播文化\n互动交流｜与网友交流传统技艺','传统文化需要适应新的传播方式，短视频、直播和数字展示让非遗能够触达更多年轻人，因此传统技艺与数字传播开始结合。','0',11,'0',NOW(),NOW()
FROM gkzh_zycck_category c WHERE c.code='digital_content'
ON DUPLICATE KEY UPDATE has_question='0',one_line_intro=VALUES(one_line_intro),main_work=VALUES(main_work),day_example=VALUES(day_example),why_exists=VALUES(why_exists),draw_candidate='0',sort_order=11,status='0',update_time=NOW();

INSERT INTO gkzh_zycck_career_question(category_id,career_name,has_question,one_line_intro,main_work,day_example,why_exists,draw_candidate,sort_order,status,create_time,update_time)
SELECT c.category_id,'电竞解说','0','用专业表达讲述精彩的电竞比赛','在电竞比赛过程中介绍比赛进程、分析选手表现和讲解精彩瞬间，用生动的语言帮助观众看懂并享受比赛。','赛前准备｜了解比赛选手信息\n比赛解说｜实时讲解比赛过程\n局势分析｜分析比赛战术变化\n赛后总结｜回顾精彩比赛内容','电竞比赛越来越专业，观众不仅想看比赛，还希望有人解释战术、分析局势，因此逐渐形成了类似体育解说的专业工作。','0',12,'0',NOW(),NOW()
FROM gkzh_zycck_category c WHERE c.code='digital_content'
ON DUPLICATE KEY UPDATE has_question='0',one_line_intro=VALUES(one_line_intro),main_work=VALUES(main_work),day_example=VALUES(day_example),why_exists=VALUES(why_exists),draw_candidate='0',sort_order=12,status='0',update_time=NOW();

INSERT INTO gkzh_zycck_career_question(category_id,career_name,has_question,one_line_intro,main_work,day_example,why_exists,draw_candidate,sort_order,status,create_time,update_time)
SELECT c.category_id,'技术经纪人','0','帮助技术成果找到合适的应用和买家','在技术成果与企业需求之间建立联系，帮助寻找合作机会、沟通双方需求，推动技术成果实现应用和转化。','技术寻找｜了解可转化技术\n需求对接｜寻找企业实际需求\n合作沟通｜协调双方合作方案\n项目跟进｜推动技术合作落地','科研成果和企业需求之间存在信息差，很多技术并不能自动找到应用场景，因此需要有人帮助技术、企业和市场建立联系。','0',11,'0',NOW(),NOW()
FROM gkzh_zycck_category c WHERE c.code='professional_service'
ON DUPLICATE KEY UPDATE has_question='0',one_line_intro=VALUES(one_line_intro),main_work=VALUES(main_work),day_example=VALUES(day_example),why_exists=VALUES(why_exists),draw_candidate='0',sort_order=11,status='0',update_time=NOW();

INSERT INTO gkzh_zycck_career_question(category_id,career_name,has_question,one_line_intro,main_work,day_example,why_exists,draw_candidate,sort_order,status,create_time,update_time)
SELECT c.category_id,'造型师','0','根据个人特点打造合适的整体形象','根据个人特点、场合和需求搭配服装、发型、妆容等整体形象，帮助客户呈现更加合适的个人风格。','需求沟通｜了解客户形象需求\n风格设计｜确定整体造型方向\n形象打造｜完成服装妆发搭配\n效果调整｜根据现场效果优化','人们越来越重视个人形象和个性表达，服装、发型和妆容需要整体协调，因此出现了帮助个人打造形象的专业服务。','0',12,'0',NOW(),NOW()
FROM gkzh_zycck_category c WHERE c.code='professional_service'
ON DUPLICATE KEY UPDATE has_question='0',one_line_intro=VALUES(one_line_intro),main_work=VALUES(main_work),day_example=VALUES(day_example),why_exists=VALUES(why_exists),draw_candidate='0',sort_order=12,status='0',update_time=NOW();

INSERT INTO gkzh_zycck_career_question(category_id,career_name,has_question,one_line_intro,main_work,day_example,why_exists,draw_candidate,sort_order,status,create_time,update_time)
SELECT c.category_id,'探店达人','0','体验真实消费场景并分享给更多人','亲自体验餐饮、娱乐、购物等消费场所，通过图文、短视频等方式分享真实体验，为消费者提供参考。','店铺选择｜寻找值得体验的店铺\n现场体验｜亲自体验消费项目\n内容拍摄｜记录真实消费过程\n分享发布｜发布体验分享内容','消费者在选择餐厅和商家时越来越依赖网络评价，商家也希望获得真实体验传播，因此个人体验分享逐渐成为一种商业内容形式。','0',11,'0',NOW(),NOW()
FROM gkzh_zycck_category c WHERE c.code='freelance_skill'
ON DUPLICATE KEY UPDATE has_question='0',one_line_intro=VALUES(one_line_intro),main_work=VALUES(main_work),day_example=VALUES(day_example),why_exists=VALUES(why_exists),draw_candidate='0',sort_order=11,status='0',update_time=NOW();

INSERT INTO gkzh_zycck_career_question(category_id,career_name,has_question,one_line_intro,main_work,day_example,why_exists,draw_candidate,sort_order,status,create_time,update_time)
SELECT c.category_id,'手工艺创作者','0','用双手把创意变成独特的手工作品','运用编织、陶艺、木作等手工技能创作具有特色的作品，并通过平台展示、销售或接受个性化订单。','灵感构思｜寻找作品创作灵感\n材料准备｜准备创作所需材料\n手工制作｜完成手工作品制作\n作品展示｜展示或销售原创作品','个性化消费和文化消费不断增长，人们愿意购买具有独特设计和手工特色的产品，因此个人手工技能也能转化为商业机会。','0',12,'0',NOW(),NOW()
FROM gkzh_zycck_category c WHERE c.code='freelance_skill'
ON DUPLICATE KEY UPDATE has_question='0',one_line_intro=VALUES(one_line_intro),main_work=VALUES(main_work),day_example=VALUES(day_example),why_exists=VALUES(why_exists),draw_candidate='0',sort_order=12,status='0',update_time=NOW();

-- 按选项中文名称回填 Excel 正式职业的引用；选项仅有中文而没有正式职业行时允许 ID 为空。
UPDATE gkzh_zycck_career_question q JOIN gkzh_zycck_career_question c ON c.career_name=q.option_a AND c.status='0'
SET q.option_a_career_id=c.career_question_id WHERE q.has_question='1' AND q.option_a IS NOT NULL;
UPDATE gkzh_zycck_career_question q JOIN gkzh_zycck_career_question c ON c.career_name=q.option_b AND c.status='0'
SET q.option_b_career_id=c.career_question_id WHERE q.has_question='1' AND q.option_b IS NOT NULL;
UPDATE gkzh_zycck_career_question q JOIN gkzh_zycck_career_question c ON c.career_name=q.option_c AND c.status='0'
SET q.option_c_career_id=c.career_question_id WHERE q.has_question='1' AND q.option_c IS NOT NULL;
UPDATE gkzh_zycck_career_question q JOIN gkzh_zycck_career_question c ON c.career_name=q.option_d AND c.status='0'
SET q.option_d_career_id=c.career_question_id WHERE q.has_question='1' AND q.option_d IS NOT NULL;

-- 删除仅选项职业并校验 56 条正式内容，需要继续执行 v29_zycck_keep_excel_careers_only.sql。
SELECT c.code,
       SUM(q.status='0') AS career_count,
       SUM(q.status='0' AND q.has_question='1') AS question_count,
       SUM(q.status='0' AND q.has_question='0') AS exploration_count,
       SUM(q.status='0' AND q.has_question='1' AND q.draw_candidate='1') AS candidate_count
FROM gkzh_zycck_category c LEFT JOIN gkzh_zycck_career_question q ON q.category_id=c.category_id
WHERE c.code IN ('digital_product','digital_content','digital_marketing','professional_service','freelance_skill')
GROUP BY c.code ORDER BY c.sort_order;
