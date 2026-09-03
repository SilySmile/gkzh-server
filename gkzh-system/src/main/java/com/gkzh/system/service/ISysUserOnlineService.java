package com.gkzh.system.service;

import com.gkzh.common.core.domain.model.LoginUser;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.system.domain.SysUserOnline;

/**
 * 在线用户 服务层
 * 
 *
 */
public interface ISysUserOnlineService
{
    /**
     * 通过登录地址查询信息
     * 
     * @param ipaddr 登录地址
     * @param user 用户信息
     * @return 在线用户信息
     */
    public SysUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user);
    public SysUserOnline selectOnlineByIpaddr(String ipaddr, StudentCheckin user);

    /**
     * 通过用户名称查询信息
     * 
     * @param userName 用户名称
     * @param user 用户信息
     * @return 在线用户信息
     */
    public SysUserOnline selectOnlineByUserName(String userName, LoginUser user);
    public SysUserOnline selectOnlineByUserName(String userName, StudentCheckin user);

    /**
     * 通过登录地址/用户名称查询信息
     * 
     * @param ipaddr 登录地址
     * @param userName 用户名称
     * @param user 用户信息
     * @return 在线用户信息
     */
    public SysUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user);

    /**
     * 通过登录地址/用户名称查询信息
     *
     * @param ipaddr 登录地址
     * @param userName 用户名称
     * @param user 用户信息
     * @return 在线用户信息
     */
    public SysUserOnline selectOnlineByInfo(String ipaddr, String userName, StudentCheckin user);

    /**
     * 设置在线用户信息
     * 
     * @param user 用户信息
     * @return 在线用户
     */
    public SysUserOnline loginUserToUserOnline(LoginUser user);

    public SysUserOnline loginUserToUserOnline(StudentCheckin user);
}
