package com.gkzh.web.controller.nlld;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.app.service.NlldRecordEnrichService;
import com.gkzh.app.service.NlldStatisticsExcelService;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.nlld.domain.NlldAnswer;
import com.gkzh.nlld.domain.NlldDimension;
import com.gkzh.nlld.domain.NlldOption;
import com.gkzh.nlld.domain.NlldRecord;
import com.gkzh.nlld.domain.NlldRound;
import com.gkzh.nlld.domain.dto.NlldRecordQuery;
import com.gkzh.nlld.mapper.NlldAnswerMapper;
import com.gkzh.nlld.mapper.NlldRecordMapper;
import com.gkzh.nlld.service.NlldConfigService;
import com.gkzh.nlld.service.NlldGameService;
import com.gkzh.nlld.service.NlldStatisticsService;
import com.gkzh.nlld.support.NlldScoring;
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
 * 能量雷达 Web 管理接口。
 * 15 个维度、6 个关卡和 30 件物品可维护文案与启停；参与记录可筛选、查看和删除；
 * 统计分析按维度偏好、TOP2/多元开放型分布展示，并支持 Excel 导出。
 */
@RestController
@RequestMapping("/nlld/admin")
public class NlldAdminController extends BaseController {

    private final NlldConfigService configService;
    private final NlldGameService gameService;
    private final NlldStatisticsService statisticsService;
    private final NlldRecordEnrichService enrichService;
    private final NlldRecordMapper recordMapper;
    private final NlldAnswerMapper answerMapper;
    private final NlldStatisticsExcelService excelService;

    public NlldAdminController(NlldConfigService configService, NlldGameService gameService,
                               NlldStatisticsService statisticsService, NlldRecordEnrichService enrichService,
                               NlldRecordMapper recordMapper, NlldAnswerMapper answerMapper,
                               NlldStatisticsExcelService excelService) {
        this.configService = configService;
        this.gameService = gameService;
        this.statisticsService = statisticsService;
        this.enrichService = enrichService;
        this.recordMapper = recordMapper;
        this.answerMapper = answerMapper;
        this.excelService = excelService;
    }

    /** 15 个维度配置，包含停用维度。 */
    @GetMapping("/dimensions")
    @PreAuthorize("@ss.hasPermi('nlld:config:list')")
    public AjaxResult dimensions() {
        return AjaxResult.success(configService.listDimensions());
    }

    /** 保存维度名称、分组、解读文案、排序和启停；编码固定不可改。 */
    @PostMapping("/dimensions/{id}")
    @PreAuthorize("@ss.hasPermi('nlld:config:edit')")
    public AjaxResult saveDimension(@PathVariable Long id, @RequestBody NlldDimension dimension) {
        dimension.setDimensionId(id);
        return AjaxResult.success(configService.saveDimension(dimension));
    }

    /** 6 个关卡及其 5 件物品，包含停用数据和所属维度。 */
    @GetMapping("/rounds")
    @PreAuthorize("@ss.hasPermi('nlld:config:list')")
    public AjaxResult rounds() {
        return AjaxResult.success(configService.listAdminRounds());
    }

    /** 保存关卡名称、情景、排序和启停。 */
    @PostMapping("/rounds/{id}")
    @PreAuthorize("@ss.hasPermi('nlld:config:edit')")
    public AjaxResult saveRound(@PathVariable Long id, @RequestBody NlldRound round) {
        round.setRoundId(id);
        return AjaxResult.success(configService.saveRound(round));
    }

    /** 保存物品名称、描述、所属维度、排序和启停。 */
    @PostMapping("/options/{id}")
    @PreAuthorize("@ss.hasPermi('nlld:config:edit')")
    public AjaxResult saveOption(@PathVariable Long id, @RequestBody NlldOption option) {
        option.setOptionId(id);
        return AjaxResult.success(configService.saveOption(option));
    }

    /** 参与记录分页查询。 */
    @GetMapping("/records")
    @PreAuthorize("@ss.hasPermi('nlld:record:list')")
    public TableDataInfo records(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String resultType,
                                 @RequestParam(required = false) String dimensionCode,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        NlldRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                resultType, dimensionCode, statusFilter, startDate, endDate);
        startPage();
        List<NlldRecord> records = recordMapper.selectAdminRecords(query);
        // 分页总数必须在转换为普通列表之前读取。
        TableDataInfo table = getDataTable(records);
        table.setRows(enrichService.enrich(records));
        return table;
    }

    /** 单条记录详情：记录展示字段 + 6 关已选物品。 */
    @GetMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('nlld:record:list')")
    public AjaxResult record(@PathVariable Long id) {
        NlldRecord record = id == null ? null : recordMapper.selectById(id);
        if (record == null || !NlldGameService.GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        Map<Long, String> dimensionNames = new LinkedHashMap<>();
        for (NlldDimension dimension : configService.listDimensions()) {
            dimensionNames.put(dimension.getDimensionId(), dimension.getDimensionName());
        }
        List<NlldAnswer> answers = answerMapper.selectList(new QueryWrapper<NlldAnswer>()
                .eq("record_id", id).orderByAsc("round_no").orderByAsc("option_label"));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (NlldAnswer answer : answers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("roundNo", answer.getRoundNo());
            row.put("optionLabel", answer.getOptionLabel());
            row.put("optionName", answer.getOptionName());
            row.put("dimensionName", dimensionNames.get(answer.getDimensionId()));
            rows.add(row);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("record", enrichService.enrichOne(record));
        data.put("answers", rows);
        return AjaxResult.success(data);
    }

    /** 删除整条参与记录及其活动游戏参与数据，删除后学生可重新扫码参与。 */
    @DeleteMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('nlld:record:remove')")
    public AjaxResult deleteRecord(@PathVariable Long id) {
        gameService.deleteRecord(id);
        return AjaxResult.success();
    }

    /** 统计汇总、维度偏好、结果类型分布、分析和当前筛选下的分页记录。 */
    @GetMapping("/statistics")
    @PreAuthorize("@ss.hasPermi('nlld:statistics:list')")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String resultType,
                                 @RequestParam(required = false) String dimensionCode,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        NlldRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                resultType, dimensionCode, statusFilter, startDate, endDate);
        Map<String, Object> summary = statisticsService.summary(query);
        List<Map<String, Object>> dimensionStats = statisticsService.dimensionStats(query, summary);
        List<Map<String, Object>> resultTypeStats = statisticsService.resultTypeStats(summary);

        // 统计页的分页记录与“参与记录”列表使用同一查询和补全规则，但不复用带权限注解的方法。
        startPage();
        List<NlldRecord> pageRecords = recordMapper.selectAdminRecords(query);
        TableDataInfo page = getDataTable(pageRecords);
        page.setRows(enrichService.enrich(pageRecords));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("dimensionStats", dimensionStats);
        data.put("resultTypeStats", resultTypeStats);
        data.put("analysis", statisticsService.analysis(summary, dimensionStats, resultTypeStats));
        data.put("total", page.getTotal());
        data.put("records", page.getRows());
        return AjaxResult.success(data);
    }

    /** 按当前筛选条件导出统计汇总、维度偏好、学生明细、选择明细和数据说明。 */
    @GetMapping("/statistics/excel")
    @PreAuthorize("@ss.hasPermi('nlld:statistics:export')")
    public void statisticsExcel(@RequestParam Long schoolId,
                                @RequestParam Long instanceId,
                                @RequestParam(required = false) Long departmentId,
                                @RequestParam(required = false) String major,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) String resultType,
                                @RequestParam(required = false) String dimensionCode,
                                @RequestParam(required = false) String statusFilter,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                HttpServletResponse response) throws IOException {
        NlldRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                resultType, dimensionCode, statusFilter, startDate, endDate);
        Map<String, Object> summary = statisticsService.summary(query);
        List<Map<String, Object>> dimensionStats = statisticsService.dimensionStats(query, summary);
        List<Map<String, Object>> resultTypeStats = statisticsService.resultTypeStats(summary);
        String analysis = statisticsService.analysis(summary, dimensionStats, resultTypeStats);
        List<Map<String, Object>> rows = enrichService.enrich(recordMapper.selectAdminRecords(query));
        List<Map<String, Object>> answerDetails = recordMapper.selectAnswerDetails(query);

        try (SXSSFWorkbook workbook = excelService.create(summary, dimensionStats, resultTypeStats, analysis,
                rows, answerDetails, schoolId, instanceId, startDate, endDate)) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("能量雷达学生数据_" + schoolId + "_" + instanceId + ".xlsx",
                                StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally {
                workbook.dispose();
            }
        }
    }

    /** 统一校验筛选条件，列表、统计与导出使用同一口径。 */
    private NlldRecordQuery filter(Long instanceId, Long schoolId, Long departmentId, String major, String gender,
                                   String studentNo, String resultType, String dimensionCode, String statusFilter,
                                   LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String type = trimToNull(resultType);
        if (type != null && !NlldScoring.RESULT_TOP2.equals(type) && !NlldScoring.RESULT_OPEN.equals(type)) {
            throw new ServiceException("结果类型筛选值无效");
        }
        String code = trimToNull(dimensionCode);
        if (code != null && !NlldScoring.isValidCode(code)) {
            throw new ServiceException("维度筛选值无效");
        }
        String status = trimToNull(statusFilter);
        if (status != null && !"participating".equals(status) && !"finished".equals(status)) {
            throw new ServiceException("参与状态筛选值无效");
        }
        NlldRecordQuery query = new NlldRecordQuery();
        query.setInstanceId(instanceId);
        query.setSchoolId(schoolId);
        query.setDepartmentId(departmentId);
        query.setMajor(trimToNull(major));
        query.setGenderValue(trimToNull(gender) == null ? null : NlldRecordEnrichService.genderText(gender));
        query.setStudentNo(trimToNull(studentNo));
        query.setResultType(type);
        query.setDimensionCode(code);
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
