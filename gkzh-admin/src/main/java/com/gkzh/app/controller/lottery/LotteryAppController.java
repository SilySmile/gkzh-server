package com.gkzh.app.controller.lottery;

import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.activity.domain.week.GkzhActivityWeekSchool;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.app.dto.LotteryPrizeResult;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.lottery.domain.LotteryPrize;
import com.gkzh.lottery.service.ILotteryPrizeService;
import com.gkzh.lottery.service.ILotteryRecordService;
import com.gkzh.lottery.domain.LotteryRecord;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.service.IGkzhStudentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端抽奖Controller
 * 
 * @author gkzh
 * @date 2025-01-27
 */
@Api("学生端抽奖")
@RestController
@RequestMapping("/api/lottery")
public class LotteryAppController extends FrontBaseController {
    
    @Autowired
    private ILotteryPrizeService lotteryPrizeService;
    @Autowired
    private ILotteryRecordService lotteryRecordService;
    @Autowired
    private IGkzhActivityParticipationRecordService activityParticipationRecordService;
    @Autowired
    private IActivityWeekService activityWeekService;
    @Autowired
    private IGkzhStudentService studentService;

    @Value("${gkzh.material-domain:}")
    private String materialDomain;

    @GetMapping("/check/{activityId}")
    public AjaxResult checkLottery(@PathVariable Long activityId) {
        StudentCheckin student = getCurrentStudent();
        GkzhStudent studentInfo = studentService.selectGkzhStudentByStudentId(student.getStuId());
        if (studentInfo == null) {
            return AjaxResult.success(java.util.Collections.singletonMap("assigned", false));
        }
        GkzhActivityWeekSchool config = activityWeekService.getSchoolConfig(activityId, studentInfo.getSchoolId());
        if (config == null || config.getLotteryId() == null) {
            return AjaxResult.success(java.util.Collections.singletonMap("assigned", false));
        }
        int finished = activityWeekService.countFinishedGames(activityId, student.getUserId());
        GkzhActivityWeekInstance instance = activityWeekService.getInstance(activityId);
        String bizType = instance == null || instance.getBizType() == null ? "career_week" : instance.getBizType();
        LotteryRecord queryRecord = new LotteryRecord();
        queryRecord.setUserId(student.getUserId());
        queryRecord.setLotteryId(config.getLotteryId());
        queryRecord.setBizType(bizType);
        queryRecord.setActivityId(activityId);
        int drawCount = lotteryRecordService.selectLotteryRecordList(queryRecord).size();
        java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("assigned", true);
        data.put("lotteryId", config.getLotteryId());
        data.put("minFinishCount", config.getMinFinishCount() == null ? 0 : config.getMinFinishCount());
        data.put("finishedCount", finished);
        data.put("maxDrawCount", config.getMaxDrawCount() == null ? 1 : config.getMaxDrawCount());
        data.put("drawCount", drawCount);
        return AjaxResult.success(data);
    }

    @GetMapping("/my/{lotteryId}")
    public AjaxResult myLottery(@PathVariable Long lotteryId, @RequestParam(required = false) Long activityId) {
        LotteryRecord query = new LotteryRecord();
        query.setUserId(getCurrentStudent().getUserId());
        query.setLotteryId(lotteryId);
        query.setActivityId(activityId);
        if (activityId != null) {
            GkzhActivityWeekInstance instance = activityWeekService.getInstance(activityId);
            query.setBizType(instance == null || instance.getBizType() == null ? "career_week" : instance.getBizType());
        }
        List<LotteryRecord> records = lotteryRecordService.selectLotteryRecordList(query);
        return AjaxResult.success(records == null || records.isEmpty() ? null : records.get(0));
    }

    @GetMapping("/my/list/{lotteryId}")
    public AjaxResult myLotteryList(@PathVariable Long lotteryId, @RequestParam(required = false) Long activityId) {
        LotteryRecord query = new LotteryRecord();
        query.setUserId(getCurrentStudent().getUserId());
        query.setLotteryId(lotteryId);
        query.setActivityId(activityId);
        if (activityId != null) {
            GkzhActivityWeekInstance instance = activityWeekService.getInstance(activityId);
            query.setBizType(instance == null || instance.getBizType() == null ? "career_week" : instance.getBizType());
        }
        List<LotteryRecord> records = lotteryRecordService.selectLotteryRecordList(query);
        return AjaxResult.success(records == null ? java.util.Collections.emptyList() : records);
    }
    /**
     * 获取转盘奖品列表
     */
    @ApiOperation("获取转盘奖品列表")
    @GetMapping("/prizes/{activityId}")
    public AjaxResult getPrizeList(
            @ApiParam(value = "活动ID", required = true) 
            @PathVariable Long activityId) {
        
        try {
            // 查询活动奖品列表（包含实际概率计算）
            List<LotteryPrize> prizeList = lotteryPrizeService.selectActivityPrizesWithProbability(activityId);
            if (prizeList == null) {
                return AjaxResult.success(java.util.Collections.emptyList());
            }
            // 转换为前端需要的格式
            List<LotteryPrizeResult> result = prizeList.stream()
                    .map(prize -> {
                        LotteryPrizeResult lotteryPrizeResult = new LotteryPrizeResult();

                        lotteryPrizeResult.setPrizeId(prize.getPrizeId());
                        lotteryPrizeResult.setImageUrl(resolveUrl(prize.getImageUrl()));
                        lotteryPrizeResult.setTitle(prize.getTitle());
                        return lotteryPrizeResult;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return AjaxResult.success(result);
            
        } catch (Exception e) {
            return AjaxResult.error("获取奖品列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/prizes/by/{lotteryId}")
    public AjaxResult getPrizesByLottery(@PathVariable Long lotteryId) {
        try {
            List<LotteryPrize> prizeList = lotteryPrizeService.selectPrizesByLotteryId(lotteryId);
            if (prizeList == null) {
                return AjaxResult.success(java.util.Collections.emptyList());
            }
            List<LotteryPrizeResult> result = prizeList.stream()
                    .map(prize -> {
                        LotteryPrizeResult dto = new LotteryPrizeResult();
                        dto.setPrizeId(prize.getPrizeId());
                        dto.setImageUrl(resolveUrl(prize.getImageUrl()));
                        dto.setTitle(prize.getTitle());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("获取奖品列表失败：" + e.getMessage());
        }
    }

    /**
     * 执行抽奖
     */
    @ApiOperation("执行抽奖")
    @PostMapping("/draw/{activityId}")
    public AjaxResult drawPrize(
            @ApiParam(value = "活动ID", required = true) 
            @PathVariable Long activityId) {
        
        try {
            // 根据权重随机选择奖品
            LotteryPrize winningPrize = lotteryPrizeService.drawPrize(activityId,getCurrentStudent());
            
            if (winningPrize == null) {
                return AjaxResult.error("暂无可用奖品");
            }

            // 构建返回结果
            LotteryPrizeResult lotteryPrizeResult = new LotteryPrizeResult();
            lotteryPrizeResult.setPrizeId(winningPrize.getPrizeId());
            lotteryPrizeResult.setTitle(winningPrize.getTitle());
            
            return AjaxResult.success(lotteryPrizeResult);
            
        } catch (Exception e) {
            return AjaxResult.error("抽奖失败：" + e.getMessage());
        }
    }

    @PostMapping("/draw/by/{lotteryId}")
    public AjaxResult drawPrizeByLottery(@PathVariable Long lotteryId, @RequestParam(required = false) Long activityId) {
        try {
            if (activityId != null) {
                GkzhStudent studentInfo = studentService.selectGkzhStudentByStudentId(getCurrentStudent().getStuId());
                if (studentInfo == null) {
                    return AjaxResult.error("学生信息不存在");
                }
                GkzhActivityWeekSchool config = activityWeekService.getSchoolConfig(activityId, studentInfo.getSchoolId());
                if (config == null || config.getLotteryId() == null || !config.getLotteryId().equals(lotteryId)) {
                    return AjaxResult.error("暂无抽奖活动");
                }
                int finished = activityWeekService.countFinishedGames(activityId, getCurrentStudent().getUserId());
                int min = config.getMinFinishCount() == null ? 0 : config.getMinFinishCount();
                if (finished < min) {
                    return AjaxResult.error("需完成至少 " + min + " 个游戏才可抽奖");
                }
                GkzhActivityWeekInstance instance = activityWeekService.getInstance(activityId);
                String bizType = instance == null || instance.getBizType() == null ? "career_week" : instance.getBizType();
                LotteryRecord queryRecord = new LotteryRecord();
                queryRecord.setUserId(getCurrentStudent().getUserId());
                queryRecord.setLotteryId(lotteryId);
                queryRecord.setBizType(bizType);
                queryRecord.setActivityId(activityId);
                int drawCount = lotteryRecordService.selectLotteryRecordList(queryRecord).size();
                int maxDrawCount = config.getMaxDrawCount() == null ? 1 : config.getMaxDrawCount();
                if (drawCount >= maxDrawCount) {
                    return AjaxResult.error("抽奖次数已用完");
                }
                return drawByLottery(lotteryId, bizType, activityId);
            }
            return drawByLottery(lotteryId, "career_week", null);
        } catch (Exception e) {
            return AjaxResult.error("抽奖失败：" + e.getMessage());
        }
    }

    private AjaxResult drawByLottery(Long lotteryId, String bizType, Long activityId) {
        try {
            LotteryPrize winningPrize = lotteryPrizeService.drawPrizeByLottery(lotteryId, getCurrentStudent(), bizType, activityId);
            if (winningPrize == null) {
                return AjaxResult.error("暂无可抽奖品");
            }
            LotteryPrizeResult dto = new LotteryPrizeResult();
            dto.setPrizeId(winningPrize.getPrizeId());
            dto.setTitle(winningPrize.getTitle());
            return AjaxResult.success(dto);
        } catch (Exception e) {
            return AjaxResult.error("抽奖失败：" + e.getMessage());
        }
    }

    private String resolveUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }
        String domain = materialDomain == null ? "" : materialDomain.replaceAll("/+$", "");
        if (url.startsWith("/")) {
            return domain + url;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            int schemeIndex = url.indexOf("://");
            int pathIndex = url.indexOf("/", schemeIndex + 3);
            String path = pathIndex >= 0 ? url.substring(pathIndex) : "/";
            return domain + path;
        }
        return domain + "/" + url;
    }
} 
