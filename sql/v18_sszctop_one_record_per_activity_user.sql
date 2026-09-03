-- ============================================================================
-- 谁是职场TOP：活动实例与用户唯一报告迁移
-- 目标：同一用户在同一活动实例中仅保留一份最终个人报告。
-- 执行前请确认已完成数据库备份；脚本仅处理 gkzh_sszctop_student_report，
-- 不会修改心愿橱窗、抽奖或其他游戏的数据。
-- ============================================================================

-- 历史测试或旧版本可能留下同一活动同一用户的多份报告。
-- 以下语句保留 report_id 最大（即最新）的一份，删除其余旧快照，保证后续唯一索引可创建。
DELETE older_report
FROM gkzh_sszctop_student_report AS older_report
INNER JOIN gkzh_sszctop_student_report AS latest_report
  ON latest_report.instance_id = older_report.instance_id
 AND latest_report.user_id = older_report.user_id
 AND latest_report.report_id > older_report.report_id;

-- 从数据库层面约束“活动实例 + 用户”唯一，防止并发结算或后续代码调整再次产生重复报告。
ALTER TABLE gkzh_sszctop_student_report
  ADD UNIQUE KEY uk_sszctop_report_instance_user (instance_id, user_id);
