package com.gkzh.school.domain;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 学校部门对象 gkzh_school_departments
 * 
 * @author gkzh
 * @date 2025-06-19
 */
@Data
public class GkzhSchoolDepartment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 部门ID */
    private Long departmentId;

    /** 学校ID */
    @Excel(name = "学校ID")
    private Long schoolId;

    /** 父ID */
    @Excel(name = "父ID")
    private Long parentId;

    /** 祖级列表 */
    @Excel(name = "祖级列表")
    private String ancestors;

    /** 名称 */
    @Excel(name = "名称") 
    private String title;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer sortNum;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;  

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 父部门名称 */
    @TableField(exist = false)
    private String parentName;
    
    /** 子部门 */
    @TableField(exist = false)
    private List<GkzhSchoolDepartment> children = new ArrayList<GkzhSchoolDepartment>();

}