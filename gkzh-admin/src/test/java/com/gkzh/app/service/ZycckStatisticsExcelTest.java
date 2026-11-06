package com.gkzh.app.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.github.pagehelper.PageHelper;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.web.controller.zycck.ZycckStatisticsExportController;
import com.gkzh.web.controller.zycck.ZycckAdminController;
import com.gkzh.zycck.domain.ZycckCareerQuestion;
import com.gkzh.zycck.domain.ZycckCategory;
import com.gkzh.zycck.dto.ZycckStatisticsExportRow;
import com.gkzh.zycck.dto.ZycckParticipationSummaryRow;
import com.gkzh.zycck.mapper.*;
import com.gkzh.zycck.service.ZycckActivityFeedbackService;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 重新打开真实生成的 XLSX 验证导出内容、全部分页、中文反馈及数据保真。 */
class ZycckStatisticsExcelTest {
    private ZycckStatisticsExportMapper exports;
    private ZycckCareerQuestionMapper careers;
    private GkzhSchoolMapper schools;
    private IActivityWeekService activities;
    private ZycckStatisticsExcelService service;

    @BeforeEach
    void setup() {
        exports = mock(ZycckStatisticsExportMapper.class);
        careers = mock(ZycckCareerQuestionMapper.class);
        schools = mock(GkzhSchoolMapper.class);
        activities = mock(IActivityWeekService.class);
        ZycckCategoryMapper categories = mock(ZycckCategoryMapper.class);
        when(schools.selectGkzhSchoolBySchoolId(9L)).thenReturn(new GkzhSchool());
        when(activities.getInstance(3L)).thenReturn(new GkzhActivityWeekInstance());
        when(categories.selectList(any())).thenReturn(Collections.emptyList());
        when(careers.selectBatchIds(any())).thenReturn(Collections.emptyList());
        ZycckActivityFeedbackService feedback = new ZycckActivityFeedbackService(mock(ZycckRecordMapper.class), mock(ZycckActivityFeedbackMapper.class), activities);
        service = new ZycckStatisticsExcelService(exports, careers, categories, schools, activities, feedback);
    }

    @Test
    void requiresSchoolActivityAndNonemptyParticipationScope() {
        assertThrows(ServiceException.class, () -> service.create(null, 3L));
        assertThrows(ServiceException.class, () -> service.create(9L, null));
        assertThrows(ServiceException.class, () -> service.create(-1L, 3L));
        assertThrows(ServiceException.class, () -> service.create(99L, 3L));
        assertThrows(ServiceException.class, () -> service.create(9L, 99L));
        when(exports.selectBatch(9L, 3L, null, null, null, null, null, null, null, 0L, 500)).thenReturn(Collections.emptyList());
        assertThrows(ServiceException.class, () -> service.create(9L, 3L));
        verify(exports, times(1)).selectBatch(9L, 3L, null, null, null, null, null, null, null, 0L, 500);
    }

    @Test
    void studentExportAppliesSelectedFiltersWithoutPageLimit() throws Exception {
        ZycckStatisticsExportRow row = record(1L);
        row.setFacultyName("信息系"); row.setMajor("软件工程");
        when(exports.selectBatch(9L, 3L, 11L, "软件", "女", "2026", null, null, null, 0L, 500))
                .thenReturn(Collections.singletonList(row));
        PageHelper.startPage(8, 1);
        try (SXSSFWorkbook workbook = service.create(9L, 3L, 11L, " 软件 ", "女", " 2026 ")) {
            assertNull(PageHelper.getLocalPage());
            assertEquals(1, workbook.getSheet("学生数据").getLastRowNum());
            assertTrue(text(workbook.getSheet("数据说明"), 1, "内容").contains("院系编号 11"));
            verify(exports).selectBatch(9L, 3L, 11L, "软件", "女", "2026", null, null, null, 0L, 500);
        } finally { PageHelper.clearPage(); }
    }

    @Test
    void bothExportsIncludeTheWholeEndDateAndRejectReversedDates() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 10, 15), endDate = LocalDate.of(2026, 10, 16);
        LocalDateTime startTime = startDate.atStartOfDay(), endExclusive = endDate.plusDays(1).atStartOfDay();
        when(exports.selectBatch(9L, 3L, null, null, null, null, null, startTime, endExclusive, 0L, 500))
                .thenReturn(Collections.singletonList(record(1L)));
        try (SXSSFWorkbook workbook = service.create(9L, 3L, null, null, null, null, startDate, endDate)) {
            assertTrue(text(workbook.getSheet("数据说明"), 1, "内容").contains("参与结束日期 2026-10-16"));
            workbook.dispose();
        }
        verify(exports).selectBatch(9L, 3L, null, null, null, null, null, startTime, endExclusive, 0L, 500);
        when(exports.selectParticipationSummary(9L, 3L, null, startTime, endExclusive))
                .thenReturn(Collections.singletonList(summary(11L, "信息系", 101L, "软件工程", 1L)));
        try (SXSSFWorkbook workbook = service.createParticipationSummary(9L, 3L, startDate, endDate)) {
            assertTrue(text(workbook.getSheet("统计说明"), 1, "内容").contains("参与结束日期 2026-10-16"));
            workbook.dispose();
        }
        verify(exports).selectParticipationSummary(9L, 3L, null, startTime, endExclusive);
        assertThrows(ServiceException.class, () -> service.create(9L, 3L, null, null, null, null, endDate, startDate));
        assertThrows(ServiceException.class, () -> service.createParticipationSummary(9L, 3L, endDate, startDate));
    }

    @Test
    void bothExportsApplyTheSelectedBusinessStatus() throws Exception {
        when(exports.selectBatch(9L, 3L, null, null, null, null, "pendingFeedback", null, null, 0L, 500))
                .thenReturn(Collections.singletonList(record(1L)));
        try (SXSSFWorkbook workbook = service.create(9L, 3L, null, null, null, null, "pendingFeedback", null, null)) {
            assertTrue(text(workbook.getSheet("数据说明"), 1, "内容").contains("参与状态 待活动反馈"));
            workbook.dispose();
        }
        verify(exports).selectBatch(9L, 3L, null, null, null, null, "pendingFeedback", null, null, 0L, 500);
        when(exports.selectParticipationSummary(9L, 3L, "inProgress", null, null))
                .thenReturn(Collections.singletonList(summary(11L, "信息系", 101L, "软件工程", 1L)));
        try (SXSSFWorkbook workbook = service.createParticipationSummary(9L, 3L, "inProgress", null, null)) {
            assertTrue(text(workbook.getSheet("统计说明"), 1, "内容").contains("参与状态 进行中"));
            workbook.dispose();
        }
        verify(exports).selectParticipationSummary(9L, 3L, "inProgress", null, null);
        assertThrows(ServiceException.class, () -> service.create(9L, 3L, null, null, null, null, "unknown", null, null));
        assertThrows(ServiceException.class, () -> service.createParticipationSummary(9L, 3L, "unknown", null, null));
    }

    @Test
    void exportsAllBatchesAndKeepsSeparateGameRowsBeyondPageSize() throws Exception {
        List<ZycckStatisticsExportRow> firstBatch = new ArrayList<>();
        for (long id = 1; id <= 500; id++) firstBatch.add(record(id));
        ZycckStatisticsExportRow secondGame = record(501L); secondGame.setGameId(40L); secondGame.setGameName("另一个游戏");
        when(exports.selectBatch(9L, 3L, null, null, null, null, null, null, null, 0L, 500)).thenReturn(firstBatch);
        when(exports.selectBatch(9L, 3L, null, null, null, null, null, null, null, 500L, 500)).thenReturn(Collections.singletonList(secondGame));
        PageHelper.startPage(5, 1);
        try (XSSFWorkbook workbook = generated()) {
            Sheet sheet = workbook.getSheet("学生数据");
            assertEquals(501, sheet.getLastRowNum());
            assertEquals("501", text(sheet, 501, "记录编号"));
            assertEquals("40", text(sheet, 501, "游戏编号"));
            assertEquals("未提交", text(sheet, 501, "反馈提交状态"));
            assertEquals("未抽题", text(sheet, 501, "第1题作答结果"));
            assertNull(PageHelper.getLocalPage());
            verify(exports).selectBatch(9L, 3L, null, null, null, null, null, null, null, 500L, 500);
            assertEquals(501, workbook.getSheet("原始记录").getLastRowNum());
        } finally { PageHelper.clearPage(); }
    }

    @Test
    void preservesAnswersMissingCareersFeedbackAndTextIdentifiers() throws Exception {
        ZycckStatisticsExportRow row = record(1L);
        row.setDepartmentName("软件工程"); row.setFacultyName("信息系"); row.setEffectiveDepartmentId(101L);
        row.setUserId(9007199254740993L); row.setStudentName("=1+1"); row.setStudentNo("000007");
        List<JSONObject> snapshots = new ArrayList<>();
        for (int no = 1; no <= 5; no++) {
            JSONObject snapshot = new JSONObject(); snapshot.put("questionNo", no); snapshot.put("questionId", 100L + no);
            snapshot.put("careerName", "场景" + no); snapshot.put("categoryId", 201L);
            for (String key : Arrays.asList("A", "B", "C", "D")) snapshot.put("option" + key, "职业" + key);
            if (no == 1) snapshot.put("correctOptionKey", "C");
            snapshots.add(snapshot);
        }
        row.setOptionSnapshotJson(snapshots.toString());
        row.setAnswerJson("[{\"questionNo\":1,\"optionKey\":\"C\",\"elapsedSeconds\":12},{\"questionNo\":2,\"optionKey\":\"C\"},{\"questionNo\":3,\"timeout\":true},{\"questionNo\":4,\"optionKey\":\"A\"}]");
        row.setAwarenessJson("[{\"questionNo\":1,\"level\":\"B\"}]");
        row.setViewedCareerIds("[8,99,8]"); row.setExplorationCareerIds("[8]"); row.setExplorationAdded("1");
        row.setStatus("finished"); row.setStage("finished"); row.setFeedbackRecordId(1L); row.setFeedbackVersion("1");
        row.setFeedbackShowQuestion2("1"); row.setFeedbackQuestion1Answers("[\"NEW_CAREER\",\"OTHER\"]"); row.setFeedbackQuestion1Other("=SUM(A1:A2)");
        row.setFeedbackQuestion2Answer("FUN"); row.setFeedbackQuestion3Answers("[\"NONE\"]");
        List<ZycckCareerQuestion> questions = new ArrayList<>();
        for (long id : Arrays.asList(101L, 102L, 103L, 105L, 8L)) {
            ZycckCareerQuestion career = new ZycckCareerQuestion(); career.setCareerQuestionId(id); career.setCorrectOptionKey("B");
            career.setCareerName("职业" + id); career.setMainWork("工作内容"); career.setStatus("0"); questions.add(career);
        }
        when(careers.selectBatchIds(any())).thenReturn(questions);
        when(exports.selectBatch(9L, 3L, null, null, null, null, null, null, null, 0L, 500)).thenReturn(Collections.singletonList(row));
        try (XSSFWorkbook workbook = generated()) {
            Sheet students = workbook.getSheet("学生数据");
            assertEquals("000007", text(students, 1, "学号"));
            assertEquals("信息系", text(students, 1, "院系"));
            assertEquals("软件工程", text(students, 1, "专业"));
            assertEquals("101", text(students, 1, "专业部门编号"));
            assertEquals("9007199254740993", text(students, 1, "用户编号"));
            assertEquals(CellType.STRING, cell(students, 1, "学生姓名").getCellType());
            assertEquals("=1+1", text(students, 1, "学生姓名"));
            assertEquals("正确", text(students, 1, "第1题作答结果"));
            assertEquals("错误", text(students, 1, "第2题作答结果"));
            assertEquals("超时", text(students, 1, "第3题作答结果"));
            assertEquals("无法判定", text(students, 1, "第4题作答结果"));
            assertEquals("未作答", text(students, 1, "第5题作答结果"));
            assertEquals("听说过但不了解", text(students, 1, "第1题职业了解程度"));
            assertEquals(2, cell(students, 1, "浏览职业数").getNumericCellValue());
            assertTrue(text(students, 1, "浏览职业").contains("职业已删除（编号：99）"));
            assertTrue(text(students, 1, "反馈第1题认识").contains("认识了以前不了解的职业"));
            assertEquals("觉得有趣，想先保留下来", text(students, 1, "反馈第2题加入原因"));
            assertEquals("暂时没有特别需求", text(students, 1, "反馈第3题后续需求"));
            assertEquals(CellType.STRING, cell(students, 1, "反馈第1题其他").getCellType());
            assertEquals(5, workbook.getSheet("答题明细").getLastRowNum());
            assertEquals("本局快照", text(workbook.getSheet("答题明细"), 1, "正确答案来源"));
            assertEquals("信息系", text(workbook.getSheet("答题明细"), 1, "院系"));
            assertEquals("软件工程", text(workbook.getSheet("答题明细"), 1, "专业"));
            assertEquals("当前题库", text(workbook.getSheet("答题明细"), 2, "正确答案来源"));
            assertEquals(2, workbook.getSheet("职业明细").getLastRowNum());
            assertEquals("信息系", text(workbook.getSheet("职业明细"), 1, "院系"));
            assertEquals("软件工程", text(workbook.getSheet("职业明细"), 1, "专业"));
        }
    }

    @Test
    void oldFinishedWithoutFeedbackIsPendingAndLongRawTextIsNotTruncated() throws Exception {
        ZycckStatisticsExportRow row = record(1L); row.setStatus("finished"); row.setStage("exploration");
        // 使用多样长文本，验证保真而不让重复测试字符触发 POI 的 ZIP 膨胀保护。
        StringBuilder source = new StringBuilder(); Random random = new Random(7);
        for (int i = 0; i < 2500; i++) source.append("职业探索").append(i).append('/').append(Integer.toHexString(random.nextInt())).append('\n');
        String content = source.toString(); row.setOptionSnapshotJson(content);
        when(exports.selectBatch(9L, 3L, null, null, null, null, null, null, null, 0L, 500)).thenReturn(Collections.singletonList(row));
        try (XSSFWorkbook workbook = generated()) {
            assertEquals("待活动反馈", text(workbook.getSheet("学生数据"), 1, "参与状态"));
            assertEquals("不适用", text(workbook.getSheet("学生数据"), 1, "反馈第2题加入原因"));
            assertEquals("未提交", text(workbook.getSheet("学生数据"), 1, "反馈第1题认识"));
            assertTrue(text(workbook.getSheet("原始记录"), 1, "题目选项快照JSON").startsWith("完整内容见长文本"));
            Sheet longText = workbook.getSheet("长文本"); StringBuilder restored = new StringBuilder();
            for (int no = 1; no <= longText.getLastRowNum(); no++) restored.append(text(longText, no, "内容"));
            assertEquals(content, restored.toString());
        }
    }

    @Test
    void controllerReturnsRealExcelAndFilename() throws Exception {
        when(exports.selectBatch(9L, 3L, null, null, null, null, null, null, null, 0L, 500)).thenReturn(Collections.singletonList(record(1L)));
        MockHttpServletResponse response = new MockHttpServletResponse();
        new ZycckStatisticsExportController(service).excel(9L, 3L, null, null, null, null, null, null, null, response);
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains(".xlsx"));
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            assertEquals(1, workbook.getSheet("学生数据").getLastRowNum());
        }
    }

    @Test
    void excelPathDoesNotBindToNumericRecordDetailRoute() throws Exception {
        ZycckStatisticsExcelService mockedService = mock(ZycckStatisticsExcelService.class);
        SXSSFWorkbook workbook = new SXSSFWorkbook(); workbook.createSheet("学生数据");
        when(mockedService.create(9L, 3L, null, null, null, null, null, null, null)).thenReturn(workbook);
        ZycckAdminController admin = new ZycckAdminController(null, null, null, null, null, null,
                null, null, null, null, null, null);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(admin, new ZycckStatisticsExportController(mockedService)).build();
        mvc.perform(get("/zycck/admin/statistics/excel").param("schoolId", "9").param("instanceId", "3"))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ZycckStatisticsExportController.class));
        LocalDate start = LocalDate.of(2026, 10, 15), end = LocalDate.of(2026, 10, 16);
        SXSSFWorkbook datedWorkbook = new SXSSFWorkbook(); datedWorkbook.createSheet("学生数据");
        SXSSFWorkbook summaryWorkbook = new SXSSFWorkbook(); summaryWorkbook.createSheet("各系参与人数");
        when(mockedService.create(9L, 3L, null, null, null, null, "pendingFeedback", start, end)).thenReturn(datedWorkbook);
        when(mockedService.createParticipationSummary(9L, 3L, "pendingFeedback", start, end)).thenReturn(summaryWorkbook);
        mvc.perform(get("/zycck/admin/statistics/excel").param("schoolId", "9").param("instanceId", "3")
                        .param("statusFilter", "pendingFeedback").param("startDate", "2026-10-15").param("endDate", "2026-10-16"))
                .andExpect(status().isOk());
        mvc.perform(get("/zycck/admin/statistics/summary-excel").param("schoolId", "9").param("instanceId", "3")
                        .param("statusFilter", "pendingFeedback").param("startDate", "2026-10-15").param("endDate", "2026-10-16"))
                .andExpect(status().isOk());
        verify(mockedService).create(9L, 3L, null, null, null, null, "pendingFeedback", start, end);
        verify(mockedService).createParticipationSummary(9L, 3L, "pendingFeedback", start, end);
        mvc.perform(get("/zycck/admin/statistics/not-a-number"))
                .andExpect(status().isNotFound());
    }

    @Test
    void summaryRequiresValidScopeAndParticipants() {
        assertThrows(ServiceException.class, () -> service.createParticipationSummary(null, 3L));
        assertThrows(ServiceException.class, () -> service.createParticipationSummary(9L, null));
        assertThrows(ServiceException.class, () -> service.createParticipationSummary(99L, 3L));
        assertThrows(ServiceException.class, () -> service.createParticipationSummary(9L, 99L));
        when(exports.selectParticipationSummary(9L, 3L, null, null, null)).thenReturn(Collections.emptyList());
        assertThrows(ServiceException.class, () -> service.createParticipationSummary(9L, 3L));
    }

    @Test
    void summaryKeepsMissingDepartmentsAndSumsFacultyMajorCounts() throws Exception {
        when(exports.selectParticipationSummary(9L, 3L, null, null, null)).thenReturn(Arrays.asList(
                summary(11L, "信息系", 101L, "软件工程", 3L),
                summary(11L, "信息系", 102L, "网络工程", 2L),
                summary(12L, "艺术系", 103L, "设计", 4L),
                summary(null, null, null, null, 1L)));
        PageHelper.startPage(5, 1);
        try (SXSSFWorkbook workbook = service.createParticipationSummary(9L, 3L)) {
            assertNull(PageHelper.getLocalPage());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try { workbook.write(output); } finally { workbook.dispose(); }
            try (XSSFWorkbook result = new XSSFWorkbook(new ByteArrayInputStream(output.toByteArray()))) {
                Sheet faculty = result.getSheet("各系参与人数"), major = result.getSheet("各专业参与人数");
                assertEquals(4, faculty.getLastRowNum());
                assertEquals("信息系", text(faculty, 1, "系"));
                assertEquals("5", text(faculty, 1, "参与人数"));
                assertEquals("未分配系", text(faculty, 3, "系"));
                assertEquals("总计", text(faculty, 4, "系"));
                assertEquals("10", text(faculty, 4, "参与人数"));
                assertEquals("未填写专业", text(major, 4, "专业"));
                assertEquals("10", text(major, 5, "参与人数"));
                assertTrue(text(result.getSheet("统计说明"), 2, "内容").contains("多个猜猜看游戏只计1人"));
            }
        } finally { PageHelper.clearPage(); }
    }

    @Test
    void summaryControllerReturnsExcelAndScopedFilename() throws Exception {
        when(exports.selectParticipationSummary(9L, 3L, null, null, null)).thenReturn(Collections.singletonList(summary(11L, "信息系", 101L, "软件工程", 3L)));
        MockHttpServletResponse response = new MockHttpServletResponse();
        new ZycckStatisticsExportController(service).summaryExcel(9L, 3L, null, null, null, response);
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains(".xlsx"));
        try (XSSFWorkbook result = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            assertEquals("3", text(result.getSheet("各专业参与人数"), 1, "参与人数"));
        }
    }

    @Test
    void mapperSqlRequiresScopeAndJoinsFeedbackWithoutPerStudentQueries() throws Exception {
        MybatisConfiguration config = new MybatisConfiguration();
        config.setMapUnderscoreToCamelCase(false);
        String resource = "mapper/zycck/ZycckStatisticsExportMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input);
            new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
        }
        Map<String, Object> params = new HashMap<>(); params.put("schoolId", 9L); params.put("instanceId", 3L); params.put("afterId", 0L); params.put("limit", 500);
        params.put("departmentId", 11L); params.put("major", "软件"); params.put("gender", "女"); params.put("studentNo", "2026");
        params.put("startTime", LocalDateTime.of(2026, 10, 15, 0, 0));
        params.put("endExclusive", LocalDateTime.of(2026, 10, 17, 0, 0));
        String sql = config.getMappedStatement(ZycckStatisticsExportMapper.class.getName() + ".selectBatch").getBoundSql(params).getSql();
        Map<String, String> mappings = new HashMap<>();
        config.getMappedStatement(ZycckStatisticsExportMapper.class.getName() + ".selectBatch").getResultMaps().get(0)
                .getResultMappings().forEach(mapping -> mappings.put(mapping.getProperty(), mapping.getColumn()));
        assertEquals("record_id", mappings.get("recordId"));
        assertEquals("student_no", mappings.get("studentNo"));
        assertEquals("faculty_name", mappings.get("facultyName"));
        assertEquals("feedback_question1_answers", mappings.get("feedbackQuestion1Answers"));
        assertEquals("feedback_submitted_time", mappings.get("feedbackSubmittedTime"));
        assertTrue(sql.contains("r.school_id = ? AND r.instance_id = ?"));
        assertTrue(sql.contains("r.record_id > ?"));
        assertTrue(sql.contains("r.scan_time >= ?"));
        assertTrue(sql.contains("r.scan_time < ?"));
        assertTrue(sql.contains("faculty.department_id = ?"));
        assertTrue(sql.contains("department.title) LIKE CONCAT('%', ?, '%')"));
        assertTrue(sql.contains("student.student_no LIKE CONCAT('%', ?, '%')"));
        assertTrue(sql.contains("LEFT JOIN gkzh_zycck_activity_feedback"));
        assertTrue(sql.contains("faculty.department_id = department.parent_id"));
        assertTrue(sql.contains("s.school_id = r.school_id"));
        assertTrue(sql.contains("LIMIT ?"));
        assertFalse(sql.contains("r.status ="), "未完成学生也必须导出");
        String summarySql = config.getMappedStatement(ZycckStatisticsExportMapper.class.getName() + ".selectParticipationSummary").getBoundSql(params).getSql();
        Map<String, String> summaryMappings = new HashMap<>();
        config.getMappedStatement(ZycckStatisticsExportMapper.class.getName() + ".selectParticipationSummary").getResultMaps().get(0)
                .getResultMappings().forEach(mapping -> summaryMappings.put(mapping.getProperty(), mapping.getColumn()));
        assertEquals("participant_count", summaryMappings.get("participantCount"));
        assertTrue(summarySql.contains("MIN(record_id)"));
        assertTrue(summarySql.contains("GROUP BY user_id"));
        assertTrue(summarySql.contains("scan_time >= ?"));
        assertTrue(summarySql.contains("scan_time < ?"));
        assertTrue(summarySql.contains("school_id = ? AND instance_id = ? AND game_type = 'zycck'"));
        assertTrue(summarySql.contains("faculty.department_id = department.parent_id"));
        assertFalse(summarySql.contains("status ="), "未完成参与记录也必须计人数");
        params.put("statusFilter", "pendingFeedback");
        String pendingSql = config.getMappedStatement(ZycckStatisticsExportMapper.class.getName() + ".selectBatch").getBoundSql(params).getSql();
        String pendingSummarySql = config.getMappedStatement(ZycckStatisticsExportMapper.class.getName() + ".selectParticipationSummary").getBoundSql(params).getSql();
        assertTrue(pendingSql.contains("r.stage = 'activity_feedback'"));
        assertTrue(pendingSummarySql.contains("stage = 'activity_feedback'"));
    }

    private static ZycckParticipationSummaryRow summary(Long facultyId, String facultyName, Long majorId, String majorName, Long count) {
        ZycckParticipationSummaryRow row = new ZycckParticipationSummaryRow();
        row.setFacultyId(facultyId); row.setFacultyName(facultyName); row.setMajorId(majorId);
        row.setMajorName(majorName); row.setParticipantCount(count); return row;
    }

    private ZycckStatisticsExportRow record(long id) {
        ZycckStatisticsExportRow row = new ZycckStatisticsExportRow(); row.setRecordId(id);
        row.setSchoolId(9L); row.setInstanceId(3L); row.setGameId(4L); row.setUserId(2L);
        row.setSchoolName("测试学校"); row.setInstanceName("测试活动"); row.setGameName("测试游戏"); row.setStudentName("测试学生");
        row.setStatus("participating"); row.setStage("scanned"); return row;
    }
    private XSSFWorkbook generated() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (SXSSFWorkbook workbook = service.create(9L, 3L)) {
            try { workbook.write(output); } finally { workbook.dispose(); }
        }
        return new XSSFWorkbook(new ByteArrayInputStream(output.toByteArray()));
    }
    private static Cell cell(Sheet sheet, int row, String label) {
        for (Cell cell : sheet.getRow(0)) if (label.equals(cell.getStringCellValue())) return sheet.getRow(row).getCell(cell.getColumnIndex());
        throw new AssertionError("缺少表头：" + label);
    }
    private static String text(Sheet sheet, int row, String label) { return new DataFormatter().formatCellValue(cell(sheet, row, label)); }
}
