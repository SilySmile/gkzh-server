-- 心愿橱窗结果绑定活动周具体游戏，避免同一活动内多个游戏共用统计结果。
ALTER TABLE xycc_user_selection
    ADD COLUMN game_id BIGINT NULL COMMENT '活动周游戏ID';

CREATE INDEX idx_xycc_user_selection_game_id ON xycc_user_selection(game_id);
