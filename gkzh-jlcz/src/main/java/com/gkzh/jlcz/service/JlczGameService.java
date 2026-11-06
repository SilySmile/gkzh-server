package com.gkzh.jlcz.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.jlcz.domain.JlczChoice;
import com.gkzh.jlcz.domain.JlczRecord;
import com.gkzh.jlcz.domain.JlczSetting;
import com.gkzh.jlcz.domain.JlczWeight;
import com.gkzh.jlcz.mapper.JlczChoiceMapper;
import com.gkzh.jlcz.mapper.JlczRecordMapper;
import com.gkzh.jlcz.support.JlczScoring;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 简历称重学生端业务：扫码进入、开始称重、提交勾选和结果查看。
 *
 * <p>扫码即创建参与记录并计入统计；提交时由服务端按当前砝码配置重新计算总重与通关判定，
 * 不信任客户端上报的重量。已完成记录只读，未通关是否可重新称重由规则参数控制。</p>
 */
@Service
public class JlczGameService {

    /** 游戏类型编码，与 gkzh_game_config.route、gkzh_game_type.game_type 保持一致。 */
    public static final String GAME_TYPE = "jlcz";

    /** 参与状态：进行中。 */
    public static final String STATUS_PARTICIPATING = "participating";

    /** 参与状态：已完成。 */
    public static final String STATUS_FINISHED = "finished";

    private final JlczRecordMapper recordMapper;
    private final JlczChoiceMapper choiceMapper;
    private final JlczConfigService configService;
    private final IActivityWeekService activityWeekService;

    public JlczGameService(JlczRecordMapper recordMapper, JlczChoiceMapper choiceMapper,
                           JlczConfigService configService, IActivityWeekService activityWeekService) {
        this.recordMapper = recordMapper;
        this.choiceMapper = choiceMapper;
        this.configService = configService;
        this.activityWeekService = activityWeekService;
    }

    /**
     * 扫码进入：校验学校、活动实例和游戏归属后幂等创建参与记录。
     * 扫码即计入参与统计，与业务记录在同一事务写入活动游戏参与表。
     */
    @Transactional
    public JlczRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId, Long studentId,
                            String studentNo, String studentName, Long departmentId, String major, String gender) {
        if (schoolId == null || instanceId == null || gameId == null || userId == null) {
            throw new ServiceException("缺少活动或学校信息，请重新扫码进入");
        }
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null || !instanceId.equals(game.getInstanceId()) || !GAME_TYPE.equals(game.getGameType())) {
            throw new ServiceException("该游戏不属于当前活动");
        }
        // 活动区域的学校归属必须与学生当前学校一致，避免扫他校二维码进入。
        Long activitySchoolId = activityWeekService.getGameSchoolId(game);
        if (activitySchoolId != null && !activitySchoolId.equals(schoolId)) {
            throw new ServiceException("该活动不属于当前学校，无法进入");
        }
        JlczRecord existing = recordMapper.selectOne(new QueryWrapper<JlczRecord>()
                .eq("school_id", schoolId).eq("instance_id", instanceId)
                .eq("game_id", gameId).eq("user_id", userId));
        if (existing != null) {
            // 再次扫码只补空的学生快照，不覆盖已经提交的结果和称重次数。
            UpdateWrapper<JlczRecord> patch = new UpdateWrapper<JlczRecord>().eq("record_id", existing.getRecordId());
            boolean changed = false;
            if (existing.getStudentId() == null && studentId != null) {
                existing.setStudentId(studentId);
                patch.set("student_id", studentId);
                changed = true;
            }
            if (isBlank(existing.getStudentNo()) && !isBlank(studentNo)) {
                existing.setStudentNo(studentNo);
                patch.set("student_no", studentNo);
                changed = true;
            }
            if (isBlank(existing.getStudentName()) && !isBlank(studentName)) {
                existing.setStudentName(studentName);
                patch.set("student_name", studentName);
                changed = true;
            }
            if (existing.getDepartmentId() == null && departmentId != null) {
                existing.setDepartmentId(departmentId);
                patch.set("department_id", departmentId);
                changed = true;
            }
            if (isBlank(existing.getMajor()) && !isBlank(major)) {
                existing.setMajor(major);
                patch.set("major", major);
                changed = true;
            }
            if (isBlank(existing.getGender()) && !isBlank(gender)) {
                existing.setGender(gender);
                patch.set("gender", gender);
                changed = true;
            }
            if (changed) {
                existing.setUpdateTime(DateUtils.getNowDate());
                patch.set("update_time", existing.getUpdateTime());
                recordMapper.update(null, patch);
            }
            return existing;
        }

        Date now = DateUtils.getNowDate();
        JlczRecord record = new JlczRecord();
        record.setSchoolId(schoolId);
        record.setInstanceId(instanceId);
        record.setGameId(gameId);
        record.setUserId(userId);
        record.setStudentId(studentId);
        record.setStudentNo(studentNo);
        record.setStudentName(studentName);
        record.setDepartmentId(departmentId);
        record.setMajor(major);
        record.setGender(gender);
        record.setGameType(GAME_TYPE);
        record.setStatus(STATUS_PARTICIPATING);
        record.setTotalWeight(0);
        record.setSelectedCount(0);
        record.setAttemptNo(1);
        record.setScanTime(now);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        recordMapper.insert(record);

        GkzhGameParticipation participation = new GkzhGameParticipation();
        participation.setInstanceId(instanceId);
        participation.setGameId(gameId);
        participation.setAreaId(game.getAreaId());
        participation.setSchoolId(schoolId);
        participation.setStudentId(studentId);
        participation.setUserId(userId);
        participation.setScanTime(now);
        participation.setStatus("0");
        activityWeekService.recordGameEnter(participation);
        return record;
    }

    /** 查询本人参与记录；不能读取其他学生的记录。 */
    public JlczRecord get(Long recordId, Long userId) {
        JlczRecord record = recordMapper.selectById(recordId);
        if (record == null || userId == null || !userId.equals(record.getUserId())
                || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    /** 开始称重：只记录开始时间，重复调用保持首次时间，用于计算称重用时和限时。 */
    @Transactional
    public Map<String, Object> start(Long recordId, Long userId) {
        JlczRecord record = lockRecord(recordId, userId);
        if (STATUS_FINISHED.equals(record.getStatus())) {
            return view(record);
        }
        JlczSetting setting = configService.getSetting();
        if (!"1".equals(setting.getEnabled())) {
            throw new ServiceException("简历称重暂未开放，请稍后再试");
        }
        if (record.getStartTime() == null) {
            Date now = DateUtils.getNowDate();
            recordMapper.update(null, new UpdateWrapper<JlczRecord>().eq("record_id", recordId)
                    .set("start_time", now).set("update_time", now));
            record.setStartTime(now);
            record.setUpdateTime(now);
        }
        return view(record);
    }

    /**
     * 提交本次勾选的砝码并结算结果。
     * 已完成记录再次提交时直接返回原结果，不覆盖首次成绩。
     *
     * @param weightNos 勾选的挂钩号列表，可包含 0-20 项
     */
    @Transactional
    public Map<String, Object> submit(Long recordId, Long userId, List<Integer> weightNos) {
        JlczRecord record = lockRecord(recordId, userId);
        if (STATUS_FINISHED.equals(record.getStatus())) {
            return view(record);
        }
        JlczSetting setting = configService.getSetting();
        if (!"1".equals(setting.getEnabled())) {
            throw new ServiceException("简历称重暂未开放，请稍后再试");
        }
        List<JlczWeight> active = configService.listActive();
        Map<Integer, JlczWeight> byNo = new LinkedHashMap<>();
        for (JlczWeight weight : active) {
            byNo.put(weight.getWeightNo(), weight);
        }
        Set<Integer> selectedNos = new LinkedHashSet<>();
        if (weightNos != null) {
            for (Integer number : weightNos) {
                if (number == null) {
                    continue;
                }
                if (!byNo.containsKey(number)) {
                    throw new ServiceException("砝码编号无效，请刷新页面后重新称重");
                }
                if (!selectedNos.add(number)) {
                    throw new ServiceException("勾选的砝码存在重复项，请重新称重");
                }
            }
        }
        Date now = DateUtils.getNowDate();
        Date startedAt = record.getStartTime() == null ? record.getScanTime() : record.getStartTime();
        long duration = startedAt == null ? 0L : Math.max(0L, now.getTime() - startedAt.getTime());
        // 限时开启时，超过时限仍接收提交，但强制判定为未通关，避免学生卡在无法提交的进行中状态。
        boolean timedOut = setting.getTimeLimitSeconds() != null && setting.getTimeLimitSeconds() > 0
                && duration > setting.getTimeLimitSeconds() * 1000L;

        List<JlczWeight> selected = new ArrayList<>();
        JSONArray selectedJson = new JSONArray();
        for (Integer number : selectedNos) {
            JlczWeight weight = byNo.get(number);
            selected.add(weight);
            JSONObject item = new JSONObject();
            item.put("weightNo", weight.getWeightNo());
            item.put("weightId", weight.getWeightId());
            item.put("label", weight.getLabel());
            item.put("icon", weight.getIcon());
            item.put("weightG", weight.getWeightG());
            item.put("categoryCode", weight.getCategoryCode());
            item.put("categoryName", weight.getCategoryName());
            selectedJson.add(item);
        }
        JlczScoring.Outcome outcome = JlczScoring.compute(selected, setting);
        String result = timedOut ? JlczScoring.RESULT_FAIL : outcome.getResult();
        JSONObject categoryJson = new JSONObject();
        for (Map.Entry<String, Integer> entry : outcome.getCategoryTotals().entrySet()) {
            categoryJson.put(entry.getKey(), entry.getValue());
        }

        recordMapper.update(null, new UpdateWrapper<JlczRecord>().eq("record_id", recordId)
                .set("status", STATUS_FINISHED)
                .set("result", result)
                .set("total_weight", outcome.getTotalWeight())
                .set("selected_count", selected.size())
                .set("category_json", categoryJson.toJSONString())
                .set("selected_json", selectedJson.toJSONString())
                .set("start_time", startedAt)
                .set("finish_time", now)
                .set("duration_ms", duration)
                .set("update_time", now));
        for (JlczWeight weight : selected) {
            JlczChoice choice = new JlczChoice();
            choice.setRecordId(recordId);
            choice.setAttemptNo(record.getAttemptNo() == null ? 1 : record.getAttemptNo());
            choice.setWeightId(weight.getWeightId());
            choice.setWeightNo(weight.getWeightNo());
            choice.setWeightLabel(weight.getLabel());
            choice.setWeightG(weight.getWeightG());
            choice.setCategoryCode(weight.getCategoryCode());
            choice.setCreateTime(now);
            choiceMapper.insert(choice);
        }
        record.setStatus(STATUS_FINISHED);
        record.setResult(result);
        record.setTotalWeight(outcome.getTotalWeight());
        record.setSelectedCount(selected.size());
        record.setCategoryJson(categoryJson.toJSONString());
        record.setSelectedJson(selectedJson.toJSONString());
        record.setStartTime(startedAt);
        record.setFinishTime(now);
        record.setDurationMs(duration);
        record.setUpdateTime(now);

        completeParticipation(record, outcome);
        return view(record);
    }

    /**
     * 未通关重新称重：同一参与记录 attempt_no 累加，历史称重明细保留用于统计。
     * 已通关、未完成首次称重或规则禁止重试时拒绝。
     */
    @Transactional
    public Map<String, Object> restart(Long recordId, Long userId) {
        JlczRecord record = lockRecord(recordId, userId);
        JlczSetting setting = configService.getSetting();
        if (!"1".equals(setting.getEnabled())) {
            throw new ServiceException("简历称重暂未开放，请稍后再试");
        }
        if (!STATUS_FINISHED.equals(record.getStatus())) {
            throw new ServiceException("当前还没有完成一次称重");
        }
        if (JlczScoring.RESULT_PASS.equals(record.getResult())) {
            throw new ServiceException("本次称重已通关，结果不可修改");
        }
        if (!"1".equals(setting.getAllowRetry())) {
            throw new ServiceException("本次结果已确定，不可重新称重");
        }
        Date now = DateUtils.getNowDate();
        int attemptNo = (record.getAttemptNo() == null ? 1 : record.getAttemptNo()) + 1;
        recordMapper.update(null, new UpdateWrapper<JlczRecord>().eq("record_id", recordId)
                .set("status", STATUS_PARTICIPATING)
                .set("result", null)
                .set("total_weight", 0)
                .set("selected_count", 0)
                .set("category_json", null)
                .set("selected_json", null)
                .set("attempt_no", attemptNo)
                .set("start_time", now)
                .set("finish_time", null)
                .set("duration_ms", null)
                .set("update_time", now));
        record.setStatus(STATUS_PARTICIPATING);
        record.setResult(null);
        record.setTotalWeight(0);
        record.setSelectedCount(0);
        record.setCategoryJson(null);
        record.setSelectedJson(null);
        record.setAttemptNo(attemptNo);
        record.setStartTime(now);
        record.setFinishTime(null);
        record.setDurationMs(null);
        record.setUpdateTime(now);
        return view(record);
    }

    /** 完成活动游戏参与记录；记录缺失时不阻断学生查看结果。 */
    private void completeParticipation(JlczRecord record, JlczScoring.Outcome outcome) {
        GkzhGameParticipation participation = activityWeekService.getLatestParticipation(record.getGameId(), record.getUserId());
        if (participation == null) {
            return;
        }
        JSONObject result = new JSONObject();
        result.put("result", record.getResult());
        result.put("totalWeight", outcome.getTotalWeight());
        result.put("passWeight", outcome.getPassWeight());
        result.put("attemptNo", record.getAttemptNo());
        participation.setStatus("1");
        participation.setFinishTime(DateUtils.getNowDate());
        participation.setResultJson(result.toJSONString());
        activityWeekService.updateGameParticipation(participation);
    }

    /** 学生端展示数据：砝码目录、本次勾选、规则参数和最终结果。 */
    public Map<String, Object> view(JlczRecord record) {
        JlczSetting setting = configService.getSetting();
        List<JlczWeight> weights = configService.listActive();
        List<JlczChoice> choices = choiceMapper.selectList(new QueryWrapper<JlczChoice>()
                .eq("record_id", record.getRecordId())
                .eq("attempt_no", record.getAttemptNo() == null ? 1 : record.getAttemptNo())
                .orderByAsc("weight_no"));
        Set<Integer> selectedNos = new HashSet<>();
        for (JlczChoice choice : choices) {
            selectedNos.add(choice.getWeightNo());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recordId", record.getRecordId());
        data.put("instanceId", record.getInstanceId());
        // 结果页据此返回对应的生涯活动或就业活动页面。
        GkzhActivityWeekInstance instance = record.getInstanceId() == null ? null
                : activityWeekService.getInstance(record.getInstanceId());
        if (instance != null) {
            data.put("bizType", instance.getBizType());
        }
        data.put("gameId", record.getGameId());
        data.put("status", record.getStatus());
        data.put("attemptNo", record.getAttemptNo());
        data.put("scanTime", record.getScanTime());
        data.put("startTime", record.getStartTime());
        data.put("finishTime", record.getFinishTime());
        data.put("durationMs", record.getDurationMs());

        Map<String, Object> settingView = new LinkedHashMap<>();
        settingView.put("leftWeightG", setting.getLeftWeightG());
        settingView.put("passWeightG", setting.getPassWeightG());
        settingView.put("timeLimitSeconds", setting.getTimeLimitSeconds());
        settingView.put("allowRetry", setting.getAllowRetry());
        data.put("setting", settingView);

        List<Map<String, Object>> catalog = new ArrayList<>();
        for (JlczWeight weight : weights) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("weightNo", weight.getWeightNo());
            item.put("categoryCode", weight.getCategoryCode());
            item.put("categoryName", weight.getCategoryName());
            item.put("label", weight.getLabel());
            item.put("icon", weight.getIcon());
            item.put("imageUrl", weight.getImageUrl());
            item.put("weightG", weight.getWeightG());
            item.put("conditionText", weight.getConditionText());
            item.put("selected", selectedNos.contains(weight.getWeightNo()));
            catalog.add(item);
        }
        data.put("weights", catalog);

        List<Map<String, Object>> selected = new ArrayList<>();
        for (JlczChoice choice : choices) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("weightNo", choice.getWeightNo());
            item.put("label", choice.getWeightLabel());
            item.put("weightG", choice.getWeightG());
            item.put("categoryCode", choice.getCategoryCode());
            item.put("categoryName", JlczScoring.categoryName(choice.getCategoryCode()));
            selected.add(item);
        }
        data.put("selected", selected);

        if (STATUS_FINISHED.equals(record.getStatus())) {
            Map<String, Object> result = new LinkedHashMap<>();
            boolean pass = JlczScoring.RESULT_PASS.equals(record.getResult());
            int total = record.getTotalWeight() == null ? 0 : record.getTotalWeight();
            int passWeight = setting.getPassWeightG() == null ? 80 : setting.getPassWeightG();
            result.put("pass", pass);
            result.put("result", record.getResult());
            result.put("totalWeight", total);
            result.put("passWeight", passWeight);
            result.put("gap", pass ? 0 : Math.max(0, passWeight - total));
            result.put("balanced", total == passWeight);
            result.put("selectedCount", record.getSelectedCount());
            result.put("durationMs", record.getDurationMs());
            result.put("attemptNo", record.getAttemptNo());
            result.put("timedOut", isTimedOut(record, setting));
            result.put("categoryTotals", categoryTotals(record, choices));
            result.put("suggestions", pass ? new ArrayList<>()
                    : JlczScoring.suggestions(weights, selectedNos, 3));
            data.put("result", result);
        }
        return data;
    }

    /** 是否因超过称重限时被判为未通关；未开启限时或未提交时返回 false。 */
    private boolean isTimedOut(JlczRecord record, JlczSetting setting) {
        if (setting.getTimeLimitSeconds() == null || setting.getTimeLimitSeconds() <= 0
                || record.getDurationMs() == null || !STATUS_FINISHED.equals(record.getStatus())) {
            return false;
        }
        return record.getDurationMs() > setting.getTimeLimitSeconds() * 1000L;
    }

    /** 分类小计优先取记录快照，历史记录缺失时按本次选择明细重新汇总。 */
    private List<Map<String, Object>> categoryTotals(JlczRecord record, List<JlczChoice> choices) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (String code : JlczScoring.CATEGORY_ORDER) {
            totals.put(code, 0);
        }
        boolean parsed = false;
        if (!isBlank(record.getCategoryJson())) {
            try {
                JSONObject json = JSON.parseObject(record.getCategoryJson());
                for (String code : JlczScoring.CATEGORY_ORDER) {
                    totals.put(code, json.getIntValue(code));
                }
                parsed = true;
            } catch (RuntimeException ignored) {
                // 快照损坏时回退到选择明细汇总。
            }
        }
        if (!parsed) {
            for (JlczChoice choice : choices) {
                totals.merge(choice.getCategoryCode(), choice.getWeightG() == null ? 0 : choice.getWeightG(),
                        Integer::sum);
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String code : JlczScoring.CATEGORY_ORDER) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("categoryCode", code);
            row.put("categoryName", JlczScoring.categoryName(code));
            row.put("totalWeight", totals.get(code));
            rows.add(row);
        }
        return rows;
    }

    /** 提交、重试前锁定记录行，避免并发请求产生两套选择或重复完成。 */
    private JlczRecord lockRecord(Long recordId, Long userId) {
        JlczRecord record = recordMapper.selectOne(new QueryWrapper<JlczRecord>()
                .eq("record_id", recordId).last("FOR UPDATE"));
        if (record == null || userId == null || !userId.equals(record.getUserId())
                || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
