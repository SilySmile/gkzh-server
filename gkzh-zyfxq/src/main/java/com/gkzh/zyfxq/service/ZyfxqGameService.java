package com.gkzh.zyfxq.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zyfxq.domain.ZyfxqAnswer;
import com.gkzh.zyfxq.domain.ZyfxqCell;
import com.gkzh.zyfxq.domain.ZyfxqOption;
import com.gkzh.zyfxq.domain.ZyfxqQuestion;
import com.gkzh.zyfxq.domain.ZyfxqRecord;
import com.gkzh.zyfxq.domain.ZyfxqSetting;
import com.gkzh.zyfxq.mapper.ZyfxqAnswerMapper;
import com.gkzh.zyfxq.mapper.ZyfxqRecordMapper;
import com.gkzh.zyfxq.support.ZyfxqBoard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 职业飞行棋学生端业务服务。
 *
 * <p>单人单骰对局：扫码进入后创建参与记录（计入活动参与统计），点击开始游戏才计时；
 * 掷骰前进，落在答题格弹出职业问答或情景任务卡，答对继续掷骰、答错或超时后退固定格数，
 * 3 分钟内抵达第 19 格即通关。通关记完成；未通关可重开一局，参与记录仍只有一条。</p>
 *
 * <p>所有计时与判定均以服务端时间为准：客户端只能决定“是否作答”，不能决定耗时、点数、
 * 得分或移动结果，因此无法通过改包获得通关。</p>
 */
@Service
public class ZyfxqGameService {

    /** 游戏类型，固定为 zyfxq。 */
    public static final String GAME_TYPE = "zyfxq";

    /** 记录状态：已扫码但未点击开始游戏。 */
    public static final String STATUS_READY = "ready";

    /** 记录状态：对局中。 */
    public static final String STATUS_PLAYING = "playing";

    /** 记录状态：本局已结束（通关或未通关）。 */
    public static final String STATUS_FINISHED = "finished";

    /** 对局结果：通关。 */
    public static final String RESULT_WIN = "win";

    /** 对局结果：未通关。 */
    public static final String RESULT_LOSE = "lose";

    /**
     * 作答超时判定的网络宽限（毫秒）。
     * 学生卡在限时最后一刻提交时，服务端收到的请求会略晚于限时；给 2 秒宽限避免误判超时，
     * 超过宽限仍按超时处理，计时不会被无限延长。
     */
    public static final int ANSWER_GRACE_MS = 2000;

    private final ZyfxqRecordMapper recordMapper;
    private final ZyfxqAnswerMapper answerMapper;
    private final ZyfxqConfigService configService;
    private final IActivityWeekService activityWeekService;

    public ZyfxqGameService(ZyfxqRecordMapper recordMapper, ZyfxqAnswerMapper answerMapper,
                            ZyfxqConfigService configService, IActivityWeekService activityWeekService) {
        this.recordMapper = recordMapper;
        this.answerMapper = answerMapper;
        this.configService = configService;
        this.activityWeekService = activityWeekService;
    }

    /**
     * 扫码进入：校验学校、活动实例、游戏归属后幂等创建参与记录。
     * 扫码即计入参与统计，并与业务记录在同一事务写入活动游戏参与表。
     */
    @Transactional
    public ZyfxqRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId, Long studentId,
                             String studentNo, String studentName, Long departmentId, String major, String gender) {
        if (schoolId == null || instanceId == null || gameId == null || userId == null) {
            throw new ServiceException("缺少活动或学校信息，请重新扫码进入");
        }
        requireEnabledSetting();
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null || !instanceId.equals(game.getInstanceId()) || !GAME_TYPE.equals(game.getGameType())) {
            throw new ServiceException("该游戏不属于当前活动");
        }
        Long activitySchoolId = activityWeekService.getGameSchoolId(game);
        if (activitySchoolId != null && !activitySchoolId.equals(schoolId)) {
            throw new ServiceException("该活动不属于当前学校，无法进入");
        }

        ZyfxqRecord existing = recordMapper.selectOne(new QueryWrapper<ZyfxqRecord>()
                .eq("school_id", schoolId).eq("instance_id", instanceId)
                .eq("game_id", gameId).eq("user_id", userId));
        if (existing != null) {
            patchStudentSnapshot(existing, studentId, studentNo, studentName, departmentId, major, gender);
            return existing;
        }

        Date now = DateUtils.getNowDate();
        ZyfxqRecord record = new ZyfxqRecord();
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
        record.setStatus(STATUS_READY);
        record.setAttemptNo(1);
        record.setPosition(0);
        record.setRollCount(0);
        record.setStepTotal(0);
        record.setAnswerCount(0);
        record.setCorrectCount(0);
        record.setTimeoutCount(0);
        ZyfxqSetting setting = requireEnabledSetting();
        record.setTotalSeconds(setting.getTotalSeconds());
        record.setQuestionSeconds(setting.getQuestionSeconds());
        record.setBackSteps(setting.getBackSteps());
        record.setDiceFaces(setting.getDiceFaces());
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
    public ZyfxqRecord get(Long recordId, Long userId) {
        ZyfxqRecord record = recordId == null ? null : recordMapper.selectById(recordId);
        if (record == null || userId == null || !userId.equals(record.getUserId())
                || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    /**
     * 学生端查询：对局中且已超过单局总时长时，先按超时结算为未通关再返回，
     * 避免页面长时间停留后仍显示“对局中”。
     */
    @Transactional
    public Map<String, Object> viewForStudent(Long recordId, Long userId) {
        ZyfxqRecord record = lockRecord(recordId, userId);
        if (STATUS_PLAYING.equals(record.getStatus()) && isExpired(record, new Date())) {
            finishLose(record, deadline(record));
        }
        return view(record);
    }

    /** 点击“开始游戏”：固化本局规则参数并开始计时；重复调用保持幂等。 */
    @Transactional
    public Map<String, Object> start(Long recordId, Long userId) {
        ZyfxqRecord record = lockRecord(recordId, userId);
        if (STATUS_PLAYING.equals(record.getStatus())) {
            return view(record);
        }
        if (STATUS_FINISHED.equals(record.getStatus())) {
            throw new ServiceException("本局已结束，请重新开始一局");
        }
        ZyfxqSetting setting = requireEnabledSetting();
        beginAttempt(record, setting, DateUtils.getNowDate());
        return view(record);
    }

    /**
     * 掷骰子：服务端生成点数并前进；抵达终点直接通关，落在答题格则下发一道题。
     * 返回数据包含最新棋盘状态与本次掷骰结果，客户端只负责展示与动画。
     */
    @Transactional
    public Map<String, Object> roll(Long recordId, Long userId) {
        ZyfxqRecord record = lockRecord(recordId, userId);
        Date now = DateUtils.getNowDate();
        if (STATUS_READY.equals(record.getStatus())) {
            throw new ServiceException("请先点击开始游戏");
        }
        if (STATUS_FINISHED.equals(record.getStatus())) {
            throw new ServiceException("本局已结束，可重新开始一局");
        }
        if (isExpired(record, now)) {
            finishLose(record, deadline(record));
            return result(record, null, null);
        }
        if (record.getCurrentQuestionId() != null) {
            throw new ServiceException("请先完成当前题目的作答");
        }

        int faces = record.getDiceFaces() == null || record.getDiceFaces() < 1 ? 6 : record.getDiceFaces();
        int dice = ThreadLocalRandom.current().nextInt(1, faces + 1);
        int from = record.getPosition() == null ? 0 : record.getPosition();
        int to = ZyfxqBoard.forward(from, dice);
        int rollCount = (record.getRollCount() == null ? 0 : record.getRollCount()) + 1;
        int stepTotal = (record.getStepTotal() == null ? 0 : record.getStepTotal()) + dice;

        Map<String, Object> rollResult = new LinkedHashMap<>();
        rollResult.put("dice", dice);
        rollResult.put("from", from);
        rollResult.put("to", to);
        rollResult.put("reachedEnd", ZyfxqBoard.reachedEnd(to));

        if (ZyfxqBoard.reachedEnd(to)) {
            UpdateWrapper<ZyfxqRecord> update = new UpdateWrapper<ZyfxqRecord>()
                    .eq("record_id", record.getRecordId())
                    .set("position", to)
                    .set("roll_count", rollCount)
                    .set("step_total", stepTotal)
                    .set("dice_value", dice)
                    .set("update_time", now);
            recordMapper.update(null, update);
            record.setPosition(to);
            record.setRollCount(rollCount);
            record.setStepTotal(stepTotal);
            record.setDiceValue(dice);
            finishWin(record, now);
            return result(record, rollResult, null);
        }

        ZyfxqCell cell = configService.getCell(to);
        ZyfxqQuestion question = null;
        boolean triggered = cell != null && ZyfxqConfigService.CELL_QUESTION.equals(cell.getCellType())
                && "0".equals(cell.getStatus()) && ZyfxqConfigService.isValidType(cell.getQuestionType());
        if (triggered) {
            question = pickQuestion(record, cell.getQuestionType());
        }

        UpdateWrapper<ZyfxqRecord> update = new UpdateWrapper<ZyfxqRecord>()
                .eq("record_id", record.getRecordId())
                .set("position", to)
                .set("roll_count", rollCount)
                .set("step_total", stepTotal)
                .set("dice_value", dice)
                .set("current_question_id", question == null ? null : question.getQuestionId())
                .set("current_question_time", question == null ? null : now)
                .set("current_cell_no", question == null ? null : to)
                .set("update_time", now);
        recordMapper.update(null, update);
        record.setPosition(to);
        record.setRollCount(rollCount);
        record.setStepTotal(stepTotal);
        record.setDiceValue(dice);
        record.setCurrentQuestionId(question == null ? null : question.getQuestionId());
        record.setCurrentQuestionTime(question == null ? null : now);
        record.setCurrentCellNo(question == null ? null : to);

        rollResult.put("cellNo", to);
        rollResult.put("cellType", cell == null ? null : cell.getCellType());
        rollResult.put("questionType", cell == null ? null : cell.getQuestionType());
        rollResult.put("questionTriggered", question != null);
        return result(record, rollResult, question);
    }

    /**
     * 作答：选项以服务端快照判定，耗时按下发时间与提交时间计算。
     * 答对原地继续掷骰，答错或超时后退固定格数；若此时已超过单局总时长，同事务结算为未通关。
     */
    @Transactional
    public Map<String, Object> answer(Long recordId, Long userId, Long questionId, String optionLabel, Boolean timeout) {
        ZyfxqRecord record = lockRecord(recordId, userId);
        Date now = DateUtils.getNowDate();
        if (STATUS_FINISHED.equals(record.getStatus())) {
            throw new ServiceException("本局已结束，结果仅可查看");
        }
        if (STATUS_READY.equals(record.getStatus())) {
            throw new ServiceException("请先点击开始游戏");
        }
        Long currentId = record.getCurrentQuestionId();
        if (currentId == null || questionId == null || !currentId.equals(questionId)) {
            throw new ServiceException("当前没有待作答的题目，请重新掷骰子");
        }
        ZyfxqQuestion question = configService.getQuestion(currentId);
        if (question == null || question.getOptions() == null || question.getOptions().isEmpty()) {
            throw new ServiceException("题目已被删除，请重新掷骰子");
        }
        int limitSeconds = record.getQuestionSeconds() == null || record.getQuestionSeconds() < 1
                ? 20 : record.getQuestionSeconds();
        int limitMs = limitSeconds * 1000;
        Date issuedAt = record.getCurrentQuestionTime() == null ? now : record.getCurrentQuestionTime();
        long elapsed = Math.max(0L, now.getTime() - issuedAt.getTime());
        String label = trimToNull(optionLabel);

        ZyfxqOption chosen = null;
        ZyfxqOption correct = null;
        for (ZyfxqOption option : question.getOptions()) {
            if ("1".equals(option.getIsCorrect())) {
                correct = option;
            }
            if (label != null && label.equalsIgnoreCase(option.getOptionLabel())) {
                chosen = option;
            }
        }
        if (correct == null) {
            throw new ServiceException("题目缺少正确答案，请联系管理员");
        }
        boolean timedOut = Boolean.TRUE.equals(timeout) || label == null || elapsed > limitMs + ANSWER_GRACE_MS;
        if (!timedOut && chosen == null) {
            throw new ServiceException("请选择有效选项");
        }
        int costMs = (int) Math.min(elapsed, limitMs);
        boolean isCorrect = !timedOut && chosen != null
                && chosen.getOptionLabel().equals(correct.getOptionLabel());

        int positionBefore = record.getPosition() == null ? 0 : record.getPosition();
        int backSteps = record.getBackSteps() == null ? 2 : record.getBackSteps();
        int positionAfter = isCorrect ? positionBefore : ZyfxqBoard.backward(positionBefore, backSteps);

        ZyfxqAnswer answer = new ZyfxqAnswer();
        answer.setRecordId(record.getRecordId());
        answer.setAttemptNo(record.getAttemptNo() == null ? 1 : record.getAttemptNo());
        answer.setCellNo(record.getCurrentCellNo() == null ? positionBefore : record.getCurrentCellNo());
        answer.setQuestionId(question.getQuestionId());
        answer.setQuestionType(question.getQuestionType());
        answer.setQuestionTitle(question.getTitle());
        answer.setOptionLabel(chosen == null ? null : chosen.getOptionLabel());
        answer.setOptionContent(chosen == null ? null : chosen.getOptionContent());
        answer.setCorrectLabel(correct.getOptionLabel());
        answer.setCorrectContent(correct.getOptionContent());
        answer.setIsCorrect(isCorrect ? "1" : "0");
        answer.setIsTimeout(timedOut ? "1" : "0");
        answer.setLimitSeconds(limitSeconds);
        answer.setCostMs(costMs);
        answer.setPositionBefore(positionBefore);
        answer.setPositionAfter(positionAfter);
        answer.setCreateTime(now);
        answerMapper.insert(answer);

        int answerCount = (record.getAnswerCount() == null ? 0 : record.getAnswerCount()) + 1;
        int correctCount = (record.getCorrectCount() == null ? 0 : record.getCorrectCount())
                + (isCorrect ? 1 : 0);
        int timeoutCount = (record.getTimeoutCount() == null ? 0 : record.getTimeoutCount())
                + (timedOut ? 1 : 0);
        boolean expired = now.getTime() >= deadline(record).getTime();

        recordMapper.update(null, new UpdateWrapper<ZyfxqRecord>()
                .eq("record_id", record.getRecordId())
                .set("position", positionAfter)
                .set("answer_count", answerCount)
                .set("correct_count", correctCount)
                .set("timeout_count", timeoutCount)
                .set("current_question_id", null)
                .set("current_question_time", null)
                .set("current_cell_no", null)
                .set("update_time", now));
        record.setPosition(positionAfter);
        record.setAnswerCount(answerCount);
        record.setCorrectCount(correctCount);
        record.setTimeoutCount(timeoutCount);
        record.setCurrentQuestionId(null);
        record.setCurrentQuestionTime(null);
        record.setCurrentCellNo(null);

        Map<String, Object> answerResult = new LinkedHashMap<>();
        answerResult.put("correct", isCorrect);
        answerResult.put("timeout", timedOut);
        answerResult.put("correctLabel", correct.getOptionLabel());
        answerResult.put("correctContent", correct.getOptionContent());
        answerResult.put("selectedLabel", chosen == null ? null : chosen.getOptionLabel());
        answerResult.put("analysis", question.getAnalysis());
        answerResult.put("backSteps", isCorrect ? 0 : backSteps);
        answerResult.put("positionBefore", positionBefore);
        answerResult.put("positionAfter", positionAfter);
        answerResult.put("costMs", costMs);
        answerResult.put("limitSeconds", limitSeconds);

        if (expired) {
            finishLose(record, deadline(record));
        }
        return result(record, null, null, answerResult);
    }

    /** 未通关后可重新开始一局：累加 attempt_no 并重置本局状态，参与记录仍复用同一条。 */
    @Transactional
    public Map<String, Object> restart(Long recordId, Long userId) {
        ZyfxqRecord record = lockRecord(recordId, userId);
        if (STATUS_PLAYING.equals(record.getStatus())) {
            throw new ServiceException("本局尚未结束，无法重新开始");
        }
        if (STATUS_READY.equals(record.getStatus())) {
            return start(recordId, userId);
        }
        if (RESULT_WIN.equals(record.getResult())) {
            throw new ServiceException("本局已通关，无需重新开始");
        }
        ZyfxqSetting setting = requireEnabledSetting();
        int attemptNo = (record.getAttemptNo() == null ? 1 : record.getAttemptNo()) + 1;
        record.setAttemptNo(attemptNo);
        beginAttempt(record, setting, DateUtils.getNowDate());
        return view(record);
    }

    /** 后台删除参与记录：同步清理答题明细和该活动游戏的全部活动参与行。 */
    @Transactional
    public void deleteRecord(Long recordId) {
        ZyfxqRecord record = recordId == null ? null : recordMapper.selectById(recordId);
        if (record == null || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        if (record.getInstanceId() == null || record.getGameId() == null || record.getUserId() == null) {
            throw new ServiceException("参与记录缺少活动上下文，无法删除");
        }
        answerMapper.delete(new QueryWrapper<ZyfxqAnswer>().eq("record_id", recordId));
        activityWeekService.deleteGameParticipations(record.getInstanceId(), record.getGameId(), record.getUserId());
        recordMapper.deleteById(recordId);
    }

    /** 组装学生端展示数据：棋盘、对局状态、当前题目和本局答题明细。 */
    public Map<String, Object> view(ZyfxqRecord record) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recordId", record.getRecordId());
        data.put("instanceId", record.getInstanceId());
        data.put("gameId", record.getGameId());
        data.put("status", record.getStatus());
        data.put("result", record.getResult());
        data.put("attemptNo", record.getAttemptNo() == null ? 1 : record.getAttemptNo());
        data.put("position", record.getPosition() == null ? 0 : record.getPosition());
        data.put("rollCount", record.getRollCount() == null ? 0 : record.getRollCount());
        data.put("stepTotal", record.getStepTotal() == null ? 0 : record.getStepTotal());
        data.put("diceValue", record.getDiceValue());
        data.put("answerCount", record.getAnswerCount() == null ? 0 : record.getAnswerCount());
        data.put("correctCount", record.getCorrectCount() == null ? 0 : record.getCorrectCount());
        data.put("timeoutCount", record.getTimeoutCount() == null ? 0 : record.getTimeoutCount());
        data.put("totalSeconds", record.getTotalSeconds() == null ? 180 : record.getTotalSeconds());
        data.put("questionSeconds", record.getQuestionSeconds() == null ? 20 : record.getQuestionSeconds());
        data.put("backSteps", record.getBackSteps() == null ? 2 : record.getBackSteps());
        data.put("diceFaces", record.getDiceFaces() == null ? 6 : record.getDiceFaces());
        data.put("startTime", record.getStartTime());
        data.put("endTime", record.getEndTime());
        data.put("durationMs", record.getDurationMs());
        data.put("scanTime", record.getScanTime());
        long elapsedMs = 0L;
        if (record.getStartTime() != null) {
            long end = record.getEndTime() == null ? System.currentTimeMillis() : record.getEndTime().getTime();
            elapsedMs = Math.max(0L, end - record.getStartTime().getTime());
        }
        int totalMs = (record.getTotalSeconds() == null ? 180 : record.getTotalSeconds()) * 1000;
        data.put("elapsedMs", elapsedMs);
        data.put("remainingSeconds", STATUS_PLAYING.equals(record.getStatus())
                ? Math.max(0, (totalMs - elapsedMs) / 1000) : 0);

        GkzhActivityWeekInstance instance = record.getInstanceId() == null ? null
                : activityWeekService.getInstance(record.getInstanceId());
        data.put("bizType", instance == null ? null : instance.getBizType());

        Map<String, Object> board = new LinkedHashMap<>();
        List<Map<String, Object>> cells = new ArrayList<>();
        for (ZyfxqCell cell : configService.listCells()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("cellNo", cell.getCellNo());
            row.put("cellType", cell.getCellType());
            row.put("questionType", cell.getQuestionType());
            row.put("cellColor", cell.getCellColor());
            row.put("cellLabel", cell.getCellLabel());
            row.put("status", cell.getStatus());
            int[] slot = ZyfxqBoard.slotOf(cell.getCellNo() == null ? 0 : cell.getCellNo());
            if (slot != null) {
                row.put("row", slot[0]);
                row.put("col", slot[1]);
            }
            cells.add(row);
        }
        board.put("cells", cells);
        Map<String, Object> startSlot = new LinkedHashMap<>();
        startSlot.put("row", ZyfxqBoard.START_SLOT[0]);
        startSlot.put("col", ZyfxqBoard.START_SLOT[1]);
        board.put("startSlot", startSlot);
        board.put("endCell", ZyfxqBoard.END_CELL);
        board.put("columns", ZyfxqBoard.COLUMNS);
        board.put("rows", ZyfxqBoard.ROWS);
        data.put("board", board);

        // 当前题目附带服务端剩余答题秒数，小程序据此恢复倒计时；
        // 超时判定仍以服务端下发的 current_question_time 为准，客户端倒计时只用于展示。
        Map<String, Object> currentQuestion = record.getCurrentQuestionId() == null
                ? null : questionPayload(record.getCurrentQuestionId(), record.getQuestionSeconds());
        if (currentQuestion != null) {
            int limitSeconds = record.getQuestionSeconds() == null ? 20 : record.getQuestionSeconds();
            long issuedAt = record.getCurrentQuestionTime() == null
                    ? System.currentTimeMillis() : record.getCurrentQuestionTime().getTime();
            long usedMs = Math.max(0L, System.currentTimeMillis() - issuedAt);
            currentQuestion.put("remainSeconds", Math.max(0, (limitSeconds * 1000L - usedMs) / 1000));
            currentQuestion.put("issuedAt", record.getCurrentQuestionTime());
        }
        data.put("currentQuestion", currentQuestion);
        data.put("answers", answerRows(record));
        return data;
    }

    /** 本局答题明细（学生结果页第二页）：题号、结果、限时、实际耗时与是否超时。 */
    private List<Map<String, Object>> answerRows(ZyfxqRecord record) {
        int attemptNo = record.getAttemptNo() == null ? 1 : record.getAttemptNo();
        List<ZyfxqAnswer> answers = answerMapper.selectList(new QueryWrapper<ZyfxqAnswer>()
                .eq("record_id", record.getRecordId()).eq("attempt_no", attemptNo)
                .orderByAsc("answer_id"));
        List<Map<String, Object>> rows = new ArrayList<>();
        int serial = 1;
        for (ZyfxqAnswer answer : answers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("serialNo", serial++);
            row.put("cellNo", answer.getCellNo());
            row.put("questionType", answer.getQuestionType());
            row.put("questionTypeName", ZyfxqConfigService.questionTypeName(answer.getQuestionType()));
            row.put("title", answer.getQuestionTitle());
            row.put("result", "1".equals(answer.getIsCorrect()) ? "答对"
                    : ("1".equals(answer.getIsTimeout()) ? "超时" : "答错"));
            row.put("correct", "1".equals(answer.getIsCorrect()));
            row.put("timeout", "1".equals(answer.getIsTimeout()));
            row.put("limitSeconds", answer.getLimitSeconds());
            row.put("costMs", answer.getCostMs());
            row.put("optionLabel", answer.getOptionLabel());
            row.put("optionContent", answer.getOptionContent());
            row.put("correctLabel", answer.getCorrectLabel());
            row.put("correctContent", answer.getCorrectContent());
            row.put("createTime", answer.getCreateTime());
            rows.add(row);
        }
        return rows;
    }

    /** 当前题目的学生端载荷：不含正确答案，避免前端提前判定。 */
    private Map<String, Object> questionPayload(Long questionId, Integer questionSeconds) {
        ZyfxqQuestion question = configService.getQuestion(questionId);
        if (question == null) {
            return null;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("questionId", question.getQuestionId());
        payload.put("questionType", question.getQuestionType());
        payload.put("questionTypeName", ZyfxqConfigService.questionTypeName(question.getQuestionType()));
        payload.put("title", question.getTitle());
        payload.put("limitSeconds", questionSeconds == null ? 20 : questionSeconds);
        List<Map<String, Object>> options = new ArrayList<>();
        for (ZyfxqOption option : question.getOptions()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("label", option.getOptionLabel());
            row.put("content", option.getOptionContent());
            options.add(row);
        }
        payload.put("options", options);
        return payload;
    }

    /** 随机抽取一道该类型的启用题目；本局未出现过的题目优先，全部出现过后才允许重复。 */
    private ZyfxqQuestion pickQuestion(ZyfxqRecord record, String questionType) {
        List<ZyfxqQuestion> pool = configService.listEnabledQuestions(questionType);
        if (pool.isEmpty()) {
            throw new ServiceException("该知识点暂无可用题目，请联系管理员");
        }
        int attemptNo = record.getAttemptNo() == null ? 1 : record.getAttemptNo();
        Set<Long> used = new HashSet<>();
        for (ZyfxqAnswer answer : answerMapper.selectList(new QueryWrapper<ZyfxqAnswer>()
                .select("question_id").eq("record_id", record.getRecordId()).eq("attempt_no", attemptNo))) {
            used.add(answer.getQuestionId());
        }
        List<ZyfxqQuestion> candidates = new ArrayList<>();
        for (ZyfxqQuestion question : pool) {
            if (!used.contains(question.getQuestionId())) {
                candidates.add(question);
            }
        }
        if (candidates.isEmpty()) {
            candidates = pool;
        }
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    /** 开局或重开：重置本局状态并固化规则参数快照。 */
    private void beginAttempt(ZyfxqRecord record, ZyfxqSetting setting, Date now) {
        recordMapper.update(null, new UpdateWrapper<ZyfxqRecord>()
                .eq("record_id", record.getRecordId())
                .set("status", STATUS_PLAYING)
                .set("result", null)
                .set("position", 0)
                .set("roll_count", 0)
                .set("step_total", 0)
                .set("dice_value", null)
                .set("answer_count", 0)
                .set("correct_count", 0)
                .set("timeout_count", 0)
                .set("current_question_id", null)
                .set("current_question_time", null)
                .set("current_cell_no", null)
                .set("total_seconds", setting.getTotalSeconds())
                .set("question_seconds", setting.getQuestionSeconds())
                .set("back_steps", setting.getBackSteps())
                .set("dice_faces", setting.getDiceFaces())
                .set("start_time", now)
                .set("end_time", null)
                .set("duration_ms", null)
                .set("update_time", now));
        record.setStatus(STATUS_PLAYING);
        record.setResult(null);
        record.setPosition(0);
        record.setRollCount(0);
        record.setStepTotal(0);
        record.setDiceValue(null);
        record.setAnswerCount(0);
        record.setCorrectCount(0);
        record.setTimeoutCount(0);
        record.setCurrentQuestionId(null);
        record.setCurrentQuestionTime(null);
        record.setCurrentCellNo(null);
        record.setTotalSeconds(setting.getTotalSeconds());
        record.setQuestionSeconds(setting.getQuestionSeconds());
        record.setBackSteps(setting.getBackSteps());
        record.setDiceFaces(setting.getDiceFaces());
        record.setStartTime(now);
        record.setEndTime(null);
        record.setDurationMs(null);
        record.setUpdateTime(now);
    }

    /** 通关结算：写入结束时间、总耗时并同步活动参与完成状态。 */
    private void finishWin(ZyfxqRecord record, Date now) {
        int durationMs = durationOf(record, now);
        recordMapper.update(null, new UpdateWrapper<ZyfxqRecord>()
                .eq("record_id", record.getRecordId())
                .set("status", STATUS_FINISHED)
                .set("result", RESULT_WIN)
                .set("position", ZyfxqBoard.END_CELL)
                .set("end_time", now)
                .set("duration_ms", durationMs)
                .set("current_question_id", null)
                .set("current_question_time", null)
                .set("current_cell_no", null)
                .set("update_time", now));
        record.setStatus(STATUS_FINISHED);
        record.setResult(RESULT_WIN);
        record.setPosition(ZyfxqBoard.END_CELL);
        record.setEndTime(now);
        record.setDurationMs(durationMs);
        record.setCurrentQuestionId(null);
        record.setCurrentQuestionTime(null);
        record.setCurrentCellNo(null);
        record.setUpdateTime(now);
        completeParticipation(record, true);
    }

    /** 未通关结算：结束时间固定为超时截止时间，保证总耗时恒等于单局总时长。 */
    private void finishLose(ZyfxqRecord record, Date endTime) {
        int durationMs = record.getTotalSeconds() == null ? 0 : record.getTotalSeconds() * 1000;
        recordMapper.update(null, new UpdateWrapper<ZyfxqRecord>()
                .eq("record_id", record.getRecordId())
                .set("status", STATUS_FINISHED)
                .set("result", RESULT_LOSE)
                .set("end_time", endTime)
                .set("duration_ms", durationMs)
                .set("current_question_id", null)
                .set("current_question_time", null)
                .set("current_cell_no", null)
                .set("update_time", DateUtils.getNowDate()));
        record.setStatus(STATUS_FINISHED);
        record.setResult(RESULT_LOSE);
        record.setEndTime(endTime);
        record.setDurationMs(durationMs);
        record.setCurrentQuestionId(null);
        record.setCurrentQuestionTime(null);
        record.setCurrentCellNo(null);
        record.setUpdateTime(DateUtils.getNowDate());
        completeParticipation(record, false);
    }

    /** 同步活动游戏参与记录：只有通关才标记完成，未通关保留未完成状态供重新挑战。 */
    private void completeParticipation(ZyfxqRecord record, boolean win) {
        GkzhGameParticipation participation = activityWeekService.getLatestParticipation(
                record.getGameId(), record.getUserId());
        if (participation == null) {
            return;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("result", win ? RESULT_WIN : RESULT_LOSE);
        result.put("attemptNo", record.getAttemptNo());
        result.put("rollCount", record.getRollCount());
        result.put("durationMs", record.getDurationMs());
        participation.setStatus(win ? "1" : "0");
        participation.setFinishTime(DateUtils.getNowDate());
        participation.setResultJson(JSON.toJSONString(result));
        activityWeekService.updateGameParticipation(participation);
    }

    /** 提交前锁定记录行，避免并发请求重复掷骰或重复结算。 */
    private ZyfxqRecord lockRecord(Long recordId, Long userId) {
        ZyfxqRecord record = recordId == null ? null : recordMapper.selectOne(new QueryWrapper<ZyfxqRecord>()
                .eq("record_id", recordId).last("FOR UPDATE"));
        if (record == null || userId == null || !userId.equals(record.getUserId())
                || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    /** 读取并校验规则参数；游戏被停用时拒绝进入。 */
    private ZyfxqSetting requireEnabledSetting() {
        ZyfxqSetting setting = configService.setting();
        if (!"1".equals(setting.getEnabled())) {
            throw new ServiceException("该游戏暂未开放，请联系工作人员");
        }
        return setting;
    }

    /** 本局截止时间：开始时间 + 单局总时长。 */
    private Date deadline(ZyfxqRecord record) {
        int totalSeconds = record.getTotalSeconds() == null ? 180 : record.getTotalSeconds();
        Date start = record.getStartTime() == null ? new Date() : record.getStartTime();
        return new Date(start.getTime() + totalSeconds * 1000L);
    }

    /** 是否已超过本局截止时间。 */
    private boolean isExpired(ZyfxqRecord record, Date now) {
        return record.getStartTime() != null && now.getTime() >= deadline(record).getTime();
    }

    /** 本局已用时长，用于结算通关耗时。 */
    private int durationOf(ZyfxqRecord record, Date now) {
        if (record.getStartTime() == null) {
            return 0;
        }
        long elapsed = Math.max(0L, now.getTime() - record.getStartTime().getTime());
        int limit = (record.getTotalSeconds() == null ? 180 : record.getTotalSeconds()) * 1000;
        return (int) Math.min(elapsed, limit);
    }

    /** 首次创建后再次扫码时，只补空的学生快照，不覆盖既有对局进度。 */
    private void patchStudentSnapshot(ZyfxqRecord existing, Long studentId, String studentNo,
                                      String studentName, Long departmentId, String major, String gender) {
        UpdateWrapper<ZyfxqRecord> patch = new UpdateWrapper<ZyfxqRecord>().eq("record_id", existing.getRecordId());
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
    }

    /** 统一响应结构：最新对局状态 + 本次动作结果 + 当前题目。 */
    private Map<String, Object> result(ZyfxqRecord record, Map<String, Object> rollResult,
                                       ZyfxqQuestion question) {
        return result(record, rollResult, question, null);
    }

    private Map<String, Object> result(ZyfxqRecord record, Map<String, Object> rollResult,
                                       ZyfxqQuestion question, Map<String, Object> answerResult) {
        Map<String, Object> data = view(record);
        data.put("rollResult", rollResult);
        data.put("answerResult", answerResult);
        return data;
    }

    private static String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
