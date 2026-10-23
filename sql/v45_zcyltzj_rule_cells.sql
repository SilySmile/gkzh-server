-- 职场压力·骰子局（zcyltzj）九宫格规则格：下中、下右两格用于展示后台配置的规则。
-- 可重复执行；INSERT IGNORE 保留后台已修改的标题和内容。
CREATE TABLE IF NOT EXISTS gkzh_zcyltzj_rule_cell (
  rule_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则格编号',
  cell_no TINYINT NOT NULL COMMENT '九宫格位置：8下中，9下右',
  title VARCHAR(50) NOT NULL COMMENT '格子标题',
  content VARCHAR(500) DEFAULT NULL COMMENT '规则说明',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY(rule_id),
  UNIQUE KEY uk_zcyltzj_rule_cell(cell_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='骰子局九宫格规则格';

INSERT IGNORE INTO gkzh_zcyltzj_rule_cell(cell_no,title,content,status,create_time,update_time) VALUES
(8,'规则一','请在后台“游戏配置 → 职场压力·骰子局 → 所需配置 → 规则格设置”中填写规则内容。','0',NOW(),NOW()),
(9,'规则二','请在后台“游戏配置 → 职场压力·骰子局 → 所需配置 → 规则格设置”中填写规则内容。','0',NOW(),NOW());
