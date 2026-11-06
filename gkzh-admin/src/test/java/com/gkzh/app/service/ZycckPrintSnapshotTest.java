package com.gkzh.app.service;

import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.zycck.domain.ZycckPrintTask;
import com.gkzh.zycck.domain.ZycckRecord;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 打印任务快照必须使用学校模块已定义的查询映射，避免 selectById 缺失。 */
class ZycckPrintSnapshotTest {

    @Test
    void fillsStudentAndSchoolFromExistingMapperStatements() throws Exception {
        GkzhStudentMapper students = mock(GkzhStudentMapper.class);
        GkzhSchoolMapper schools = mock(GkzhSchoolMapper.class);
        GkzhStudent student = new GkzhStudent();
        student.setStudentId(11L);
        student.setSchoolId(22L);
        student.setStudentNo("20260001");
        student.setStudentName("测试学生");
        GkzhSchool school = new GkzhSchool();
        school.setTitle("测试学校");
        when(students.selectGkzhStudentByStudentId(11L)).thenReturn(student);
        when(schools.selectGkzhSchoolBySchoolId(22L)).thenReturn(school);

        ZycckRecord record = new ZycckRecord();
        record.setStudentId(11L);
        ZycckPrintTask task = new ZycckPrintTask();
        snapshot(service(students, schools), task, record);

        assertEquals(11L, task.getStudentId());
        assertEquals(22L, task.getSchoolId());
        assertEquals("20260001", task.getStudentNo());
        assertEquals("测试学生", task.getStudentName());
        assertEquals("测试学校", task.getSchoolName());
        verify(students).selectGkzhStudentByStudentId(11L);
        verify(schools).selectGkzhSchoolBySchoolId(22L);
    }

    @Test
    void fallsBackToUserIdWhenRecordHasNoStudentId() throws Exception {
        GkzhStudentMapper students = mock(GkzhStudentMapper.class);
        GkzhStudent student = new GkzhStudent();
        student.setStudentId(11L);
        when(students.selectGkzhStudentByUserId(33L)).thenReturn(student);

        ZycckRecord record = new ZycckRecord();
        record.setUserId(33L);
        ZycckPrintTask task = new ZycckPrintTask();
        snapshot(service(students, mock(GkzhSchoolMapper.class)), task, record);

        assertEquals(11L, task.getStudentId());
        verify(students).selectGkzhStudentByUserId(33L);
    }

    private ZycckPrintService service(GkzhStudentMapper students, GkzhSchoolMapper schools) {
        return new ZycckPrintService(null, null, null, null, null,
                students, schools, null, null, null);
    }

    private void snapshot(ZycckPrintService service, ZycckPrintTask task, ZycckRecord record) throws Exception {
        Method method = ZycckPrintService.class.getDeclaredMethod(
                "fillTaskSnapshot", ZycckPrintTask.class, ZycckRecord.class);
        method.setAccessible(true);
        method.invoke(service, task, record);
    }
}
