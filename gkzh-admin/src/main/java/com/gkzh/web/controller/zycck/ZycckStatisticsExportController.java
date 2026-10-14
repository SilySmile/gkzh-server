package com.gkzh.web.controller.zycck;

import com.gkzh.app.service.ZycckStatisticsExcelService;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** 统计分析学生数据导出，只接受学校和活动范围，不使用列表筛选及分页参数。 */
@RestController
@RequestMapping("/zycck/admin/statistics")
public class ZycckStatisticsExportController {
    /** 流式 Excel 生成服务。 */
    private final ZycckStatisticsExcelService service;
    public ZycckStatisticsExportController(ZycckStatisticsExcelService service) { this.service = service; }

    /** 下载所选学校活动的全部猜猜看学生记录；生成失败时不输出伪 Excel 文件。 */
    @PreAuthorize("@ss.hasPermi('zycck:statistics:export')")
    @GetMapping("/excel")
    public void excel(@RequestParam Long schoolId, @RequestParam Long instanceId, HttpServletResponse response) throws IOException {
        try (SXSSFWorkbook workbook = service.create(schoolId, instanceId)) {
            try {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", ContentDisposition.attachment()
                        .filename("职业猜猜看学生数据_" + schoolId + "_" + instanceId + ".xlsx", StandardCharsets.UTF_8).build().toString());
                workbook.write(response.getOutputStream());
            } finally { workbook.dispose(); }
        }
    }
}
