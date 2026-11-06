package com.gkzh.web.controller.zcyltzj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.app.service.ZcyltzjRecordEnrichService;
import com.gkzh.app.service.ZcyltzjStatisticsExcelService;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zcyltzj.domain.ZcyltzjPoint;
import com.gkzh.zcyltzj.domain.ZcyltzjQuestion;
import com.gkzh.zcyltzj.domain.ZcyltzjRecord;
import com.gkzh.zcyltzj.domain.ZcyltzjRuleCell;
import com.gkzh.zcyltzj.mapper.ZcyltzjRecordMapper;
import com.gkzh.zcyltzj.service.ZcyltzjConfigService;
import com.gkzh.zcyltzj.service.ZcyltzjRuleService;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职场压力·骰子局 Web 管理接口。
 * 配置仅维护固定点数 1-6 的名称与试题；参与记录和统计按活动、点数和学生属性筛选，
 * 记录可整条删除便于测试，统计数据可按当前筛选条件导出 Excel。
 */
@RestController
@RequestMapping("/zcyltzj/admin")
public class ZcyltzjAdminController extends BaseController {

    private final ZcyltzjConfigService configService;
    private final ZcyltzjRuleService ruleService;
    private final ZcyltzjRecordMapper recordMapper;
    private final IActivityWeekService activityWeekService;
    private final ZcyltzjRecordEnrichService enrichService;
    private final ZcyltzjStatisticsExcelService excelService;

    public ZcyltzjAdminController(ZcyltzjConfigService configService, ZcyltzjRuleService ruleService,
                                  ZcyltzjRecordMapper recordMapper, IActivityWeekService activityWeekService,
                                  ZcyltzjRecordEnrichService enrichService,
                                  ZcyltzjStatisticsExcelService excelService) {
        this.configService = configService;
        this.ruleService = ruleService;
        this.recordMapper = recordMapper;
        this.activityWeekService = activityWeekService;
        this.enrichService = enrichService;
        this.excelService = excelService;
    }

    /** 六个骰子点数的名称、主题、职场投射和题目数量。 */
    @GetMapping("/points")
    @PreAuthorize("@ss.hasPermi('zcyltzj:config:list')")
    public AjaxResult points() {
        return AjaxResult.success(configService.listPoints());
    }

    /** 保存点数名称、主题和职场投射。 */
    @PostMapping("/points")
    @PreAuthorize("@ss.hasPermi('zcyltzj:config:edit')")
    public AjaxResult savePoint(@RequestBody ZcyltzjPoint point) {
        return AjaxResult.success(configService.savePoint(point));
    }

    /** 查询指定点数下的试题，未传点数时返回全部。 */
    @GetMapping("/questions")
    @PreAuthorize("@ss.hasPermi('zcyltzj:config:list')")
    public AjaxResult questions(@RequestParam(required = false) Integer pointNo) {
        return AjaxResult.success(configService.listQuestions(pointNo));
    }

    /** 新增或修改试题。 */
    @PostMapping("/questions")
    @PreAuthorize("@ss.hasPermi('zcyltzj:config:edit')")
    public AjaxResult saveQuestion(@RequestBody ZcyltzjQuestion question) {
        return AjaxResult.success(configService.saveQuestion(question));
    }

    /** 删除试题。 */
    @DeleteMapping("/questions/{id}")
    @PreAuthorize("@ss.hasPermi('zcyltzj:config:edit')")
    public AjaxResult deleteQuestion(@PathVariable Long id) {
        configService.deleteQuestion(id);
        return AjaxResult.success();
    }

    /** 九宫格下中、下右两格的规则配置。 */
    @GetMapping("/rule-cells")
    @PreAuthorize("@ss.hasPermi('zcyltzj:config:list')")
    public AjaxResult ruleCells() {
        return AjaxResult.success(ruleService.listAll());
    }

    /** 保存规则格标题、规则内容和状态。 */
    @PostMapping("/rule-cells")
    @PreAuthorize("@ss.hasPermi('zcyltzj:config:edit')")
    public AjaxResult saveRuleCell(@RequestBody ZcyltzjRuleCell cell) {
        return AjaxResult.success(ruleService.save(cell));
    }

    /** 参与记录分页查询。 */
    @GetMapping("/records")
    @PreAuthorize("@ss.hasPermi('zcyltzj:record:list')")
    public TableDataInfo records(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) Integer pointNo,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        RecordFilter filter = filter(instanceId, schoolId, departmentId, major, gender, studentNo, pointNo,
                statusFilter, startDate, endDate);
        startPage();
        List<ZcyltzjRecord> records = recordMapper.selectAdminRecords(filter.instanceId, filter.schoolId,
                filter.departmentId, filter.major, filter.genderValue, filter.studentNo, filter.pointNo,
                filter.status, filter.startDate, filter.endDate);
        // 分页总数必须在转换为普通列表之前读取。
        TableDataInfo table = getDataTable(records);
        table.setRows(enrichService.enrich(records));
        return table;
    }

    /** 统计汇总与当前筛选条件下的分页记录。 */
    @GetMapping("/statistics")
    @PreAuthorize("@ss.hasPermi('zcyltzj:statistics:list')")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) Integer pointNo,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        RecordFilter filter = filter(instanceId, schoolId, departmentId, major, gender, studentNo, pointNo,
                statusFilter, startDate, endDate);
        Map<String, Object> summary = recordMapper.selectAdminSummary(filter.instanceId, filter.schoolId,
                filter.departmentId, filter.major, filter.genderValue, filter.studentNo, filter.pointNo,
                filter.status, filter.startDate, filter.endDate);
        if (summary == null) {
            summary = new LinkedHashMap<>();
        }
        long total = number(summary.get("total"));
        long finished = number(summary.get("finished"));
        summary.put("total", total);
        summary.put("finished", finished);
        summary.put("completionRate", total == 0 ? 0 : Math.round(finished * 1000.0 / total) / 10.0);

        TableDataInfo page = records(instanceId, schoolId, departmentId, major, gender, studentNo, pointNo,
                statusFilter, startDate, endDate);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("total", page.getTotal());
        result.put("records", page.getRows());
        return AjaxResult.success(result);
    }

    /** 按当前筛选条件导出包含统计汇总、学生数据和答题明细的 Excel。 */
    @GetMapping("/statistics/excel")
    @PreAuthorize("@ss.hasPermi('zcyltzj:statistics:export')")
    public void statisticsExcel(@RequestParam Long schoolId,
                                @RequestParam Long instanceId,
                                @RequestParam(required = false) Long departmentId,
                                @RequestParam(required = false) String major,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) Integer pointNo,
                                @RequestParam(required = false) String statusFilter,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                HttpServletResponse response) throws IOException {
        try (SXSSFWorkbook workbook = excelService.create(schoolId, instanceId, departmentId, major, gender,
                studentNo, pointNo, statusFilter, startDate, endDate)) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("职场压力骰子局学生数据_" + schoolId + "_" + instanceId + ".xlsx",
                                StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally {
                workbook.dispose();
            }
        }
    }

    /** 删除整条参与记录及其活动游戏参与数据，删除后学生可重新扫码参与。 */
    @DeleteMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('zcyltzj:record:remove')")
    @Transactional
    public AjaxResult deleteRecord(@PathVariable Long id) {
        ZcyltzjRecord record = recordMapper.selectOne(new QueryWrapper<ZcyltzjRecord>()
                .eq("record_id", id).last("FOR UPDATE"));
        if (record == null || !"zcyltzj".equals(record.getGameType())) {
            return AjaxResult.error("参与记录不存在");
        }
        // 删除范围来自记录本身，不能按用户批量删除其他活动或其他游戏的数据。
        activityWeekService.deleteGameParticipations(record.getInstanceId(), record.getGameId(), record.getUserId());
        int rows = recordMapper.deleteById(id);
        if (rows != 1) {
            throw new ServiceException("参与记录删除失败，请重试");
        }
        return AjaxResult.success();
    }

    /** 统一校验筛选条件，列表、统计与导出使用同一口径。 */
    private RecordFilter filter(Long instanceId, Long schoolId, Long departmentId, String major, String gender,
                                String studentNo, Integer pointNo, String statusFilter,
                                LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        if (pointNo != null && (pointNo < 1 || pointNo > 6)) {
            throw new ServiceException("骰子点数必须为 1 到 6");
        }
        String status = statusFilter == null ? "" : statusFilter.trim();
        if (!status.isEmpty() && !"finished".equals(status) && !"inProgress".equals(status)) {
            throw new ServiceException("参与状态筛选值无效");
        }
        RecordFilter filter = new RecordFilter();
        filter.instanceId = instanceId;
        filter.schoolId = schoolId;
        filter.departmentId = departmentId;
        filter.major = trimToNull(major);
        filter.genderValue = trimToNull(gender) == null ? null : ZcyltzjRecordEnrichService.genderText(gender);
        filter.studentNo = trimToNull(studentNo);
        filter.pointNo = pointNo;
        filter.status = status.isEmpty() ? null : status;
        filter.startDate = startDate;
        // 参与日期以扫码时间为准，结束日次日零点之前均包含，避免对列做 DATE() 运算。
        filter.endDate = endDate == null ? null : endDate.plusDays(1);
        return filter;
    }

    private static long number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? 0 : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    /** 列表、汇总和导出共用的筛选参数。 */
    private static final class RecordFilter {
        /** 活动实例编号。 */
        private Long instanceId;
        /** 学校编号。 */
        private Long schoolId;
        /** 院系编号，查询时包含其下级专业。 */
        private Long departmentId;
        /** 专业名称模糊条件。 */
        private String major;
        /** 性别中文值：男、女、其他。 */
        private String genderValue;
        /** 学号模糊条件。 */
        private String studentNo;
        /** 骰子点数 1-6。 */
        private Integer pointNo;
        /** finished 已完成，inProgress 进行中。 */
        private String status;
        /** 参与日期开始。 */
        private LocalDate startDate;
        /** 参与日期结束次日零点，不含。 */
        private LocalDate endDate;
    }
}
