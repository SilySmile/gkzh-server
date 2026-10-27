package com.gkzh.web.controller.zyjdc;

import com.gkzh.app.service.ZyjdcRecordEnrichService;
import com.gkzh.app.service.ZyjdcStatisticsExcelService;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zyjdc.domain.ZyjdcRecord;
import com.gkzh.zyjdc.domain.ZyjdcRole;
import com.gkzh.zyjdc.domain.dto.ZyjdcRecordQuery;
import com.gkzh.zyjdc.mapper.ZyjdcRecordMapper;
import com.gkzh.zyjdc.service.ZyjdcConfigService;
import com.gkzh.zyjdc.service.ZyjdcGameService;
import com.gkzh.zyjdc.service.ZyjdcStatisticsService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职业角斗场 Web 管理接口。
 * 角色内容固定 8 个，可维护文案、头像和启停；参与记录可筛选和删除；
 * 统计分析按角色偏好、胜负分布和完成率展示，并支持 Excel 导出。
 */
@RestController
@RequestMapping("/zyjdc/admin")
public class ZyjdcAdminController extends BaseController {

    private final ZyjdcConfigService configService;
    private final ZyjdcGameService gameService;
    private final ZyjdcStatisticsService statisticsService;
    private final ZyjdcRecordEnrichService enrichService;
    private final ZyjdcRecordMapper recordMapper;
    private final ZyjdcStatisticsExcelService excelService;

    public ZyjdcAdminController(ZyjdcConfigService configService, ZyjdcGameService gameService,
                                ZyjdcStatisticsService statisticsService, ZyjdcRecordEnrichService enrichService,
                                ZyjdcRecordMapper recordMapper, ZyjdcStatisticsExcelService excelService) {
        this.configService = configService;
        this.gameService = gameService;
        this.statisticsService = statisticsService;
        this.enrichService = enrichService;
        this.recordMapper = recordMapper;
        this.excelService = excelService;
    }

    /** 8 个角色配置，包含停用角色。 */
    @GetMapping("/roles")
    @PreAuthorize("@ss.hasPermi('zyjdc:config:list')")
    public AjaxResult roles() {
        return AjaxResult.success(configService.listAll());
    }

    /** 保存角色文案、头像、排序和启停；编码和等级固定不可改。 */
    @PostMapping("/roles/{id}")
    @PreAuthorize("@ss.hasPermi('zyjdc:config:edit')")
    public AjaxResult saveRole(@PathVariable Long id, @RequestBody ZyjdcRole role) {
        role.setRoleId(id);
        return AjaxResult.success(configService.save(role));
    }

    /** 参与记录分页查询。 */
    @GetMapping("/records")
    @PreAuthorize("@ss.hasPermi('zyjdc:record:list')")
    public TableDataInfo records(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) Long roleId,
                                 @RequestParam(required = false) String result,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ZyjdcRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                roleId, result, statusFilter, startDate, endDate);
        startPage();
        List<ZyjdcRecord> records = recordMapper.selectAdminRecords(query);
        // 分页总数必须在转换为普通列表之前读取。
        TableDataInfo table = getDataTable(records);
        table.setRows(enrichService.enrich(records));
        return table;
    }

    /** 删除整条参与记录及其活动游戏参与数据，删除后学生可重新扫码参与。 */
    @DeleteMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('zyjdc:record:remove')")
    public AjaxResult deleteRecord(@PathVariable Long id) {
        gameService.deleteRecord(id);
        return AjaxResult.success();
    }

    /** 统计汇总、角色分布、胜负分布、分析和当前筛选下的分页记录。 */
    @GetMapping("/statistics")
    @PreAuthorize("@ss.hasPermi('zyjdc:statistics:list')")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) Long roleId,
                                 @RequestParam(required = false) String result,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ZyjdcRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                roleId, result, statusFilter, startDate, endDate);
        Map<String, Object> summary = statisticsService.summary(query);
        List<Map<String, Object>> roleStats = statisticsService.roleStats(query);
        List<Map<String, Object>> resultStats = statisticsService.resultStats(query);

        // 统计页的分页记录与“参与记录”列表使用同一查询和补全规则，但不复用带权限注解的方法。
        startPage();
        List<ZyjdcRecord> pageRecords = recordMapper.selectAdminRecords(query);
        TableDataInfo page = getDataTable(pageRecords);
        page.setRows(enrichService.enrich(pageRecords));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("roleStats", roleStats);
        data.put("resultStats", resultStats);
        data.put("analysis", statisticsService.analysis(summary, roleStats, resultStats));
        data.put("total", page.getTotal());
        data.put("records", page.getRows());
        return AjaxResult.success(data);
    }

    /** 按当前筛选条件导出统计汇总、角色分布和学生明细。 */
    @GetMapping("/statistics/excel")
    @PreAuthorize("@ss.hasPermi('zyjdc:statistics:export')")
    public void statisticsExcel(@RequestParam Long schoolId,
                                @RequestParam Long instanceId,
                                @RequestParam(required = false) Long departmentId,
                                @RequestParam(required = false) String major,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) Long roleId,
                                @RequestParam(required = false) String result,
                                @RequestParam(required = false) String statusFilter,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                HttpServletResponse response) throws IOException {
        ZyjdcRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                roleId, result, statusFilter, startDate, endDate);
        Map<String, Object> summary = statisticsService.summary(query);
        List<Map<String, Object>> roleStats = statisticsService.roleStats(query);
        List<Map<String, Object>> resultStats = statisticsService.resultStats(query);
        String analysis = statisticsService.analysis(summary, roleStats, resultStats);
        List<Map<String, Object>> rows = enrichService.enrich(recordMapper.selectAdminRecords(query));

        try (SXSSFWorkbook workbook = excelService.create(summary, roleStats, resultStats, analysis, rows,
                schoolId, instanceId, startDate, endDate)) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("职业角斗场学生数据_" + schoolId + "_" + instanceId + ".xlsx",
                                StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally {
                workbook.dispose();
            }
        }
    }

    /** 统一校验筛选条件，列表、统计与导出使用同一口径。 */
    private ZyjdcRecordQuery filter(Long instanceId, Long schoolId, Long departmentId, String major, String gender,
                                    String studentNo, Long roleId, String result, String statusFilter,
                                    LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String resultValue = trimToNull(result);
        if (resultValue != null && !"win".equals(resultValue) && !"lose".equals(resultValue) && !"draw".equals(resultValue)) {
            throw new ServiceException("对局结果筛选值无效");
        }
        String status = trimToNull(statusFilter);
        if (status != null && !"participating".equals(status) && !"finished".equals(status)) {
            throw new ServiceException("参与状态筛选值无效");
        }
        ZyjdcRecordQuery query = new ZyjdcRecordQuery();
        query.setInstanceId(instanceId);
        query.setSchoolId(schoolId);
        query.setDepartmentId(departmentId);
        query.setMajor(trimToNull(major));
        query.setGenderValue(trimToNull(gender) == null ? null : ZyjdcRecordEnrichService.genderText(gender));
        query.setStudentNo(trimToNull(studentNo));
        query.setRoleId(roleId);
        query.setResult(resultValue);
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
