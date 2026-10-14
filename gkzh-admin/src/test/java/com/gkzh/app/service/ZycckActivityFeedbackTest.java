package com.gkzh.app.service;

import com.gkzh.common.exception.ServiceException;
import com.gkzh.zycck.domain.ZycckActivityFeedback;
import com.gkzh.zycck.domain.ZycckCareerQuestion;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.dto.ZycckActivityFeedbackRequest;
import com.gkzh.zycck.dto.ZycckActivityFeedbackView;
import com.gkzh.zycck.mapper.ZycckActivityFeedbackMapper;
import com.gkzh.zycck.mapper.ZycckCareerQuestionMapper;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.gkzh.zycck.service.ZycckActivityFeedbackService;
import com.gkzh.zycck.service.ZycckRecordService;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 活动反馈规则、活动归属及重复提交回归测试。 */
class ZycckActivityFeedbackTest {
    private ZycckRecord record;
    private ZycckRecordMapper records;
    private ZycckActivityFeedbackMapper feedbacks;
    private ZycckActivityFeedbackService service;
    private IActivityWeekService activities;
    private AtomicReference<ZycckActivityFeedback> stored;

    @BeforeEach
    void setup() {
        records = mock(ZycckRecordMapper.class);
        feedbacks = mock(ZycckActivityFeedbackMapper.class);
        activities = mock(IActivityWeekService.class);
        service = new ZycckActivityFeedbackService(records, feedbacks, activities);
        record = new ZycckRecord();
        record.setRecordId(1L); record.setUserId(2L); record.setInstanceId(3L); record.setGameId(4L);
        record.setGameType("zycck"); record.setStatus("finished"); record.setExplorationCareerIds("[]");
        when(records.selectById(1L)).thenReturn(record);
        when(records.selectOne(any())).thenReturn(record);
        when(records.updateById(any())).thenReturn(1);
        when(activities.updateGameParticipation(any())).thenReturn(1);
        stored = new AtomicReference<>();
        when(feedbacks.selectById(1L)).thenAnswer(invocation -> stored.get());
        when(feedbacks.insert(any())).thenAnswer(invocation -> { stored.set(invocation.getArgument(0)); return 1; });
        when(feedbacks.updateById(any())).thenAnswer(invocation -> { stored.set(invocation.getArgument(0)); return 1; });
    }

    @Test
    void studentsWithoutExplorationSeeOnlyQuestionsOneAndThree() {
        ZycckActivityFeedbackView view = service.get(1L, 2L, 3L, 4L);
        assertFalse(view.isSubmitted());
        assertFalse(view.isShowQuestion2());
        assertEquals(Arrays.asList(1, 3), Arrays.asList(view.getQuestions().get(0).getQuestionNo(), view.getQuestions().get(1).getQuestionNo()));
        assertTrue(service.save(1L, 2L, 3L, 4L, valid()).isSubmitted());
        assertEquals("", stored.get().getQuestion2Answer());
    }

    @Test
    void everAddedFlagShowsQuestionTwoEvenWhenListWasEmptied() {
        record.setExplorationAdded("1");
        assertTrue(service.get(1L, 2L, 3L, 4L).isShowQuestion2());
        assertThrows(ServiceException.class, () -> service.save(1L, 2L, 3L, 4L, valid()));
        ZycckActivityFeedbackRequest input = valid(); input.setQuestion2Answer("WORK_CONTENT");
        assertTrue(service.save(1L, 2L, 3L, 4L, input).isShowQuestion2());
    }

    @Test
    void oldNonemptyListAlsoRequiresQuestionTwo() {
        record.setExplorationCareerIds("[8]");
        assertTrue(service.get(1L, 2L, 3L, 4L).isShowQuestion2());
    }

    @Test
    void cannotSubmitQuestionTwoForStudentsWhoNeverAddedCareers() {
        ZycckActivityFeedbackRequest input = valid(); input.setQuestion2Answer("FUN");
        assertThrows(ServiceException.class, () -> service.save(1L, 2L, 3L, 4L, input));
        verify(feedbacks, never()).insert(any());
    }

    @Test
    void bothMultipleQuestionsRejectOverLimitDuplicatesInvalidAndConflictingOptions() {
        for (boolean first : Arrays.asList(true, false)) {
            for (java.util.List<String> invalid : Arrays.asList(Collections.<String>emptyList(),
                    Arrays.asList("NONE", "OTHER"), Arrays.asList("OTHER", "OTHER"),
                    Arrays.asList("UNKNOWN"), Arrays.asList("OTHER", "NONE", "NEW_WORK", "PROFESSIONAL_LINK"))) {
                ZycckActivityFeedbackRequest input = valid();
                if (first) input.setQuestion1Answers(invalid); else input.setQuestion3Answers(invalid);
                assertThrows(ServiceException.class, () -> service.save(1L, 2L, 3L, 4L, input));
            }
        }
        verify(feedbacks, never()).insert(any());
    }

    @Test
    void acceptsExactlyThreeSelectionsAndStandaloneNone() {
        ZycckActivityFeedbackRequest input = valid();
        input.setQuestion1Answers(Arrays.asList("FUTURE_WORK", "NEW_CAREER", "WORK_CONTENT"));
        input.setQuestion3Answers(Collections.singletonList("NONE"));
        ZycckActivityFeedbackView view = service.save(1L, 2L, 3L, 4L, input);
        assertEquals(Arrays.asList("NEW_CAREER", "WORK_CONTENT", "FUTURE_WORK"), view.getAnswers().getQuestion1Answers());
        assertEquals(Collections.singletonList("NONE"), view.getAnswers().getQuestion3Answers());
    }

    @Test
    void otherRequiresTextForAllThreeQuestions() {
        record.setExplorationAdded("1");
        for (int no = 1; no <= 3; no++) {
            ZycckActivityFeedbackRequest input = valid(); input.setQuestion2Answer("FUN");
            if (no == 1) input.setQuestion1Answers(Collections.singletonList("OTHER"));
            if (no == 2) input.setQuestion2Answer("OTHER");
            if (no == 3) input.setQuestion3Answers(Collections.singletonList("OTHER"));
            assertThrows(ServiceException.class, () -> service.save(1L, 2L, 3L, 4L, input));
        }
        ZycckActivityFeedbackRequest input = valid(); input.setQuestion2Answer("FUN");
        input.setQuestion1Answers(Collections.singletonList("OTHER")); input.setQuestion1Other(String.join("", Collections.nCopies(501, "字")));
        assertThrows(ServiceException.class, () -> service.save(1L, 2L, 3L, 4L, input));
    }

    @Test
    void resubmissionCannotOverwriteAnswersOrSubmissionTime() {
        ZycckActivityFeedbackRequest input = valid();
        input.setQuestion1Answers(Collections.singletonList("OTHER")); input.setQuestion1Other("  想了解新职业  ");
        service.save(1L, 2L, 3L, 4L, input);
        assertEquals("想了解新职业", stored.get().getQuestion1Other());
        ZycckActivityFeedback original = stored.get();
        java.util.Date submitted = original.getSubmittedTime();
        ServiceException error = assertThrows(ServiceException.class, () -> service.save(1L, 2L, 3L, 4L, valid()));
        assertEquals("活动反馈已提交，不可再次修改", error.getMessage());
        assertSame(original, stored.get());
        assertSame(submitted, stored.get().getSubmittedTime());
        assertEquals("想了解新职业", service.get(1L, 2L, 3L, 4L).getAnswers().getQuestion1Other());
        verify(feedbacks, times(1)).insert(any()); verify(feedbacks, never()).updateById(any());
    }

    @Test
    void rejectsOtherUserOtherActivityOtherGameAndUnfinishedReport() {
        assertThrows(ServiceException.class, () -> service.get(1L, 99L, 3L, 4L));
        assertThrows(ServiceException.class, () -> service.get(1L, 2L, 99L, 4L));
        assertThrows(ServiceException.class, () -> service.save(1L, 2L, 3L, 99L, valid()));
        assertThrows(ServiceException.class, () -> service.save(1L, 99L, 3L, 4L, valid()));
        record.setStatus("participating");
        assertThrows(ServiceException.class, () -> service.save(1L, 2L, 3L, 4L, valid()));
        verify(feedbacks, never()).insert(any());
    }

    @Test
    void addingThenRemovingLastCareerRetainsHistory() {
        record.setStatus("participating");
        ZycckCareerQuestionMapper careers = mock(ZycckCareerQuestionMapper.class);
        ZycckCareerQuestion career = new ZycckCareerQuestion(); career.setStatus("0"); career.setHasQuestion("0");
        when(careers.selectById(8L)).thenReturn(career);
        ZycckRecordService exploration = new ZycckRecordService(records, null, null, careers);
        exploration.updateExploration(1L, 2L, 8L, false);
        exploration.updateExploration(1L, 2L, 8L, true);
        assertEquals("[]", record.getExplorationCareerIds());
        assertEquals("1", record.getExplorationAdded());
    }

    @Test
    void confirmingExplorationPreparesFeedbackWithoutFinishingGame() {
        record.setStatus("participating"); record.setStage("summary"); record.setViewedCareerIds("[8]");
        ZycckRecordService exploration = new ZycckRecordService(records, activities, null, null);
        exploration.finish(1L, 2L);
        assertEquals("participating", record.getStatus());
        assertEquals("activity_feedback", record.getStage());
        assertNull(record.getFinishTime());
        verify(activities, never()).updateGameParticipation(any());
        assertFalse(service.get(1L, 2L, 3L, 4L).isGameFinished());
        assertThrows(ServiceException.class, () -> exploration.updateExploration(1L, 2L, 8L, false));
        assertThrows(ServiceException.class, () -> exploration.browse(1L, 2L, 8L));
        assertTrue((Boolean) exploration.exploration(1L, 2L).get("readOnly"));
        assertEquals("activity_feedback", exploration.start(1L, 2L).getStage());
    }

    @Test
    void validFeedbackFinishesRecordAndParticipationAfterAnswersAreSaved() {
        record.setStatus("participating"); record.setStage("activity_feedback");
        GkzhGameParticipation participation = new GkzhGameParticipation(); participation.setInstanceId(3L);
        when(activities.getLatestParticipation(4L, 2L)).thenReturn(participation);
        ZycckActivityFeedbackView result = service.save(1L, 2L, 3L, 4L, valid());
        assertTrue(result.isGameFinished());
        assertEquals("finished", record.getStatus());
        assertEquals("finished", record.getStage());
        assertEquals(result.getSubmittedTime(), record.getFinishTime());
        assertEquals("1", participation.getStatus());
        org.mockito.InOrder order = inOrder(feedbacks, records, activities);
        order.verify(feedbacks).insert(any()); order.verify(records).updateById(record);
        order.verify(activities).updateGameParticipation(participation);
        java.util.Date ended = record.getFinishTime();
        assertThrows(ServiceException.class, () -> service.save(1L, 2L, 3L, 4L, valid()));
        assertSame(ended, record.getFinishTime());
        verify(activities, times(1)).updateGameParticipation(any());
    }

    @Test
    void invalidOrFailedFeedbackDoesNotFinishGame() {
        record.setStatus("participating"); record.setStage("activity_feedback");
        ZycckActivityFeedbackRequest input = valid(); input.setQuestion1Answers(Collections.emptyList());
        assertThrows(ServiceException.class, () -> service.save(1L, 2L, 3L, 4L, input));
        when(feedbacks.insert(any())).thenReturn(0);
        assertThrows(ServiceException.class, () -> service.save(1L, 2L, 3L, 4L, valid()));
        assertEquals("participating", record.getStatus());
        assertNull(record.getFinishTime());
        verify(records, never()).updateById(any());
        verify(activities, never()).updateGameParticipation(any());
    }

    @Test
    void feedbackCannotFinishParticipationFromAnotherActivity() {
        record.setStatus("participating"); record.setStage("activity_feedback");
        GkzhGameParticipation other = new GkzhGameParticipation(); other.setInstanceId(99L);
        when(activities.getLatestParticipation(4L, 2L)).thenReturn(other);
        service.save(1L, 2L, 3L, 4L, valid());
        verify(activities, never()).updateGameParticipation(any());
    }

    /** 使用真实 MyBatis-Plus 映射验证主键查询存在，普通更新不会回写历史标记。 */
    @Test
    void generatedMapperSqlHasPrimaryKeyStatementsAndProtectsHistory() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.addMapper(ZycckActivityFeedbackMapper.class);
        configuration.addMapper(ZycckRecordMapper.class);
        assertTrue(configuration.hasStatement(ZycckActivityFeedbackMapper.class.getName() + ".selectById"));
        record.setExplorationAdded("0");
        String sql = configuration.getMappedStatement(ZycckRecordMapper.class.getName() + ".updateById")
                .getBoundSql(Collections.singletonMap("et", record)).getSql();
        assertFalse(sql.contains("exploration_added"));
        assertTrue(sql.contains("record_id"));
    }

    private ZycckActivityFeedbackRequest valid() {
        ZycckActivityFeedbackRequest input = new ZycckActivityFeedbackRequest();
        input.setQuestion1Answers(Collections.singletonList("NEW_CAREER"));
        input.setQuestion3Answers(Collections.singletonList("CAREER_PATH"));
        return input;
    }
}
