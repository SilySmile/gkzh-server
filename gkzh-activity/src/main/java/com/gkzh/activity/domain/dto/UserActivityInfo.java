package com.gkzh.activity.domain.dto;

import java.util.List;

/**
 * 用户活动信息数据传输对象
 */
public class UserActivityInfo {
    /** 活动所有环节 */
    private List modules;


    // Getters and Setters
    public List getModules() {
        return modules;
    }

    public void setModules(List modules) {
        this.modules = modules;
    }

}