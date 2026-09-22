package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("gkzh_zycck_print_task")
public class ZycckPrintTask {
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;
    private Long recordId;
    private Long printerId;
    private String equipmentSn;
    private String printId;
    private String orderNo;
    private String printType;
    private String sourcePdfUrl;
    private String sourceImageUrl;
    private String status;
    private String errorMessage;
    private Integer retryCount;
    private Date createTime;
    private Date printTime;
    private Date updateTime;
}
