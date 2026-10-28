package com.gkzh.nlld.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.nlld.domain.NlldAnswer;
import com.gkzh.nlld.domain.NlldDimension;
import com.gkzh.nlld.domain.NlldOption;
import com.gkzh.nlld.domain.NlldRecord;
import com.gkzh.nlld.domain.NlldRound;
import com.gkzh.nlld.mapper.NlldAnswerMapper;
import com.gkzh.nlld.mapper.NlldOptionMapper;
import com.gkzh.nlld.mapper.NlldRecordMapper;
import com.gkzh.nlld.support.NlldScoring;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 能量雷达学生端业务服务。
 *
 * <p>扫码进入时校验学校、活动实例和游戏归属并幂等创建参与记录；每关提交 2 件物品，
 * 第 6 关提交后在同一个事务内计分、写入 TOP2/多元开放型结果并同步活动参与完成状态。</p>
 */
@Service
public class NlldGameService {

    /** 游戏类型，固定为 nlld。 */
    public static final String GAME_TYPE = "nlld";

    /** 总关卡数。 */
    public static final int TOTAL_ROUNDS = 6;

    /** 每关必须选择的物品数。 */
    public static final int PICKS_PER_ROUND = 2;

    private final NlldRecordMapper recordMapper;
    private final NlldAnswerMapper answerMapper;
    private final NlldOptionMapper optionMapper;
    private final NlldConfigService configService;
    private final IActivityWeekService activityWeekService;

    public NlldGameService(NlldRecordMapper recordMapper, NlldAnswerMapper answerMapper,
                           NlldOptionMapper optionMapper, NlldConfigService configService,
                           IActivityWeekService activityWeekService) {
        this.recordMapper = recordMapper;
        this.answerMapper = answerMapper;
        this.optionMapper = optionMapper;
        this.configService = configService;
        this.activityWeekService = activityWeekService;
    }

    /**
     * 扫码进入：校验学校、活动实例、游戏归属后幂等创建参与记录。
     * 扫码即计入参与统计，并与业务记录在同一事务写入活动游戏参与表。
     */
    @Transactional
    public NlldRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId, Long studentId,
                            String studentNo, String studentName, Long departmentId, String major, String gender) {
        if (schoolId == null || instanceId == null || gameId == null || userId == null) {
            throw new ServiceException("缺少活动或学校信息，请重新扫码进入");
        }
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null || !instanceId.equals(game.getInstanceId()) || !GAME_TYPE.equals(game.getGameType())) {
            throw new ServiceException("该游戏不属于当前活动");
        }
        Long activitySchoolId = activityWeekService.getGameSchoolId(game);
        if (activitySchoolId != null && !activitySchoolId.equals(schoolId)) {
            throw new ServiceException("该活动不属于当前学校，无法进入");
        }

        NlldRecord existing = recordMapper.selectOne(new QueryWrapper<NlldRecord>()
                .eq("school_id", schoolId).eq("instance_id", instanceId)
                .eq("game_id", gameId).eq("user_id", userId));
        if (existing != null) {
            // 再次扫码只补空的学生快照，不覆盖已经提交的关卡和结果。
            UpdateWrapper<NlldRecord> patch = new UpdateWrapper<NlldRecord>().eq("record_id", existing.getRecordId());
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
        NlldRecord record = new NlldRecord();
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
        record.setAnsweredRounds(0);
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

    /** 按编号查询本人记录；不存在或不属于本人时返回业务错误。 */
    public NlldRecord get(Long recordId, Long userId) {
        NlldRecord record = recordId == null ? null : recordMapper.selectById(recordId);
        if (record == null || userId == null || !userId.equals(record.getUserId())
                || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    /** 查询本人测评进度、关卡配置、已选物品和结果。 */
    public Map<String, Object> viewRecord(Long recordId, Long userId) {
        return view(get(recordId, userId));
    }

    /**
     * 提交一关的 2 件物品。
     * 必须按 1→6 顺序提交；已完成记录只读；第 6 关提交后结算并完成活动参与。
     */
    @Transactional
    public Map<String, Object> answer(Long recordId, Long userId, Integer roundNo, List<Long> optionIds) {
        NlldRecord record = lockRecord(recordId, userId);
        if ("finished".equals(record.getStatus())) {
            throw new ServiceException("本次测评已完成，结果仅可查看");
        }
        int answered = record.getAnsweredRounds() == null ? 0 : record.getAnsweredRounds();
        if (roundNo == null || roundNo < 1 || roundNo > TOTAL_ROUNDS) {
            throw new ServiceException("关卡参数无效，请重新进入");
        }
        if (roundNo != answered + 1) {
            throw new ServiceException("请按顺序完成关卡");
        }
        if (optionIds == null || optionIds.size() != PICKS_PER_ROUND) {
            throw new ServiceException("每关必须选择 2 件物品");
        }
        List<Long> picks = new ArrayList<>();
        for (Long optionId : optionIds) {
            if (optionId != null) {
                picks.add(optionId);
            }
        }
        if (picks.size() != PICKS_PER_ROUND || new HashSet<>(picks).size() != PICKS_PER_ROUND) {
            throw new ServiceException("每关必须选择 2 件不同的物品");
        }
        NlldRound round = configService.getActiveRound(roundNo);
        if (round == null) {
            throw new ServiceException("关卡已停用，请联系管理员");
        }
        List<NlldOption> options = optionMapper.selectBatchIds(picks);
        if (options == null || options.size() != PICKS_PER_ROUND) {
            throw new ServiceException("所选物品不存在，请重新选择");
        }
        Map<Long, NlldDimension> dimensionMap = new LinkedHashMap<>();
        for (NlldDimension dimension : configService.listDimensions()) {
            dimensionMap.put(dimension.getDimensionId(), dimension);
        }
        Date now = DateUtils.getNowDate();
        for (NlldOption option : options) {
            if (!round.getRoundId().equals(option.getRoundId()) || !"0".equals(option.getStatus())) {
                throw new ServiceException("所选物品不属于当前关卡，请重新选择");
            }
            NlldDimension dimension = dimensionMap.get(option.getDimensionId());
            if (dimension == null) {
                throw new ServiceException("物品维度配置缺失，请联系管理员");
            }
            NlldAnswer answer = new NlldAnswer();
            answer.setRecordId(record.getRecordId());
            answer.setRoundNo(roundNo);
            answer.setOptionId(option.getOptionId());
            answer.setOptionLabel(option.getOptionLabel());
            answer.setOptionName(option.getOptionName());
            answer.setDimensionId(dimension.getDimensionId());
            answer.setDimensionCode(dimension.getDimensionCode());
            answer.setCreateTime(now);
            answerMapper.insert(answer);
        }

        if (roundNo < TOTAL_ROUNDS) {
            recordMapper.update(null, new UpdateWrapper<NlldRecord>().eq("record_id", record.getRecordId())
                    .set("answered_rounds", roundNo)
                    .set("stage", "choosing")
                    .set("update_time", now));
            record.setAnsweredRounds(roundNo);
            record.setStage("choosing");
            record.setUpdateTime(now);
            return view(record);
        }
        return finish(record, now);
    }

    /** 后台删除参与记录：同步清理选择明细和该活动游戏的全部活动参与行。 */
    @Transactional
    public void deleteRecord(Long recordId) {
        NlldRecord record = recordId == null ? null : recordMapper.selectById(recordId);
        if (record == null || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        if (record.getInstanceId() == null || record.getGameId() == null || record.getUserId() == null) {
            throw new ServiceException("参与记录缺少活动上下文，无法删除");
        }
        answerMapper.delete(new QueryWrapper<NlldAnswer>().eq("record_id", recordId));
        activityWeekService.deleteGameParticipations(record.getInstanceId(), record.getGameId(), record.getUserId());
        recordMapper.deleteById(recordId);
    }

    /** 第 6 关结算：汇总 15 维得分，写入结果快照并完成活动参与。 */
    private Map<String, Object> finish(NlldRecord record, Date now) {
        List<NlldAnswer> answers = answerMapper.selectList(new QueryWrapper<NlldAnswer>()
                .eq("record_id", record.getRecordId()));
        Map<String, Integer> scores = NlldScoring.scoreOf(answers);
        String resultType = NlldScoring.resultType(scores);
        List<NlldDimension> dimensions = configService.listDimensions();
        List<NlldDimension> top = NlldScoring.twoPointDimensions(scores, dimensions);

        Map<String, Object> scoreSnapshot = new LinkedHashMap<>();
        scoreSnapshot.put("resultType", resultType);
        scoreSnapshot.put("scores", scores);

        UpdateWrapper<NlldRecord> update = new UpdateWrapper<NlldRecord>().eq("record_id", record.getRecordId())
                .set("answered_rounds", TOTAL_ROUNDS)
                .set("stage", "finished")
                .set("status", "finished")
                .set("result_type", resultType)
                .set("score_json", JSON.toJSONString(scoreSnapshot))
                .set("finish_time", now)
                .set("update_time", now);
        if (NlldScoring.RESULT_TOP2.equals(resultType) && top.size() == PICKS_PER_ROUND) {
            update.set("top1_dimension_id", top.get(0).getDimensionId())
                    .set("top1_dimension_name", top.get(0).getDimensionName())
                    .set("top2_dimension_id", top.get(1).getDimensionId())
                    .set("top2_dimension_name", top.get(1).getDimensionName());
            record.setTop1DimensionId(top.get(0).getDimensionId());
            record.setTop1DimensionName(top.get(0).getDimensionName());
            record.setTop2DimensionId(top.get(1).getDimensionId());
            record.setTop2DimensionName(top.get(1).getDimensionName());
        }
        recordMapper.update(null, update);

        record.setAnsweredRounds(TOTAL_ROUNDS);
        record.setStage("finished");
        record.setStatus("finished");
        record.setResultType(resultType);
        record.setScoreJson(JSON.toJSONString(scoreSnapshot));
        record.setFinishTime(now);
        record.setUpdateTime(now);
        completeParticipation(record, resultType);
        return view(record);
    }

    /** 完成时同步活动游戏参与记录；参与记录缺失不影响学生查看结果。 */
    private void completeParticipation(NlldRecord record, String resultType) {
        GkzhGameParticipation participation = activityWeekService.getLatestParticipation(
                record.getGameId(), record.getUserId());
        if (participation == null) {
            return;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resultType", resultType);
        result.put("top1", record.getTop1DimensionName());
        result.put("top2", record.getTop2DimensionName());
        participation.setStatus("1");
        participation.setFinishTime(DateUtils.getNowDate());
        participation.setResultJson(JSON.toJSONString(result));
        activityWeekService.updateGameParticipation(participation);
    }

    /** 提交前锁定记录行，避免并发请求重复写入同一关或重复结算。 */
    private NlldRecord lockRecord(Long recordId, Long userId) {
        NlldRecord record = recordId == null ? null : recordMapper.selectOne(new QueryWrapper<NlldRecord>()
                .eq("record_id", recordId).last("FOR UPDATE"));
        if (record == null || userId == null || !userId.equals(record.getUserId())
                || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    /** 组装学生端展示数据：进度、关卡、已选物品和结果。 */
    public Map<String, Object> view(NlldRecord record) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recordId", record.getRecordId());
        data.put("instanceId", record.getInstanceId());
        data.put("gameId", record.getGameId());
        data.put("status", record.getStatus());
        data.put("stage", record.getStage());
        data.put("answeredRounds", record.getAnsweredRounds() == null ? 0 : record.getAnsweredRounds());
        data.put("resultType", record.getResultType());
        data.put("top1Name", record.getTop1DimensionName());
        data.put("top2Name", record.getTop2DimensionName());
        data.put("scanTime", record.getScanTime());
        data.put("finishTime", record.getFinishTime());
        GkzhActivityWeekInstance instance = record.getInstanceId() == null ? null
                : activityWeekService.getInstance(record.getInstanceId());
        data.put("bizType", instance == null ? null : instance.getBizType());

        List<NlldAnswer> answers = answerMapper.selectList(new QueryWrapper<NlldAnswer>()
                .eq("record_id", record.getRecordId()).orderByAsc("round_no").orderByAsc("option_label"));
        List<Map<String, Object>> answerRows = new ArrayList<>();
        for (NlldAnswer answer : answers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("roundNo", answer.getRoundNo());
            row.put("optionId", answer.getOptionId());
            row.put("label", answer.getOptionLabel());
            row.put("name", answer.getOptionName());
            answerRows.add(row);
        }
        data.put("answers", answerRows);

        if (!"finished".equals(record.getStatus())) {
            data.put("rounds", configService.listStudentRounds());
            return data;
        }

        List<NlldDimension> dimensions = configService.listDimensions();
        Map<Long, NlldDimension> byId = new LinkedHashMap<>();
        Map<String, NlldDimension> byName = new LinkedHashMap<>();
        for (NlldDimension dimension : dimensions) {
            byId.put(dimension.getDimensionId(), dimension);
            byName.put(dimension.getDimensionName(), dimension);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        String resultType = record.getResultType();
        result.put("resultType", resultType);
        result.put("openText", NlldScoring.RESULT_OPEN.equals(resultType) ? NlldScoring.OPEN_TEXT : null);
        NlldDimension top1 = record.getTop1DimensionId() == null ? null : byId.get(record.getTop1DimensionId());
        if (top1 == null && record.getTop1DimensionName() != null) {
            top1 = byName.get(record.getTop1DimensionName());
        }
        NlldDimension top2 = record.getTop2DimensionId() == null ? null : byId.get(record.getTop2DimensionId());
        if (top2 == null && record.getTop2DimensionName() != null) {
            top2 = byName.get(record.getTop2DimensionName());
        }
        result.put("top1", top1 == null ? null : dimensionView(top1));
        result.put("top2", top2 == null ? null : dimensionView(top2));

        Map<String, Integer> scores = NlldScoring.emptyScores();
        if (record.getScoreJson() != null && !record.getScoreJson().trim().isEmpty()) {
            try {
                Map<String, Object> snapshot = JSON.parseObject(record.getScoreJson());
                Object rawScores = snapshot == null ? null : snapshot.get("scores");
                if (rawScores instanceof Map) {
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) rawScores).entrySet()) {
                        String code = String.valueOf(entry.getKey());
                        if (NlldScoring.isValidCode(code) && entry.getValue() instanceof Number) {
                            scores.put(code, ((Number) entry.getValue()).intValue());
                        }
                    }
                }
            } catch (Exception ignored) {
                // 历史快照损坏时退回 15 维 0 分展示，不阻断报告页。
            }
        }
        result.put("scores", NlldScoring.scoreRows(scores, dimensions));
        data.put("result", result);
        return data;
    }

    /** 报告页单个维度的展示字段。 */
    private Map<String, Object> dimensionView(NlldDimension dimension) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("dimensionId", dimension.getDimensionId());
        row.put("code", dimension.getDimensionCode());
        row.put("name", dimension.getDimensionName());
        row.put("group", dimension.getDimensionGroup());
        row.put("summary", dimension.getSummary());
        row.put("careerReference", dimension.getCareerReference());
        return row;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
