package com.gkzh.activity.service;

import java.util.List;
import java.util.Map;

import com.gkzh.activity.domain.staff.GkzhPrizeRedemptionLog;
import com.gkzh.activity.dto.staff.StaffPrizeView;

public interface IStaffService {
    StaffPrizeView scanPrize(Long recordId, Long staffId, Long schoolId);

    StaffPrizeView getPrize(Long recordId, Long schoolId);

    StaffPrizeView resolvePrize(Long recordId, String redemptionCode, Long schoolId);

    StaffPrizeView redeemPrize(Long recordId, Long staffId, Long schoolId, String remark);

    StaffPrizeView adminRedeemPrize(Long recordId, Long adminUserId, String remark);

    List<StaffPrizeView> listPrizeRecords(Long schoolId);

    List<GkzhPrizeRedemptionLog> listPrizeLogs(Long recordId);

    List<Map<String, Object>> codeStatistics(Long schoolId);

    Map<String, Object> activityStatistics(String bizType, Long schoolId);

    Map<String, Object> gameStatistics(Long gameId, Long schoolId, Long collegeId, Long majorId, String gender);
}
