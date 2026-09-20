package com.gkzh.web.controller.zycck;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ZycckAdminFilterTest {
    private final ZycckRecordMapper records = mock(ZycckRecordMapper.class);
    private final GkzhStudentMapper students = mock(GkzhStudentMapper.class);
    private final GkzhSchoolDepartmentMapper departments = mock(GkzhSchoolDepartmentMapper.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ZycckAdminController(
            null, null, records, mock(GkzhSchoolMapper.class), departments, students,
            mock(IActivityWeekService.class), null)).build();

    @AfterEach void clearPage() { PageHelper.clearPage(); }

    private QueryWrapper<?> listQuery() {
        ArgumentCaptor<QueryWrapper> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(records).selectList(captor.capture());
        QueryWrapper<?> query = captor.getValue();
        query.getSqlSegment(); // 生成绑定参数。
        return query;
    }

    @Test void studentNumberIsTrimmedAndBoundForRecordsAndBothStatisticsCounts() throws Exception {
        String number = "  001'23  ";
        mvc.perform(get("/zycck/admin/statistics").param("studentNo", number)
                        .param("schoolId", "2").param("instanceId", "3"))
                .andExpect(status().isOk());
        QueryWrapper<?> list = listQuery();
        ArgumentCaptor<QueryWrapper> counts = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(records, times(2)).selectCount(counts.capture());
        for (QueryWrapper<?> query : new QueryWrapper<?>[] {list, counts.getAllValues().get(0), counts.getAllValues().get(1)}) {
            String sql = query.getSqlSegment();
            assertTrue(sql.contains("s.student_no"));
            assertTrue(sql.contains("s.student_id = gkzh_zycck_record.student_id"));
            assertTrue(sql.contains("s.user_id = gkzh_zycck_record.user_id"));
            assertTrue(sql.contains("s.school_id = gkzh_zycck_record.school_id"));
            assertFalse(sql.contains("001'23"), "学号必须通过绑定参数传入");
            assertTrue(query.getParamNameValuePairs().containsValue("%001'23%"));
            assertTrue(query.getParamNameValuePairs().containsValue(2L));
            assertTrue(query.getParamNameValuePairs().containsValue(3L));
        }
    }

    @Test void departmentAndMajorIncludeStudentFallbackAndDescendants() throws Exception {
        mvc.perform(get("/zycck/admin/records").param("departmentId", "10").param("major", "  计算机  "))
                .andExpect(status().isOk());
        QueryWrapper<?> query = listQuery();
        String sql = query.getSqlSegment();
        assertTrue(sql.contains("COALESCE(gkzh_zycck_record.department_id"));
        assertTrue(sql.contains("FIND_IN_SET("));
        assertTrue(sql.contains("d.ancestors"));
        assertTrue(sql.contains("NULLIF(TRIM(gkzh_zycck_record.major), '')"));
        assertTrue(sql.contains("SELECT d.title"));
        assertTrue(query.getParamNameValuePairs().containsValue(10L));
        assertTrue(query.getParamNameValuePairs().containsValue("%计算机%"));
    }

    @Test void genderFilterAcceptsCodesAndMissingStudentData() throws Exception {
        for (String gender : new String[] {"男", "女", "其他"}) {
            reset(records);
            mvc.perform(get("/zycck/admin/records").param("gender", gender)).andExpect(status().isOk());
            QueryWrapper<?> query = listQuery();
            String sql = query.getSqlSegment();
            assertTrue(sql.contains("SELECT s.gender"));
            assertTrue(sql.contains("'其他')"), "未填性别归入其他");
            assertEquals("其他".equals(gender), sql.contains("NOT IN"));
            assertTrue(query.getParamNameValuePairs().containsValue("女".equals(gender) ? "1" : "0"));
            PageHelper.clearPage();
        }
    }

    @Test void clearedTextFiltersDoNotRestrictResults() throws Exception {
        mvc.perform(get("/zycck/admin/records").param("studentNo", "  ").param("major", " ").param("gender", ""))
                .andExpect(status().isOk());
        QueryWrapper<?> query = listQuery();
        assertFalse(query.getSqlSegment().contains("gkzh_student"));
        assertEquals(Collections.singletonList("zycck"), new java.util.ArrayList<>(query.getParamNameValuePairs().values()));
    }

    @Test void displayedFieldsUseSameFallbackAndNormalizeSnapshotGender() throws Exception {
        ZycckRecord record = new ZycckRecord();
        record.setRecordId(1L);
        record.setStudentId(2L);
        record.setMajor(" ");
        record.setGender(" ");
        GkzhStudent student = new GkzhStudent();
        student.setStudentId(2L);
        student.setStudentNo("00123");
        student.setDepartmentId(11L);
        student.setGender("1");
        GkzhSchoolDepartment dept = new GkzhSchoolDepartment();
        dept.setTitle("计算机专业");
        when(students.selectGkzhStudentByStudentId(2L)).thenReturn(student);
        when(departments.selectDepartmentById(11L)).thenReturn(dept);
        when(records.selectList(any())).thenAnswer(invocation -> {
            PageHelper.clearPage();
            return Collections.singletonList(record);
        });
        mvc.perform(get("/zycck/admin/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].studentNo").value("00123"))
                .andExpect(jsonPath("$.rows[0].departmentId").value(11))
                .andExpect(jsonPath("$.rows[0].major").value("计算机专业"))
                .andExpect(jsonPath("$.rows[0].gender").value("女"));
        record.setGender("0");
        record.setMajor("原专业");
        mvc.perform(get("/zycck/admin/records"))
                .andExpect(jsonPath("$.rows[0].major").value("原专业"))
                .andExpect(jsonPath("$.rows[0].gender").value("男"));
    }
}
