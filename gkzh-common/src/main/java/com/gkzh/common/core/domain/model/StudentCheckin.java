package com.gkzh.common.core.domain.model;

import com.gkzh.common.core.domain.entity.SysUser;
import lombok.Data;

@Data
public class StudentCheckin {
    private static final long serialVersionUID = 1L;

    /**
     * studentID
     */
    private Long stuId;

    private String stuName;

    private String stuNo;
    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    private String os;
    private String browser;
    private Long userId;
}
