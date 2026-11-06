package com.gkzh.zycck.dto;

import lombok.Data;

/** 学生端提交打印时的请求参数。 */
@Data
public class ZycckPrintRequest {
    /** Web 管理端维护的本地打印机编号。 */
    private Long printerId;
    /** 开启信标限制后必填；未开启时可为空。 */
    private ZycckBeaconEvidence beacon;
}
