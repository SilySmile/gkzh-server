package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/** 职业猜猜看模块使用的汉印云打印机实体。 */
@Data
@TableName("gkzh_zycck_printer")
public class ZycckPrinter {
    /** 本地打印机主键。 */
    @TableId(value = "printer_id", type = IdType.AUTO)
    private Long printerId;
    /** 管理员在本系统中设置的业务名称。 */
    private String printerName;
    /** 汉印云开放平台返回的设备名称。 */
    private String cloudName;
    /** 汉印云打印设备序列号 SN。 */
    private String equipmentSn;
    /** 绑定打印机时使用的设备密钥，仅服务端保存，不返回前端。 */
    private String equipmentSecret;
    /** 汉印云返回的打印机型号名称。 */
    private String modelName;
    /** 汉印云设备状态码，1 表示在线，其他状态见 ZycckPrinterView。 */
    private Integer status;
    /** 本系统启用状态：0 启用，1 禁用。 */
    private String enabled;
    /** 汉印云绑定状态：0 已绑定，1 未绑定。 */
    private String boundStatus;
    /** 最近一次单独查询设备状态的时间。 */
    private Date lastStatusSyncTime;
    /** 最近一次从汉印云同步设备列表的时间。 */
    private Date lastSyncTime;
    /** 本地记录创建时间。 */
    private Date createTime;
    /** 本地记录最后更新时间。 */
    private Date updateTime;
}
