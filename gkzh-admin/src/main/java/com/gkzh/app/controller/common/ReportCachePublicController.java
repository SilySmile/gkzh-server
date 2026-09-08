package com.gkzh.app.controller.common;

import com.gkzh.common.config.GkzhConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.File;

@RestController
@RequestMapping({"/profile/report-cache", "/api/common/report-cache"})
public class ReportCachePublicController {
    @GetMapping("/{token}.{type}")
    public ResponseEntity<FileSystemResource> get(@PathVariable String token, @PathVariable String type) {
        if (!token.matches("[a-fA-F0-9]{32}")) return ResponseEntity.badRequest().build();
        if (!type.matches("html|pdf|zip")) return ResponseEntity.badRequest().build();
        File file = new File(new File(GkzhConfig.getProfile(), "report-cache"), token + "." + type);
        if (!file.isFile()) return ResponseEntity.notFound().build();
        if (System.currentTimeMillis() - file.lastModified() > 24 * 60 * 60 * 1000L) return ResponseEntity.status(HttpStatus.GONE).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType("pdf".equals(type) ? MediaType.APPLICATION_PDF : ("zip".equals(type) ? MediaType.APPLICATION_OCTET_STREAM : MediaType.TEXT_HTML));
        if (!"html".equals(type)) headers.setContentDisposition(ContentDisposition.attachment().filename("职业兴趣测评报告." + type, java.nio.charset.StandardCharsets.UTF_8).build());
        headers.setCacheControl(CacheControl.noStore());
        headers.setContentLength(file.length());
        return new ResponseEntity<>(new FileSystemResource(file), headers, HttpStatus.OK);
    }
}
