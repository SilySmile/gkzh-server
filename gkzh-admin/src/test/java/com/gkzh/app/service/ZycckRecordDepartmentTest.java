package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.app.controller.zycck.ZycckAppController;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.gkzh.zycck.service.ZycckRecordService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

/** 验证专业资料从服务端学生档案写入，且旧记录仅补空字段。 */
class ZycckRecordDepartmentTest {
    @AfterEach void clearRequest() { RequestContextHolder.resetRequestAttributes(); }

    @Test
    void controllerUsesStudentMajorInsteadOfUntrustedClientFields() {
        GkzhStudentMapper students = mock(GkzhStudentMapper.class);
        ZycckRecordService records = mock(ZycckRecordService.class);
        GkzhStudent profile = student(21L, 9L, 3L, 101L, "软件工程");
        when(students.selectGkzhStudentByStudentId(21L)).thenReturn(profile);
        StudentCheckin login = new StudentCheckin(); login.setStuId(21L); login.setUserId(3L);
        MockHttpServletRequest request = new MockHttpServletRequest(); request.setAttribute("CURRENT_STUDENT", login);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Map<String, Object> body = new HashMap<>();
        body.put("schoolId", 9L); body.put("instanceId", 4L); body.put("gameId", 5L);
        body.put("departmentId", 999L); body.put("major", "伪造专业"); body.put("gender", "9");
        ZycckAppController controller = new ZycckAppController(records, null, null, null, null, null, students);
        controller.enter(body);
        verify(records).enter(9L, 4L, 5L, 3L, 21L, 101L, "软件工程", "0");
        body.put("schoolId", 8L);
        assertThrows(ServiceException.class, () -> controller.enter(body));
        verify(records, times(1)).enter(anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    void newRecordStoresProfessionalDepartmentIdAndMajorName() {
        ZycckRecordMapper mapper = mock(ZycckRecordMapper.class);
        IActivityWeekService activities = mock(IActivityWeekService.class);
        GkzhActivityGame game = new GkzhActivityGame(); game.setInstanceId(4L); game.setGameType("zycck");
        when(activities.getGame(5L)).thenReturn(game);
        ZycckRecordService service = new ZycckRecordService(mapper, activities, null, null);
        ZycckRecord record = service.enter(9L, 4L, 5L, 3L, 21L, 101L, "软件工程", "0");
        assertEquals(101L, record.getDepartmentId());
        assertEquals("软件工程", record.getMajor());
        verify(mapper).insert(record);
    }

    @Test
    void existingRecordOnlyFillsEmptyIdentityFields() {
        ZycckRecordMapper mapper = mock(ZycckRecordMapper.class);
        IActivityWeekService activities = mock(IActivityWeekService.class);
        GkzhActivityGame game = new GkzhActivityGame(); game.setInstanceId(4L); game.setGameType("zycck");
        when(activities.getGame(5L)).thenReturn(game);
        ZycckRecord existing = new ZycckRecord(); existing.setRecordId(1L); existing.setMajor("  ");
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(existing);
        ZycckRecordService service = new ZycckRecordService(mapper, activities, null, null);
        service.enter(9L, 4L, 5L, 3L, 21L, 101L, "软件工程", "0");
        assertEquals(21L, existing.getStudentId());
        assertEquals(101L, existing.getDepartmentId());
        assertEquals("软件工程", existing.getMajor());
        ArgumentCaptor<Wrapper> patch = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).update(isNull(), patch.capture());
        String changedColumns = ((UpdateWrapper<?>) patch.getValue()).getSqlSet();
        assertTrue(changedColumns.contains("department_id"));
        assertTrue(changedColumns.contains("major"));
        assertFalse(changedColumns.contains("answer_json"), "补空不能覆盖并发作答内容");
        service.enter(9L, 4L, 5L, 3L, 21L, 102L, "其他专业", "1");
        assertEquals(101L, existing.getDepartmentId());
        assertEquals("软件工程", existing.getMajor());
        verify(mapper, times(1)).update(isNull(), any(Wrapper.class));
    }

    private static GkzhStudent student(Long id, Long schoolId, Long userId, Long majorId, String majorName) {
        GkzhStudent value = new GkzhStudent(); value.setStudentId(id); value.setSchoolId(schoolId);
        value.setUserId(userId); value.setDepartmentId(majorId); value.setDepartmentName(majorName);
        value.setGender("0"); return value;
    }
}
