package com.gkzh.web.controller.sszctop;

import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.app.service.SszctopStatisticsExcelService;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.sszctop.domain.dto.SszctopStatisticsQuery;
import com.gkzh.sszctop.service.SszctopStatisticsService;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
 * 谁是职场TOP Web 统计接口。
 * 统计页和 Excel 导出共用同一筛选口径；Excel 必须选择学校和活动，与其它游戏保持一致。
 */
@RestController
@RequestMapping("/sszctop/admin")
public class SszctopStatisticsController extends BaseController {

    private final SszctopStatisticsService statisticsService;
    private final SszctopStatisticsExcelService excelService;
    private final IActivityWeekService weekService;
    private final GkzhSchoolMapper schoolMapper;
    private final GkzhSchoolDepartmentMapper departmentMapper;

    public SszctopStatisticsController(SszctopStatisticsService statisticsService,
                                       SszctopStatisticsExcelService excelService,
                                       IActivityWeekService weekService,
                                       GkzhSchoolMapper schoolMapper,
                                       GkzhSchoolDepartmentMapper departmentMapper) {
        this.statisticsService = statisticsService;
        this.excelService = excelService;
        this.weekService = weekService;
        this.schoolMapper = schoolMapper;
        this.departmentMapper = departmentMapper;
    }

    /** 统计汇总、七维度完成情况、结果分布、分析和当前筛选下的分页明细。 */
    @GetMapping("/statistics")
    @PreAuthorize("@ss.hasPermi('sszctop:statistics:list')")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId,
                                 @RequestParam(required = false) Long schoolId,
                                 @RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) String major,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String result,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        SszctopStatisticsQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                result, startDate, endDate);
        return AjaxResult.success(statisticsService.statistics(query, pageNum, pageSize));
    }

    /** 按当前筛选条件导出统计汇总、维度统计和学生明细。 */
    @GetMapping("/statistics/excel")
    @PreAuthorize("@ss.hasPermi('sszctop:statistics:export')")
    public void statisticsExcel(@RequestParam Long schoolId,
                                @RequestParam Long instanceId,
                                @RequestParam(required = false) Long departmentId,
                                @RequestParam(required = false) String major,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) String result,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                HttpServletResponse response) throws IOException {
        SszctopStatisticsQuery query = filter(instanceId, schoolId, departmentId, major, gender, studentNo,
                result, startDate, endDate);
        List<Map<String, Object>> rows = statisticsService.rows(query);
        Map<String, Object> summary = statisticsService.summary(rows);
        List<Map<String, Object>> dimensionStats = statisticsService.dimensionStats(rows);
        List<Map<String, Object>> resultStats = statisticsService.resultStats(rows);

        try (SXSSFWorkbook workbook = excelService.create(summary, dimensionStats, resultStats,
                statisticsService.analysis(summary, dimensionStats), rows,
                criteria(schoolId, instanceId, departmentId, major, gender, studentNo, result, startDate, endDate))) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("谁是职场TOP统计_" + schoolId + "_" + instanceId + ".xlsx",
                                StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally {
                workbook.dispose();
            }
        }
    }

    /** 导出说明中的筛选条件，使用中文名称而不是编号。 */
    private Map<String, String> criteria(Long schoolId, Long instanceId, Long departmentId, String major,
                                         String gender, String studentNo, String result,
                                         LocalDate startDate, LocalDate endDate) {
        Map<String, String> criteria = new LinkedHashMap<>();
        GkzhSchool school = schoolMapper.selectGkzhSchoolBySchoolId(schoolId);
        criteria.put("学校", school == null ? String.valueOf(schoolId) : school.getTitle());
        GkzhActivityWeekInstance instance = weekService.getInstance(instanceId);
        criteria.put("活动", instance == null ? String.valueOf(instanceId) : instance.getTitle());
        if (departmentId != null) {
            GkzhSchoolDepartment department = departmentMapper.selectDepartmentById(departmentId);
            criteria.put("院系", department == null ? String.valueOf(departmentId) : department.getTitle());
        }
        criteria.put("专业", major);
        criteria.put("学号", studentNo);
        criteria.put("性别", gender);
        criteria.put("结果", "passed".equals(result) ? "挑战成功" : ("failed".equals(result) ? "本局未通过" : result));
        criteria.put("完成日期", dateRange(startDate, endDate));
        return criteria;
    }

    private String dateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }
        return (startDate == null ? "不限" : startDate.toString()) + " 至 "
                + (endDate == null ? "不限" : endDate.toString());
    }

    /** 统计与导出统一校验筛选条件。 */
    private SszctopStatisticsQuery filter(Long instanceId, Long schoolId, Long departmentId, String major,
                                          String gender, String studentNo, String result,
                                          LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String resultValue = trimToNull(result);
        if (resultValue != null && !"passed".equals(resultValue) && !"failed".equals(resultValue)) {
            throw new ServiceException("结果筛选值无效");
        }
        SszctopStatisticsQuery query = new SszctopStatisticsQuery();
        query.setInstanceId(instanceId);
        query.setSchoolId(schoolId);
        query.setDepartmentId(departmentId);
        query.setMajor(trimToNull(major));
        query.setGenderValue(trimToNull(gender));
        query.setStudentNo(trimToNull(studentNo));
        query.setResult(resultValue);
        query.setStartDate(startDate);
        // 完成日期以报告生成时间为准，结束日次日零点之前均包含。
        query.setEndDate(endDate == null ? null : endDate.plusDays(1));
        return query;
    }

    private static String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
