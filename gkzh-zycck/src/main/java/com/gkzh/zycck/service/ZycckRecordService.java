package com.gkzh.zycck.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gkzh.activity.domain.week.GkzhActivityGame;
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
        recordMapper.insert(record);
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
        }
        record.setStage("question"); record.setCurrentQuestionNo(record.getCurrentQuestionNo() == null || record.getCurrentQuestionNo() < 1 ? 1 : record.getCurrentQuestionNo()); record.setQuestionStartTime(DateUtils.getNowDate()); record.setUpdateTime(DateUtils.getNowDate());
        recordMapper.updateById(record); return record;
    }

    private void prepareQuestionSnapshot(ZycckRecord record) {
        java.util.List<ZycckCategory> categories = categoryMapper.selectList(new QueryWrapper<ZycckCategory>().eq("status", "0").orderByAsc("sort_order"));
        if (categories.size() != 5) throw new ServiceException("职业大类配置必须为 5 类");
        java.util.List<Long> questionIds = new java.util.ArrayList<>(); java.util.List<Long> categoryIds = new java.util.ArrayList<>(); java.util.List<Long> careerIds = new java.util.ArrayList<>();
        java.util.List<JSONObject> snapshots = new java.util.ArrayList<>();
        for (ZycckCategory category : categories) {
            java.util.List<ZycckCareerQuestion> candidates = questionMapper.selectList(new QueryWrapper<ZycckCareerQuestion>().eq("category_id", category.getCategoryId()).eq("status", "0").eq("draw_candidate", "1").orderByAsc("sort_order", "career_question_id"));
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
        int no = record.getCurrentQuestionNo() == null ? 1 : record.getCurrentQuestionNo();
        boolean timeout = Boolean.parseBoolean(String.valueOf(answer.getOrDefault("timeoutFlag", false)));
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("questionNo", no); result.put("timeout", timeout);
        if (record.getAnswerJson() == null) record.setAnswerJson("[]");
        int next = no + 1;
        if (next > 5) { record.setStage("summary"); record.setStatus("participating"); result.put("stage", "summary"); result.put("finishedFive", true); }
        else { record.setCurrentQuestionNo(next); result.put("currentQuestionNo", next); result.put("remainingSeconds", 60); result.put("nextQuestion", new LinkedHashMap<>()); }
        record.setUpdateTime(DateUtils.getNowDate()); recordMapper.updateById(record); return result;
    }

    @Transactional
    public ZycckRecord finish(Long recordId, Long userId) {
        ZycckRecord record = get(recordId, userId);
        if ("finished".equals(record.getStatus())) return record;
        if (!"summary".equals(record.getStage()) && (record.getCurrentQuestionNo() == null || record.getCurrentQuestionNo() <= 5)) {
            throw new ServiceException("请先完成五道题");
        }
        record.setStatus("finished"); record.setStage("exploration"); record.setFinishTime(DateUtils.getNowDate()); record.setUpdateTime(DateUtils.getNowDate());
        recordMapper.updateById(record); return record;
    }
}
