package com.gkzh.xycc.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 用户记录对象 xycc_user_selection
 * 
 * @author gkzh
 * @date 2025-06-16
 */
@Data
public class UserSelection extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long userSelectionId;

    /** 用户ID */
   
    private Long userId;

    /** 活动周游戏ID，用于区分同一活动实例中的不同游戏结果。 */
    private Long gameId;

    /** 用户名 */
    @Excel(name = "姓名")
    private String nickName;

    @Excel(name = "学号")
    private String userName;

    private String patternIds;
    /** 如"RIA" */
    @Excel(name = "编码组合")
    private String patternComboCode;

    /** 职业方向标题 */
    @Excel(name = "职业方向")
    private String careerTitles;

    /** 工作环境偏好标题 */
    @Excel(name = "工作环境偏好")
    private String envTitles;

    /** 选择时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "选择时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
}
