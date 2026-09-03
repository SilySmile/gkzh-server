package com.gkzh.app.controller.zycck;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.service.ZycckRecordService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/zycck")
public class ZycckAppController extends FrontBaseController {
    private final ZycckRecordService recordService;
    public ZycckAppController(ZycckRecordService recordService) { this.recordService = recordService; }

    @PostMapping("/records/enter")
    public AjaxResult enter(@RequestBody Map<String,Object> body) {
        StudentCheckin student = getCurrentStudent();
        return AjaxResult.success(recordService.enter(id(body.get("schoolId")), id(body.get("instanceId")), id(body.get("gameId")), student.getUserId()));
    }

    @PostMapping("/records/{id}/start")
    public AjaxResult start(@PathVariable Long id) { return AjaxResult.success(recordService.start(id, getCurrentStudent().getUserId())); }

    @GetMapping("/records/{id}")
    public AjaxResult record(@PathVariable Long id) { return AjaxResult.success(recordService.get(id, getCurrentStudent().getUserId())); }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam Long recordId) {
        recordService.get(recordId, getCurrentStudent().getUserId());
        byte[] data = "%PDF-1.4\n% zycck report\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=zycck-report.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(data);
    }

    private static Long id(Object value) { return value == null ? null : Long.valueOf(String.valueOf(value)); }
}
