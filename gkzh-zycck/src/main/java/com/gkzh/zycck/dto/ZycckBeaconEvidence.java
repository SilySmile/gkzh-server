package com.gkzh.zycck.dto;

import lombok.Data;

/** 手机端最近一次打印点蓝牙检测结果。 */
@Data
public class ZycckBeaconEvidence {
    /** 小程序实际搜索到的信标 UUID。 */
    private String uuid;
    /** 信标 Major，与 Web 信标配置值相同时才可打印。 */
    private Integer major;
    /** 信标 Minor，与 Web 信标配置值相同时才可打印。 */
    private Integer minor;
    /** 信号强度，数值越接近 0 表示距离越近。 */
    private Integer rssi;
    /** 微信 iBeacon 接口估算的距离，单位米；普通 BLE 扫描时为空。 */
    private Double accuracy;
    /** 扫描来源：ble 表示从普通 BLE 广播读取，旧版 iBeacon 扫描可不传。 */
    private String source;
    /** 普通 BLE 扫描读取的原始广播十六进制；服务端重新解析身份，不只信任上报字段。 */
    private String advertisData;
    /** 手机检测到信标的毫秒时间戳，防止重放旧的检测结果。 */
    private Long observedAt;
}
