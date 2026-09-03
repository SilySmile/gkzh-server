-- 游戏查看页按配置分流，避免将展示规则硬编码在游戏类型文本中。
ALTER TABLE gkzh_game_config
    ADD COLUMN view_type VARCHAR(32) NOT NULL DEFAULT 'generic' COMMENT 'Web查看模板类型';

UPDATE gkzh_game_config
SET view_type = 'mind-window'
WHERE route = 'mind-window' OR game_type = 'mind-window';
