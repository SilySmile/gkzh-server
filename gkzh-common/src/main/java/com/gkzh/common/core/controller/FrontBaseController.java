package com.gkzh.common.core.controller;

import com.gkzh.common.core.domain.model.StudentCheckin;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class FrontBaseController extends BaseController{
    /**
     * 获取当前登录的学生信息
     */
    public StudentCheckin getCurrentStudent() {
        // 从请求属性中获取用户信息
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return (StudentCheckin) request.getAttribute("CURRENT_STUDENT");
    }

    public Long getCurrentStudentId() {
        StudentCheckin student = getCurrentStudent();
        return student.getStuId();
    }

    public String getCurrentStudentName() {
        StudentCheckin student = getCurrentStudent();
        return student.getStuName();
    }

    public Long getCurrentStudentDeptId() {
        StudentCheckin student = getCurrentStudent();
        return student.getDeptId();
    }
    public String getCurrentStudentNo() {
        StudentCheckin student = getCurrentStudent();
        return student.getStuNo();
    }
}



