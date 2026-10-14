-- 职业猜猜看：把蓝牙信标从打印机页面拆为独立的“信标管理”菜单（可重复执行）。
-- 依赖 v34 创建的“系统管理 → 打印管理”目录，沿用 v37 的信标配置表。
INSERT IGNORE INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
VALUES
  (2418, '信标管理', 2415, 2, 'beacons', 'zycck/beacons/index', '', '',
   1, 0, 'C', '0', '0', 'zycck:beacon:list', 'guide',
   'admin', NOW(), 'admin', NOW(), '打印点多蓝牙信标、范围与扫描规则维护'),
  (2419, '信标配置修改', 2418, 1, '#', '', '', '',
   1, 0, 'F', '1', '0', 'zycck:beacon:edit', '#',
   'admin', NOW(), 'admin', NOW(), '允许新增、修改、删除信标并保存配置');

-- 重复执行时修正菜单位置和权限，不覆盖已有角色授权。
UPDATE sys_menu SET parent_id=2415, order_num=2, menu_name='信标管理', path='beacons',
  component='zycck/beacons/index', menu_type='C', perms='zycck:beacon:list',
  update_time=NOW() WHERE menu_id=2418;
UPDATE sys_menu SET parent_id=2418, menu_name='信标配置修改', perms='zycck:beacon:edit',
  update_time=NOW() WHERE menu_id=2419;
UPDATE sys_menu SET order_num=3, update_time=NOW() WHERE menu_id=2408;

-- 仅给已有打印管理权限的角色增加页面；有打印机修改权限的角色同时获得编辑权限。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT DISTINCT role_id, 2418 FROM sys_role_menu WHERE menu_id=2415;
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT DISTINCT role_id, 2419 FROM sys_role_menu WHERE menu_id=2411;
