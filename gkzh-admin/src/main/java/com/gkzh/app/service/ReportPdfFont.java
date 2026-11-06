package com.gkzh.app.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.File;
import java.io.IOException;

/** Registers an installed CJK font so generated report text remains visible and selectable. */
public final class ReportPdfFont {
    private static final String[] CANDIDATES = {
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJKsc-Regular.otf",
            "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
            "C:/Windows/Fonts/simsun.ttc",
            "C:/Windows/Fonts/simsunb.ttf"
    };

    private ReportPdfFont() { }

    public static void register(PdfRendererBuilder builder) throws IOException {
        for (String candidate : CANDIDATES) {
            File font = new File(candidate);
            if (font.isFile() && font.canRead()) {
                builder.useFont(font, "GKZH");
                return;
            }
        }
        throw new IOException("服务器缺少中文字体，无法生成可读的个人报告 PDF");
    }
}
