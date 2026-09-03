package com.gkzh.qdqt.dto;

// ... existing code ...
public class StudentTokenResponse {

    private String token;

    private String studentName;

    private String studentNo;

    private String departmentName;

    private String gender;

    public StudentTokenResponse(String token) {
        this.token = token;
    }

    public StudentTokenResponse(String token, String studentName, String studentNo, String departmentName, String gender) {
        this.token = token;
        this.studentName = studentName;
        this.studentNo = studentNo;
        this.departmentName = departmentName;
        this.gender = gender;
    }

    public String getToken() {
        return token;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getGender() {
        return gender;
    }
}
