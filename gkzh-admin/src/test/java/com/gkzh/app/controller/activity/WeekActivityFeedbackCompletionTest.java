package com.gkzh.app.controller.activity;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.domain.week.GkzhActivityArea;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.mapper.week.GkzhGameParticipationMapper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.activity.service.impl.ActivityWeekServiceImpl;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.service.IGkzhStudentService;
import com.gkzh.xycc.mapper.UserSelectionMapper;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckActivityFeedbackMapper;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.gkzh.zycck.service.ZycckActivityFeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 活动页完成状态及通用完成入口回归，防止未反馈就显示完成。 */
class WeekActivityFeedbackCompletionTest {
    private IActivityWeekService activities;
    private ZycckRecordMapper records;
    private WeekAppController controller;
    private GkzhActivityGame game;

    @BeforeEach
    void setup() {
        activities = mock(IActivityWeekService.class);
        records = mock(ZycckRecordMapper.class);
        controller = spy(new WeekAppController());
        StudentCheckin student = new StudentCheckin(); student.setUserId(2L); student.setStuId(8L);
        doReturn(student).when(controller).getCurrentStudent();
        IGkzhStudentService students = mock(IGkzhStudentService.class);
        GkzhStudent info = new GkzhStudent(); info.setSchoolId(9L);
        when(students.selectGkzhStudentByStudentId(8L)).thenReturn(info);
        ReflectionTestUtils.setField(controller, "activityWeekService", activities);
        ReflectionTestUtils.setField(controller, "studentService", students);
        ReflectionTestUtils.setField(controller, "userSelectionMapper", mock(UserSelectionMapper.class));
        ReflectionTestUtils.setField(controller, "zycckActivityFeedbackService",
                new ZycckActivityFeedbackService(records, mock(ZycckActivityFeedbackMapper.class), activities));
        when(activities.getInstance(3L)).thenReturn(new GkzhActivityWeekInstance());
        GkzhActivityArea area = new GkzhActivityArea(); area.setAreaId(7L);
        when(activities.listAreas(3L, 9L)).thenReturn(Collections.singletonList(area));
        game = new GkzhActivityGame(); game.setGameId(4L); game.setInstanceId(3L); game.setGameType("zycck");
        when(activities.listGames(7L)).thenReturn(Collections.singletonList(game));
        when(activities.getGame(4L)).thenReturn(game);
        GkzhGameParticipation old = new GkzhGameParticipation(); old.setStatus("1"); old.setInstanceId(3L);
        when(activities.getLatestParticipation(4L, 2L)).thenReturn(old);
        when(records.selectList(any())).thenReturn(Collections.emptyList());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void oldCompletedParticipationWithoutFeedbackIsNotFinished() {
        assertFalse(flowGame().getBooleanValue("isFinish"));
        ArgumentCaptor<QueryWrapper> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(records).selectList(query.capture());
        String sql = query.getValue().getSqlSegment();
        assertTrue(sql.contains("gkzh_zycck_activity_feedback"));
        assertTrue(sql.contains("instance_id") && sql.contains("user_id") && sql.contains("status"));
        assertTrue(query.getValue().getParamNameValuePairs().values().containsAll(Arrays.asList(3L, 2L, "zycck", "finished")));
        verify(records, never()).updateById(any());
    }

    @Test
    void onlyFinishedRecordsWithFeedbackAppearCompleted() {
        ZycckRecord completed = new ZycckRecord(); completed.setGameId(4L);
        when(records.selectList(any())).thenReturn(Collections.singletonList(completed));
        assertTrue(flowGame().getBooleanValue("isFinish"));
    }

    @Test
    void feedbackForAnotherGameCannotCompleteThisGame() {
        ZycckRecord otherGame = new ZycckRecord(); otherGame.setGameId(99L);
        when(records.selectList(any())).thenReturn(Collections.singletonList(otherGame));
        assertFalse(flowGame().getBooleanValue("isFinish"));
    }

    @Test
    void genericCompleteCannotBypassFeedback() {
        AjaxResult response = controller.completeGame(4L, 3L, Collections.emptyMap());
        assertEquals(500, response.get("code"));
        assertEquals("请提交活动反馈完成本次游戏", response.get("msg"));
        verify(activities, never()).updateGameParticipation(any());
    }

    @Test
    void otherGamesKeepTheirExistingCompletionRules() {
        game.setGameType("wjyd");
        assertTrue(flowGame().getBooleanValue("isFinish"));
        verify(records, never()).selectList(any());
        assertEquals(200, controller.completeGame(4L, 3L, Collections.emptyMap()).get("code"));
        verify(activities).updateGameParticipation(any());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void lotteryCompletionCountAlsoRequiresFeedbackForZycck() {
        ActivityWeekServiceImpl service = new ActivityWeekServiceImpl();
        GkzhGameParticipationMapper participationMapper = mock(GkzhGameParticipationMapper.class);
        ReflectionTestUtils.setField(service, "gameParticipationMapper", participationMapper);
        when(participationMapper.selectCount(any())).thenReturn(1L);
        assertEquals(1, service.countFinishedGames(3L, 2L));
        ArgumentCaptor<QueryWrapper> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(participationMapper).selectCount(query.capture());
        String sql = query.getValue().getSqlSegment();
        assertTrue(sql.contains("NOT EXISTS") && sql.contains("gkzh_activity_game"));
        assertTrue(sql.contains("JOIN gkzh_zycck_activity_feedback"));
        assertTrue(sql.contains("r.instance_id = gkzh_game_participation.instance_id"));
        assertTrue(sql.contains("r.user_id = gkzh_game_participation.user_id"));
        assertTrue(sql.contains("r.game_id = gkzh_game_participation.game_id"));
    }

    @SuppressWarnings("unchecked")
    private JSONObject flowGame() {
        Map<String, Object> data = (Map<String, Object>) controller.flow(3L).get("data");
        Map<String, Object> area = ((List<Map<String, Object>>) data.get("areas")).get(0);
        return ((List<JSONObject>) area.get("games")).get(0);
    }
}
