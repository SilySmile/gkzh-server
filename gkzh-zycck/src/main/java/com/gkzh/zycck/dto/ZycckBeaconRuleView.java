package com.gkzh.zycck.dto;

import lombok.Builder;
import lombok.Data;

/** 小程序可用于打印的一处蓝牙信标规则。 */
@Data
@Builder
public class ZycckBeaconRuleView {
    /** 信标名称，用于区分多个现场打印点。 */
    private String name;
    /** 需要搜索的 iBeacon UUID。 */
    private String uuid;
    /** Major 为空时不限制该值。 */
    private Integer major;
    /** Minor 为空时不限制该值。 */
    private Integer minor;
    /** 允许的最大估算距离，单位米。 */
    private double maxDistance;
    /** 距离 1 米处 RSSI 的绝对值 A，用于按公式估算距离。 */
    private double referenceRssi;
    /** 环境衰减因子 n，用于按公式估算距离。 */
    private double pathLossExponent;
    /** 旧版 iBeacon 客户端使用的最弱 RSSI；普通 BLE 测距不使用。 */
    private int minRssi;
}
