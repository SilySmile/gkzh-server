package com.gkzh.web.controller.jlcz;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.app.service.JlczRecordEnrichService;
import com.gkzh.app.service.JlczStatisticsExcelService;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.jlcz.domain.JlczChoice;
import com.gkzh.jlcz.domain.JlczRecord;
import com.gkzh.jlcz.domain.JlczSetting;
import com.gkzh.jlcz.domain.JlczWeight;
import com.gkzh.jlcz.domain.dto.JlczRecordQuery;
import com.gkzh.jlcz.mapper.JlczChoiceMapper;
import com.gkzh.jlcz.mapper.JlczRecordMapper;
import com.gkzh.jlcz.service.JlczConfigService;
import com.gkzh.jlcz.service.JlczStatisticsService;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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
 * 简历称重 Web 管理接口。
 * 配置维护 20 个砝码与天平规则参数；参与记录和统计按活动、结果、学生属性与参与日期筛选，
 * 记录可整条删除便于测试，统计数据可按当前筛选条件导出 Excel。
 */
@RestController
@RequestMapping("/jlcz/admin")
public class JlczAdminController extends BaseController {

    private final JlczConfigService configService;
    private final JlczRecordMapper recordMapper;
    private final JlczChoiceMapper choiceMapper;
    private final IActivityWeekService activityWeekService;
    private final JlczRecordEnrichService enrichService;
    private final JlczStatisticsService statisticsService;
    private final JlczStatisticsExcelService excelService;

    public JlczAdminController(JlczConfigService configService, JlczRecordMapper recordMapper,
                               JlczChoiceMapper choiceMapper, IActivityWeekService activityWeekService,
                               JlczRecordEnrichService enrichService, JlczStatisticsService statisticsService,
                               JlczStatisticsExcelService excelService) {
        this.configService = configService;
        this.recordMapper = recordMapper;
        this.choiceMapper = choiceMapper;
        this.activityWeekService = activityWeekService;
        this.enrichService = enrichService;
        this.statisticsService = statisticsService;
        this.excelService = excelService;
    }

    /** 配置页数据：20 个砝码与规则参数。 */
    @GetMapping("/config")
    @PreAuthorize("@ss.hasPermi('jlcz:config:list')")
    public AjaxResult config() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("weights", configService.listAll());
        data.put("setting", configService.getSetting());
        return AjaxResult.success(data);
    }

    /** 保存单个砝码；挂钩号由路径决定，不允许修改。 */
    @PutMapping("/weights/{weightNo}")
    @PreAuthorize("@ss.hasPermi('jlcz:config:edit')")
    public AjaxResult saveWeight(@PathVariable Integer weightNo, @RequestBody JlczWeight weight) {
        if (weight == null) {
            throw new ServiceException("砝码数据不能为空");
        }
        weight.setWeightNo(weightNo);
        return AjaxResult.success(configService.saveWeight(weight));
    }

    /** 保存天平规则参数。 */
    @PutMapping("/setting")
    @PreAuthorize("@ss.hasPermi('jlcz:config:edit')")
    public AjaxResult saveSetting(@RequestBody JlczSetting setting) {
        return AjaxResult.success(configService.saveSetting(setting));
    }

    /** 参与记录分页查询。 */
    @GetMapping("/records")
    @PreAuthorize("@ss.hasPermi('jlcz:record:list')")
    public TableDataInfo records(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String resultFilter,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        JlczRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                resultFilter, statusFilter, startDate, endDate);
        startPage();
        List<JlczRecord> records = recordMapper.selectAdminRecords(query);
        // 分页总数必须在转换为普通列表之前读取。
        TableDataInfo table = getDataTable(records);
        table.setRows(enrichService.enrich(records));
        return table;
    }

    /** 单条记录详情：基础信息 + 本次勾选的砝码明细。 */
    @GetMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('jlcz:record:list')")
    public AjaxResult record(@PathVariable Long id) {
        JlczRecord record = recordMapper.selectById(id);
        if (record == null || !"jlcz".equals(record.getGameType())) {
            return AjaxResult.error("参与记录不存在");
        }
        List<JlczChoice> choices = choiceMapper.selectList(new QueryWrapper<JlczChoice>()
                .eq("record_id", id)
                .eq("attempt_no", record.getAttemptNo() == null ? 1 : record.getAttemptNo())
                .orderByAsc("weight_no"));
        Map<String, Object> data = new LinkedHashMap<>();
        List<Map<String, Object>> rows = enrichService.enrich(java.util.Collections.singletonList(record));
        data.put("record", rows.isEmpty() ? null : rows.get(0));
        data.put("choices", choices);
        return AjaxResult.success(data);
    }

    /** 统计汇总、砝码选择率、类别统计、总重分布与当前筛选下的分页记录。 */
    @GetMapping("/statistics")
    @PreAuthorize("@ss.hasPermi('jlcz:statistics:list')")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId,
                                @RequestParam(required = false) Long schoolId,
                                @RequestParam(required = false) Long departmentId,
                                @RequestParam(required = false) String major,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) String resultFilter,
                                @RequestParam(required = false) String statusFilter,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        JlczRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                resultFilter, statusFilter, startDate, endDate);
        Map<String, Object> summary = statisticsService.summary(query);
        long finished = number(summary.get("finishedCount"));
        List<Map<String, Object>> weightStats = statisticsService.weightStats(query, finished);
        List<Map<String, Object>> categoryStats = statisticsService.categoryStats(weightStats, finished);
        List<Map<String, Object>> distribution = statisticsService.distribution(query, summary);

        TableDataInfo page = records(instanceId, schoolId, departmentId, major, gender, studentNo,
                resultFilter, statusFilter, startDate, endDate);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("weightStats", weightStats);
        result.put("categoryStats", categoryStats);
        result.put("distribution", distribution);
        result.put("analysis", statisticsService.analysis(summary, categoryStats, weightStats));
        result.put("total", page.getTotal());
        result.put("records", page.getRows());
        return AjaxResult.success(result);
    }

    /** 按当前筛选条件导出包含统计汇总、砝码选择率、类别统计和学生明细的 Excel。 */
    @GetMapping("/statistics/excel")
    @PreAuthorize("@ss.hasPermi('jlcz:statistics:export')")
    public void statisticsExcel(@RequestParam Long schoolId,
                                @RequestParam Long instanceId,
                                @RequestParam(required = false) Long departmentId,
                                @RequestParam(required = false) String major,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) String resultFilter,
                                @RequestParam(required = false) String statusFilter,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                HttpServletResponse response) throws IOException {
        try (SXSSFWorkbook workbook = excelService.create(schoolId, instanceId, departmentId, major, gender,
                studentNo, resultFilter, statusFilter, startDate, endDate)) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("简历称重学生数据_" + schoolId + "_" + instanceId + ".xlsx",
                                StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally {
                workbook.dispose();
            }
        }
    }

    /** 删除整条参与记录及其砝码明细和活动游戏参与数据，删除后学生可重新扫码参与。 */
    @DeleteMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('jlcz:record:remove')")
    @Transactional
    public AjaxResult deleteRecord(@PathVariable Long id) {
        JlczRecord record = recordMapper.selectOne(new QueryWrapper<JlczRecord>()
                .eq("record_id", id).last("FOR UPDATE"));
        if (record == null || !"jlcz".equals(record.getGameType())) {
            return AjaxResult.error("参与记录不存在");
        }
        // 删除范围来自记录本身，不能按用户批量删除其他活动或其他游戏的数据。
        choiceMapper.delete(new QueryWrapper<JlczChoice>().eq("record_id", id));
        activityWeekService.deleteGameParticipations(record.getInstanceId(), record.getGameId(), record.getUserId());
        int rows = recordMapper.deleteById(id);
        if (rows != 1) {
            throw new ServiceException("参与记录删除失败，请重试");
        }
        return AjaxResult.success();
    }

    /** 统一校验筛选条件，列表、统计与导出使用同一口径。 */
    private JlczRecordQuery filter(Long instanceId, Long schoolId, Long departmentId, String major, String gender,
                                   String studentNo, String resultFilter, String statusFilter,
                                   LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String result = trimToNull(resultFilter);
        if (result != null && !"pass".equals(result) && !"fail".equals(result)) {
            throw new ServiceException("称重结果筛选值无效");
        }
        String status = trimToNull(statusFilter);
        if (status != null && !"finished".equals(status) && !"inProgress".equals(status)) {
            throw new ServiceException("参与状态筛选值无效");
        }
        JlczRecordQuery query = new JlczRecordQuery();
        query.setInstanceId(instanceId);
        query.setSchoolId(schoolId);
        query.setDepartmentId(departmentId);
        query.setMajor(trimToNull(major));
        query.setGenderValue(trimToNull(gender) == null ? null : JlczRecordEnrichService.genderText(gender));
        query.setStudentNo(trimToNull(studentNo));
        query.setResultValue(result);
        query.setStatus(status);
        query.setStartDate(startDate);
        // 参与日期以扫码时间为准，结束日次日零点之前均包含，避免对列做 DATE() 运算。
        query.setEndDate(endDate == null ? null : endDate.plusDays(1));
        return query;
    }

    private static long number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? 0L : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
