-- 心愿橱窗“用户记录”仅保留后台功能入口，清空所有历史选择结果数据。
-- TRUNCATE 适合大量测试脏数据并重置自增编号；只影响 xycc_user_selection，
-- 不会删除心愿橱窗配置、菜单、路由或其他游戏记录。
TRUNCATE TABLE `xycc_user_selection`;
