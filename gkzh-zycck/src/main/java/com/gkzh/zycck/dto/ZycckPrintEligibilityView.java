package com.gkzh.zycck.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** 小程序打开打印面板前获取的打印条件。 */
@Data
@Builder
public class ZycckPrintEligibilityView {
    /** 当前用户是否还可提交打印。 */
    private boolean allowed;
    /** 当前用户是否已经成功打印过。 */
    private boolean printed;
    /** 是否必须检测到现场 iBeacon。 */
    private boolean beaconEnabled;
    /** 可用于打印的全部现场信标，匹配任意一项即可。 */
    private List<ZycckBeaconRuleView> beacons;
    /** 兼容旧版小程序的第一个 iBeacon UUID。 */
    private String beaconUuid;
    /** 兼容旧版小程序的第一个信标 Major，空值表示不限制。 */
    private Integer beaconMajor;
    /** 兼容旧版小程序的第一个信标 Minor，空值表示不限制。 */
    private Integer beaconMinor;
    /** 兼容旧版小程序的第一个信标最大距离，单位米。 */
    private double beaconMaxDistance;
    /** 兼容旧版小程序的第一个信标最弱 RSSI。 */
    private int beaconMinRssi;
    /** 小程序最长搜索信标时间，单位秒。 */
    private int beaconScanTimeoutSeconds;
    /** 信标检测证据的最大有效时间，单位秒；超时需重新扫描。 */
    private int beaconMaxAgeSeconds;
    /** 不允许打印时展示给用户的中文说明。 */
    private String message;
}
