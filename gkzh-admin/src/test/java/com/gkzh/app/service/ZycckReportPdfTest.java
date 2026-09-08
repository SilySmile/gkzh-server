package com.gkzh.app.service;

import com.gkzh.app.controller.common.ReportCachePublicController;
import com.gkzh.common.config.GkzhConfig;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zycck.domain.*;
import com.gkzh.zycck.mapper.*;
import com.gkzh.zycck.service.ZycckRecordService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ZycckReportPdfTest {
    @TempDir Path temp;

    private ZycckCareerQuestion career(long id) {
        ZycckCareerQuestion career = new ZycckCareerQuestion();
        career.setCareerQuestionId(id); career.setCareerName("短视频编导");
        career.setOneLineIntro("设计短视频内容和拍摄流程");
        career.setMainWork("负责短视频的主题策划、脚本设计、拍摄方案制定，并和团队一起完成高质量的短视频作品。");
        career.setDayExample("选题策划｜找到有趣的内容主题\n脚本设计｜写出视频的故事和台词\n拍摄安排｜确定拍摄方式和流程\n后期跟进｜与剪辑、运营沟通优化");
        career.setWhyExists("短视频平台快速发展，需要专业的人创作更有吸引力的内容。");
        return career;
    }

    @Test void realContentAndPaginationIncludingEmptySelection() throws Exception {
        Path output = Paths.get("target/report-pdf-test"); Files.createDirectories(output);
        ZycckRecord record = new ZycckRecord(); record.setFinishTime(new Date(1788825600000L));
        byte[] six = ZycckReportPdfRenderer.render(record, Arrays.asList(career(1), career(2), career(3), career(4), career(5), career(6)));
        Files.write(output.resolve("six-careers.pdf"), six);
        try (PDDocument pdf = PDDocument.load(six)) {
            assertEquals(6, pdf.getNumberOfPages());
            PDFRenderer renderer = new PDFRenderer(pdf);
            for (int i = 0; i < pdf.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 96);
                int ink = 0;
                for (int y = 40; y < image.getHeight() - 90; y++) for (int x = 20; x < image.getWidth() - 20; x++) {
                    if ((image.getRGB(x, y) & 0xFFFFFF) < 0x999999) ink++;
                }
                assertTrue(ink > 4000, "报告正文不能是空白页");
                if (i == 0 || i == 5) ImageIO.write(image, "png", output.resolve("page-" + (i + 1) + ".png").toFile());
            }
        }
        byte[] empty = ZycckReportPdfRenderer.render(record, Collections.emptyList());
        try (PDDocument pdf = PDDocument.load(empty)) {
            assertEquals(1, pdf.getNumberOfPages());
            assertTrue(empty.length > 5000);
            ImageIO.write(new PDFRenderer(pdf).renderImageWithDPI(0, 96), "png", output.resolve("empty.png").toFile());
        }
        ZycckCareerQuestion longCareer = career(1);
        longCareer.setDayExample("超长日常描述｜" + "需要与不同团队合作完成职业任务。".repeat(150));
        try (PDDocument pdf = PDDocument.load(ZycckReportPdfRenderer.render(record, Collections.singletonList(longCareer)))) {
            assertTrue(pdf.getNumberOfPages() >= 3, "长文本必须自动分页");
            ImageIO.write(new PDFRenderer(pdf).renderImageWithDPI(pdf.getNumberOfPages() - 1, 96), "png", output.resolve("long-last.png").toFile());
        }
    }

    @Test void checksOwnershipBeforeFetchingCareerData() {
        ZycckRecordService records = mock(ZycckRecordService.class);
        ZycckCareerQuestionMapper careers = mock(ZycckCareerQuestionMapper.class);
        when(records.get(10L, 20L)).thenThrow(new ServiceException("记录不属于当前用户"));
        ZycckReportPdfService service = new ZycckReportPdfService(records, mock(ZycckRecordMapper.class), careers);
        assertThrows(ServiceException.class, () -> service.create(10L, 20L));
        verifyNoInteractions(careers);
    }

    @Test void onlySelectedCareersAreLoadedAndMissingContentIsNotSilentlyLost() throws Exception {
        ZycckRecordService records = mock(ZycckRecordService.class);
        ZycckCareerQuestionMapper careers = mock(ZycckCareerQuestionMapper.class);
        ZycckRecord record = new ZycckRecord(); record.setExplorationCareerIds("[2,1]");
        when(records.get(10L, 20L)).thenReturn(record);
        when(careers.selectBatchIds(Arrays.asList(2L, 1L))).thenReturn(Arrays.asList(career(1), career(2)));
        ZycckReportPdfService service = new ZycckReportPdfService(records, mock(ZycckRecordMapper.class), careers);
        try (PDDocument pdf = PDDocument.load(service.create(10L, 20L))) { assertEquals(2, pdf.getNumberOfPages()); }
        verify(careers).selectBatchIds(Arrays.asList(2L, 1L));
        when(careers.selectBatchIds(Arrays.asList(2L, 1L))).thenReturn(Collections.singletonList(career(1)));
        assertThrows(ServiceException.class, () -> service.create(10L, 20L));
    }

    @Test void publicApiDownloadsZipInsteadOfHtmlAndRejectsInvalidOrExpiredLinks() throws Exception {
        String previous = GkzhConfig.getProfile();
        GkzhConfig config = new GkzhConfig(); config.setProfile(temp.toString());
        try {
            Path dir = Files.createDirectories(temp.resolve("report-cache"));
            String token = "1234567890abcdef1234567890abcdef";
            Path archive = dir.resolve(token + ".zip");
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive))) {
                zip.putNextEntry(new ZipEntry("职业探索/未来职业猜猜看/个人报告.pdf"));
                zip.write(ZycckReportPdfRenderer.render(new ZycckRecord(), Collections.singletonList(career(1)))); zip.closeEntry();
            }
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new ReportCachePublicController()).build();
            byte[] body = mvc.perform(get("/api/common/report-cache/" + token + ".zip"))
                    .andExpect(status().isOk()).andExpect(content().contentType("application/octet-stream"))
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andReturn().getResponse().getContentAsByteArray();
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(body))) {
                assertTrue(zip.getNextEntry().getName().endsWith("个人报告.pdf"));
                try (PDDocument pdf = PDDocument.load(zip.readAllBytes())) { assertTrue(pdf.getNumberOfPages() > 0); }
            }
            mvc.perform(get("/api/common/report-cache/bad.zip")).andExpect(status().isBadRequest());
            mvc.perform(get("/api/common/report-cache/00000000000000000000000000000000.zip")).andExpect(status().isNotFound());
            Files.setLastModifiedTime(archive, FileTime.fromMillis(System.currentTimeMillis() - 25 * 3600000L));
            mvc.perform(get("/api/common/report-cache/" + token + ".zip")).andExpect(status().isGone());
        } finally { config.setProfile(previous); }
    }
}
