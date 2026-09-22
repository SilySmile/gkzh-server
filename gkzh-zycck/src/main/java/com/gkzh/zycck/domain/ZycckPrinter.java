package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("gkzh_zycck_printer")
public class ZycckPrinter {
    @TableId(value = "printer_id", type = IdType.AUTO)
    private Long printerId;
    private String printerName;
    private String cloudName;
    private String equipmentSn;
    private String equipmentSecret;
    private String modelName;
    private Integer status;
    private String enabled;
    private String boundStatus;
    private Date lastStatusSyncTime;
    private Date lastSyncTime;
    private Date createTime;
    private Date updateTime;
}
