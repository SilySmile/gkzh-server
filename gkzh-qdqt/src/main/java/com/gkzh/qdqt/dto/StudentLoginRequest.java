package com.gkzh.qdqt.dto;

import lombok.Data;

@Data
public class StudentLoginRequest {
    private Long schoolId;
    private Long deptId;
    private Long activityId;
    private String name;
    private String no;
    private String gender; //性别
    private String grade; //年级
    private String syd; //生源地
    private String lqfs; //录取方式

}
