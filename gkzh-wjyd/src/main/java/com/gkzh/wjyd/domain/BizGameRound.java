package com.gkzh.wjyd.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.annotation.Excel;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 职场危机对象 biz_question
 * 
 * @author gkzh
 * @date 2025-10-13
 */
@Data
public class BizGameRound
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "关卡记录ID")
    @TableId(type = IdType.AUTO) // 使用数据库自增
    private Long id;
    @Excel(name = "参与者ID")
    private Long userId;
    @Excel(name = "是否通关", readConverterExp = "0=失败,1=成功")
    private Integer isSuccess;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    /** 记录创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 用户学号 */
    @Excel(name = "学号")
    private String username;

    /** 用户姓名 */
    @Excel(name = "姓名")
    private String nickname;


}
