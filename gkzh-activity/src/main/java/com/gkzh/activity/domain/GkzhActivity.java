package com.gkzh.activity.domain;

import java.io.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 活动举办对象 gkzh_activity
 * 
 * @author gkzh
 * @date 2025-06-22
 */
@Data
public class GkzhActivity extends BaseEntity
{
    private static final long serialVersionUID = 1L;
   

    /** 活动ID */
    private Long activityId;

    /** 活动名称 */
    @Excel(name = "活动名称")
    private String title;

    /** 活动描述 */
    @Excel(name = "活动描述")
    private String description;

    /** 活动开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "活动开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startTime;

    /** 活动结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "活动结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endTime;

    /** 状态（0-禁用，1-启用） */
    @Excel(name = "活动状态", readConverterExp = "0=开启,1=关闭")
    private String status;

    /** 活动类型（1-校园活动，2-学术讲座，3-文化活动，4-其他） */
    @Excel(name = "活动类型", readConverterExp = "1=-校园活动，2-学术讲座，3-文化活动，4-其他")
    private Integer activityType;

    /** 活动地点 */
    @Excel(name = "活动地点")
    private String location;

    /** 活动主办方 */
    @Excel(name = "活动主办方")
    private String organizer;

    /** 活动协办方 */
    @Excel(name = "活动协办方")
    private String coOrganizer;

    /** 活动联系人 */
    @Excel(name = "活动联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 活动二维码 */
    @Excel(name = "活动二维码")
    private String qrCode;

    /** 活动海报 */
    @Excel(name = "活动海报")
    private String poster;

    /** 活动环节配置（JSON格式） */
    @Excel(name = "活动环节配置", readConverterExp = "J=SON格式")
    private String moduleConfig;

    /** 参与人配置（JSON格式） */
    @Excel(name = "参与人配置", readConverterExp = "J=SON格式")
    private String participantConfig;


}
