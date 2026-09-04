package com.gkzh.zycck.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.gkzh.zycck.mapper.ZycckCategoryMapper;
import com.gkzh.zycck.mapper.ZycckCareerQuestionMapper;
import com.gkzh.zycck.domain.ZycckCategory;
import com.gkzh.zycck.domain.ZycckCareerQuestion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ZycckRecordService {
    private final ZycckRecordMapper recordMapper;
    private final IActivityWeekService activityWeekService;
    private final ZycckCategoryMapper categoryMapper;
    private final ZycckCareerQuestionMapper questionMapper;

    public ZycckRecordService(ZycckRecordMapper recordMapper, IActivityWeekService activityWeekService,
                              ZycckCategoryMapper categoryMapper, ZycckCareerQuestionMapper questionMapper) {
        this.recordMapper = recordMapper; this.activityWeekService = activityWeekService;
        this.categoryMapper = categoryMapper; this.questionMapper = questionMapper;
    }

    @Transactional
    public ZycckRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId) {
        return enter(schoolId, instanceId, gameId, userId, null);
    }

    @Transactional
    public ZycckRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId, Long studentId) {
        return enter(schoolId, instanceId, gameId, userId, studentId, null, null, null);
    }

    @Transactional
    public ZycckRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId, Long studentId, Long departmentId, String major, String gender) {
        if (schoolId == null || instanceId == null || gameId == null || userId == null) {
            throw new ServiceException("缺少活动或学校信息");
        }
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null || !instanceId.equals(game.getInstanceId()) || !"zycck".equals(game.getGameType())) {
            throw new ServiceException("该游戏不属于当前活动");
        }
        ZycckRecord existing = recordMapper.selectOne(new QueryWrapper<ZycckRecord>()
                .eq("school_id", schoolId).eq("instance_id", instanceId).eq("game_id", gameId).eq("user_id", userId));
        if (existing != null) return existing;
        Date now = DateUtils.getNowDate();
        ZycckRecord record = new ZycckRecord();
        record.setSchoolId(schoolId); record.setInstanceId(instanceId); record.setGameId(gameId); record.setUserId(userId);
        record.setGameType("zycck"); record.setStatus("participating"); record.setStage("scanned");
        record.setCurrentQuestionNo(0); record.setScanTime(now); record.setCreateTime(now); record.setUpdateTime(now);
        record.setStudentId(studentId);
        record.setDepartmentId(departmentId); record.setMajor(major); record.setGender(gender);
        recordMapper.insert(record);
        // 扫码即参与统计：与 zycck 业务记录在同一事务内写入活动参与表，且按活动实例隔离。
        GkzhGameParticipation participation = new GkzhGameParticipation();
        participation.setInstanceId(instanceId); participation.setGameId(gameId);
        participation.setAreaId(game.getAreaId()); participation.setSchoolId(schoolId);
        participation.setStudentId(studentId); participation.setUserId(userId);
        participation.setScanTime(now); participation.setStatus("0");
        activityWeekService.recordGameEnter(participation);
        return record;
    }

    public ZycckRecord get(Long recordId, Long userId) {
        ZycckRecord record = recordMapper.selectById(recordId);
        if (record == null || !userId.equals(record.getUserId())) throw new ServiceException("记录不存在");
        return record;
    }

    @Transactional
    public ZycckRecord start(Long recordId, Long userId) {
        ZycckRecord record = get(recordId, userId);
        if ("finished".equals(record.getStatus())) throw new ServiceException("本次游戏已完成");
        if (record.getStartTime() == null) {
            record.setStartTime(DateUtils.getNowDate());
            prepareQuestionSnapshot(record);
            record.setStage("question");
            record.setCurrentQuestionNo(1);
        } else if ("question".equals(record.getStage()) && findAnswer(record, currentQuestionNo(record)) != null) {
            // 兼容旧逻辑曾将已答题记录错误重置为 question 的数据，恢复到反馈阶段。
            record.setStage("feedback");
        } else if (record.getStage() == null || "scanned".equals(record.getStage())) {
            record.setStage("question");
            record.setCurrentQuestionNo(currentQuestionNo(record));
        }
        if (record.getQuestionStartTime() == null && record.getCurrentQuestionNo() > 1) record.setQuestionStartTime(DateUtils.getNowDate());
        recordMapper.updateById(record); return record;
    }

    @Transactional
    public ZycckRecord openQuestion(Long recordId, Long userId) { ZycckRecord record = get(recordId, userId); if ("finished".equals(record.getStatus())) throw new ServiceException("本次游戏已完成"); if (record.getQuestionStartTime() == null) { record.setStage("question"); record.setQuestionStartTime(DateUtils.getNowDate()); record.setUpdateTime(DateUtils.getNowDate()); recordMapper.updateById(record); } return record; }

    private void prepareQuestionSnapshot(ZycckRecord record) {
        java.util.List<ZycckCategory> categories = categoryMapper.selectList(new QueryWrapper<ZycckCategory>().eq("status", "0").orderByAsc("sort_order"));
        if (categories.size() != 5) throw new ServiceException("职业大类配置必须为 5 类");
        java.util.List<Long> questionIds = new java.util.ArrayList<>(); java.util.List<Long> categoryIds = new java.util.ArrayList<>(); java.util.List<Long> careerIds = new java.util.ArrayList<>();
        java.util.List<JSONObject> snapshots = new java.util.ArrayList<>();
        for (ZycckCategory category : categories) {
            java.util.List<ZycckCareerQuestion> candidates = questionMapper.selectList(new QueryWrapper<ZycckCareerQuestion>().eq("category_id", category.getCategoryId()).eq("status", "0").eq("has_question", "1").eq("draw_candidate", "1").orderByAsc("sort_order", "career_question_id"));
            int required = "random".equalsIgnoreCase(category.getDrawMode()) ? 3 : 1;
            if (candidates.size() != required) throw new ServiceException(category.getName() + "的抽题候选数量不符合配置");
            ZycckCareerQuestion q = candidates.get("random".equalsIgnoreCase(category.getDrawMode()) ? new java.util.Random().nextInt(candidates.size()) : 0);
            questionIds.add(q.getCareerQuestionId()); categoryIds.add(category.getCategoryId()); careerIds.add(q.getCareerQuestionId());
            JSONObject safe = new JSONObject(); safe.put("questionId", q.getCareerQuestionId()); safe.put("questionNo", snapshots.size() + 1); safe.put("categoryId", q.getCategoryId()); safe.put("careerName", q.getCareerName()); safe.put("questionImageUrl", q.getQuestionImageUrl()); safe.put("optionA", q.getOptionA()); safe.put("optionB", q.getOptionB()); safe.put("optionC", q.getOptionC()); safe.put("optionD", q.getOptionD()); snapshots.add(safe);
        }
        record.setQuestionIds(JSON.toJSONString(questionIds)); record.setQuestionOrder(JSON.toJSONString(questionIds)); record.setCategoryIds(JSON.toJSONString(categoryIds)); record.setCareerIds(JSON.toJSONString(careerIds)); record.setOptionSnapshotJson(JSON.toJSONString(snapshots)); record.setConfigVersion(String.valueOf(System.currentTimeMillis()));
    }

    @Transactional
    public Map<String,Object> answer(Long recordId, Long userId, Map<String,Object> answer) {
        ZycckRecord record = get(recordId, userId);
        if ("finished".equals(record.getStatus())) throw new ServiceException("本次游戏已完成");
        int no = currentQuestionNo(record);
        boolean timeout = Boolean.parseBoolean(String.valueOf(answer.getOrDefault("timeoutFlag", false)));
        java.util.List<JSONObject> snapshots = record.getOptionSnapshotJson() == null ? java.util.Collections.emptyList() : JSON.parseArray(record.getOptionSnapshotJson(), JSONObject.class);
        JSONObject current = no <= snapshots.size() ? snapshots.get(no - 1) : null;
        if (current == null) throw new ServiceException("题目已失效，请重新进入游戏");
        JSONObject submitted = findAnswer(record, no);
        if (submitted != null && submitted.getString("optionKey") != null) {
            // 网络重试或旧版本错误恢复时返回首次提交结果，避免让用户卡在第一题。
            record.setStage("feedback"); record.setUpdateTime(DateUtils.getNowDate()); recordMapper.updateById(record);
            return feedbackResult(no, current, submitted.getString("optionKey"));
        }
        if (!"question".equals(record.getStage())) throw new ServiceException("当前不在答题阶段");
        String optionKey = answer.get("optionKey") == null ? null : String.valueOf(answer.get("optionKey")).toUpperCase();
        Integer requestedNo = answer.get("questionNo") == null ? null : Integer.valueOf(String.valueOf(answer.get("questionNo"))); if (requestedNo != null && requestedNo != no) throw new ServiceException("这道题已经提交过了");
        String requestedQuestionId = answer.get("questionId") == null ? null : String.valueOf(answer.get("questionId")); if (requestedQuestionId != null && !requestedQuestionId.equals(String.valueOf(current.getLong("questionId")))) throw new ServiceException("题目已更新，请按当前题目作答");
        if (submitted != null) throw new ServiceException("这道题已经提交过了");
        long elapsed = record.getQuestionStartTime() == null ? 0 : Math.max(0, (System.currentTimeMillis() - record.getQuestionStartTime().getTime()) / 1000);
        if (elapsed > 60) timeout = true;
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("questionNo", no); result.put("timeout", timeout);
        java.util.List<JSONObject> answers = record.getAnswerJson() == null ? new java.util.ArrayList<>() : JSON.parseArray(record.getAnswerJson(), JSONObject.class);
        JSONObject answerItem = new JSONObject(); answerItem.put("questionNo", no); answerItem.put("questionId", current.getLong("questionId")); answerItem.put("optionKey", timeout ? null : optionKey); answerItem.put("timeout", timeout); answerItem.put("elapsedSeconds", Math.min(60, (int) elapsed));
        answers.removeIf(item -> no == item.getIntValue("questionNo")); answers.add(answerItem); record.setAnswerJson(JSON.toJSONString(answers));
        record.setQuestionElapsedSeconds(Math.min(60, (int) elapsed));
        if (!timeout && optionKey != null) {
            appendFeedback(result, no, current, optionKey);
        }
        int next = no + 1;
        if (!timeout && optionKey != null) {
            // 有效作答先进入反馈页，提交了解程度后才推进下一题。
            record.setStage("feedback"); result.put("stage", "feedback"); result.put("currentQuestionNo", no);
        } else if (next > 5) { record.setStage("summary"); record.setStatus("participating"); result.put("stage", "summary"); result.put("finishedFive", true); }
        else { record.setStage("question"); record.setCurrentQuestionNo(next); record.setQuestionStartTime(DateUtils.getNowDate()); result.put("currentQuestionNo", next); result.put("remainingSeconds", 60); result.put("nextQuestion", snapshots.get(next - 1)); }
        record.setUpdateTime(DateUtils.getNowDate()); recordMapper.updateById(record); return result;
    }

    public Map<String,Object> feedbackView(ZycckRecord record) {
        if (record == null || !"feedback".equals(record.getStage())) return java.util.Collections.emptyMap();
        int no = currentQuestionNo(record);
        java.util.List<JSONObject> snapshots = record.getOptionSnapshotJson() == null ? java.util.Collections.emptyList() : JSON.parseArray(record.getOptionSnapshotJson(), JSONObject.class);
        JSONObject submitted = findAnswer(record, no);
        if (submitted == null || submitted.getString("optionKey") == null || no > snapshots.size()) return java.util.Collections.emptyMap();
        return feedbackResult(no, snapshots.get(no - 1), submitted.getString("optionKey"));
    }

    private Map<String,Object> feedbackResult(int no, JSONObject current, String optionKey) {
        Map<String,Object> result = new LinkedHashMap<>(); result.put("questionNo", no); result.put("timeout", false);
        appendFeedback(result, no, current, optionKey); result.put("stage", "feedback"); result.put("currentQuestionNo", no); return result;
    }

    private void appendFeedback(Map<String,Object> result, int no, JSONObject current, String optionKey) {
        ZycckCareerQuestion detail = questionMapper.selectById(current.getLong("questionId"));
        if (detail == null) return;
        JSONObject feedback = new JSONObject(); feedback.put("questionNo", no); feedback.put("selectedOptionKey", optionKey); feedback.put("guessedCareer", careerName(detail, optionKey)); feedback.put("correctOptionKey", detail.getCorrectOptionKey()); feedback.put("correct", optionKey.equalsIgnoreCase(detail.getCorrectOptionKey())); feedback.put("correctCareerId", careerId(detail, detail.getCorrectOptionKey())); feedback.put("correctCareerName", careerName(detail, detail.getCorrectOptionKey())); feedback.put("oneLineIntro", detail.getOneLineIntro()); feedback.put("mainWork", detail.getMainWork()); feedback.put("dayExample", detail.getDayExample()); feedback.put("whyExists", detail.getWhyExists()); feedback.put("careerImageUrl", detail.getCareerImageUrl()); feedback.put("explanation", detail.getExplanation()); result.put("feedback", feedback); result.put("guessedCareer", feedback.get("guessedCareer")); result.put("correctCareer", feedback.get("correctCareerName")); result.put("explanation", detail.getExplanation());
    }

    private static int currentQuestionNo(ZycckRecord record) { return record.getCurrentQuestionNo() == null || record.getCurrentQuestionNo() < 1 ? 1 : record.getCurrentQuestionNo(); }
    private static JSONObject findAnswer(ZycckRecord record, int no) {
        if (record.getAnswerJson() == null) return null;
        return JSON.parseArray(record.getAnswerJson(), JSONObject.class).stream().filter(v -> no == v.getIntValue("questionNo")).findFirst().orElse(null);
    }

    private static Long careerId(ZycckCareerQuestion q, String key) { if ("A".equalsIgnoreCase(key)) return q.getOptionACareerId(); if ("B".equalsIgnoreCase(key)) return q.getOptionBCareerId(); if ("C".equalsIgnoreCase(key)) return q.getOptionCCareerId(); return q.getOptionDCareerId(); }
    private static String careerName(ZycckCareerQuestion q, String key) { if ("A".equalsIgnoreCase(key)) return q.getOptionA(); if ("B".equalsIgnoreCase(key)) return q.getOptionB(); if ("C".equalsIgnoreCase(key)) return q.getOptionC(); return q.getOptionD(); }

    @Transactional
    public Map<String,Object> awareness(Long recordId, Long userId, Map<String,Object> body) {
        ZycckRecord record = get(recordId, userId); if ("finished".equals(record.getStatus())) throw new ServiceException("本次游戏已完成");
        if (!"feedback".equals(record.getStage())) throw new ServiceException("当前没有待提交的了解程度");
        java.util.List<JSONObject> values = record.getAwarenessJson() == null ? new java.util.ArrayList<>() : JSON.parseArray(record.getAwarenessJson(), JSONObject.class);
        JSONObject item = new JSONObject(); item.put("questionNo", record.getCurrentQuestionNo()); item.put("level", body == null ? null : (body.get("level") != null ? body.get("level") : body.get("awareness"))); values.removeIf(v -> record.getCurrentQuestionNo().equals(v.getInteger("questionNo"))); values.add(item); record.setAwarenessJson(JSON.toJSONString(values));
        int no = record.getCurrentQuestionNo() == null ? 1 : record.getCurrentQuestionNo(); int next = no + 1; Map<String,Object> result = new LinkedHashMap<>();
        java.util.List<JSONObject> snapshots = JSON.parseArray(record.getOptionSnapshotJson(), JSONObject.class);
        if (next > 5) { record.setStage("summary"); result.put("stage", "summary"); result.put("finishedFive", true); }
        else { record.setStage("question"); record.setCurrentQuestionNo(next); record.setQuestionStartTime(DateUtils.getNowDate()); result.put("stage", "question"); result.put("currentQuestionNo", next); result.put("remainingSeconds", 60); result.put("nextQuestion", snapshots.get(next - 1)); }
        record.setUpdateTime(DateUtils.getNowDate()); recordMapper.updateById(record); return result;
    }

    @Transactional
    public ZycckRecord finish(Long recordId, Long userId) {
        ZycckRecord record = get(recordId, userId);
        if ("finished".equals(record.getStatus())) return record;
        if (!"summary".equals(record.getStage()) && (record.getCurrentQuestionNo() == null || record.getCurrentQuestionNo() <= 5)) {
            throw new ServiceException("请先完成五道题");
        }
        if (record.getViewedCareerIds() == null || JSON.parseArray(record.getViewedCareerIds(), Long.class).isEmpty()) throw new ServiceException("请先查看职业详情");
        record.setStatus("finished"); record.setStage("exploration"); record.setFinishTime(DateUtils.getNowDate()); record.setUpdateTime(DateUtils.getNowDate());
        recordMapper.updateById(record);
        GkzhGameParticipation participation = activityWeekService.getLatestParticipation(record.getGameId(), record.getUserId());
        if (participation != null && record.getInstanceId().equals(participation.getInstanceId())) { participation.setFinishTime(record.getFinishTime()); participation.setStatus("1"); participation.setResultJson(JSON.toJSONString(java.util.Collections.singletonMap("recordId", record.getRecordId()))); activityWeekService.updateGameParticipation(participation); }
        return record;
    }

    @Transactional
    public ZycckRecord browse(Long recordId, Long userId, Long careerId) {
        ZycckRecord record = get(recordId, userId); if ("finished".equals(record.getStatus())) throw new ServiceException("本次游戏已完成");
        ZycckCareerQuestion career = questionMapper.selectById(careerId);
        if (career == null || !"0".equals(career.getStatus()) || "1".equals(career.getHasQuestion())) throw new ServiceException("该职业仅用于竞猜，暂不可探索");
        java.util.List<Long> ids = record.getViewedCareerIds() == null ? new java.util.ArrayList<>() : JSON.parseArray(record.getViewedCareerIds(), Long.class); if (careerId != null && !ids.contains(careerId)) ids.add(careerId); record.setViewedCareerIds(JSON.toJSONString(ids)); record.setUpdateTime(DateUtils.getNowDate()); recordMapper.updateById(record); return record;
    }

    public Map<String,Object> exploration(Long recordId, Long userId) {
        ZycckRecord record = get(recordId, userId); Map<String,Object> out = new LinkedHashMap<>(); out.put("record", record); out.put("viewedCareerIds", record.getViewedCareerIds() == null ? java.util.Collections.emptyList() : JSON.parseArray(record.getViewedCareerIds(), Long.class));
        java.util.List<Long> ids = record.getExplorationCareerIds() == null ? new java.util.ArrayList<>() : JSON.parseArray(record.getExplorationCareerIds(), Long.class); out.put("explorationCareerIds", ids);
        java.util.List<Map<String,Object>> items = new java.util.ArrayList<>(); if (!ids.isEmpty()) for (ZycckCareerQuestion q : questionMapper.selectBatchIds(ids)) { Map<String,Object> item = new LinkedHashMap<>(); item.put("careerId", q.getCareerQuestionId()); item.put("careerName", q.getCareerName()); item.put("careerImageUrl", q.getCareerImageUrl()); items.add(item); } out.put("items", items); out.put("count", items.size()); out.put("limit", 6); out.put("readOnly", "finished".equals(record.getStatus())); return out;
    }

    @Transactional
    public ZycckRecord updateExploration(Long recordId, Long userId, Long careerId, boolean remove) {
        ZycckRecord record = get(recordId, userId); if ("finished".equals(record.getStatus())) throw new ServiceException("本次游戏已完成，结果仅可查看");
        ZycckCareerQuestion career = questionMapper.selectById(careerId);
        if (career == null || !"0".equals(career.getStatus()) || "1".equals(career.getHasQuestion())) throw new ServiceException("该职业仅用于竞猜，不能加入探索清单");
        java.util.List<Long> ids = record.getExplorationCareerIds() == null ? new java.util.ArrayList<>() : JSON.parseArray(record.getExplorationCareerIds(), Long.class); if (remove) ids.remove(careerId); else { if (!ids.contains(careerId) && ids.size() >= 6) throw new ServiceException("探索清单最多添加 6 个职业"); if (!ids.contains(careerId)) ids.add(careerId); } record.setExplorationCareerIds(JSON.toJSONString(ids)); record.setUpdateTime(DateUtils.getNowDate()); recordMapper.updateById(record); return record;
    }
}
