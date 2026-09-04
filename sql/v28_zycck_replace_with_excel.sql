-- zycck Excel 全量重建脚本。
-- 适用场景：当前职业/题库不是 Excel 内容，需要清空后重新导入。
-- 执行顺序：先执行本文件，再依次执行 v25_zycck_excel_content.sql、v26_zycck_excel_exploration.sql。
-- 本文件不删除参与记录；职业题库删除前请先备份。

SET @backup_table := CONCAT('gkzh_zycck_career_question_backup_', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'));
SET @backup_sql := CONCAT(
  'CREATE TABLE `', @backup_table, '` AS SELECT * FROM gkzh_zycck_career_question'
);
PREPARE backup_stmt FROM @backup_sql;
EXECUTE backup_stmt;
DEALLOCATE PREPARE backup_stmt;

DELETE FROM gkzh_zycck_career_question;

SELECT DATABASE() AS current_database,
       '职业题库已清空，请继续执行 v25、v26 导入 Excel 内容' AS message;
