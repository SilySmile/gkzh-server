package com.gkzh.school.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;
import java.util.List;

/**
 * 学生对象 gkzh_student
 * 
 * @author gkzh
 * @date 2025-06-19
 */
@Data
public class GkzhStudent extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 学生ID */
    private Long studentId;

    /** 学校ID */

    private Long schoolId;

    /** 院系ID */
    @TableField(exist = false)
    private Long collegeId;

    /** 专业部门ID */
    private Long departmentId;

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
    //@Excel(name = "手机号码",sort = 7)
    private String phone;

    /** 邮箱 */
    //@Excel(name = "邮箱",sort = 8)
    private String email;

    /** 状态（0正常 1停用） */
    //@Excel(name = "状态",sort = 9, readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 学校名称 */
    @TableField(exist = false)
    @Excel(name = "学校名称",sort = 1)
    private String schoolName;
    @TableField(exist = false)
    @Excel(name = "院系名称",sort = 2)
    private String collegeName;
    /** 专业名称 */
    @TableField(exist = false)
    @Excel(name = "专业名称",sort = 3)
    private String departmentName;

    private Long userId;
    /** 专业ID列表（用于查询） */

    @TableField(exist = false)
    private List<Long> departmentIds;

    @Excel(name = "年级",dictType = "grade_type", sort = 8)
    private String grade;
    @Excel(name = "录取方式",dictType = "lqfs", sort = 9)
    private String lqfs;
    @Excel(name = "生源地",dictType = "syd",sort = 10)
    private String syd;

    /** 密码(BCrypt加密) */
    private String password;

    /** 是否已完成小程序注册：0未注册，1已注册。 */
    @TableField(exist = false)
    private Integer registered;

    /** 班级 */
    private String className;

    /** 入学年份 */
    private String enrollmentYear;
}
