-- 2025-10-28 处理用户选择查询缓慢问题
-- 用户选择表相关索引
CREATE INDEX idx_user_selection_user_id ON xycc_user_selection(user_id);
CREATE INDEX idx_user_selection_pattern_code ON xycc_user_selection(pattern_combo_code);

-- 模式组合相关索引
CREATE INDEX idx_pattern_combo_code ON xycc_pattern_combo(code);

-- 关联表索引
CREATE INDEX idx_combo_career_combo_id ON xycc_pattern_combo_career(combo_id);
CREATE INDEX idx_combo_env_combo_id ON xycc_pattern_combo_env(combo_id);

-- 增加上述索引后，原sql查询效率也可以了