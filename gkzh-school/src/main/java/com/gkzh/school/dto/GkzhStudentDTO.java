package com.gkzh.school.dto;

import com.gkzh.common.annotation.Excel;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 学生对象 gkzh_student
 * 
 * @author gkzh
 * @date 2025-06-19
 */
@Data
public class GkzhStudentDTO {
    private static final long serialVersionUID = 1L;

    /** 学号 */
    @Excel(name = "学号",sort = 5)
    private String studentNo;

    /** 学生姓名 */
    @Excel(name = "学生姓名",sort = 4)
    private String studentName;

    /** 性别（0男 1女 2未知） */
    @Excel(name = "性别",sort = 6, readConverterExp = "0=男,1=女,2=未知")
    private String gender;

    /** 手机号码 */
    @Excel(name = "手机号码",sort = 7)
    private String phone;

    /** 邮箱 */
    @Excel(name = "邮箱",sort = 8)
    private String email;

    @Excel(name = "学校名称",sort = 1)
    private String schoolName;
    @Excel(name = "院系名称",sort = 2)
    private String collegeName;
    /** 专业名称 */
    @Excel(name = "专业名称",sort = 3)
    private String departmentName;

}