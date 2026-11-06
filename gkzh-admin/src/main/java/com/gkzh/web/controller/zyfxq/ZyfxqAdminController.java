package com.gkzh.web.controller.zyfxq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.app.service.ZyfxqRecordEnrichService;
import com.gkzh.app.service.ZyfxqStatisticsExcelService;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zyfxq.domain.ZyfxqAnswer;
import com.gkzh.zyfxq.domain.ZyfxqCell;
import com.gkzh.zyfxq.domain.ZyfxqQuestion;
import com.gkzh.zyfxq.domain.ZyfxqRecord;
import com.gkzh.zyfxq.domain.ZyfxqSetting;
import com.gkzh.zyfxq.domain.dto.ZyfxqRecordQuery;
import com.gkzh.zyfxq.mapper.ZyfxqAnswerMapper;
import com.gkzh.zyfxq.mapper.ZyfxqRecordMapper;
import com.gkzh.zyfxq.service.ZyfxqConfigService;
import com.gkzh.zyfxq.service.ZyfxqGameService;
import com.gkzh.zyfxq.service.ZyfxqStatisticsService;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职业飞行棋 Web 管理接口。
 * 所需配置维护 19 格棋盘、规则参数与题库；参与记录可筛选、查看和删除；
 * 统计分析输出了需求中的学校端两页内容，并支持 Excel 导出。
 */
@RestController
@RequestMapping("/zyfxq/admin")
public class ZyfxqAdminController extends BaseController {

    private final ZyfxqConfigService configService;
    private final ZyfxqGameService gameService;
    private final ZyfxqStatisticsService statisticsService;
    private final ZyfxqRecordEnrichService enrichService;
    private final ZyfxqRecordMapper recordMapper;
    private final ZyfxqAnswerMapper answerMapper;
    private final ZyfxqStatisticsExcelService excelService;

    public ZyfxqAdminController(ZyfxqConfigService configService, ZyfxqGameService gameService,
                                ZyfxqStatisticsService statisticsService, ZyfxqRecordEnrichService enrichService,
                                ZyfxqRecordMapper recordMapper, ZyfxqAnswerMapper answerMapper,
                                ZyfxqStatisticsExcelService excelService) {
        this.configService = configService;
        this.gameService = gameService;
        this.statisticsService = statisticsService;
        this.enrichService = enrichService;
        this.recordMapper = recordMapper;
        this.answerMapper = answerMapper;
        this.excelService = excelService;
    }

    /** 所需配置：19 个格子、规则参数和两类题库。 */
    @GetMapping("/config")
    @PreAuthorize("@ss.hasPermi('zyfxq:config:list')")
    public AjaxResult config() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cells", configService.listCells());
        data.put("setting", configService.setting());
        data.put("questions", configService.listAdminQuestions(null));
        return AjaxResult.success(data);
    }

    /** 保存单个格子的类型、题目类型、文案、底色与启停。 */
    @PostMapping("/cells/{cellNo}")
    @PreAuthorize("@ss.hasPermi('zyfxq:config:edit')")
    public AjaxResult saveCell(@PathVariable Integer cellNo, @RequestBody ZyfxqCell cell) {
        cell.setCellNo(cellNo);
        return AjaxResult.success(configService.saveCell(cell));
    }

    /** 保存规则参数：单局时长、单题限时、后退格数、骰子面数与启停。 */
    @PostMapping("/setting")
    @PreAuthorize("@ss.hasPermi('zyfxq:config:edit')")
    public AjaxResult saveSetting(@RequestBody ZyfxqSetting setting) {
        return AjaxResult.success(configService.saveSetting(setting));
    }

    /** 题库列表；可按职业问答或情景任务卡过滤。 */
    @GetMapping("/questions")
    @PreAuthorize("@ss.hasPermi('zyfxq:config:list')")
    public AjaxResult questions(@RequestParam(required = false) String questionType) {
        return AjaxResult.success(configService.listAdminQuestions(questionType));
    }

    /** 新增或修改题目及其 4 个选项；题目编号为空表示新增。 */
    @PostMapping("/questions")
    @PreAuthorize("@ss.hasPermi('zyfxq:config:edit')")
    public AjaxResult saveQuestion(@RequestBody ZyfxqQuestion question) {
        return AjaxResult.success(configService.saveQuestion(question));
    }

    /** 删除题目及其选项；历史答题明细保留快照。 */
    @DeleteMapping("/questions/{id}")
    @PreAuthorize("@ss.hasPermi('zyfxq:config:edit')")
    public AjaxResult deleteQuestion(@PathVariable Long id) {
        configService.deleteQuestion(id);
        return AjaxResult.success();
    }

    /** 参与记录分页查询。 */
    @GetMapping("/records")
    @PreAuthorize("@ss.hasPermi('zyfxq:record:list')")
    public TableDataInfo records(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String result,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ZyfxqRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                result, statusFilter, startDate, endDate);
        startPage();
        List<ZyfxqRecord> records = recordMapper.selectAdminRecords(query);
        // 分页总数必须在转换为普通列表之前读取。
        TableDataInfo table = getDataTable(records);
        table.setRows(enrichService.enrich(records));
        return table;
    }

    /** 单条记录详情：记录展示字段 + 本局答题明细。 */
    @GetMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('zyfxq:record:list')")
    public AjaxResult record(@PathVariable Long id) {
        ZyfxqRecord record = id == null ? null : recordMapper.selectById(id);
        if (record == null || !ZyfxqGameService.GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        int serial = 1;
        for (ZyfxqAnswer answer : answerMapper.selectList(new QueryWrapper<ZyfxqAnswer>()
                .eq("record_id", id)
                .eq("attempt_no", record.getAttemptNo() == null ? 1 : record.getAttemptNo())
                .orderByAsc("answer_id"))) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("serialNo", serial++);
            row.put("cellNo", answer.getCellNo());
            row.put("questionTypeName", ZyfxqConfigService.questionTypeName(answer.getQuestionType()));
            row.put("title", answer.getQuestionTitle());
            row.put("optionContent", answer.getOptionContent());
            row.put("correctContent", answer.getCorrectContent());
            row.put("resultName", "1".equals(answer.getIsCorrect()) ? "答对"
                    : ("1".equals(answer.getIsTimeout()) ? "超时" : "答错"));
            row.put("limitSeconds", answer.getLimitSeconds());
            row.put("costMs", answer.getCostMs());
            row.put("isTimeout", "1".equals(answer.getIsTimeout()));
            row.put("createTime", answer.getCreateTime());
            rows.add(row);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("record", enrichService.enrichOne(record));
        data.put("answers", rows);
        return AjaxResult.success(data);
    }

    /** 删除整条参与记录及其答题明细和活动参与数据，删除后学生可重新扫码参与。 */
    @DeleteMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('zyfxq:record:remove')")
    public AjaxResult deleteRecord(@PathVariable Long id) {
        gameService.deleteRecord(id);
        return AjaxResult.success();
    }

    /** 统计分析：学校端两页内容（汇总、知识点、题目、高频错题、分析）与分页记录。 */
    @GetMapping("/statistics")
    @PreAuthorize("@ss.hasPermi('zyfxq:statistics:list')")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String result,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ZyfxqRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                result, statusFilter, startDate, endDate);
        Map<String, Object> summary = statisticsService.summary(query);
        List<Map<String, Object>> typeStats = statisticsService.questionTypeStats(query);
        List<Map<String, Object>> questionStats = statisticsService.questionStats(query);
        List<Map<String, Object>> wrongQuestions = statisticsService.wrongQuestions(questionStats, 5);

        // 分页记录与“参与记录”列表使用同一查询与补全规则。
        startPage();
        List<ZyfxqRecord> pageRecords = recordMapper.selectAdminRecords(query);
        TableDataInfo page = getDataTable(pageRecords);
        page.setRows(enrichService.enrich(pageRecords));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("questionTypeStats", typeStats);
        data.put("questionStats", questionStats);
        data.put("wrongQuestions", wrongQuestions);
        data.put("analysis", statisticsService.analysis(summary, typeStats, wrongQuestions));
        data.put("total", page.getTotal());
        data.put("records", page.getRows());
        return AjaxResult.success(data);
    }

    /** 按当前筛选条件导出统计汇总、知识点统计、题目统计、学生明细、答题明细和数据说明。 */
    @GetMapping("/statistics/excel")
    @PreAuthorize("@ss.hasPermi('zyfxq:statistics:export')")
    public void statisticsExcel(@RequestParam Long schoolId,
                                @RequestParam Long instanceId,
                                @RequestParam(required = false) Long departmentId,
                                @RequestParam(required = false) String major,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) String result,
                                @RequestParam(required = false) String statusFilter,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                HttpServletResponse response) throws IOException {
        ZyfxqRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                result, statusFilter, startDate, endDate);
        Map<String, Object> summary = statisticsService.summary(query);
        List<Map<String, Object>> typeStats = statisticsService.questionTypeStats(query);
        List<Map<String, Object>> questionStats = statisticsService.questionStats(query);
        List<Map<String, Object>> wrongQuestions = statisticsService.wrongQuestions(questionStats, 5);
        String analysis = statisticsService.analysis(summary, typeStats, wrongQuestions);
        List<Map<String, Object>> rows = enrichService.enrich(recordMapper.selectAdminRecords(query));

        try (SXSSFWorkbook workbook = excelService.create(summary, typeStats, questionStats, wrongQuestions,
                analysis, rows, schoolId, instanceId, startDate, endDate)) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("职业飞行棋学生数据_" + schoolId + "_" + instanceId + ".xlsx",
                                StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally {
                workbook.dispose();
            }
        }
    }

    /** 统一校验筛选条件，列表、统计与导出使用同一口径。 */
    private ZyfxqRecordQuery filter(Long instanceId, Long schoolId, Long departmentId, String major,
                                    String gender, String studentNo, String result, String statusFilter,
                                    LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String resultValue = trimToNull(result);
        if (resultValue != null && !ZyfxqGameService.RESULT_WIN.equals(resultValue)
                && !ZyfxqGameService.RESULT_LOSE.equals(resultValue)) {
            throw new ServiceException("对局结果筛选值无效");
        }
        String status = trimToNull(statusFilter);
        if (status != null && !ZyfxqGameService.STATUS_READY.equals(status)
                && !ZyfxqGameService.STATUS_PLAYING.equals(status)
                && !ZyfxqGameService.STATUS_FINISHED.equals(status)) {
            throw new ServiceException("参与状态筛选值无效");
        }
        ZyfxqRecordQuery query = new ZyfxqRecordQuery();
        query.setInstanceId(instanceId);
        query.setSchoolId(schoolId);
        query.setDepartmentId(departmentId);
        query.setMajor(trimToNull(major));
        query.setGenderValue(trimToNull(gender) == null ? null : ZyfxqRecordEnrichService.genderText(gender));
        query.setStudentNo(trimToNull(studentNo));
        query.setResultValue(resultValue);
        query.setStatus(status);
        query.setStartDate(startDate);
        // 参与日期以扫码时间为准，结束日次日零点之前均包含，避免对列做 DATE() 运算。
        query.setEndDate(endDate == null ? null : endDate.plusDays(1));
        return query;
    }

    private static String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
