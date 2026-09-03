package com.gkzh.activity.service.impl;

import java.util.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.domain.staff.GkzhPrizeRedemption;
import com.gkzh.activity.domain.staff.GkzhPrizeRedemptionLog;
import com.gkzh.activity.dto.staff.StaffPrizeView;
import com.gkzh.activity.dto.staff.StaffActivitySummary;
import com.gkzh.activity.dto.staff.StaffGameDimensionRow;
import com.gkzh.activity.dto.staff.StaffGameSummary;
import com.gkzh.activity.mapper.staff.GkzhPrizeRedemptionLogMapper;
import com.gkzh.activity.mapper.staff.GkzhPrizeRedemptionMapper;
import com.gkzh.activity.mapper.staff.GkzhStaffStatisticsMapper;
import com.gkzh.activity.mapper.staff.GkzhStaffActivityStatisticsMapper;
import com.gkzh.activity.service.IStaffService;
import com.gkzh.common.exception.ServiceException;

@Service
public class StaffServiceImpl implements IStaffService {
    @Autowired
    private GkzhPrizeRedemptionMapper redemptionMapper;
    @Autowired
    private GkzhPrizeRedemptionLogMapper logMapper;
    @Autowired
    private GkzhStaffStatisticsMapper statisticsMapper;
    @Autowired
    private GkzhStaffActivityStatisticsMapper activityStatisticsMapper;

    @Override
    @Transactional
    public StaffPrizeView scanPrize(Long recordId, Long staffId, Long schoolId) {
        StaffPrizeView view = getPrize(recordId, schoolId);
        GkzhPrizeRedemption redemption = getOrCreate(recordId, view, schoolId);
        GkzhPrizeRedemptionLog log = new GkzhPrizeRedemptionLog();
        log.setRedemptionId(redemption.getRedemptionId());
        log.setLotteryRecordId(recordId);
        log.setSchoolId(schoolId);
        log.setStaffId(staffId);
        log.setAction("SCAN");
        log.setBeforeStatus(redemption.getStatus());
        log.setAfterStatus(redemption.getStatus());
        log.setCreateTime(new Date());
        logMapper.insert(log);
        view.setRedemptionId(redemption.getRedemptionId());
        view.setStatus(redemption.getStatus());
        view.setStaffId(redemption.getStaffId());
        view.setRedeemTime(redemption.getRedeemTime());
        view.setRemark(redemption.getRemark());
        return view;
    }

    @Override
    public StaffPrizeView getPrize(Long recordId, Long schoolId) {
        StaffPrizeView view = redemptionMapper.selectPrizeView(recordId);
        if (view == null || !schoolId.equals(view.getSchoolId()))
            throw new ServiceException("奖品不存在或不属于当前学校");
        return view;
    }

    @Override
    public StaffPrizeView resolvePrize(Long recordId, String redemptionCode, Long schoolId) {
        Long resolvedId = recordId;
        if (resolvedId == null && redemptionCode != null && !redemptionCode.trim().isEmpty()) {
            resolvedId = redemptionMapper.selectRecordIdByRedemptionCode(redemptionCode.trim());
        }
        if (resolvedId == null) throw new ServiceException("核销码无效");
        return getPrize(resolvedId, schoolId);
    }

    @Override
    @Transactional
    public StaffPrizeView redeemPrize(Long recordId, Long staffId, Long schoolId, String remark) {
        StaffPrizeView view = getPrize(recordId, schoolId);
        GkzhPrizeRedemption redemption = getOrCreate(recordId, view, schoolId);
        if (!"0".equals(redemption.getStatus())) {
            writeLog(redemption, staffId, null, "REPEAT", redemption.getStatus(), redemption.getStatus(), "重复核销");
            view.setStatus(redemption.getStatus());
            view.setRedeemTime(redemption.getRedeemTime());
            view.setStaffId(redemption.getStaffId());
            return view;
        }
        int updated = redemptionMapper.redeem(redemption.getRedemptionId(), schoolId, staffId, null, remark);
        if (updated != 1) throw new ServiceException("奖品状态已发生变化，请刷新后重试");
        writeLog(redemption, staffId, null, "REDEEM", "0", "1", remark);
        return redemptionMapper.selectPrizeView(recordId);
    }

    @Override
    @Transactional
    public StaffPrizeView adminRedeemPrize(Long recordId, Long adminUserId, String remark) {
        StaffPrizeView view = redemptionMapper.selectPrizeView(recordId);
        if (view == null || "谢谢参与".equals(view.getPrizeTitle()))
            throw new ServiceException("该记录没有可核销的实物奖品");
        GkzhPrizeRedemption redemption = getOrCreate(recordId, view, view.getSchoolId());
        if (!"0".equals(redemption.getStatus())) return redemptionMapper.selectPrizeView(recordId);
        if (redemptionMapper.redeem(redemption.getRedemptionId(), view.getSchoolId(), null, adminUserId, remark) != 1) {
            throw new ServiceException("奖品状态已发生变化，请刷新后重试");
        }
        writeLog(redemption, null, adminUserId, "ADMIN_REDEEM", "0", "1", remark);
        return redemptionMapper.selectPrizeView(recordId);
    }

    private GkzhPrizeRedemption getOrCreate(Long recordId, StaffPrizeView view, Long schoolId) {
        GkzhPrizeRedemption redemption = redemptionMapper.selectOne(new QueryWrapper<GkzhPrizeRedemption>().eq("lottery_record_id", recordId));
        if (redemption == null) {
            redemption = new GkzhPrizeRedemption();
            redemption.setLotteryRecordId(recordId);
            redemption.setSchoolId(schoolId);
            redemption.setStudentId(view.getStudentId());
            redemption.setStatus("0");
            redemption.setCreateTime(new Date());
            redemption.setUpdateTime(new Date());
            redemptionMapper.insert(redemption);
        }
        return redemption;
    }

    private void writeLog(GkzhPrizeRedemption redemption, Long staffId, Long adminUserId, String action, String before, String after, String remark) {
        GkzhPrizeRedemptionLog log = new GkzhPrizeRedemptionLog();
        log.setRedemptionId(redemption.getRedemptionId());
        log.setLotteryRecordId(redemption.getLotteryRecordId());
        log.setSchoolId(redemption.getSchoolId());
        log.setStaffId(staffId);
        log.setAdminUserId(adminUserId);
        log.setAction(action);
        log.setBeforeStatus(before);
        log.setAfterStatus(after);
        log.setRemark(remark);
        log.setCreateTime(new Date());
        logMapper.insert(log);
    }

    @Override
    public List<StaffPrizeView> listPrizeRecords(Long schoolId) {
        return redemptionMapper.selectPrizeViewsBySchool(schoolId);
    }

    @Override
    public List<GkzhPrizeRedemptionLog> listPrizeLogs(Long recordId) {
        return logMapper.selectByRecordId(recordId);
    }

    @Override
    public List<Map<String, Object>> codeStatistics(Long schoolId) {
        return statisticsMapper.selectCodeStatistics(schoolId);
    }

    @Override
    public Map<String, Object> activityStatistics(String bizType, Long schoolId) {
        if (!"career_week".equals(bizType) && !"job_week".equals(bizType)) {
            throw new ServiceException("活动类型无效");
        }
        StaffActivitySummary activity = activityStatisticsMapper.selectCurrentActivity(bizType, schoolId);
        Map<String, Object> data = new LinkedHashMap<>();
        if (activity == null) {
            data.put("activity", null);
            data.put("games", new ArrayList<>());
            return data;
        }
        List<StaffGameSummary> games = activityStatisticsMapper.selectActivityGames(activity.getInstanceId(), schoolId);
        activity.setGames(games);
        data.put("activity", activity);
        data.put("games", games);
        return data;
    }

    @Override
    public Map<String, Object> gameStatistics(Long gameId, Long schoolId, Long collegeId, Long majorId, String gender) {
        StaffGameSummary game = activityStatisticsMapper.selectGame(gameId, schoolId);
        if (game == null) {
            throw new ServiceException("游戏不存在或不属于当前学校");
        }
        List<StaffGameDimensionRow> rows = activityStatisticsMapper
                .selectDimensionRows(gameId, schoolId, collegeId, majorId, gender);
        long participants = rows.stream().mapToLong(item -> value(item.getParticipantCount())).sum();
        long completed = rows.stream().mapToLong(item -> value(item.getCompletedCount())).sum();
        long inProgress = rows.stream().mapToLong(item -> value(item.getInProgressCount())).sum();
        long failed = rows.stream().mapToLong(item -> value(item.getFailedCount())).sum();
        game.setParticipantCount(participants);
        game.setCompletedCount(completed);
        game.setInProgressCount(inProgress);
        game.setFailedCount(failed);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("game", game);
        data.put("rows", rows);
        data.put("students", activityStatisticsMapper.selectStudentRows(gameId, schoolId, collegeId, majorId, gender));
        data.put("colleges", activityStatisticsMapper.selectCollegeOptions(gameId, schoolId));
        data.put("majors", activityStatisticsMapper.selectMajorOptions(gameId, schoolId));
        return data;
    }

    private long value(Long number) {
        return number == null ? 0L : number;
    }
}
