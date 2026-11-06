-- zycck 打印任务删除权限（MySQL 5.7+，可重复执行）。
-- 删除功能用于管理员清理测试记录；进行中的任务仍需先取消。

INSERT IGNORE INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,component,query,route_name,
    is_frame,is_cache,menu_type,visible,status,perms,icon,
    create_by,create_time,update_by,update_time,remark
) VALUES (
    2417,'删除打印记录',2408,4,'#','','','',
    1,0,'F','1','0','zycck:print-task:remove','#',
    'admin',NOW(),'admin',NOW(),'删除本地打印任务测试记录'
);

UPDATE sys_menu
SET parent_id=2408,
    order_num=4,
    menu_name='删除打印记录',
    menu_type='F',
    visible='1',
    status='0',
    perms='zycck:print-task:remove',
    update_by='admin',
    update_time=NOW(),
    remark='删除本地打印任务测试记录'
WHERE menu_id=2417;

-- 沿用打印管理现有策略，为系统中的角色补充该按钮权限。
INSERT IGNORE INTO sys_role_menu(role_id,menu_id)
SELECT role_id,2417 FROM sys_role;
