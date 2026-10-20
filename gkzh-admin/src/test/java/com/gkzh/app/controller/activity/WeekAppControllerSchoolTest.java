package com.gkzh.app.controller.activity;

import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.service.IGkzhStudentService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/** 扫码进入活动游戏时，必须校验游戏所属区域学校与当前学生学校一致。 */
class WeekAppControllerSchoolTest {

    @Test
    void rejectsGameFromAnotherSchoolBeforeCreatingParticipation() {
        IActivityWeekService activities = mock(IActivityWeekService.class);
        IGkzhStudentService students = mock(IGkzhStudentService.class);
        GkzhActivityGame game = new GkzhActivityGame();
        game.setGameId(5L);
        game.setInstanceId(4L);
        game.setAreaId(19L);
        game.setGameType("mind-window");
        when(activities.getGame(5L)).thenReturn(game);
        when(activities.getGameSchoolId(game)).thenReturn(3L);

        GkzhStudent profile = new GkzhStudent();
        profile.setSchoolId(1L);
        when(students.selectGkzhStudentByStudentId(21L)).thenReturn(profile);

        WeekAppController controller = spy(new WeekAppController());
        StudentCheckin login = new StudentCheckin();
        login.setStuId(21L);
        login.setUserId(3L);
        doReturn(login).when(controller).getCurrentStudent();
        ReflectionTestUtils.setField(controller, "activityWeekService", activities);
        ReflectionTestUtils.setField(controller, "studentService", students);

        AjaxResult result = controller.enterGame(5L, 4L);

        assertEquals(500, result.get("code"));
        assertEquals("该活动不属于当前学校，无法进入", result.get("msg"));
        verify(activities, never()).recordGameEnter(any());
    }
}
