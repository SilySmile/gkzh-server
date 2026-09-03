-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职愿探究-MBTI商品', '3', '1', 'product', 'zytj/product/index', 1, 0, 'C', '0', '0', 'zytj:product:list', '#', 'admin', sysdate(), '', null, '职愿探究-MBTI商品菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职愿探究-MBTI商品查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'zytj:product:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职愿探究-MBTI商品新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'zytj:product:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职愿探究-MBTI商品修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'zytj:product:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职愿探究-MBTI商品删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'zytj:product:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('职愿探究-MBTI商品导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'zytj:product:export',       '#', 'admin', sysdate(), '', null, '');