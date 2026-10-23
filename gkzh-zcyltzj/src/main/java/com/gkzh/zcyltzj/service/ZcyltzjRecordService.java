package com.gkzh.zcyltzj.service;

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
import com.gkzh.zcyltzj.domain.ZcyltzjPoint;
import com.gkzh.zcyltzj.domain.ZcyltzjQuestion;
import com.gkzh.zcyltzj.domain.ZcyltzjRecord;
import com.gkzh.zcyltzj.mapper.ZcyltzjPointMapper;
import com.gkzh.zcyltzj.mapper.ZcyltzjQuestionMapper;
import com.gkzh.zcyltzj.mapper.ZcyltzjRecordMapper;
import com.gkzh.zcyltzj.support.ZcyltzjScoring;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 骰子局学生端业务：扫码进入、掷骰子抽题、提交答案和结果计算。
 *
 * <p>全部写操作都会校验记录归属与活动实例；一局只允许掷一次骰子，
 * 完成后结果永久只读，重复提交返回已有结果。</p>
 */
@Service
public class ZcyltzjRecordService {

    /** 游戏类型编码，与 gkzh_game_config.route、gkzh_game_type.game_type 保持一致。 */
    public static final String GAME_TYPE = "zcyltzj";

    private final ZcyltzjRecordMapper recordMapper;
    private final ZcyltzjPointMapper pointMapper;
    private final ZcyltzjQuestionMapper questionMapper;
    private final IActivityWeekService activityWeekService;

    public ZcyltzjRecordService(ZcyltzjRecordMapper recordMapper, ZcyltzjPointMapper pointMapper,
                                ZcyltzjQuestionMapper questionMapper, IActivityWeekService activityWeekService) {
        this.recordMapper = recordMapper;
        this.pointMapper = pointMapper;
        this.questionMapper = questionMapper;
        this.activityWeekService = activityWeekService;
    }

    /**
     * 扫码进入游戏：校验学校、活动实例和游戏归属后幂等创建参与记录。
     * 扫码即计入参与统计，与业务记录在同一事务写入活动游戏参与表。
     */
    @Transactional
    public ZcyltzjRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId, Long studentId,
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
        ZcyltzjRecord existing = recordMapper.selectOne(new QueryWrapper<ZcyltzjRecord>()
                .eq("school_id", schoolId).eq("instance_id", instanceId)
                .eq("game_id", gameId).eq("user_id", userId));
        if (existing != null) {
            // 再次扫码只补空的学生快照，不覆盖已经掷出的点数、答案和完成状态。
            UpdateWrapper<ZcyltzjRecord> patch = new UpdateWrapper<ZcyltzjRecord>().eq("record_id", existing.getRecordId());
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
        ZcyltzjRecord record = new ZcyltzjRecord();
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
        record.setStatus("participating");
        record.setStage("entered");
        record.setRedCount(0);
        record.setGreenCount(0);
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
    public ZcyltzjRecord get(Long recordId, Long userId) {
        ZcyltzjRecord record = recordMapper.selectById(recordId);
        if (record == null || userId == null || !userId.equals(record.getUserId())) {
            throw new ServiceException("参与记录不存在");
        }
        if (!GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    /** 掷骰子：服务端生成 1-6 点数并固定本局试题快照，重复调用返回同一结果。 */
    @Transactional
    public Map<String, Object> roll(Long recordId, Long userId) {
        ZcyltzjRecord record = lockRecord(recordId, userId);
        if ("finished".equals(record.getStatus())) {
            throw new ServiceException("本次游戏已完成，结果仅可查看");
        }
        // 已掷出点数时直接返回当前进度，避免重复点击改变点数。
        if (record.getPointNo() != null) {
            return view(record);
        }
        int pointNo = ThreadLocalRandom.current().nextInt(1, 7);
        ZcyltzjPoint point = pointMapper.selectOne(new QueryWrapper<ZcyltzjPoint>()
                .eq("point_no", pointNo).eq("status", "0"));
        if (point == null) {
            throw new ServiceException("骰子点 " + pointNo + " 暂未配置，请联系管理员");
        }
        List<ZcyltzjQuestion> questions = activeQuestions(pointNo);
        if (questions.isEmpty()) {
            throw new ServiceException("“" + point.getPointName() + "”暂未配置试题，请联系管理员");
        }

        JSONObject snapshot = new JSONObject();
        snapshot.put("pointNo", pointNo);
        snapshot.put("pointName", point.getPointName());
        snapshot.put("theme", point.getTheme());
        snapshot.put("projection", point.getProjection());
        JSONArray items = new JSONArray();
        for (ZcyltzjQuestion question : questions) {
            JSONObject item = new JSONObject();
            item.put("questionId", question.getQuestionId());
            item.put("questionNo", question.getQuestionNo());
            item.put("content", question.getContent());
            item.put("optionA", question.getOptionA());
            item.put("optionB", question.getOptionB());
            item.put("optionC", question.getOptionC());
            item.put("optionD", question.getOptionD());
            items.add(item);
        }
        snapshot.put("questions", items);

        Date now = DateUtils.getNowDate();
        // 只更新本局字段，避免整体 updateById 覆盖并发写入的其他字段。
        recordMapper.update(null, new UpdateWrapper<ZcyltzjRecord>().eq("record_id", recordId)
                .set("point_no", pointNo)
                .set("point_name", point.getPointName())
                .set("question_snapshot_json", JSON.toJSONString(snapshot))
                .set("stage", "answering")
                .set("start_time", now)
                .set("update_time", now));
        record.setPointNo(pointNo);
        record.setPointName(point.getPointName());
        record.setQuestionSnapshotJson(JSON.toJSONString(snapshot));
        record.setStage("answering");
        record.setStartTime(now);
        record.setUpdateTime(now);
        return view(record);
    }

    /**
     * 提交整套答案并结算压力值：必须答完本局全部题目，选项只能是 A、B、C、D。
     * 已完成记录再次提交时直接返回原结果，不覆盖首次成绩。
     */
    @Transactional
    public Map<String, Object> submit(Long recordId, Long userId, List<Map<String, Object>> answers) {
        ZcyltzjRecord record = lockRecord(recordId, userId);
        if ("finished".equals(record.getStatus())) {
            return view(record);
        }
        if (record.getPointNo() == null || isBlank(record.getQuestionSnapshotJson())) {
            throw new ServiceException("请先掷骰子开始答题");
        }
        JSONObject snapshot = JSON.parseObject(record.getQuestionSnapshotJson());
        JSONArray questions = snapshot.getJSONArray("questions");
        if (questions == null || questions.isEmpty()) {
            throw new ServiceException("本局试题已失效，请重新进入游戏");
        }
        Map<Integer, String> submitted = new HashMap<>();
        if (answers != null) {
            for (Map<String, Object> item : answers) {
                if (item == null || item.get("questionNo") == null) {
                    continue;
                }
                Integer no;
                try {
                    no = Integer.valueOf(String.valueOf(item.get("questionNo")).trim());
                } catch (NumberFormatException e) {
                    throw new ServiceException("答案题号无效，请重新作答");
                }
                String option = item.get("option") == null ? "" : String.valueOf(item.get("option")).trim().toUpperCase();
                if (!"A".equals(option) && !"B".equals(option) && !"C".equals(option) && !"D".equals(option)) {
                    throw new ServiceException("每道题只能选择 A、B、C 或 D");
                }
                if (submitted.put(no, option) != null) {
                    throw new ServiceException("答案中存在重复题号，请重新作答");
                }
            }
        }

        List<String> selected = new ArrayList<>();
        JSONArray answerJson = new JSONArray();
        for (int i = 0; i < questions.size(); i++) {
            JSONObject question = questions.getJSONObject(i);
            Integer no = question.getInteger("questionNo");
            String option = submitted.get(no);
            if (option == null) {
                throw new ServiceException("还有题目未作答，请完成全部题目后提交");
            }
            selected.add(option);
            JSONObject item = new JSONObject();
            item.put("questionNo", no);
            item.put("questionId", question.getLong("questionId"));
            item.put("option", option);
            answerJson.add(item);
        }

        ZcyltzjScoring.Outcome outcome = ZcyltzjScoring.compute(selected);
        Date now = DateUtils.getNowDate();
        recordMapper.update(null, new UpdateWrapper<ZcyltzjRecord>().eq("record_id", recordId)
                .set("answers_json", JSON.toJSONString(answerJson))
                .set("red_count", outcome.getRedCount())
                .set("green_count", outcome.getGreenCount())
                .set("pressure_value", outcome.getPressureValue())
                .set("color", outcome.getColor())
                .set("status", "finished")
                .set("stage", "finished")
                .set("finish_time", now)
                .set("update_time", now));
        record.setAnswersJson(JSON.toJSONString(answerJson));
        record.setRedCount(outcome.getRedCount());
        record.setGreenCount(outcome.getGreenCount());
        record.setPressureValue(outcome.getPressureValue());
        record.setColor(outcome.getColor());
        record.setStatus("finished");
        record.setStage("finished");
        record.setFinishTime(now);
        record.setUpdateTime(now);

        completeParticipation(record, outcome);
        return view(record);
    }

    /** 完成活动游戏参与记录；记录缺失时不阻断学生查看结果。 */
    private void completeParticipation(ZcyltzjRecord record, ZcyltzjScoring.Outcome outcome) {
        GkzhGameParticipation participation = activityWeekService.getLatestParticipation(record.getGameId(), record.getUserId());
        if (participation == null) {
            return;
        }
        JSONObject result = new JSONObject();
        result.put("pointNo", record.getPointNo());
        result.put("pointName", record.getPointName());
        result.put("pressureValue", outcome.getPressureValue());
        result.put("color", outcome.getColor());
        participation.setStatus("1");
        participation.setFinishTime(DateUtils.getNowDate());
        participation.setResultJson(JSON.toJSONString(result));
        activityWeekService.updateGameParticipation(participation);
    }

    /** 学生端展示数据：点数、试题、已提交答案和最终结果。 */
    public Map<String, Object> view(ZcyltzjRecord record) {
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
        data.put("stage", record.getStage());
        data.put("pointNo", record.getPointNo());
        data.put("pointName", record.getPointName());
        data.put("scanTime", record.getScanTime());
        data.put("startTime", record.getStartTime());
        data.put("finishTime", record.getFinishTime());
        if (!isBlank(record.getQuestionSnapshotJson())) {
            try {
                JSONObject snapshot = JSON.parseObject(record.getQuestionSnapshotJson());
                data.put("pointName", snapshot.getString("pointName") == null ? record.getPointName() : snapshot.getString("pointName"));
                data.put("theme", snapshot.getString("theme"));
                data.put("projection", snapshot.getString("projection"));
                data.put("questions", snapshot.getJSONArray("questions"));
            } catch (Exception ignored) {
                // 快照异常时仍返回记录状态，页面按“试题失效”提示重新进入。
            }
        }
        Map<Integer, String> answers = new HashMap<>();
        if (!isBlank(record.getAnswersJson())) {
            try {
                for (Object item : JSON.parseArray(record.getAnswersJson())) {
                    JSONObject answer = (JSONObject) item;
                    answers.put(answer.getInteger("questionNo"), answer.getString("option"));
                }
            } catch (Exception ignored) {
                // 历史答案格式异常不影响展示结果。
            }
        }
        data.put("answers", answers);
        if ("finished".equals(record.getStatus())) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("pressureValue", record.getPressureValue());
            result.put("color", record.getColor());
            result.put("redCount", record.getRedCount());
            result.put("greenCount", record.getGreenCount());
            int total = (record.getRedCount() == null ? 0 : record.getRedCount())
                    + (record.getGreenCount() == null ? 0 : record.getGreenCount());
            result.put("total", total);
            data.put("result", result);
        }
        return data;
    }

    private List<ZcyltzjQuestion> activeQuestions(int pointNo) {
        return questionMapper.selectList(new QueryWrapper<ZcyltzjQuestion>()
                .eq("point_no", pointNo).eq("status", "0")
                .orderByAsc("sort_order", "question_no"));
    }

    /** 掷骰子、提交答案前锁定记录行，避免并发请求产生两套答案或两次完成。 */
    private ZcyltzjRecord lockRecord(Long recordId, Long userId) {
        ZcyltzjRecord record = recordMapper.selectOne(new QueryWrapper<ZcyltzjRecord>()
                .eq("record_id", recordId).last("FOR UPDATE"));
        if (record == null || userId == null || !userId.equals(record.getUserId())) {
            throw new ServiceException("参与记录不存在");
        }
        if (!GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
