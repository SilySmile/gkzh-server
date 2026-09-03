package com.gkzh.xycc.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
@TableName("xycc_holland_code")
public class HollandCode {

    @TableId
    private String code;

    private String name;

    private String fullName;

    private String summary;

    private String traits;

    private String life;

    @TableField("`work`")
    private String work;

    private Integer sortOrder;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
