package com.gkzh.qdqt.vo;


import com.gkzh.common.annotation.Excel;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 签到签退对象 gkzh_student_checkin
 * 
 * @author gkzh
 * @date 2025-06-22
 */
@Data
public class GkzhStudentCheckinExportVO extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long checkinId;
    private Long departmentId;
    @Excel(name = "活动名称")
    private String title;

    /**
     * 学校名称
     */
    @Excel(name = "学校名称")
    private String schoolName;

    /**
     * 专业部门id
     */
    @Excel(name = "院系专业")
    private String departmentMajor ;

    /**
     * 学生姓名
     */
    @Excel(name = "学生姓名")
    private String studentName;
    /**
     * 学号
     */
    @Excel(name = "学号")
    private String studentNo;

    /**
     * 签到时间
     */
    @Excel(name = "签到时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date checkinTime;

    @Excel(name = "签退时间", width = 30,dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date checkoutTime;

}
