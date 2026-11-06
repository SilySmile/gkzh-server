-- 职业角斗场增加“兽穴”快速获胜目标：率先踏入对方底线中央兽穴立即获胜。
-- 本脚本只更新线上游戏配置说明，不修改表结构和历史参与记录，可重复执行。
UPDATE gkzh_game_config
SET description = '选择动物角色进行7×9斗兽棋对弈，双方各8枚；率先踏入对方兽穴、吃光对方，或3分钟到时按剩余最高等级/数量结算',
    update_time = NOW()
WHERE game_type = 'zyjdc';
