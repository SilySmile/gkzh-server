package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/** 职业猜猜看打印点蓝牙信标配置实体，固定使用编号 1。 */
@Data
@TableName("gkzh_zycck_beacon_config")
public class ZycckBeaconConfig {
    /** 配置主键，固定为 1。 */
    @TableId(value = "config_id", type = IdType.INPUT)
    private Long configId;
    /** 是否要求检测信标：0 关闭，1 开启。 */
    private String beaconEnabled;
    /** 多个信标规则的 JSON，包含名称、身份、最大距离和测距参数 A、n。 */
    private String beaconsJson;
    /** 未单独指定时允许的最大估算距离，单位米。 */
    private Double defaultMaxDistance;
    /** 旧版 iBeacon 客户端使用的最低 RSSI，普通 BLE 测距不使用。 */
    private Integer defaultMinRssi;
    /** 手机扫描结果的最大有效时间，单位秒。 */
    private Integer maxAgeSeconds;
    /** 小程序搜索信标的最长时间，单位秒。 */
    private Integer scanTimeoutSeconds;
    /** 最近一次保存配置的时间。 */
    private Date updateTime;
}
