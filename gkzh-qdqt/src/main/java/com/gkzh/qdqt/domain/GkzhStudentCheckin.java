package com.gkzh.qdqt.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.core.domain.BaseEntity;
import com.gkzh.common.annotation.Excel;
import lombok.Data;

/**
 * 签到签退对象 gkzh_student_checkin
 * 
 * @author gkzh
 * @date 2025-06-22
 */
@Data
public class GkzhStudentCheckin extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 签到记录id
     */
    private Long checkinId;

    /**
     * 活动ID
     */
    @Excel(name = "活动ID")
    private Long activityId;

    /**
     * 学生id
     */
    @Excel(name = "学生id")
    private Long studentId;

    /**
     * 专业部门id
     */
    @Excel(name = "专业部门id")
    private Long departmentId;

    /**
     * 学号
     */
    @Excel(name = "学号")
    private String studentNo;

    /**
     * 学生姓名
     */
    @Excel(name = "学生姓名")
    private String studentName;

    /**
     * 签到时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "签到时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date checkinTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "签退时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date checkoutTime;

    /**
     * 签到IP
     */
    @Excel(name = "签到IP")
    private String checkinIp;

    /**
     * 签到地点
     */
    @Excel(name = "签到地点")
    private String checkinLocation;

    /**
     * 签到设备
     */
    @Excel(name = "签到设备")
    private String checkinDevice;

    /**
     * 状态（0成功 1失败）
     */
    @Excel(name = "状态", readConverterExp = "0=成功,1=失败")
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

}
