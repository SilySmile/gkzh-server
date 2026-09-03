package com.gkzh.app.controller.activity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gkzh.activity.domain.week.GkzhActivityArea;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.domain.week.GkzhGameType;
import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.service.IGkzhStudentService;
import com.gkzh.xycc.mapper.UserSelectionMapper;

/**
 * 学生端活动接口
 */
@RestController
@RequestMapping("/api/activity/week")
public class WeekAppController extends FrontBaseController {

    @Autowired
    private IActivityWeekService activityWeekService;

    @Autowired
    private IGkzhActivityParticipationRecordService legacyParticipationService;

    @Autowired
    private IGkzhStudentService studentService;

    @Autowired
    private UserSelectionMapper userSelectionMapper;

    @GetMapping("/{instanceId}/flow")
    public AjaxResult flow(@PathVariable Long instanceId) {
        GkzhActivityWeekInstance instance = activityWeekService.getInstance(instanceId);
        if (instance == null) {
            return AjaxResult.error("活动不存在");
        }

        StudentCheckin student = getCurrentStudent();
        Long schoolId = getCurrentStudentSchoolId();
        List<GkzhActivityArea> areas = schoolId == null
                ? new ArrayList<>()
                : activityWeekService.listAreas(instanceId, schoolId);
        Long userId = student == null ? null : student.getUserId();
        List<Map<String, Object>> result = new ArrayList<>();
        for (GkzhActivityArea area : areas) {
            List<GkzhActivityGame> games = activityWeekService.listGames(area.getAreaId());
            List<JSONObject> gameList = new ArrayList<>();
            for (GkzhActivityGame game : games) {
                JSONObject gameJson = JSON.parseObject(JSON.toJSONString(game));
                if (userId != null) {
                    GkzhGameParticipation participation = activityWeekService.getLatestParticipation(game.getGameId(), userId);
                    // 心愿橱窗完成状态必须同时具备活动参与记录和当前游戏的用户选择结果；
                    // 任一记录被测试清理后，都应重新进入游戏而非打开空报告。
                    boolean finished = participation != null && "1".equals(participation.getStatus());
                    if (finished && "mind-window".equals(game.getGameType())) {
                        finished = userSelectionMapper.selectByGameIdAndUserId(game.getGameId(), userId) != null;
                    }
                    gameJson.put("isFinish", finished);
                    gameJson.put("isFail", participation != null && "2".equals(participation.getStatus()));
                } else {
                    gameJson.put("isFinish", false);
                    gameJson.put("isFail", false);
                }
                gameList.add(gameJson);
            }
            Map<String, Object> areaMap = new LinkedHashMap<>();
            areaMap.put("areaId", area.getAreaId());
            areaMap.put("title", area.getTitle());
            areaMap.put("sortOrder", area.getSortOrder());
            areaMap.put("games", gameList);
            result.add(areaMap);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("instance", instance);
        data.put("areas", result);
        return AjaxResult.success(data);
    }

    @GetMapping("/active/{bizType}")
    public AjaxResult activeInstance(@PathVariable String bizType) {
        List<GkzhActivityWeekInstance> instances = activityWeekService.listInstances(bizType);
        GkzhActivityWeekInstance active = instances.stream()
                .filter(item -> "1".equals(item.getStatus()))
                .findFirst()
                .orElse(null);
        if (active == null) {
            return AjaxResult.error("暂无进行中的活动");
        }
        return AjaxResult.success(active);
    }

    @GetMapping("/game/{gameId}")
    public AjaxResult game(@PathVariable Long gameId, @RequestParam(required = false) Long instanceId) {
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null) {
            return AjaxResult.error("游戏不存在");
        }
        if (instanceId != null && !instanceId.equals(game.getInstanceId())) {
            return AjaxResult.error("该游戏不属于当前活动");
        }
        return AjaxResult.success(game);
    }

    @PostMapping("/game/{gameId}/enter")
    public AjaxResult enterGame(@PathVariable Long gameId, @RequestParam(required = false) Long instanceId) {
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null) {
            return AjaxResult.error("游戏不存在");
        }
        // 扫码或活动页进入时必须校验活动实例，不能只凭 gameId 跨活动进入同名游戏。
        if (instanceId != null && !instanceId.equals(game.getInstanceId())) {
            return AjaxResult.error("该游戏不属于当前活动");
        }

        StudentCheckin student = getCurrentStudent();
        Long schoolId = findSchoolId(game);

        // 扫码入口必须幂等：已完成的游戏不再重复创建未完成记录。
        GkzhGameParticipation latest = activityWeekService.getLatestParticipation(gameId, student.getUserId());
        if (latest != null && "1".equals(latest.getStatus())) {
            return AjaxResult.success(game);
        }
        // 兼容旧活动记录：用户以前已经完成过旧版本关卡时，扫码直接补齐活动周记录。
        Integer legacyType = legacyParticipationType(game.getGameType());
        if (legacyType != null) {
            GkzhActivityParticipationRecord legacy = legacyParticipationService
                    .selectGkzhActivityParticipationRecordByUserIdAndActivityId(
                            student.getUserId(), game.getInstanceId(), legacyType);
            if (legacy != null && Integer.valueOf(1).equals(legacy.getStatus())) {
                GkzhGameParticipation migrated = latest == null ? new GkzhGameParticipation() : latest;
                migrated.setInstanceId(game.getInstanceId());
                migrated.setGameId(game.getGameId());
                migrated.setAreaId(game.getAreaId());
                migrated.setSchoolId(schoolId);
                migrated.setStudentId(student.getStuId());
                migrated.setUserId(student.getUserId());
                migrated.setScanTime(legacy.getParticipationTime());
                migrated.setStartTime(legacy.getParticipationTime());
                migrated.setFinishTime(legacy.getParticipationTime());
                migrated.setStatus("1");
                JSONObject migratedResult = new JSONObject();
                migratedResult.put("legacyRecordId", legacy.getRecordId());
                migratedResult.put("result", legacy.getResult());
                migratedResult.put("remark", legacy.getRemark());
                migrated.setResultJson(migratedResult.toJSONString());
                if (latest == null) {
                    activityWeekService.recordGameEnter(migrated);
                } else {
                    activityWeekService.updateGameParticipation(migrated);
                }
                return AjaxResult.success(game);
            }
        }

        if (latest != null && "0".equals(latest.getStatus())) {
            return AjaxResult.success(game);
        }

        GkzhGameParticipation participation = new GkzhGameParticipation();
        participation.setInstanceId(game.getInstanceId());
        participation.setGameId(game.getGameId());
        participation.setAreaId(game.getAreaId());
        participation.setSchoolId(schoolId);
        participation.setStudentId(student.getStuId());
        participation.setUserId(student.getUserId());
        participation.setScanTime(DateUtils.getNowDate());
        participation.setStartTime(DateUtils.getNowDate());
        participation.setStatus("0");

        activityWeekService.recordGameEnter(participation);
        return AjaxResult.success(game);
    }

    private Integer legacyParticipationType(String gameType) {
        if (gameType == null) {
            return null;
        }
        switch (gameType) {
            case "check-in": return 1;
            case "check-out": return 2;
            case "lottery": return 3;
            case "mind-window": return 4;
            case "survey": return 5;
            case "wjyd": return 6;
            case "cyzs": return 7;
            case "zytj": return 8;
            case "zyxxz": return 9;
            default: return null;
        }
    }

    @PostMapping("/game/{gameId}/complete")
    public AjaxResult completeGame(@PathVariable Long gameId, @RequestParam(required = false) Long instanceId, @RequestBody(required = false) Map<String, Object> body) {
        StudentCheckin student = getCurrentStudent();
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null) {
            return AjaxResult.error("游戏不存在");
        }
        if (instanceId != null && !instanceId.equals(game.getInstanceId())) {
            return AjaxResult.error("该游戏不属于当前活动");
        }
        GkzhGameParticipation participation = activityWeekService.getLatestParticipation(gameId, student.getUserId());
        if (participation == null) {
            return AjaxResult.error("暂无参与记录");
        }

        boolean passed = true;
        GkzhGameType gameType = activityWeekService.getGameType(game.getGameType());
        if (gameType != null && "answer".equals(gameType.getResultType())) {
            JSONObject config = JSON.parseObject(game.getConfig());
            double passScore = config == null ? 0 : config.getDoubleValue("passScore");
            double score = 0;
            if (body != null && body.get("score") != null) {
                score = Double.parseDouble(String.valueOf(body.get("score")));
            }
            passed = score >= passScore;
        }

        participation.setFinishTime(DateUtils.getNowDate());
        participation.setStatus(passed ? "1" : "2");
        participation.setResultJson(body == null ? null : JSON.toJSONString(body));
        activityWeekService.updateGameParticipation(participation);
        return AjaxResult.success(passed ? "完成" : "未通过");
    }

    private Long findSchoolId(GkzhActivityGame game) {
        List<GkzhActivityArea> areas = activityWeekService.listAreas(game.getInstanceId(), null);
        for (GkzhActivityArea area : areas) {
            if (area.getAreaId().equals(game.getAreaId())) {
                return area.getSchoolId();
            }
        }
        return null;
    }

    private Long getCurrentStudentSchoolId() {
        StudentCheckin student = getCurrentStudent();
        if (student == null || student.getStuId() == null) {
            return null;
        }
        GkzhStudent studentInfo = studentService.selectGkzhStudentByStudentId(student.getStuId());
        return studentInfo == null ? null : studentInfo.getSchoolId();
    }
}
