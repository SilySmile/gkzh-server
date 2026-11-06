package com.gkzh.zycck.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Web 打印机管理页编辑及小程序打印校验共用的信标配置。 */
@Data
public class ZycckBeaconSettingsView {
    /** 是否要求手机处于任意一个信标范围内。 */
    private boolean beaconEnabled;
    /** 打印点信标列表。 */
    private List<ZycckBeaconRule> beacons = new ArrayList<>();
    /** 信标未单独配置时允许的最大距离，单位米。 */
    private double defaultMaxDistance;
    /** 旧版 iBeacon 客户端使用的最低 RSSI，普通 BLE 测距不使用。 */
    private int defaultMinRssi;
    /** 手机扫描结果的最大有效时间，单位秒。 */
    private int maxAgeSeconds;
    /** 小程序搜索信标的最长时间，单位秒。 */
    private int scanTimeoutSeconds;
}
