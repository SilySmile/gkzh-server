package com.gkzh.zdyp.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zdyp.domain.ZdypAnswer;
import com.gkzh.zdyp.domain.ZdypAttempt;
import com.gkzh.zdyp.domain.ZdypCard;
import com.gkzh.zdyp.domain.ZdypCareer;
import com.gkzh.zdyp.domain.ZdypRecord;
import com.gkzh.zdyp.domain.ZdypSetting;
import com.gkzh.zdyp.domain.dto.ZdypPlacement;
import com.gkzh.zdyp.mapper.ZdypAnswerMapper;
import com.gkzh.zdyp.mapper.ZdypAttemptMapper;
import com.gkzh.zdyp.mapper.ZdypRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职得一拼学生端业务：扫码进入、选择岗位开始闯关、提交四维拼图与结果查看。
 *
 * <p>扫码即创建参与记录并计入统计；每次开始闯关生成一条尝试记录，重闯只累加 attempt_no，
 * 参与记录始终只有一条。判定、超时与用时全部由服务端计算，客户端只提交卡槽与卡片编号。</p>
 */
@Service
public class ZdypGameService {

    /** 游戏类型编码，与 gkzh_game_config.route、gkzh_game_type.game_type 保持一致。 */
    public static final String GAME_TYPE = "zdyp";

    /** 参与状态：进行中。 */
    public static final String STATUS_PARTICIPATING = "participating";

    /** 参与状态：已通关。 */
    public static final String STATUS_FINISHED = "finished";

    /** 超时判定的网络宽限（毫秒），避免学生在最后一刻提交被误判为超时。 */
    private static final long TIMEOUT_GRACE_MILLIS = 1_500L;

    private final ZdypRecordMapper recordMapper;
    private final ZdypAttemptMapper attemptMapper;
    private final ZdypAnswerMapper answerMapper;
    private final ZdypConfigService configService;
    private final IActivityWeekService activityWeekService;

    public ZdypGameService(ZdypRecordMapper recordMapper, ZdypAttemptMapper attemptMapper,
                           ZdypAnswerMapper answerMapper, ZdypConfigService configService,
                           IActivityWeekService activityWeekService) {
        this.recordMapper = recordMapper;
        this.attemptMapper = attemptMapper;
        this.answerMapper = answerMapper;
        this.configService = configService;
        this.activityWeekService = activityWeekService;
    }

    /**
     * 扫码进入：校验学校、活动实例和游戏归属后幂等创建参与记录。
     * 扫码即计入参与统计，与业务记录在同一事务写入活动游戏参与表。
     */
    @Transactional
    public ZdypRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId, Long studentId,
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
        ZdypRecord existing = recordMapper.selectOne(new QueryWrapper<ZdypRecord>()
                .eq("school_id", schoolId).eq("instance_id", instanceId)
                .eq("game_id", gameId).eq("user_id", userId));
        if (existing != null) {
            // 再次扫码只补空的学生快照，不覆盖已通关的岗位与行为分型快照。
            UpdateWrapper<ZdypRecord> patch = new UpdateWrapper<ZdypRecord>().eq("record_id", existing.getRecordId());
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
        ZdypRecord record = new ZdypRecord();
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
        record.setAttemptNo(0);
        record.setFirstAttemptCorrect("0");
        record.setTotalWrong(0);
        record.setTimeoutCount(0);
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
    public ZdypRecord get(Long recordId, Long userId) {
        ZdypRecord record = recordMapper.selectById(recordId);
        if (record == null || userId == null || !userId.equals(record.getUserId())
                || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    /**
     * 选择岗位并开始一次闯关：生成尝试记录并返回四张卡片与服务端截止时间。
     * 同一岗位重复点击开始会复用未提交的尝试，避免一次点击产生多条尝试记录。
     *
     * @param careerCode 学生选择的启用岗位编码
     */
    @Transactional
    public Map<String, Object> start(Long recordId, Long userId, String careerCode) {
        ZdypRecord record = lockRecord(recordId, userId);
        ZdypSetting setting = requireEnabled();
        if (STATUS_FINISHED.equals(record.getStatus())) {
            throw new ServiceException("本局已通关，不能重新开始，可直接查看报告");
        }
        if (isBlank(careerCode)) {
            throw new ServiceException("请先选择一个岗位");
        }
        ZdypCareer career = findActiveCareerByCode(careerCode);
        if (career == null) {
            throw new ServiceException("该岗位暂不可选，请刷新后重新选择");
        }
        List<ZdypCard> cards = configService.listActiveCards(career.getCareerId());
        if (cards.size() != 4) {
            throw new ServiceException("该岗位卡片暂不完整，请联系老师后再试");
        }

        ZdypAttempt pending = findPendingAttempt(record.getRecordId());
        int attemptNo;
        if (pending != null) {
            // 未提交的尝试没有作答明细，可直接清理后复用编号，避免统计里出现空号。
            answerMapper.delete(new QueryWrapper<ZdypAnswer>().eq("attempt_id", pending.getAttemptId()));
            attemptMapper.deleteById(pending.getAttemptId());
            attemptNo = record.getAttemptNo() != null && record.getAttemptNo() > pending.getAttemptNo()
                    ? record.getAttemptNo() + 1 : pending.getAttemptNo();
        } else {
            attemptNo = (record.getAttemptNo() == null ? 0 : record.getAttemptNo()) + 1;
        }

        Date now = DateUtils.getNowDate();
        ZdypAttempt attempt = new ZdypAttempt();
        attempt.setRecordId(record.getRecordId());
        attempt.setAttemptNo(attemptNo);
        attempt.setCareerId(career.getCareerId());
        attempt.setCorrectCount(0);
        attempt.setWrongCount(0);
        attempt.setFilledCount(0);
        attempt.setIsTimeout("0");
        attempt.setIsPass("0");
        attempt.setCreateTime(now);
        attemptMapper.insert(attempt);

        Date startTime = record.getStartTime() == null ? now : record.getStartTime();
        recordMapper.update(null, new UpdateWrapper<ZdypRecord>().eq("record_id", record.getRecordId())
                .set("career_id", career.getCareerId())
                .set("career_code", career.getCareerCode())
                .set("career_name", career.getCareerName())
                .set("attempt_no", attemptNo)
                .set("start_time", startTime)
                .set("update_time", now));
        record.setCareerId(career.getCareerId());
        record.setCareerCode(career.getCareerCode());
        record.setCareerName(career.getCareerName());
        record.setAttemptNo(attemptNo);
        record.setStartTime(startTime);
        record.setUpdateTime(now);
        return startView(record, career, cards, attempt, setting);
    }

    /**
     * 提交本次闯关：服务端判定四个卡槽、写入作答明细并更新汇总。
     * 全部放对且未超时才通关；未通关不写活动完成状态，学生可继续重闯。
     *
     * @param attemptNo  客户端持有的尝试序号，必须与服务端一致，避免旧页面覆盖新尝试
     * @param placements 本次提交的卡槽放置，超时未填的卡槽可以不传
     */
    @Transactional
    public Map<String, Object> submit(Long recordId, Long userId, Integer attemptNo, List<ZdypPlacement> placements) {
        ZdypRecord record = lockRecord(recordId, userId);
        ZdypSetting setting = requireEnabled();
        if (attemptNo == null) {
            throw new ServiceException("缺少本局编号，请重新开始");
        }
        ZdypAttempt attempt = attemptMapper.selectOne(new QueryWrapper<ZdypAttempt>()
                .eq("record_id", recordId).eq("attempt_no", attemptNo));
        if (attempt == null) {
            throw new ServiceException("本局已失效，请重新开始");
        }
        ZdypCareer career = attempt.getCareerId() == null ? null : configService.findActiveCareer(attempt.getCareerId());
        if (career == null) {
            throw new ServiceException("本局岗位已停用，请联系老师后再试");
        }
        if (attempt.getSubmitTime() != null) {
            // 网络重试或重复点击：直接返回已判定的结果，不重复累计错误次数。
            return resultView(record, attempt, career);
        }

        List<ZdypCard> cards = configService.listActiveCards(career.getCareerId());
        Map<Long, ZdypCard> cardById = new HashMap<>();
        for (ZdypCard card : cards) {
            cardById.put(card.getCardId(), card);
        }
        List<ZdypPlacement> list = placements == null ? new ArrayList<>() : placements;
        if (list.size() > 4) {
            throw new ServiceException("一次最多提交四个卡槽");
        }
        Map<String, ZdypPlacement> bySlot = new LinkedHashMap<>();
        Map<Long, Boolean> usedCards = new HashMap<>();
        for (ZdypPlacement placement : list) {
            if (placement == null || isBlank(placement.getSlotCode()) || placement.getCardId() == null) {
                throw new ServiceException("提交内容不完整，请重新选择卡片");
            }
            String slot = placement.getSlotCode().trim();
            if (!ZdypConfigService.SLOTS.containsKey(slot)) {
                throw new ServiceException("卡槽位置无效，请重新开始本局");
            }
            if (bySlot.containsKey(slot)) {
                throw new ServiceException("同一个卡槽不能放两张卡片");
            }
            ZdypCard card = cardById.get(placement.getCardId());
            if (card == null) {
                throw new ServiceException("卡片已失效，请重新开始本局");
            }
            if (usedCards.containsKey(card.getCardId())) {
                throw new ServiceException("同一张卡片不能重复使用");
            }
            usedCards.put(card.getCardId(), Boolean.TRUE);
            bySlot.put(slot, placement);
        }

        Date now = DateUtils.getNowDate();
        long duration = Math.max(0L, now.getTime() - attempt.getCreateTime().getTime());
        int limit = setting.getTimeLimit() == null ? 0 : setting.getTimeLimit();
        boolean timeout = limit > 0 && duration > limit * 1000L + TIMEOUT_GRACE_MILLIS;

        int correctCount = 0;
        int wrongCount = 0;
        int placeOrder = 0;
        for (Map.Entry<String, ZdypPlacement> entry : bySlot.entrySet()) {
            placeOrder++;
            ZdypPlacement placement = entry.getValue();
            ZdypCard card = cardById.get(placement.getCardId());
            boolean correct = entry.getKey().equals(card.getDimensionCode());
            if (correct) {
                correctCount++;
            } else {
                wrongCount++;
            }
            ZdypAnswer answer = new ZdypAnswer();
            answer.setAttemptId(attempt.getAttemptId());
            answer.setRecordId(record.getRecordId());
            answer.setSlotCode(entry.getKey());
            answer.setCardId(card.getCardId());
            answer.setCardDimension(card.getDimensionCode());
            answer.setIsCorrect(correct ? "1" : "0");
            answer.setPlaceOrder(placeOrder);
            answer.setElapsedMs(placement.getElapsedMs());
            answer.setCreateTime(now);
            answerMapper.insert(answer);
        }
        // 超时未填的卡槽按错误计入，保证四维错误率口径一致。
        wrongCount += Math.max(0, 4 - bySlot.size());
        boolean pass = correctCount == 4 && !timeout;

        String firstSlot = bySlot.isEmpty() ? null : bySlot.keySet().iterator().next();
        attemptMapper.update(null, new UpdateWrapper<ZdypAttempt>().eq("attempt_id", attempt.getAttemptId())
                .set("correct_count", correctCount)
                .set("wrong_count", wrongCount)
                .set("filled_count", bySlot.size())
                .set("is_timeout", timeout ? "1" : "0")
                .set("is_pass", pass ? "1" : "0")
                .set("duration_ms", duration)
                .set("first_slot_code", firstSlot)
                .set("submit_time", now));
        attempt.setCorrectCount(correctCount);
        attempt.setWrongCount(wrongCount);
        attempt.setFilledCount(bySlot.size());
        attempt.setIsTimeout(timeout ? "1" : "0");
        attempt.setIsPass(pass ? "1" : "0");
        attempt.setDurationMs(duration);
        attempt.setFirstSlotCode(firstSlot);
        attempt.setSubmitTime(now);

        int totalWrong = (record.getTotalWrong() == null ? 0 : record.getTotalWrong()) + wrongCount;
        int timeoutCount = (record.getTimeoutCount() == null ? 0 : record.getTimeoutCount()) + (timeout ? 1 : 0);
        UpdateWrapper<ZdypRecord> patch = new UpdateWrapper<ZdypRecord>()
                .eq("record_id", record.getRecordId())
                .set("total_wrong", totalWrong)
                .set("timeout_count", timeoutCount)
                .set("update_time", now);
        record.setTotalWrong(totalWrong);
        record.setTimeoutCount(timeoutCount);
        if (attemptNo == 1) {
            patch.set("first_attempt_ms", duration)
                    .set("first_attempt_correct", pass ? "1" : "0");
            record.setFirstAttemptMs(duration);
            record.setFirstAttemptCorrect(pass ? "1" : "0");
        }
        if (pass) {
            long best = record.getBestMs() == null ? duration : Math.min(record.getBestMs(), duration);
            patch.set("best_ms", best);
            record.setBestMs(best);
            if (!STATUS_FINISHED.equals(record.getStatus())) {
                // 行为分型只依据首次尝试，且只在第一次通关时写入快照。
                applyBehavior(record, patch);
                patch.set("status", STATUS_FINISHED)
                        .set("finish_time", now);
                record.setStatus(STATUS_FINISHED);
                record.setFinishTime(now);
            }
        }
        recordMapper.update(null, patch);
        record.setUpdateTime(now);
        if (pass) {
            completeParticipation(record);
        }
        return resultView(record, attempt, career);
    }

    /** 按首次尝试的作答计算四类行为分型，并写入结论与建议快照。 */
    private void applyBehavior(ZdypRecord record, UpdateWrapper<ZdypRecord> patch) {
        int totalWrong = 0;
        int weaknessWrong = 0;
        int otherWrong = 0;
        long duration = record.getFirstAttemptMs() == null ? 0L : record.getFirstAttemptMs();
        ZdypAttempt first = findAttempt(record.getRecordId(), 1);
        if (first != null) {
            duration = first.getDurationMs() == null ? duration : first.getDurationMs();
            List<ZdypAnswer> answers = answerMapper.selectList(new QueryWrapper<ZdypAnswer>()
                    .eq("attempt_id", first.getAttemptId()));
            for (ZdypAnswer answer : answers) {
                if ("1".equals(answer.getIsCorrect())) {
                    continue;
                }
                totalWrong++;
                if ("weakness".equals(answer.getSlotCode())) {
                    weaknessWrong++;
                } else {
                    otherWrong++;
                }
            }
            // 超时未填的卡槽没有明细，按未放对计入，避免低估错误数。
            totalWrong = Math.max(totalWrong, first.getWrongCount() == null ? totalWrong : first.getWrongCount());
            otherWrong = Math.max(otherWrong, Math.max(0, totalWrong - weaknessWrong));
        } else {
            totalWrong = "1".equals(record.getFirstAttemptCorrect()) ? 0 : 4;
            otherWrong = totalWrong;
        }
        String type = ZdypBehavior.classify(totalWrong, weaknessWrong, otherWrong, duration);
        patch.set("behavior_type", type)
                .set("behavior_name", ZdypBehavior.name(type))
                .set("conclusion", ZdypBehavior.conclusion(type))
                .set("suggestion", ZdypBehavior.suggestion(type));
        record.setBehaviorType(type);
        record.setBehaviorName(ZdypBehavior.name(type));
        record.setConclusion(ZdypBehavior.conclusion(type));
        record.setSuggestion(ZdypBehavior.suggestion(type));
    }

    /** 完成活动游戏参与记录；记录缺失时不阻断学生查看报告。 */
    private void completeParticipation(ZdypRecord record) {
        GkzhGameParticipation participation = activityWeekService.getLatestParticipation(record.getGameId(), record.getUserId());
        if (participation == null) {
            return;
        }
        JSONObject result = new JSONObject();
        result.put("result", "pass");
        result.put("careerCode", record.getCareerCode());
        result.put("careerName", record.getCareerName());
        result.put("behaviorType", record.getBehaviorType());
        result.put("behaviorName", record.getBehaviorName());
        result.put("attemptNo", record.getAttemptNo());
        result.put("bestMs", record.getBestMs());
        participation.setStatus("1");
        participation.setFinishTime(DateUtils.getNowDate());
        participation.setResultJson(result.toJSONString());
        activityWeekService.updateGameParticipation(participation);
    }

    /** 学生端展示数据：记录状态、岗位目录、规则参数、进行中的尝试与最近一次结果。 */
    public Map<String, Object> view(ZdypRecord record) {
        ZdypSetting setting = configService.getSetting();
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
        data.put("bestMs", record.getBestMs());
        data.put("firstAttemptCorrect", "1".equals(record.getFirstAttemptCorrect()));

        Map<String, Object> settingView = new LinkedHashMap<>();
        settingView.put("timeLimit", setting.getTimeLimit());
        settingView.put("enabled", setting.getEnabled());
        data.put("setting", settingView);

        data.put("careers", careerCatalog());
        data.put("selectedCareer", careerView(record.getCareerId() == null ? null
                : configService.findActiveCareer(record.getCareerId()), record));

        ZdypAttempt pending = findPendingAttempt(record.getRecordId());
        if (pending != null && !STATUS_FINISHED.equals(record.getStatus())) {
            ZdypCareer career = pending.getCareerId() == null ? null
                    : configService.findActiveCareer(pending.getCareerId());
            List<ZdypCard> cards = career == null ? new ArrayList<>()
                    : configService.listActiveCards(career.getCareerId());
            if (career != null && cards.size() == 4) {
                data.put("pendingAttempt", startView(record, career, cards, pending, setting));
            }
        }

        ZdypAttempt last = attemptMapper.selectOne(new QueryWrapper<ZdypAttempt>()
                .eq("record_id", record.getRecordId()).isNotNull("submit_time")
                .orderByDesc("attempt_no").last("LIMIT 1"));
        if (last != null) {
            ZdypCareer career = last.getCareerId() == null ? null : configService.findActiveCareer(last.getCareerId());
            if (career != null) {
                data.put("lastResult", resultView(record, last, career));
            }
        }
        return data;
    }

    /** 一次闯关的开始视图：卡片只返回编号与正文，维度由服务端保留，避免客户端直接得到答案。 */
    private Map<String, Object> startView(ZdypRecord record, ZdypCareer career, List<ZdypCard> cards,
                                          ZdypAttempt attempt, ZdypSetting setting) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recordId", record.getRecordId());
        data.put("attemptNo", attempt.getAttemptNo());
        data.put("startedAt", attempt.getCreateTime());
        int limit = setting.getTimeLimit() == null ? 0 : setting.getTimeLimit();
        data.put("timeLimit", limit);
        data.put("deadlineAt", limit > 0 && attempt.getCreateTime() != null
                ? attempt.getCreateTime().getTime() + limit * 1000L : null);
        data.put("career", careerView(career, record));
        data.put("slots", slotList());
        List<Map<String, Object>> cardViews = new ArrayList<>();
        List<ZdypCard> ordered = orderedCards(cards, attemptSeed(attempt));
        for (int i = 0; i < ordered.size(); i++) {
            ZdypCard card = ordered.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("cardId", card.getCardId());
            item.put("cardNo", i + 1);
            item.put("cardText", card.getCardText());
            cardViews.add(item);
        }
        data.put("cards", cardViews);
        return data;
    }

    /** 每次尝试的随机种子：优先使用尝试编号（全局唯一），缺失时退回尝试序号。 */
    private long attemptSeed(ZdypAttempt attempt) {
        if (attempt == null) {
            return 0L;
        }
        return attempt.getAttemptId() != null ? attempt.getAttemptId()
                : (attempt.getAttemptNo() == null ? 0L : attempt.getAttemptNo().longValue());
    }

    /**
     * 按本局种子打乱卡片顺序，使每局的选项排列随机，避免固定顺序被记住或直接对应卡槽。
     * 结果页用同一套规则还原正确答案的编号，因此必须与开始闯关时保持完全一致；
     * 这里用「种子 + 卡片编号」的混合哈希排序，不依赖数据库随机函数，同一次尝试可重复得到相同顺序。
     */
    static List<ZdypCard> orderedCards(List<ZdypCard> cards, long seed) {
        List<ZdypCard> copy = new ArrayList<>(cards);
        copy.sort(java.util.Comparator.comparingLong(card -> shuffleKey(seed, card.getCardId())));
        return copy;
    }

    /**
     * SplitMix64 风格的混合哈希。
     * 早期版本用「31 * hash + 字符」的简单累加，同一前缀的编号（41、42、43、44）算出的键
     * 只差最后一位，排序结果恒等于原顺序，等于没有随机；这里改用乘法混合，种子不同则结果互不相关。
     */
    static long shuffleKey(long seed, Long cardId) {
        long x = seed * 0x9E3779B97F4A7C15L + (cardId == null ? 0L : cardId) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 30)) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 27)) * 0x94D049BB133111EBL;
        return x ^ (x >>> 31);
    }

    /** 一次闯关的结果视图：四个卡槽的正确答案、学生放置内容、科普文案与行为分型。 */
    private Map<String, Object> resultView(ZdypRecord record, ZdypAttempt attempt, ZdypCareer career) {
        Map<String, ZdypCard> solution = configService.cardsByDimension(career.getCareerId());
        List<ZdypAnswer> answers = answerMapper.selectList(new QueryWrapper<ZdypAnswer>()
                .eq("attempt_id", attempt.getAttemptId()).orderByAsc("place_order"));
        Map<String, ZdypAnswer> bySlot = new LinkedHashMap<>();
        for (ZdypAnswer answer : answers) {
            bySlot.put(answer.getSlotCode(), answer);
        }
        Map<Long, String> cardTexts = new HashMap<>();
        for (ZdypCard card : configService.listAllCards(career.getCareerId())) {
            cardTexts.put(card.getCardId(), card.getCardText());
        }
        // 与本局开始闯关时相同的编号规则：结果页“正确排序”必须能对上学生看到的卡片编号。
        Map<Long, Integer> cardNos = new HashMap<>();
        List<ZdypCard> ordered = orderedCards(configService.listActiveCards(career.getCareerId()),
                attemptSeed(attempt));
        for (int i = 0; i < ordered.size(); i++) {
            cardNos.put(ordered.get(i).getCardId(), i + 1);
        }
        List<Map<String, Object>> slots = new ArrayList<>();
        for (Map.Entry<String, String> slot : ZdypConfigService.SLOTS.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            ZdypCard correctCard = solution.get(slot.getKey());
            ZdypAnswer answer = bySlot.get(slot.getKey());
            row.put("slotCode", slot.getKey());
            row.put("slotName", slot.getValue());
            row.put("correctCardId", correctCard == null ? null : correctCard.getCardId());
            row.put("correctCardNo", correctCard == null ? null : cardNos.get(correctCard.getCardId()));
            row.put("correctCardText", correctCard == null ? null : correctCard.getCardText());
            row.put("cardId", answer == null ? null : answer.getCardId());
            row.put("cardNo", answer == null ? null : cardNos.get(answer.getCardId()));
            row.put("cardText", answer == null ? null : cardTexts.get(answer.getCardId()));
            row.put("correct", answer != null && "1".equals(answer.getIsCorrect()));
            row.put("filled", answer != null);
            row.put("elapsedMs", answer == null ? null : answer.getElapsedMs());
            slots.add(row);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recordId", record.getRecordId());
        data.put("attemptNo", attempt.getAttemptNo());
        data.put("career", careerView(career, record));
        data.put("slots", slots);
        data.put("pass", "1".equals(attempt.getIsPass()));
        data.put("timeout", "1".equals(attempt.getIsTimeout()));
        data.put("correctCount", attempt.getCorrectCount());
        data.put("wrongCount", attempt.getWrongCount());
        data.put("filledCount", attempt.getFilledCount());
        data.put("durationMs", attempt.getDurationMs());
        data.put("timeLimit", configService.getSetting().getTimeLimit());

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("intro", career.getIntro());
        detail.put("industryValue", career.getIndustryValue());
        detail.put("newcomerAdvice", career.getNewcomerAdvice());
        data.put("detail", detail);

        data.put("finished", STATUS_FINISHED.equals(record.getStatus()));
        data.put("behaviorType", record.getBehaviorType());
        data.put("behaviorName", record.getBehaviorName());
        data.put("conclusion", record.getConclusion());
        data.put("suggestion", record.getSuggestion());
        data.put("bestMs", record.getBestMs());
        data.put("totalAttempts", record.getAttemptNo());
        return data;
    }

    /** 学生端岗位目录：只返回编号、编码与名称，不返回卡片维度。 */
    private List<Map<String, Object>> careerCatalog() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ZdypCareer career : configService.listActiveCareers()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("careerId", career.getCareerId());
            item.put("careerCode", career.getCareerCode());
            item.put("careerName", career.getCareerName());
            rows.add(item);
        }
        return rows;
    }

    private List<Map<String, Object>> slotList() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, String> slot : ZdypConfigService.SLOTS.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("slotCode", slot.getKey());
            item.put("slotName", slot.getValue());
            rows.add(item);
        }
        return rows;
    }

    private Map<String, Object> careerView(ZdypCareer career, ZdypRecord record) {
        if (career == null) {
            // 岗位后续被停用时，历史记录仍要能显示当时的岗位名称。
            if (record == null || record.getCareerCode() == null) {
                return null;
            }
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("careerId", record.getCareerId());
            fallback.put("careerCode", record.getCareerCode());
            fallback.put("careerName", record.getCareerName());
            return fallback;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("careerId", career.getCareerId());
        data.put("careerCode", career.getCareerCode());
        data.put("careerName", career.getCareerName());
        return data;
    }

    private ZdypCareer findActiveCareerByCode(String careerCode) {
        for (ZdypCareer career : configService.listActiveCareers()) {
            if (career.getCareerCode().equals(careerCode == null ? null : careerCode.trim())) {
                return career;
            }
        }
        return null;
    }

    private ZdypAttempt findPendingAttempt(Long recordId) {
        return attemptMapper.selectOne(new QueryWrapper<ZdypAttempt>()
                .eq("record_id", recordId).isNull("submit_time")
                .orderByDesc("attempt_no").last("LIMIT 1"));
    }

    private ZdypAttempt findAttempt(Long recordId, int attemptNo) {
        return attemptMapper.selectOne(new QueryWrapper<ZdypAttempt>()
                .eq("record_id", recordId).eq("attempt_no", attemptNo));
    }

    /** 校验游戏是否启用并返回规则参数；停用时给出可理解的业务提示。 */
    private ZdypSetting requireEnabled() {
        ZdypSetting setting = configService.getSetting();
        if (!"1".equals(setting.getEnabled())) {
            throw new ServiceException("职得一拼暂未开放，请稍后再试");
        }
        return setting;
    }

    /** 开始或提交前锁定记录行，避免并发请求产生两套进行中的尝试。 */
    private ZdypRecord lockRecord(Long recordId, Long userId) {
        ZdypRecord record = recordMapper.selectOne(new QueryWrapper<ZdypRecord>()
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
