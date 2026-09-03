package com.gkzh.activity.dto.staff;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class StaffPrizeView {
    private String redemptionCode;
    private Long redemptionId;
    private Long lotteryRecordId;
    private Long userId;
    private Long studentId;
    private Long schoolId;
    private String studentName;
    private String studentNo;
    private String schoolName;
    private String prizeTitle;
    private Date drawTime;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date redeemTime;
    private Long staffId;
    private String remark;
}
