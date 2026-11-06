-- 职业角斗场棋盘由 4×4 调整为 7×9、双方各 8 枚动物棋子；更新游戏配置说明。
-- 本脚本只修正 gkzh_game_config 的描述，不修改表结构和历史参与记录，可重复执行。
UPDATE gkzh_game_config
SET description = '选择动物角色进行7×9斗兽棋对弈，双方各8枚动物棋子，3分钟内吃光对方或剩余最大棋子等级高者获胜',
    update_time = NOW()
WHERE game_type = 'zyjdc';
