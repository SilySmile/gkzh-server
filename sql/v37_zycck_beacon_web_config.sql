-- 职业猜猜看：在 Web 打印机管理中维护多蓝牙信标（MySQL 5.7+，可重复执行）。
-- 部署顺序：先执行本脚本，再部署新版服务端和 Web；默认关闭信标限制，管理员在 Web 中配置后启用。
CREATE TABLE IF NOT EXISTS gkzh_zycck_beacon_config (
  config_id BIGINT NOT NULL COMMENT '配置编号，固定为1',
  beacon_enabled CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否启用蓝牙信标限制：0关闭，1开启',
  beacons_json JSON NOT NULL COMMENT '信标规则列表JSON，含名称、UUID、Major、Minor、最大估算距离、1米参考值A、环境衰减因子n及旧版RSSI',
  default_max_distance DECIMAL(8,2) NOT NULL DEFAULT 3.00 COMMENT '信标未单独配置时允许的最大距离，单位米',
  default_min_rssi INT NOT NULL DEFAULT -90 COMMENT '信标未单独配置时允许的最低RSSI',
  max_age_seconds INT NOT NULL DEFAULT 15 COMMENT '手机扫描结果最长有效时间，单位秒',
  scan_timeout_seconds INT NOT NULL DEFAULT 10 COMMENT '小程序搜索信标的最长时间，单位秒',
  update_time DATETIME DEFAULT NULL COMMENT '最近一次保存配置的时间',
  PRIMARY KEY (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职业猜猜看打印点蓝牙信标配置';

-- 为已有环境创建安全的初始配置；重复执行时保留管理员已保存的配置。
INSERT IGNORE INTO gkzh_zycck_beacon_config
  (config_id, beacon_enabled, beacons_json, default_max_distance, default_min_rssi, max_age_seconds, scan_timeout_seconds, update_time)
VALUES (1, '0', '[]', 3.00, -90, 15, 10, NOW());
