package com.gkzh.app.controller.activity;

import com.alibaba.fastjson2.JSONObject;
import com.gkzh.activity.domain.GkzhActivity;
import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;
import com.gkzh.activity.service.IGkzhActivityService;
import com.gkzh.activity.domain.dto.UserActivityInfo;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.wjdc.service.IWjdcSurveyResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 活动Controller
 *
 * @author gkzh
 * @date 2025-06-22
 */
@Api("活动相关")
@RestController
@RequestMapping("/api/activity")
public class ActivityController extends FrontBaseController {
    @Autowired
    private IGkzhActivityService activityService;

    @Autowired
    private IWjdcSurveyResponseService wjdcSurveyResponseService;

    @Autowired
    private IGkzhActivityParticipationRecordService activityParticipationRecordService;

    @ApiOperation("获取当前用户参与的活动信息")
    @GetMapping("/{activityId}")
    public AjaxResult getUserActivityInfo(@PathVariable Long activityId) {
        // 获取当前用户ID，这里假设通过安全框架获取当前登录用户ID

        Long currentUserId = getCurrentStudent().getUserId();

        // 查询活动的所有环节
        List modules = activityService.selectActivityModulesByActivityId(activityId);

        // 查询用户参与活动的具体环节
        List<GkzhActivityParticipationRecord> records = activityParticipationRecordService.selectLParticipationRecord(currentUserId, activityId);


        // 构建已完成的 activityId 集合
        Set<String> finishedTypes = new HashSet<>();
        HashMap<String, Object> finishedTime = new HashMap<>();
        HashMap<String, Object> statusHash = new HashMap<>();
        HashMap<String, Object> resultMap = new HashMap<>();
        HashMap<String, Object> remarkMap = new HashMap<>();
        for (GkzhActivityParticipationRecord record : records) {
            switch (record.getParticipationType()){
                case 1:
                    finishedTypes.add("check-in");
                    finishedTime.put("check-in", record.getParticipationTime());
                    statusHash.put("check-in", record.getStatus());
                    resultMap.put("check-in", record.getResult());
                    remarkMap.put("check-in", record.getRemark());
                    break;
                case 2:
                    finishedTypes.add("check-out");
                    finishedTime.put("check-out", record.getParticipationTime());
                    statusHash.put("check-out", record.getStatus());
                    resultMap.put("check-out", record.getResult());
                    remarkMap.put("check-out", record.getRemark());
                    break;
                case 3:
                    finishedTypes.add("lottery");
                    finishedTime.put("lottery", record.getParticipationTime());
                    statusHash.put("lottery", record.getStatus());
                    resultMap.put("lottery", record.getResult());
                    remarkMap.put("lottery", record.getRemark());
                    break;
                case 4:
                    finishedTypes.add("mind-window");
                    finishedTime.put("mind-window", record.getParticipationTime());
                    statusHash.put("mind-window", record.getStatus());
                    resultMap.put("mind-window", record.getResult());
                    remarkMap.put("mind-window", record.getRemark());
                    break;
                case 5:
                    finishedTypes.add("survey");
                    finishedTime.put("survey", record.getParticipationTime());
                    statusHash.put("survey", record.getStatus());
                    resultMap.put("survey", record.getResult());
                    remarkMap.put("survey", record.getRemark());
                    break;
                case 6:
                    finishedTypes.add("wjyd");
                    finishedTime.put("wjyd", record.getParticipationTime());
                    statusHash.put("wjyd", record.getStatus());
                    resultMap.put("wjyd", record.getResult());
                    remarkMap.put("wjyd", record.getRemark());
                    break;
                case 7:
                    finishedTypes.add("cyzs");
                    finishedTime.put("cyzs", record.getParticipationTime());
                    statusHash.put("cyzs", record.getStatus());
                    resultMap.put("cyzs", record.getResult());
                    remarkMap.put("cyzs", record.getRemark());
                    break;
                case 8:
                    finishedTypes.add("zytj");
                    finishedTime.put("zytj", record.getParticipationTime());
                    statusHash.put("zytj", record.getStatus());
                    resultMap.put("zytj", record.getResult());
                    remarkMap.put("zytj", record.getRemark());
                    break;
                case 9:
                    finishedTypes.add("zyxxz");
                    finishedTime.put("zyxxz", record.getParticipationTime());
                    statusHash.put("zyxxz", record.getStatus());
                    resultMap.put("zyxxz", record.getResult());
                    remarkMap.put("zyxxz", record.getRemark());
                    break;
            }
        }
        for (Object obj : modules) {
            JSONObject module = (JSONObject) obj;
            module.put("finished", finishedTypes.contains(module.getString("type")));
            Date finishDate = (Date) finishedTime.get(module.getString("type"));
            module.put("finishedTime", finishDate != null ? DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", finishDate) : null);
            module.put("status", statusHash.get(module.getString("type")));
            module.put("result", resultMap.get(module.getString("type")));
            module.put("remark", remarkMap.get(module.getString("type")));

            if(module.get("config") != null && module.getJSONObject("config").getString("title") != null){
                module.put("title", module.getJSONObject("config").getString("title"));
            }
        }


        return AjaxResult.success(modules);
    }

    @GetMapping("/getActivitySurveyStatus")
    public AjaxResult getActivitySurveyStatus(@RequestParam Long activityId,@RequestParam Long surveyId) {
        // 获取当前用户ID，这里假设通过安全框架获取当前登录用户ID
        Long currentUserId = getCurrentStudent().getUserId();
        boolean finished = wjdcSurveyResponseService.hasUserSubmitted(surveyId,currentUserId);
        return AjaxResult.success(finished?1:0);
    }
}
