package com.gkzh.app.service;

import com.gkzh.zycck.domain.ZycckCareerQuestion;
import com.gkzh.zycck.domain.ZycckRecord;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/** 按页绘制，先换行再分页；中文随页面嵌入，不依赖阅读器字体。 */
public final class ZycckReportPdfRenderer {
    private static final int WIDTH = 1080, HEIGHT = 1528, PAD = 76, BOTTOM = 1400;
    private final PDDocument document;
    private final Font font;
    private BufferedImage image;
    private Graphics2D graphics;
    private int y;

    private ZycckReportPdfRenderer(PDDocument document) throws IOException {
        this.document = document;
        Font available = null;
        for (Font candidate : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
            if (candidate.canDisplayUpTo("未来职业探索报告介绍为什么") == -1) { available = candidate; break; }
        }
        if (available == null) throw new IOException("服务器缺少中文字体，请安装 Noto Sans CJK 后重新生成报告");
        font = available;
    }

    public static byte[] render(ZycckRecord record, List<ZycckCareerQuestion> careers) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ZycckReportPdfRenderer writer = new ZycckReportPdfRenderer(document);
            try {
                writer.newPage();
                writer.text("我的未来职业探索报告", 44, true);
                writer.text("这些是你主动关注、想进一步了解的职业", 28, false);
                writer.text("我想进一步了解：" + careers.size() + " 个职业", 30, true);
                if (record.getFinishTime() != null) writer.text("完成时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(record.getFinishTime()), 25, false);
                if (careers.isEmpty()) {
                    writer.text("本次没有加入进一步了解的职业。", 32, true);
                    writer.text("你已经完成了未来职业探索，本次参与记录已保存。未来还可以继续探索更多可能。", 30, false);
                }
                for (int i = 0; i < careers.size(); i++) {
                    ZycckCareerQuestion career = careers.get(i);
                    if (i > 0) { writer.flushPage(); writer.newPage(); }
                    writer.ensure(160);
                    writer.text((i + 1) + ". " + value(career.getCareerName()), 38, true);
                    writer.text(value(career.getOneLineIntro()), 30, false);
                    writer.section("这个职业主要做什么？", career.getMainWork());
                    writer.ensure(160);
                    writer.text("一天可能做什么？", 32, true);
                    writer.dayItems(career.getDayExample());
                    writer.section("为什么会有这样的职业？", career.getWhyExists());
                }
                writer.flushPage();
                document.getDocumentInformation().setTitle("我的未来职业探索报告");
                document.save(out);
                return out.toByteArray();
            } finally { if (writer.graphics != null) writer.graphics.dispose(); }
        }
    }

    private static String value(String text) { return text == null || text.trim().isEmpty() ? "暂无介绍" : text.trim(); }
    private void newPage() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE); graphics.fillRect(0, 0, WIDTH, HEIGHT); y = 110;
    }
    private void flushPage() throws IOException {
        graphics.setFont(font.deriveFont(24f)); graphics.setColor(new Color(120, 130, 145));
        String footer = "未来职业猜猜看   |   第 " + (document.getNumberOfPages() + 1) + " 页";
        graphics.drawString(footer, (WIDTH - graphics.getFontMetrics().stringWidth(footer)) / 2, HEIGHT - 52);
        graphics.dispose(); graphics = null;
        PDPage page = new PDPage(PDRectangle.A4); document.addPage(page);
        try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
            stream.drawImage(LosslessFactory.createFromImage(document, image), 0, 0, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
        }
    }
    private void ensure(int height) throws IOException { if (y + height > BOTTOM) { flushPage(); newPage(); } }
    private List<String> wrap(String text, int size, int width, boolean bold) {
        graphics.setFont(font.deriveFont(bold ? Font.BOLD : Font.PLAIN, (float) size));
        FontMetrics metrics = graphics.getFontMetrics(); List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\\R", -1)) {
            StringBuilder line = new StringBuilder();
            for (int cp : paragraph.codePoints().toArray()) {
                String next = new String(Character.toChars(cp));
                if (line.length() > 0 && metrics.stringWidth(line.toString() + next) > width) { lines.add(line.toString()); line.setLength(0); }
                line.append(next);
            }
            lines.add(line.toString());
        }
        return lines;
    }
    private void text(String value, int size, boolean bold) throws IOException {
        for (String line : wrap(value, size, WIDTH - PAD * 2, bold)) {
            ensure(size + 22);
            graphics.setFont(font.deriveFont(bold ? Font.BOLD : Font.PLAIN, (float) size));
            graphics.setColor(bold ? new Color(26, 44, 74) : new Color(75, 85, 99));
            graphics.drawString(line, PAD, y); y += size + 18;
        }
        y += 18;
    }
    private void section(String title, String body) throws IOException { ensure(140); text(title, 32, true); text(value(body), 30, false); }
    private void dayItems(String value) throws IOException {
        String[] items = value(value).split("\\R");
        int columnWidth = (WIDTH - PAD * 2) / 4;
        for (int start = 0; start < items.length; start += 4) {
            List<List<String>> cells = new ArrayList<>(); int maxLines = 0;
            for (int i = start; i < Math.min(start + 4, items.length); i++) {
                List<String> lines = wrap(items[i].replace('｜', '\n'), 25, columnWidth - 24, false);
                cells.add(lines); maxLines = Math.max(maxLines, lines.size());
            }
            // 较长的日常描述分段换页，不能超出纸面或丢失后半段。
            for (int offset = 0; offset < maxLines; ) {
                ensure(180);
                int count = Math.min(maxLines - offset, Math.max(1, (BOTTOM - y - 100) / 38));
                for (int column = 0; column < cells.size(); column++) {
                    int x = PAD + column * columnWidth;
                    graphics.setColor(new Color(255, 244, 232)); graphics.fillRoundRect(x + columnWidth / 2 - 30, y - 12, 60, 60, 16, 16);
                    graphics.setFont(font.deriveFont(Font.BOLD, 26f)); graphics.setColor(new Color(197, 109, 27));
                    graphics.drawString(String.valueOf(start + column + 1), x + columnWidth / 2 - 8, y + 28);
                    List<String> lines = cells.get(column);
                    graphics.setFont(font.deriveFont(25f)); graphics.setColor(new Color(75, 85, 99));
                    for (int n = offset; n < Math.min(offset + count, lines.size()); n++) {
                        String line = lines.get(n);
                        graphics.drawString(line, x + (columnWidth - graphics.getFontMetrics().stringWidth(line)) / 2, y + 90 + (n - offset) * 38);
                    }
                }
                y += 110 + count * 38; offset += count;
            }
        }
    }
}
