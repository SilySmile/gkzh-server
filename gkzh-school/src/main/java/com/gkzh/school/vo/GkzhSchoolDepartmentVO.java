package com.gkzh.school.vo;

import com.gkzh.common.annotation.Excel;
import lombok.Data;

@Data
public class GkzhSchoolDepartmentVO {
    @Excel(name = "学校名称")
    private String schoolName;
    @Excel(name = "院系名称")
    private String departmentName;
    @Excel(name = "专业名称")
    private String majorName;
}
