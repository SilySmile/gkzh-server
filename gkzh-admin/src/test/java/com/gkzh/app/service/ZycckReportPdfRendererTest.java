package com.gkzh.app.service;

import com.gkzh.zycck.domain.ZycckCareerQuestion;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ZycckReportPdfRendererTest {

    @Test
    void reportPdfContainsSelectableReportText() throws Exception {
        ZycckCareerQuestion career = new ZycckCareerQuestion();
        career.setCareerName("职业规划师");
        career.setOneLineIntro("帮助个人探索职业方向");

        byte[] bytes = ZycckReportPdfRenderer.render(null, Collections.singletonList(career),
                Collections.singletonList(career), Collections.emptyMap(), "测试学生");
        try (PDDocument document = PDDocument.load(bytes)) {
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("我的未来职业探索清单"));
            assertTrue(text.contains("职业规划师"));
            assertTrue(text.contains("帮助个人探索职业方向"));
        }
    }
}
