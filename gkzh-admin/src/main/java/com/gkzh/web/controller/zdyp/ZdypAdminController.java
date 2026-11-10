package com.gkzh.web.controller.zdyp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.app.service.ZdypRecordEnrichService;
import com.gkzh.app.service.ZdypStatisticsExcelService;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zdyp.domain.ZdypAnswer;
import com.gkzh.zdyp.domain.ZdypAttempt;
import com.gkzh.zdyp.domain.ZdypCard;
import com.gkzh.zdyp.domain.ZdypCareer;
import com.gkzh.zdyp.domain.ZdypRecord;
import com.gkzh.zdyp.domain.ZdypSetting;
import com.gkzh.zdyp.domain.dto.ZdypRecordQuery;
import com.gkzh.zdyp.mapper.ZdypAnswerMapper;
import com.gkzh.zdyp.mapper.ZdypAttemptMapper;
import com.gkzh.zdyp.mapper.ZdypRecordMapper;
import com.gkzh.zdyp.service.ZdypBehavior;
import com.gkzh.zdyp.service.ZdypConfigService;
import com.gkzh.zdyp.service.ZdypStatisticsService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职得一拼 Web 管理接口。
 * 配置维护 10 套岗位、40 张维度卡片与规则参数；参与记录和统计按活动、岗位、行为分型、
 * 状态、学生属性与参与日期筛选，记录可整条删除便于测试，统计数据可按当前筛选条件导出 Excel。
 */
@RestController
@RequestMapping("/zdyp/admin")
public class ZdypAdminController extends BaseController {

    private final ZdypConfigService configService;
    private final ZdypRecordMapper recordMapper;
    private final ZdypAttemptMapper attemptMapper;
    private final ZdypAnswerMapper answerMapper;
    private final IActivityWeekService activityWeekService;
    private final ZdypRecordEnrichService enrichService;
    private final ZdypStatisticsService statisticsService;
    private final ZdypStatisticsExcelService excelService;

    public ZdypAdminController(ZdypConfigService configService, ZdypRecordMapper recordMapper,
                               ZdypAttemptMapper attemptMapper, ZdypAnswerMapper answerMapper,
                               IActivityWeekService activityWeekService, ZdypRecordEnrichService enrichService,
                               ZdypStatisticsService statisticsService, ZdypStatisticsExcelService excelService) {
        this.configService = configService;
        this.recordMapper = recordMapper;
        this.attemptMapper = attemptMapper;
        this.answerMapper = answerMapper;
        this.activityWeekService = activityWeekService;
        this.enrichService = enrichService;
        this.statisticsService = statisticsService;
        this.excelService = excelService;
    }

    /** 配置页数据：10 套岗位、每套四张卡片与规则参数。 */
    @GetMapping("/config")
    @PreAuthorize("@ss.hasPermi('zdyp:config:list')")
    public AjaxResult config() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("careers", configService.careerConfigs());
        data.put("setting", configService.getSetting());
        data.put("slots", slotDefinitions());
        return AjaxResult.success(data);
    }

    /** 保存单个岗位；岗位编码是历史统计依据，创建后不允许修改。 */
    @PutMapping("/careers/{careerId}")
    @PreAuthorize("@ss.hasPermi('zdyp:config:edit')")
    public AjaxResult saveCareer(@PathVariable Long careerId, @RequestBody ZdypCareer career) {
        if (career == null) {
            throw new ServiceException("岗位数据不能为空");
        }
        career.setCareerId(careerId);
        return AjaxResult.success(configService.saveCareer(career));
    }

    /** 保存单张卡片；卡片维度不可改，只允许改正文、排序与启停用。 */
    @PutMapping("/cards/{cardId}")
    @PreAuthorize("@ss.hasPermi('zdyp:config:edit')")
    public AjaxResult saveCard(@PathVariable Long cardId, @RequestBody ZdypCard card) {
        if (card == null) {
            throw new ServiceException("卡片数据不能为空");
        }
        card.setCardId(cardId);
        return AjaxResult.success(configService.saveCard(card));
    }

    /** 保存规则参数（限时与启用开关）。 */
    @PutMapping("/setting")
    @PreAuthorize("@ss.hasPermi('zdyp:config:edit')")
    public AjaxResult saveSetting(@RequestBody ZdypSetting setting) {
        return AjaxResult.success(configService.saveSetting(setting));
    }

    /** 参与记录分页查询。 */
    @GetMapping("/records")
    @PreAuthorize("@ss.hasPermi('zdyp:record:list')")
    public TableDataInfo records(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String careerCode,
                                 @RequestParam(required = false) String behaviorType,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ZdypRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                careerCode, behaviorType, statusFilter, startDate, endDate);
        startPage();
        List<ZdypRecord> records = recordMapper.selectAdminRecords(query);
        // 分页总数必须在转换为普通列表之前读取。
        TableDataInfo table = getDataTable(records);
        table.setRows(enrichService.enrich(records));
        return table;
    }

    /** 单条记录详情：基础信息 + 每次尝试的四个卡槽放置明细。 */
    @GetMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('zdyp:record:list')")
    public AjaxResult record(@PathVariable Long id) {
        ZdypRecord record = recordMapper.selectById(id);
        if (record == null || !"zdyp".equals(record.getGameType())) {
            return AjaxResult.error("参与记录不存在");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        List<Map<String, Object>> rows = enrichService.enrich(Collections.singletonList(record));
        data.put("record", rows.isEmpty() ? null : rows.get(0));
        data.put("attempts", attemptDetails(record));
        return AjaxResult.success(data);
    }

    /** 统计汇总、行为分型、维度错误、首填偏好、岗位分布、院系分布与当前筛选下的分页记录。 */
    @GetMapping("/statistics")
    @PreAuthorize("@ss.hasPermi('zdyp:statistics:list')")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String careerCode,
                                 @RequestParam(required = false) String behaviorType,
                                 @RequestParam(required = false) String statusFilter,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ZdypRecordQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                careerCode, behaviorType, statusFilter, startDate, endDate);
        Map<String, Object> summary = statisticsService.summary(query);
        long finished = number(summary.get("finishedCount"));
        long started = number(summary.get("startedCount"));
        List<Map<String, Object>> behaviorStats = statisticsService.behaviorStats(query, finished);
        List<Map<String, Object>> slotStats = statisticsService.slotStats(query, started);
        List<Map<String, Object>> firstSlotStats = statisticsService.firstSlotStats(query, started);
        List<Map<String, Object>> careerStats = statisticsService.careerStats(query);
        List<Map<String, Object>> departmentStats = statisticsService.departmentStats(query);

        TableDataInfo page = records(instanceId, schoolId, departmentId, major, gender, studentNo,
                careerCode, behaviorType, statusFilter, startDate, endDate);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("behaviorStats", behaviorStats);
        result.put("slotStats", slotStats);
        result.put("firstSlotStats", firstSlotStats);
        result.put("careerStats", careerStats);
        result.put("departmentStats", departmentStats);
        result.put("analysis", statisticsService.analysis(summary, behaviorStats, slotStats));
        result.put("total", page.getTotal());
        result.put("records", page.getRows());
        return AjaxResult.success(result);
    }

    /** 按当前筛选条件导出包含统计汇总、行为分型、维度错误、岗位分布和学生明细的 Excel。 */
    @GetMapping("/statistics/excel")
    @PreAuthorize("@ss.hasPermi('zdyp:statistics:export')")
    public void statisticsExcel(@RequestParam Long schoolId,
                                @RequestParam Long instanceId,
                                @RequestParam(required = false) Long departmentId,
                                @RequestParam(required = false) String major,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) String careerCode,
                                @RequestParam(required = false) String behaviorType,
                                @RequestParam(required = false) String statusFilter,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                HttpServletResponse response) throws IOException {
        try (SXSSFWorkbook workbook = excelService.create(schoolId, instanceId, departmentId, major, gender,
                studentNo, careerCode, behaviorType, statusFilter, startDate, endDate)) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("职得一拼学生数据_" + schoolId + "_" + instanceId + ".xlsx",
                                StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally {
                workbook.dispose();
            }
        }
    }

    /** 删除整条参与记录及其尝试、作答与活动游戏参与数据，删除后学生可重新扫码参与。 */
    @DeleteMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('zdyp:record:remove')")
    @Transactional
    public AjaxResult deleteRecord(@PathVariable Long id) {
        ZdypRecord record = recordMapper.selectOne(new QueryWrapper<ZdypRecord>()
                .eq("record_id", id).last("FOR UPDATE"));
        if (record == null || !"zdyp".equals(record.getGameType())) {
            return AjaxResult.error("参与记录不存在");
        }
        // 删除范围来自记录本身，不能按用户批量删除其他活动或其他游戏的数据。
        activityWeekService.deleteGameParticipations(record.getInstanceId(), record.getGameId(), record.getUserId());
        answerMapper.delete(new QueryWrapper<ZdypAnswer>().eq("record_id", id));
        attemptMapper.delete(new QueryWrapper<ZdypAttempt>().eq("record_id", id));
        int rows = recordMapper.deleteById(id);
        if (rows != 1) {
            throw new ServiceException("参与记录删除失败，请重试");
        }
        return AjaxResult.success();
    }

    /** 每次尝试及其四个卡槽的放置明细，供后台详情弹层展示。 */
    private List<Map<String, Object>> attemptDetails(ZdypRecord record) {
        List<Map<String, Object>> attempts = new ArrayList<>();
        List<ZdypAttempt> list = attemptMapper.selectList(new QueryWrapper<ZdypAttempt>()
                .eq("record_id", record.getRecordId()).orderByAsc("attempt_no"));
        for (ZdypAttempt attempt : list) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("attemptNo", attempt.getAttemptNo());
            row.put("correctCount", attempt.getCorrectCount());
            row.put("wrongCount", attempt.getWrongCount());
            row.put("filledCount", attempt.getFilledCount());
            row.put("timeout", "1".equals(attempt.getIsTimeout()));
            row.put("pass", "1".equals(attempt.getIsPass()));
            row.put("durationMs", attempt.getDurationMs());
            row.put("submitTime", attempt.getSubmitTime());
            row.put("firstSlotCode", attempt.getFirstSlotCode());
            List<Map<String, Object>> answers = new ArrayList<>();
            for (ZdypAnswer answer : answerMapper.selectList(new QueryWrapper<ZdypAnswer>()
                    .eq("attempt_id", attempt.getAttemptId()).orderByAsc("place_order"))) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("slotCode", answer.getSlotCode());
                item.put("slotName", ZdypConfigService.SLOTS.get(answer.getSlotCode()));
                item.put("cardId", answer.getCardId());
                item.put("cardDimension", answer.getCardDimension());
                item.put("correct", "1".equals(answer.getIsCorrect()));
                item.put("placeOrder", answer.getPlaceOrder());
                item.put("elapsedMs", answer.getElapsedMs());
                answers.add(item);
            }
            row.put("answers", answers);
            attempts.add(row);
        }
        return attempts;
    }

    /** 四个卡槽的编码与中文名，供配置页下拉展示。 */
    private List<Map<String, Object>> slotDefinitions() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, String> slot : ZdypConfigService.SLOTS.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("slotCode", slot.getKey());
            item.put("slotName", slot.getValue());
            rows.add(item);
        }
        return rows;
    }

    /** 统一校验筛选条件，列表、统计与导出使用同一口径。 */
    private ZdypRecordQuery filter(Long instanceId, Long schoolId, Long departmentId, String major, String gender,
                                   String studentNo, String careerCode, String behaviorType, String statusFilter,
                                   LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String status = trimToNull(statusFilter);
        if (status != null && !"finished".equals(status) && !"inProgress".equals(status)) {
            throw new ServiceException("参与状态筛选值无效");
        }
        String behavior = trimToNull(behaviorType);
        if (behavior != null && !ZdypBehavior.BALANCED.equals(behavior) && !ZdypBehavior.ADVANTAGE.equals(behavior)
                && !ZdypBehavior.WEAK.equals(behavior) && !ZdypBehavior.RUSHED.equals(behavior)) {
            throw new ServiceException("行为分型筛选值无效");
        }
        ZdypRecordQuery query = new ZdypRecordQuery();
        query.setInstanceId(instanceId);
        query.setSchoolId(schoolId);
        query.setDepartmentId(departmentId);
        query.setMajor(trimToNull(major));
        query.setGenderValue(trimToNull(gender) == null ? null : ZdypRecordEnrichService.genderText(gender));
        query.setStudentNo(trimToNull(studentNo));
        query.setCareerCode(trimToNull(careerCode));
        query.setBehaviorType(behavior);
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
