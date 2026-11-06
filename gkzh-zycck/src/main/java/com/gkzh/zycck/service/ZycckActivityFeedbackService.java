package com.gkzh.zycck.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.zycck.domain.ZycckActivityFeedback;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.dto.ZycckActivityFeedbackRequest;
import com.gkzh.zycck.dto.ZycckActivityFeedbackView;
import com.gkzh.zycck.dto.ZycckFeedbackQuestionView;
import com.gkzh.zycck.mapper.ZycckActivityFeedbackMapper;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** 探索清单确认后的活动反馈；有效反馈保存成功后才结束游戏。 */
@Service
public class ZycckActivityFeedbackService {
    /** 当前固定问卷版本；修改题意或选项编码时应升级版本。 */
    private static final String VERSION = "1";
    private final ZycckRecordMapper records;
    private final ZycckActivityFeedbackMapper feedbacks;
    private final IActivityWeekService activityWeekService;

    public ZycckActivityFeedbackService(ZycckRecordMapper records, ZycckActivityFeedbackMapper feedbacks, IActivityWeekService activityWeekService) {
        this.records = records;
        this.feedbacks = feedbacks;
        this.activityWeekService = activityWeekService;
    }

    /** 本人清单已确认即可填写反馈；兼容查看旧版已结束记录的反馈。 */
    public ZycckActivityFeedbackView get(Long recordId, Long userId, Long instanceId, Long gameId) {
        ZycckRecord record = records.selectById(recordId);
        validateContext(record, userId, instanceId, gameId);
        return view(record);
    }

    /** 活动页一次查询本人的已完成游戏；旧完成标记没有对应反馈时不能显示完成。 */
    public Set<Long> completedGameIds(Long instanceId, Long userId) {
        if (instanceId == null || userId == null) return Collections.emptySet();
        return records.selectList(new QueryWrapper<ZycckRecord>().select("game_id")
                .eq("instance_id", instanceId).eq("user_id", userId).eq("game_type", "zycck")
                .eq("status", "finished")
                .apply("EXISTS (SELECT 1 FROM gkzh_zycck_activity_feedback f "
                        + "WHERE f.record_id = gkzh_zycck_record.record_id)"))
                .stream().map(ZycckRecord::getGameId).collect(Collectors.toSet());
    }

    /** 导出使用同一套中文题意和稳定选项编码，不能单独维护另一份问卷文字。 */
    public List<ZycckFeedbackQuestionView> exportQuestionDefinitions() {
        return questions(true);
    }

    /** Web 按已经查到的参与记录查看问卷，不产生反馈写入。 */
    public ZycckActivityFeedbackView view(ZycckRecord record) {
        ZycckActivityFeedback row = feedbacks.selectById(record.getRecordId());
        ZycckActivityFeedbackView view = new ZycckActivityFeedbackView();
        view.setRecordId(record.getRecordId());
        view.setQuestionnaireVersion(row == null ? VERSION : row.getQuestionnaireVersion());
        view.setSubmitted(row != null);
        view.setGameFinished("finished".equals(record.getStatus()));
        view.setShowQuestion2(row == null ? hasExplorationHistory(record) : "1".equals(row.getShowQuestion2()));
        view.setQuestions(questions(view.isShowQuestion2()));
        ZycckActivityFeedbackRequest answers = new ZycckActivityFeedbackRequest();
        answers.setQuestion1Answers(row == null ? Collections.emptyList() : JSON.parseArray(row.getQuestion1Answers(), String.class));
        answers.setQuestion1Other(row == null ? "" : row.getQuestion1Other());
        answers.setQuestion2Answer(row == null ? "" : row.getQuestion2Answer());
        answers.setQuestion2Other(row == null ? "" : row.getQuestion2Other());
        answers.setQuestion3Answers(row == null ? Collections.emptyList() : JSON.parseArray(row.getQuestion3Answers(), String.class));
        answers.setQuestion3Other(row == null ? "" : row.getQuestion3Other());
        view.setAnswers(answers);
        view.setSubmittedTime(row == null ? null : row.getSubmittedTime());
        return view;
    }

    /** 锁住本次参与记录后首次提交；已提交记录只读，并发请求也不能覆盖原答案。 */
    @Transactional
    public ZycckActivityFeedbackView save(Long recordId, Long userId, Long instanceId, Long gameId,
                                         ZycckActivityFeedbackRequest input) {
        ZycckRecord record = records.selectOne(new QueryWrapper<ZycckRecord>()
                .eq("record_id", recordId).last("FOR UPDATE"));
        validateContext(record, userId, instanceId, gameId);
        if (feedbacks.selectById(recordId) != null) throw new ServiceException("活动反馈已提交，不可再次修改");
        if (input == null) throw new ServiceException("请填写活动反馈");
        boolean showQuestion2 = hasExplorationHistory(record);
        List<ZycckFeedbackQuestionView> definitions = questions(true);
        List<String> first = validateMultiple(input.getQuestion1Answers(), definitions.get(0));
        List<String> third = validateMultiple(input.getQuestion3Answers(), definitions.get(2));
        String second = trim(input.getQuestion2Answer());
        if (showQuestion2) {
            if (!codes(definitions.get(1)).contains(second)) throw new ServiceException("请选择第2题的选项");
        } else if (!second.isEmpty() || !trim(input.getQuestion2Other()).isEmpty()) {
            throw new ServiceException("未加入过探索清单，无需填写第2题");
        }
        ZycckActivityFeedback row = new ZycckActivityFeedback();
        row.setRecordId(recordId);
        row.setQuestionnaireVersion(VERSION);
        row.setQuestion1Answers(JSON.toJSONString(first));
        row.setQuestion1Other(otherText(first.contains("OTHER"), input.getQuestion1Other(), 1));
        row.setShowQuestion2(showQuestion2 ? "1" : "0");
        row.setQuestion2Answer(showQuestion2 ? second : "");
        row.setQuestion2Other(otherText(showQuestion2 && "OTHER".equals(second), input.getQuestion2Other(), 2));
        row.setQuestion3Answers(JSON.toJSONString(third));
        row.setQuestion3Other(otherText(third.contains("OTHER"), input.getQuestion3Other(), 3));
        row.setSubmittedTime(new Date());
        int saved = feedbacks.insert(row);
        if (saved != 1) throw new ServiceException("活动反馈保存失败，请重试");
        completeGame(record, row.getSubmittedTime());
        return view(record);
    }

    /** 与答案保存处于同一事务；校验或保存失败时不能标记完成，旧记录保留原结束时间。 */
    private void completeGame(ZycckRecord record, Date now) {
        if ("finished".equals(record.getStatus())) return;
        record.setStatus("finished"); record.setStage("finished"); record.setFinishTime(now); record.setUpdateTime(now);
        if (records.updateById(record) != 1) throw new ServiceException("活动反馈完成状态保存失败，请重试");
        GkzhGameParticipation participation = activityWeekService.getLatestParticipation(record.getGameId(), record.getUserId());
        if (participation != null && Objects.equals(record.getInstanceId(), participation.getInstanceId())) {
            participation.setFinishTime(now); participation.setStatus("1");
            participation.setResultJson(JSON.toJSONString(Collections.singletonMap("recordId", record.getRecordId())));
            if (activityWeekService.updateGameParticipation(participation) != 1) throw new ServiceException("活动参与状态保存失败，请重试");
        }
    }

    /** 删除参与记录时同步清理反馈，避免测试重建后遗留无归属答案。 */
    public void deleteForRecord(Long recordId) {
        feedbacks.deleteById(recordId);
    }

    /** 当前清单作为旧记录的兼容依据，新记录使用永久保留的历史标记。 */
    private boolean hasExplorationHistory(ZycckRecord record) {
        return "1".equals(record.getExplorationAdded()) || (record.getExplorationCareerIds() != null
                && !JSON.parseArray(record.getExplorationCareerIds(), Long.class).isEmpty());
    }

    private void validateContext(ZycckRecord record, Long userId, Long instanceId, Long gameId) {
        if (record == null || userId == null || !Objects.equals(userId, record.getUserId())
                || instanceId == null || !Objects.equals(instanceId, record.getInstanceId())
                || gameId == null || !Objects.equals(gameId, record.getGameId()) || !"zycck".equals(record.getGameType())) {
            throw new ServiceException("本次活动反馈入口无效，请从探索报告重新进入");
        }
        if (!"finished".equals(record.getStatus()) && !("participating".equals(record.getStatus())
                && "activity_feedback".equals(record.getStage()))) throw new ServiceException("请先确认探索清单，再填写反馈");
    }

    /** 校验多选上下限、非法编码、重复编码及互斥选项；不依赖客户端限制。 */
    private List<String> validateMultiple(List<String> selected, ZycckFeedbackQuestionView question) {
        String prefix = "第" + question.getQuestionNo() + "题";
        if (selected == null || selected.isEmpty() || selected.size() > question.getMaxSelections()) {
            throw new ServiceException(prefix + "请选择1至3项");
        }
        if (new HashSet<>(selected).size() != selected.size() || !codes(question).containsAll(selected)) {
            throw new ServiceException(prefix + "包含无效或重复的选项");
        }
        if (selected.contains(question.getExclusiveOption()) && selected.size() > 1) {
            throw new ServiceException(prefix + "的暂无选项不能与其他选项同时选择");
        }
        // 按问卷选项顺序存储，让不同点击顺序得到相同的统计编码数组。
        return codes(question).stream().filter(selected::contains).collect(Collectors.toList());
    }

    private List<String> codes(ZycckFeedbackQuestionView question) {
        return question.getOptions().stream().map(ZycckFeedbackQuestionView.Option::getCode).collect(Collectors.toList());
    }

    /** 未选择其他时清空旧文字，避免改答后总结报告仍读到上一份补充。 */
    private String otherText(boolean selected, String value, int no) {
        if (!selected) return "";
        String text = trim(value);
        if (text.isEmpty()) throw new ServiceException("请填写第" + no + "题的其他内容");
        if (text.length() > 500) throw new ServiceException("第" + no + "题的其他内容最多500字");
        return text;
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }

    /** 稳定编码和中文内容由服务端统一下发，后续汇总按版本和编码统计。 */
    private List<ZycckFeedbackQuestionView> questions(boolean showQuestion2) {
        List<ZycckFeedbackQuestionView> list = new ArrayList<>();
        list.add(question(1, "参与本次活动后，你对职业世界有了哪些新的认识？", "multiple", 3, "NONE",
                new String[] {"NEW_CAREER", "WORK_CONTENT", "NEW_WORK", "PROFESSIONAL_LINK", "FUTURE_WORK", "NONE", "OTHER"},
                new String[] {"认识了以前不了解的职业", "对一些职业的具体工作内容有了进一步了解", "对新就业形态、新职业和新的工作方式有了更多认识",
                        "发现职业与自己所学专业可能存在更多联系", "对未来可以从事什么样的工作有了新的思考", "暂时没有明显的新认识", "其他"}));
        if (showQuestion2) list.add(question(2, "你把职业加入“未来职业探索清单”，主要是因为什么？", "single", 1, null,
                new String[] {"WORK_CONTENT", "LEARN_MORE", "PROFESSIONAL_LINK", "INDUSTRY", "CAPABILITY", "FUN", "OTHER"},
                new String[] {"工作内容吸引我", "以前不太了解，想进一步了解", "与我的专业或学习方向有关", "对这个行业或工作方式感兴趣",
                        "想进一步了解这个职业需要哪些能力、如何发展", "觉得有趣，想先保留下来", "其他"}));
        list.add(question(3, "你希望学校后续提供哪些职业探索活动？", "multiple", 3, "NONE",
                new String[] {"CAREER_PATH", "PROFESSIONAL_LINK", "TALKS", "PRACTICE", "NEW_WORK", "INTERNSHIP", "NONE", "OTHER"},
                new String[] {"职业工作内容与发展路径介绍", "专业与职业之间的联系探索", "职业人物分享、行业讲座", "职业体验、项目实践或技能体验",
                        "新就业形态、新职业及AI带来的工作变化", "实习、就业及职场经验分享", "暂时没有特别需求", "其他"}));
        return list;
    }

    private ZycckFeedbackQuestionView question(int no, String title, String type, int max, String exclusive,
                                               String[] codes, String[] labels) {
        ZycckFeedbackQuestionView question = new ZycckFeedbackQuestionView();
        question.setQuestionNo(no); question.setTitle(title); question.setType(type);
        question.setMaxSelections(max); question.setExclusiveOption(exclusive);
        List<ZycckFeedbackQuestionView.Option> options = new ArrayList<>();
        for (int i = 0; i < codes.length; i++) {
            ZycckFeedbackQuestionView.Option option = new ZycckFeedbackQuestionView.Option();
            option.setCode(codes[i]); option.setLabel(labels[i]); option.setRequiresText("OTHER".equals(codes[i]));
            options.add(option);
        }
        question.setOptions(options);
        return question;
    }
}
