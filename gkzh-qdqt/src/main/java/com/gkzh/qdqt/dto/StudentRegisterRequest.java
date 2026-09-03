package com.gkzh.qdqt.dto;

import lombok.Data;

@Data
public class StudentRegisterRequest {
    private Long schoolId;
    private Long collegeId;
    private Long departmentId;
    private String className;
    private String enrollmentYear;
    private String studentNo;
    private String password;
    private String studentName;
    private String gender;
    private String phone;
}
