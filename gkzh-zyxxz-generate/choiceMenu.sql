-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职业信息站-学生选择记录', '3', '1', 'choice', 'lottery/choice/index', 1, 0, 'C', '0', '0', 'lottery:choice:list', '#', 'admin', sysdate(), '', null, '职业信息站-学生选择记录菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职业信息站-学生选择记录查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'lottery:choice:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职业信息站-学生选择记录新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'lottery:choice:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职业信息站-学生选择记录修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'lottery:choice:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职业信息站-学生选择记录删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'lottery:choice:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职业信息站-学生选择记录导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'lottery:choice:export',       '#', 'admin', sysdate(), '', null, '');