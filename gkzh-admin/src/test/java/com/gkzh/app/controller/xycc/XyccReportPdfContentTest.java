package com.gkzh.app.controller.xycc;

import com.gkzh.app.controller.xycc.ReportCacheController;
import com.gkzh.xycc.domain.Career;
import com.gkzh.xycc.domain.HollandCode;
import com.gkzh.xycc.domain.WorkEnv;
import com.gkzh.xycc.service.IHollandCodeService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 心愿橱窗个人报告 PDF 内容回归。
 * 「职业方向」「工作环境偏好」传入的是 Career / WorkEnv 实体，报告中必须只出现标题，
 * 不能出现实体类名、字段名或 toString() 的括号结构。
 */
public class XyccReportPdfContentTest {

    @Test
    void reportShowsCareerTitlesInsteadOfEntityToString() throws Exception {
        IHollandCodeService hollandCodeService = mock(IHollandCodeService.class);
        List<HollandCode> codes = new ArrayList<>();
        codes.add(code("R", "研究型", "喜欢分析与推理"));
        codes.add(code("I", "艺术型", "喜欢创意表达"));
        codes.add(code("A", "社会型", "喜欢与人合作"));
        when(hollandCodeService.listCodes()).thenReturn(codes);

        ReportCacheController controller = new ReportCacheController(null, hollandCodeService, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null);

        Career career = new Career();
        career.setCareerId(11L);
        career.setTitle("人工智能训练师");
        WorkEnv env = new WorkEnv();
        env.setWorkEnvId(21L);
        env.setTitle("室内办公环境");

        Map<String, Object> result = new HashMap<>();
        result.put("code", "RIA");
        result.put("careers", new ArrayList<>(List.of(career)));
        result.put("workEnvs", new ArrayList<>(List.of(env)));

        byte[] pdf = (byte[]) ReflectionTestUtils.invokeMethod(controller, "createPdf", result);
        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "PDF 不能为空");

        String text = extract(pdf);
        assertTrue(text.contains("人工智能训练师"), text);
        assertTrue(text.contains("室内办公环境"), text);
        assertFalse(text.contains("com.gkzh"), text);
        assertFalse(text.contains("Career@"), text);
        assertFalse(text.contains("WorkEnv@"), text);
        assertFalse(text.contains("careerId"), text);
        assertFalse(text.contains("workEnvId"), text);
        assertFalse(text.contains("["), "报告中不应出现 toString 的括号结构: " + text);
    }

    private HollandCode code(String code, String name, String summary) {
        HollandCode item = new HollandCode();
        item.setCode(code);
        item.setName(name);
        item.setSummary(summary);
        item.setLife("生活事例：" + name);
        return item;
    }

    private String extract(byte[] pdf) throws Exception {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdf))) {
            return new PDFTextStripper().getText(document);
        }
    }
}
