-- zycck 只保留 Excel Sheet1 B4:I59 中的 56 条正式职业。
-- 下列名称只出现在题目选项中，不创建独立职业；题目仍保留中文选项文本。
-- MySQL 5.7 可执行。删除前创建备份，参与记录及历史题目快照不删除。

SET @backup_table := CONCAT('gkzh_zycck_career_question_before_v29_', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'));
SET @backup_sql := CONCAT(
  'CREATE TABLE `', @backup_table, '` AS SELECT * FROM gkzh_zycck_career_question'
);
PREPARE backup_stmt FROM @backup_sql;
EXECUTE backup_stmt;
DEALLOCATE PREPARE backup_stmt;

-- 先解除指向仅选项职业的内部 ID；中文选项 option_a/option_b/option_c/option_d 保留。
UPDATE gkzh_zycck_career_question q
LEFT JOIN gkzh_zycck_career_question a ON a.career_question_id = q.option_a_career_id
LEFT JOIN gkzh_zycck_career_question b ON b.career_question_id = q.option_b_career_id
LEFT JOIN gkzh_zycck_career_question c ON c.career_question_id = q.option_c_career_id
LEFT JOIN gkzh_zycck_career_question d ON d.career_question_id = q.option_d_career_id
SET q.option_a_career_id = IF(a.career_name IN ('无人机飞手','无人机应用工程师','无人机航拍师','旅行博主','旅游产品运营','宠物训练师','宠物美容师','宠物医生','旅行策划师','户外教练','导游','游戏主播','健身教练','健康顾问'), NULL, q.option_a_career_id),
    q.option_b_career_id = IF(b.career_name IN ('无人机飞手','无人机应用工程师','无人机航拍师','旅行博主','旅游产品运营','宠物训练师','宠物美容师','宠物医生','旅行策划师','户外教练','导游','游戏主播','健身教练','健康顾问'), NULL, q.option_b_career_id),
    q.option_c_career_id = IF(c.career_name IN ('无人机飞手','无人机应用工程师','无人机航拍师','旅行博主','旅游产品运营','宠物训练师','宠物美容师','宠物医生','旅行策划师','户外教练','导游','游戏主播','健身教练','健康顾问'), NULL, q.option_c_career_id),
    q.option_d_career_id = IF(d.career_name IN ('无人机飞手','无人机应用工程师','无人机航拍师','旅行博主','旅游产品运营','宠物训练师','宠物美容师','宠物医生','旅行策划师','户外教练','导游','游戏主播','健身教练','健康顾问'), NULL, q.option_d_career_id);

DELETE FROM gkzh_zycck_career_question
WHERE career_name IN (
  '无人机飞手','无人机应用工程师','无人机航拍师',
  '旅行博主','旅游产品运营','宠物训练师','宠物美容师','宠物医生',
  '旅行策划师','户外教练','导游','游戏主播','健身教练','健康顾问'
);

-- 校验：正式职业数量必须为 10、12、10、12、12，题目总数为 11。
SELECT c.code,
       COUNT(q.career_question_id) AS career_count,
       SUM(q.has_question='1') AS question_count,
       SUM(q.has_question='0') AS exploration_count
FROM gkzh_zycck_category c
LEFT JOIN gkzh_zycck_career_question q
  ON q.category_id=c.category_id AND q.status='0'
WHERE c.code IN ('digital_product','digital_content','digital_marketing','professional_service','freelance_skill')
GROUP BY c.code
ORDER BY c.sort_order;
