package com.gkzh.zycck.dto;

import lombok.Data;

/** Web 管理的单个打印点蓝牙信标规则。 */
@Data
public class ZycckBeaconRule {
    /** 信标名称，供管理员辨认打印点。 */
    private String name;
    /** iBeacon UUID；输入支持连续 32 位或带连字符格式，保存后统一为标准格式。 */
    private String uuid;
    /** iBeacon Major；为空时不限制。 */
    private Integer major;
    /** iBeacon Minor；为空时不限制。 */
    private Integer minor;
    /** 当前信标允许的最大估算距离，单位米；为空时使用全局值。 */
    private Double maxDistance;
    /** 距离 1 米处 RSSI 的绝对值 A，例如实测 -60 dBm 时填写 60；为空时默认 60。 */
    private Double referenceRssi;
    /** 环境衰减因子 n，常见取值 2 至 4；为空时默认 2。 */
    private Double pathLossExponent;
    /** 旧版 iBeacon 客户端使用的最低 RSSI；普通 BLE 测距不使用。 */
    private Integer minRssi;
}
