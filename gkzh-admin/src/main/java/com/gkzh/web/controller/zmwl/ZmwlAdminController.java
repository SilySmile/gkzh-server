package com.gkzh.web.controller.zmwl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.app.service.ZmwlRecordEnrichService;
import com.gkzh.app.service.ZmwlStatisticsExcelService;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zmwl.domain.ZmwlDirection;
import com.gkzh.zmwl.domain.ZmwlRecord;
import com.gkzh.zmwl.domain.ZmwlSetting;
import com.gkzh.zmwl.domain.dto.ZmwlRecordQuery;
import com.gkzh.zmwl.mapper.ZmwlRecordMapper;
import com.gkzh.zmwl.service.ZmwlConfigService;
import com.gkzh.zmwl.service.ZmwlStatisticsService;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职面未来 Web 管理接口。
 * 配置维护 8 个未来发展方向与规则参数；参与记录和统计按活动、方向、状态、学生属性与参与日期筛选，
 * 记录可整条删除便于测试，统计数据可按当前筛选条件导出 Excel。
 */
@RestController
@RequestMapping("/zmwl/admin")
public class ZmwlAdminController extends BaseController {

    private final ZmwlConfigService configService;
    private final ZmwlRecordMapper recordMapper;
    private final IActivityWeekService activityWeekService;
    private final ZmwlRecordEnrichService enrichService;
    private final ZmwlStatisticsService statisticsService;
    private final ZmwlStatisticsExcelService excelService;

    public ZmwlAdminController(ZmwlConfigService configService, ZmwlRecordMapper recordMapper,
                                IActivityWeekService activityWeekService, ZmwlRecordEnrichService enrichService,
                                ZmwlStatisticsService statisticsService, ZmwlStatisticsExcelService excelService) {
        this.configService = configService;
        this.recordMapper = recordMapper;
        this.activityWeekService = activityWeekService;
        this.enrichService = enrichService;
        this.statisticsService = statisticsService;
        this.excelService = excelService;
    }

    /** 配置页数据：8 个发展方向与规则参数。 */
    @GetMapping("/config")
    @PreAuthorize("@ss.hasPermi('zmwl:config:list')")
    public AjaxResult config() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("directions", configService.listAll());
        data.put("setting", configService.getSetting());
        return AjaxResult.success(data);
    }

    /** 保存单个发展方向；方向编码用于历史统计，创建后不允许修改。 */
    @PutMapping("/directions/{directionId}")
    @PreAuthorize("@ss.hasPermi('zmwl:config:edit')")
    public AjaxResult saveDirection(@PathVariable Long directionId, @RequestBody ZmwlDirection direction) {
        if (direction == null) {
            throw new ServiceException("方向数据不能为空");
        }
        direction.setDirectionId(directionId);
        return AjaxResult.success(configService.saveDirection(direction));
    }

    /** 保存规则参数。 */
    @PutMapping("/setting")
    @PreAuthorize("@ss.hasPermi('zmwl:config:edit')")
    public AjaxResult saveSetting(@RequestBody ZmwlSetting setting) {
        return AjaxResult.success(configService.saveSetting(setting));
    }

    /** 参与记录分页查询。 */
    @GetMapping("/records")
    @PreAuthorize("@ss.hasPermi('zmwl:record:list')")
    public TableDataInfo records(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String directionCode,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ZmwlRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                directionCode, statusFilter, startDate, endDate);
        startPage();
        List<ZmwlRecord> records = recordMapper.selectAdminRecords(query);
        // 分页总数必须在转换为普通列表之前读取。
        TableDataInfo table = getDataTable(records);
        table.setRows(enrichService.enrich(records));
        return table;
    }

    /** 单条记录详情：基础信息 + 所选方向与文案快照。 */
    @GetMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('zmwl:record:list')")
    public AjaxResult record(@PathVariable Long id) {
        ZmwlRecord record = recordMapper.selectById(id);
        if (record == null || !"zmwl".equals(record.getGameType())) {
            return AjaxResult.error("参与记录不存在");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        List<Map<String, Object>> rows = enrichService.enrich(Collections.singletonList(record));
        data.put("record", rows.isEmpty() ? null : rows.get(0));
        data.put("direction", directionView(record));
        return AjaxResult.success(data);
    }

    /** 统计汇总、方向偏好、院系分布与当前筛选下的分页记录。 */
    @GetMapping("/statistics")
    @PreAuthorize("@ss.hasPermi('zmwl:statistics:list')")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String directionCode,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ZmwlRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                directionCode, statusFilter, startDate, endDate);
        Map<String, Object> summary = statisticsService.summary(query);
        long finished = number(summary.get("finishedCount"));
        List<Map<String, Object>> directionStats = statisticsService.directionStats(query, finished);
        List<Map<String, Object>> departmentStats = statisticsService.departmentStats(query);

        TableDataInfo page = records(instanceId, schoolId, departmentId, major, gender, studentNo,
                directionCode, statusFilter, startDate, endDate);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("directionStats", directionStats);
        result.put("departmentStats", departmentStats);
        result.put("analysis", statisticsService.analysis(summary, directionStats));
        result.put("total", page.getTotal());
        result.put("records", page.getRows());
        return AjaxResult.success(result);
    }

    /** 按当前筛选条件导出包含统计汇总、方向分布、院系偏好和学生明细的 Excel。 */
    @GetMapping("/statistics/excel")
    @PreAuthorize("@ss.hasPermi('zmwl:statistics:export')")
    public void statisticsExcel(@RequestParam Long schoolId,
                                @RequestParam Long instanceId,
                                @RequestParam(required = false) Long departmentId,
                                @RequestParam(required = false) String major,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) String directionCode,
                                @RequestParam(required = false) String statusFilter,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                HttpServletResponse response) throws IOException {
        try (SXSSFWorkbook workbook = excelService.create(schoolId, instanceId, departmentId, major, gender,
                studentNo, directionCode, statusFilter, startDate, endDate)) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("职面未来学生数据_" + schoolId + "_" + instanceId + ".xlsx",
                                StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally {
                workbook.dispose();
            }
        }
    }

    /** 删除整条参与记录及其活动游戏参与数据，删除后学生可重新扫码参与。 */
    @DeleteMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('zmwl:record:remove')")
    @Transactional
    public AjaxResult deleteRecord(@PathVariable Long id) {
        ZmwlRecord record = recordMapper.selectOne(new QueryWrapper<ZmwlRecord>()
                .eq("record_id", id).last("FOR UPDATE"));
        if (record == null || !"zmwl".equals(record.getGameType())) {
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

    /** 单条记录的所选方向快照；未确认时返回 null。 */
    private Map<String, Object> directionView(ZmwlRecord record) {
        if (record.getDirectionCode() == null) {
            return null;
        }
        Map<String, Object> direction = new LinkedHashMap<>();
        direction.put("directionCode", record.getDirectionCode());
        direction.put("name", record.getDirectionName());
        direction.put("tagName", record.getTagName());
        direction.put("interpretation", record.getInterpretation());
        direction.put("message", record.getMessage());
        direction.put("iconUrl", record.getIconUrl());
        return direction;
    }

    /** 统一校验筛选条件，列表、统计与导出使用同一口径。 */
    private ZmwlRecordQuery filter(Long instanceId, Long schoolId, Long departmentId, String major, String gender,
                                    String studentNo, String directionCode, String statusFilter,
                                    LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String status = trimToNull(statusFilter);
        if (status != null && !"finished".equals(status) && !"inProgress".equals(status)) {
            throw new ServiceException("参与状态筛选值无效");
        }
        ZmwlRecordQuery query = new ZmwlRecordQuery();
        query.setInstanceId(instanceId);
        query.setSchoolId(schoolId);
        query.setDepartmentId(departmentId);
        query.setMajor(trimToNull(major));
        query.setGenderValue(trimToNull(gender) == null ? null : ZmwlRecordEnrichService.genderText(gender));
        query.setStudentNo(trimToNull(studentNo));
        query.setDirectionCode(trimToNull(directionCode));
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
