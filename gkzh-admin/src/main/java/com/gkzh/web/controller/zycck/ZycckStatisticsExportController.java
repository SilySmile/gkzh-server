package com.gkzh.web.controller.zycck;

import com.gkzh.app.service.ZycckStatisticsExcelService;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/** 统计分析学生数据导出：使用当前筛选条件，不使用列表分页参数。 */
@RestController
@RequestMapping("/zycck/admin/statistics")
public class ZycckStatisticsExportController {
    /** 流式 Excel 生成服务。 */
    private final ZycckStatisticsExcelService service;
    public ZycckStatisticsExportController(ZycckStatisticsExcelService service) { this.service = service; }

    /** 下载所选学校活动内符合页面条件的全部猜猜看学生记录；生成失败时不输出伪 Excel 文件。 */
    @PreAuthorize("@ss.hasPermi('zycck:statistics:export')")
    @GetMapping("/excel")
    public void excel(@RequestParam Long schoolId, @RequestParam Long instanceId,
            @RequestParam(required = false) Long departmentId, @RequestParam(required = false) String major,
            @RequestParam(required = false) String gender, @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        try (SXSSFWorkbook workbook = service.create(schoolId, instanceId, departmentId, major, gender, studentNo, statusFilter,
                startDate, endDate)) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("职业猜猜看学生数据_" + schoolId + "_" + instanceId + ".xlsx", StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally { workbook.dispose(); }
        }
    }

    /** 下载同校同活动按系、专业去重后的参与人数汇总。 */
    @PreAuthorize("@ss.hasPermi('zycck:statistics:export')")
    @GetMapping("/summary-excel")
    public void summaryExcel(@RequestParam Long schoolId, @RequestParam Long instanceId,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        try (SXSSFWorkbook workbook = service.createParticipationSummary(schoolId, instanceId, statusFilter, startDate, endDate)) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("职业猜猜看系专业参与人数_" + schoolId + "_" + instanceId + ".xlsx", StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally { workbook.dispose(); }
        }
    }
}
