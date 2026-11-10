package com.gkzh.web.controller.zytj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.app.service.ZytjRecordEnrichService;
import com.gkzh.app.service.ZytjStatisticsExcelService;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zytj.domain.ZytjItem;
import com.gkzh.zytj.domain.ZytjRecord;
import com.gkzh.zytj.domain.ZytjSetting;
import com.gkzh.zytj.domain.ZytjType;
import com.gkzh.zytj.domain.dto.ZytjRecordQuery;
import com.gkzh.zytj.mapper.ZytjRecordMapper;
import com.gkzh.zytj.service.ZytjConfigService;
import com.gkzh.zytj.service.ZytjStatisticsService;
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
 * 职愿探究 Web 管理接口。
 * 配置维护 8 个橱窗物品、16 型人格对照与规则参数；参与记录和统计按活动、人格类型、状态、
 * 学生属性与参与日期筛选，记录可整条删除便于测试，统计数据可按当前筛选条件导出 Excel。
 */
@RestController
@RequestMapping("/zytj/admin")
public class ZytjAdminController extends BaseController {

    private final ZytjConfigService configService;
    private final ZytjRecordMapper recordMapper;
    private final IActivityWeekService activityWeekService;
    private final ZytjRecordEnrichService enrichService;
    private final ZytjStatisticsService statisticsService;
    private final ZytjStatisticsExcelService excelService;

    public ZytjAdminController(ZytjConfigService configService, ZytjRecordMapper recordMapper,
                                IActivityWeekService activityWeekService, ZytjRecordEnrichService enrichService,
                                ZytjStatisticsService statisticsService, ZytjStatisticsExcelService excelService) {
        this.configService = configService;
        this.recordMapper = recordMapper;
        this.activityWeekService = activityWeekService;
        this.enrichService = enrichService;
        this.statisticsService = statisticsService;
        this.excelService = excelService;
    }

    /** 配置页数据：8 个橱窗物品、16 型人格对照与规则参数。 */
    @GetMapping("/config")
    @PreAuthorize("@ss.hasPermi('zytj:config:list')")
    public AjaxResult config() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", configService.listItems());
        data.put("types", configService.listTypes());
        data.put("setting", configService.getSetting());
        return AjaxResult.success(data);
    }

    /** 保存单个橱窗物品；物品编码即维度字母，创建后不允许修改。 */
    @PutMapping("/items/{itemId}")
    @PreAuthorize("@ss.hasPermi('zytj:config:edit')")
    public AjaxResult saveItem(@PathVariable Long itemId, @RequestBody ZytjItem item) {
        if (item == null) {
            throw new ServiceException("物品数据不能为空");
        }
        item.setItemId(itemId);
        return AjaxResult.success(configService.saveItem(item));
    }

    /** 保存单个人格类型文案；类型代码是记录快照依据，创建后不允许修改。 */
    @PutMapping("/types/{typeId}")
    @PreAuthorize("@ss.hasPermi('zytj:config:edit')")
    public AjaxResult saveType(@PathVariable Long typeId, @RequestBody ZytjType type) {
        if (type == null) {
            throw new ServiceException("人格类型数据不能为空");
        }
        type.setTypeId(typeId);
        return AjaxResult.success(configService.saveType(type));
    }

    /** 保存规则参数：单局限时、是否启用、提交后是否允许重新探究。 */
    @PutMapping("/setting")
    @PreAuthorize("@ss.hasPermi('zytj:config:edit')")
    public AjaxResult saveSetting(@RequestBody ZytjSetting setting) {
        return AjaxResult.success(configService.saveSetting(setting));
    }

    /** 参与记录分页查询。 */
    @GetMapping("/records")
    @PreAuthorize("@ss.hasPermi('zytj:record:list')")
    public TableDataInfo records(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String typeCode,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ZytjRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                typeCode, statusFilter, startDate, endDate);
        startPage();
        List<ZytjRecord> records = recordMapper.selectAdminRecords(query);
        // 分页总数必须在转换为普通列表之前读取。
        TableDataInfo table = getDataTable(records);
        table.setRows(enrichService.enrich(records));
        return table;
    }

    /** 单条记录详情：基础信息 + 人格类型与三段文案快照。 */
    @GetMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('zytj:record:list')")
    public AjaxResult record(@PathVariable Long id) {
        ZytjRecord record = recordMapper.selectById(id);
        if (record == null || !"zytj".equals(record.getGameType())) {
            return AjaxResult.error("参与记录不存在");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        List<Map<String, Object>> rows = enrichService.enrich(Collections.singletonList(record));
        data.put("record", rows.isEmpty() ? null : rows.get(0));
        data.put("type", typeView(record));
        return AjaxResult.success(data);
    }

    /** 统计汇总、16 型分布、维度倾向、院系分布与当前筛选下的分页记录。 */
    @GetMapping("/statistics")
    @PreAuthorize("@ss.hasPermi('zytj:statistics:list')")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String typeCode,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ZytjRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                typeCode, statusFilter, startDate, endDate);
        Map<String, Object> summary = statisticsService.summary(query);
        long finished = number(summary.get("finishedCount"));
        long total = number(summary.get("participantCount"));

        TableDataInfo page = records(instanceId, schoolId, departmentId, major, gender, studentNo,
                typeCode, statusFilter, startDate, endDate);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("typeStats", statisticsService.typeStats(query, finished));
        result.put("dimensionStats", statisticsService.dimensionStats(query, finished));
        result.put("departmentStats", statisticsService.departmentStats(query, total));
        result.put("total", page.getTotal());
        result.put("records", page.getRows());
        return AjaxResult.success(result);
    }

    /** 按当前筛选条件导出统计汇总、型别分布、维度倾向、院系分布和学生明细。 */
    @GetMapping("/statistics/excel")
    @PreAuthorize("@ss.hasPermi('zytj:statistics:export')")
    public void statisticsExcel(@RequestParam Long schoolId,
                                @RequestParam Long instanceId,
                                @RequestParam(required = false) Long departmentId,
                                @RequestParam(required = false) String major,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) String typeCode,
                                @RequestParam(required = false) String statusFilter,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                HttpServletResponse response) throws IOException {
        try (SXSSFWorkbook workbook = excelService.create(schoolId, instanceId, departmentId, major, gender,
                studentNo, typeCode, statusFilter, startDate, endDate)) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("职愿探究学生数据_" + schoolId + "_" + instanceId + ".xlsx",
                                StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally {
                workbook.dispose();
            }
        }
    }

    /** 删除整条参与记录及其活动游戏参与数据，删除后学生可重新扫码参与。 */
    @DeleteMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('zytj:record:remove')")
    @Transactional
    public AjaxResult deleteRecord(@PathVariable Long id) {
        ZytjRecord record = recordMapper.selectOne(new QueryWrapper<ZytjRecord>()
                .eq("record_id", id).last("FOR UPDATE"));
        if (record == null || !"zytj".equals(record.getGameType())) {
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

    /** 单条记录的人格类型快照；未提交时返回 null。 */
    private Map<String, Object> typeView(ZytjRecord record) {
        if (record.getTypeCode() == null) {
            return null;
        }
        Map<String, Object> type = new LinkedHashMap<>();
        type.put("typeCode", record.getTypeCode());
        type.put("feature", record.getFeature());
        type.put("careerField", record.getCareerField());
        type.put("typicalCareers", record.getTypicalCareers());
        type.put("itemCodes", record.getItemCodes());
        type.put("isTimeout", "1".equals(record.getIsTimeout()));
        return type;
    }

    /** 统一校验筛选条件，列表、统计与导出使用同一口径。 */
    private ZytjRecordQuery filter(Long instanceId, Long schoolId, Long departmentId, String major, String gender,
                                    String studentNo, String typeCode, String statusFilter,
                                    LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String status = trimToNull(statusFilter);
        if (status != null && !"finished".equals(status) && !"inProgress".equals(status)) {
            throw new ServiceException("参与状态筛选值无效");
        }
        ZytjRecordQuery query = new ZytjRecordQuery();
        query.setInstanceId(instanceId);
        query.setSchoolId(schoolId);
        query.setDepartmentId(departmentId);
        query.setMajor(trimToNull(major));
        query.setGenderValue(trimToNull(gender) == null ? null : ZytjRecordEnrichService.genderText(gender));
        query.setStudentNo(trimToNull(studentNo));
        query.setTypeCode(trimToNull(typeCode) == null ? null : typeCode.trim().toUpperCase());
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
