-- zycck 既有基础表中文注释补全（MySQL 5.7+，可重复执行）。
-- 说明：MODIFY COLUMN 必须完整保留字段类型、空值和默认值，仅补充中文 COMMENT。

ALTER TABLE gkzh_zycck_category
  COMMENT = '职业猜猜看职业大类',
  MODIFY COLUMN category_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '职业大类编号',
  MODIFY COLUMN code VARCHAR(32) NOT NULL COMMENT '职业大类唯一编码',
  MODIFY COLUMN name VARCHAR(64) NOT NULL COMMENT '职业大类名称',
  MODIFY COLUMN description VARCHAR(1000) DEFAULT NULL COMMENT '职业大类说明',
  MODIFY COLUMN draw_mode VARCHAR(16) NOT NULL DEFAULT 'fixed' COMMENT '抽题模式：fixed固定抽取，random随机抽取',
  MODIFY COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序，数值越小越靠前',
  MODIFY COLUMN status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常，1停用',
  MODIFY COLUMN create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME DEFAULT NULL COMMENT '最后更新时间';

ALTER TABLE gkzh_zycck_career_question
  COMMENT = '职业猜猜看职业资料及竞猜题目',
  MODIFY COLUMN career_question_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '职业资料编号，同时作为题目编号',
  MODIFY COLUMN category_id BIGINT NOT NULL COMMENT '所属职业大类编号',
  MODIFY COLUMN career_name VARCHAR(100) NOT NULL COMMENT '职业名称',
  MODIFY COLUMN has_question CHAR(1) NOT NULL DEFAULT '1' COMMENT '是否有对应竞猜题：1有题目，0仅探索职业',
  MODIFY COLUMN one_line_intro VARCHAR(500) DEFAULT NULL COMMENT '职业一句话介绍',
  MODIFY COLUMN main_work VARCHAR(2000) DEFAULT NULL COMMENT '职业主要工作内容',
  MODIFY COLUMN day_example VARCHAR(2000) DEFAULT NULL COMMENT '职业一天工作示例',
  MODIFY COLUMN why_exists VARCHAR(2000) DEFAULT NULL COMMENT '职业产生或存在的原因',
  MODIFY COLUMN career_image_url VARCHAR(500) DEFAULT NULL COMMENT '职业展示图片地址',
  MODIFY COLUMN question_image_url VARCHAR(500) DEFAULT NULL COMMENT '竞猜题目场景图片地址',
  MODIFY COLUMN option_a VARCHAR(200) DEFAULT NULL COMMENT '竞猜题A选项文字',
  MODIFY COLUMN option_b VARCHAR(200) DEFAULT NULL COMMENT '竞猜题B选项文字',
  MODIFY COLUMN option_c VARCHAR(200) DEFAULT NULL COMMENT '竞猜题C选项文字',
  MODIFY COLUMN option_d VARCHAR(200) DEFAULT NULL COMMENT '竞猜题D选项文字',
  MODIFY COLUMN option_a_career_id BIGINT DEFAULT NULL COMMENT 'A选项关联的职业资料编号',
  MODIFY COLUMN option_b_career_id BIGINT DEFAULT NULL COMMENT 'B选项关联的职业资料编号',
  MODIFY COLUMN option_c_career_id BIGINT DEFAULT NULL COMMENT 'C选项关联的职业资料编号',
  MODIFY COLUMN option_d_career_id BIGINT DEFAULT NULL COMMENT 'D选项关联的职业资料编号',
  MODIFY COLUMN correct_option_key CHAR(1) DEFAULT NULL COMMENT '正确选项标识：A、B、C或D',
  MODIFY COLUMN explanation VARCHAR(2000) DEFAULT NULL COMMENT '答案解析说明',
  MODIFY COLUMN draw_candidate CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否进入抽题候选池：0否，1是',
  MODIFY COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT '同一大类中的显示顺序',
  MODIFY COLUMN status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常，1停用',
  MODIFY COLUMN create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME DEFAULT NULL COMMENT '最后更新时间';

ALTER TABLE gkzh_zycck_record
  COMMENT = '职业猜猜看学生参与记录',
  MODIFY COLUMN record_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '参与记录编号',
  MODIFY COLUMN school_id BIGINT NOT NULL COMMENT '学校编号',
  MODIFY COLUMN instance_id BIGINT NOT NULL COMMENT '活动周实例编号',
  MODIFY COLUMN game_id BIGINT NOT NULL COMMENT '活动游戏编号',
  MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '平台用户编号',
  MODIFY COLUMN student_id BIGINT DEFAULT NULL COMMENT '学生编号',
  MODIFY COLUMN department_id BIGINT DEFAULT NULL COMMENT '院系或专业部门编号',
  MODIFY COLUMN major VARCHAR(100) DEFAULT NULL COMMENT '专业名称快照',
  MODIFY COLUMN gender CHAR(1) DEFAULT NULL COMMENT '性别快照：0男，1女，其他未知',
  MODIFY COLUMN game_type VARCHAR(32) NOT NULL DEFAULT 'zycck' COMMENT '游戏类型，固定为zycck',
  MODIFY COLUMN status VARCHAR(16) NOT NULL DEFAULT 'participating' COMMENT '参与状态：participating进行中，finished已完成',
  MODIFY COLUMN stage VARCHAR(32) NOT NULL DEFAULT 'scanned' COMMENT '当前业务阶段',
  MODIFY COLUMN current_question_no INT NOT NULL DEFAULT 0 COMMENT '当前题号',
  MODIFY COLUMN question_ids VARCHAR(200) DEFAULT NULL COMMENT '本局抽中的题目编号列表',
  MODIFY COLUMN question_order VARCHAR(200) DEFAULT NULL COMMENT '本局题目实际作答顺序',
  MODIFY COLUMN career_ids VARCHAR(200) DEFAULT NULL COMMENT '本局关联的职业编号列表',
  MODIFY COLUMN category_ids VARCHAR(200) DEFAULT NULL COMMENT '本局关联的职业大类编号列表',
  MODIFY COLUMN option_snapshot_json JSON DEFAULT NULL COMMENT '题目和选项快照JSON',
  MODIFY COLUMN answer_json JSON DEFAULT NULL COMMENT '学生答案JSON',
  MODIFY COLUMN awareness_json JSON DEFAULT NULL COMMENT '学生职业了解程度JSON',
  MODIFY COLUMN viewed_career_ids VARCHAR(1000) DEFAULT NULL COMMENT '已查看的职业编号列表',
  MODIFY COLUMN exploration_career_ids VARCHAR(1000) DEFAULT NULL COMMENT '进一步探索清单职业编号列表',
  MODIFY COLUMN config_version VARCHAR(64) DEFAULT NULL COMMENT '创建记录时使用的游戏配置版本',
  MODIFY COLUMN question_start_time DATETIME DEFAULT NULL COMMENT '当前题目开始作答时间',
  MODIFY COLUMN question_elapsed_seconds INT NOT NULL DEFAULT 0 COMMENT '当前题目累计作答秒数',
  MODIFY COLUMN scan_time DATETIME NOT NULL COMMENT '学生扫码进入游戏时间',
  MODIFY COLUMN start_time DATETIME DEFAULT NULL COMMENT '学生正式开始游戏时间',
  MODIFY COLUMN finish_time DATETIME DEFAULT NULL COMMENT '学生完成游戏时间',
  MODIFY COLUMN create_time DATETIME DEFAULT NULL COMMENT '参与记录创建时间',
  MODIFY COLUMN update_time DATETIME DEFAULT NULL COMMENT '参与记录最后更新时间';

-- 打印机与打印任务表在 v32、v34 中已经为表和全部字段设置中文注释。
ALTER TABLE gkzh_zycck_printer COMMENT = '职业猜猜看汉印云打印机';
ALTER TABLE gkzh_zycck_print_task COMMENT = '职业猜猜看打印任务';

-- 旧版 v28、v29 使用 CREATE TABLE AS SELECT 创建的历史备份表会丢失全部注释。
-- 以下过程按备份表原有字段定义补回当前职业资料表的中文字段注释，执行完成后立即删除。
DROP PROCEDURE IF EXISTS zycck_fill_backup_comments;
DELIMITER $$
CREATE PROCEDURE zycck_fill_backup_comments()
BEGIN
  DECLARE finished INT DEFAULT 0;
  DECLARE backup_table_name VARCHAR(64);
  DECLARE last_backup_table_name VARCHAR(64) DEFAULT '';
  DECLARE backup_column_name VARCHAR(64);
  DECLARE backup_column_type LONGTEXT;
  DECLARE backup_nullable VARCHAR(3);
  DECLARE backup_default LONGTEXT;
  DECLARE backup_extra VARCHAR(255);
  DECLARE source_comment TEXT;
  DECLARE null_sql VARCHAR(20);
  DECLARE default_sql LONGTEXT;
  DECLARE extra_sql VARCHAR(300);
  DECLARE backup_cursor CURSOR FOR
    SELECT backup_column.table_name,
           backup_column.column_name,
           backup_column.column_type,
           backup_column.is_nullable,
           backup_column.column_default,
           backup_column.extra,
           source_column.column_comment
    FROM information_schema.columns backup_column
    INNER JOIN information_schema.columns source_column
      ON source_column.table_schema = DATABASE()
     AND source_column.table_name = 'gkzh_zycck_career_question'
     AND source_column.column_name = backup_column.column_name
    WHERE backup_column.table_schema = DATABASE()
      AND (
        backup_column.table_name LIKE 'gkzh\_zycck\_career\_question\_backup\_%'
        OR backup_column.table_name LIKE 'gkzh\_zycck\_career\_question\_before\_v29\_%'
      )
    ORDER BY backup_column.table_name, backup_column.ordinal_position;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

  OPEN backup_cursor;
  backup_loop: LOOP
    FETCH backup_cursor INTO backup_table_name, backup_column_name, backup_column_type,
      backup_nullable, backup_default, backup_extra, source_comment;
    IF finished = 1 THEN
      LEAVE backup_loop;
    END IF;

    IF backup_table_name <> last_backup_table_name THEN
      SET @zycck_comment_sql = CONCAT(
        'ALTER TABLE `', REPLACE(backup_table_name, '`', '``'),
        '` COMMENT = ''职业猜猜看职业资料及竞猜题目历史备份'''
      );
      PREPARE zycck_comment_stmt FROM @zycck_comment_sql;
      EXECUTE zycck_comment_stmt;
      DEALLOCATE PREPARE zycck_comment_stmt;
      SET last_backup_table_name = backup_table_name;
    END IF;

    SET null_sql = IF(backup_nullable = 'NO', ' NOT NULL', ' NULL');
    IF backup_default IS NULL THEN
      SET default_sql = IF(backup_nullable = 'YES', ' DEFAULT NULL', '');
    ELSEIF UPPER(backup_default) LIKE 'CURRENT_TIMESTAMP%' THEN
      SET default_sql = CONCAT(' DEFAULT ', backup_default);
    ELSE
      SET default_sql = CONCAT(' DEFAULT ', QUOTE(backup_default));
    END IF;
    SET extra_sql = IF(backup_extra = '', '', CONCAT(' ', backup_extra));
    SET @zycck_comment_sql = CONCAT(
      'ALTER TABLE `', REPLACE(backup_table_name, '`', '``'),
      '` MODIFY COLUMN `', REPLACE(backup_column_name, '`', '``'), '` ',
      backup_column_type, null_sql, default_sql, extra_sql,
      ' COMMENT ', QUOTE(source_comment)
    );
    PREPARE zycck_comment_stmt FROM @zycck_comment_sql;
    EXECUTE zycck_comment_stmt;
    DEALLOCATE PREPARE zycck_comment_stmt;
  END LOOP;
  CLOSE backup_cursor;
END$$
DELIMITER ;

CALL zycck_fill_backup_comments();
DROP PROCEDURE IF EXISTS zycck_fill_backup_comments;
