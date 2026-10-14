-- 职业猜猜看：补充信标规则 JSON 字段的中文注释；A、n 存在现有 JSON 中，无需新增表字段。
-- 可重复执行；先部署此注释脚本，再部署新版服务端和 Web。
ALTER TABLE gkzh_zycck_beacon_config
  MODIFY COLUMN beacons_json JSON NOT NULL
  COMMENT '信标规则列表JSON，含名称、UUID、Major、Minor、最大估算距离、1米参考值A、环境衰减因子n及旧版RSSI';
