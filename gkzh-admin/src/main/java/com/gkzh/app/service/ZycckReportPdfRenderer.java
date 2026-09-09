package com.gkzh.app.service;

import com.gkzh.zycck.domain.ZycckCareerQuestion;
import com.gkzh.zycck.domain.ZycckRecord;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 按页绘制，先换行再分页；中文随页面嵌入，不依赖阅读器字体。 */
public final class ZycckReportPdfRenderer {
    // 成品按 76mm × 130mm 热敏/标签纸输出（PDF 点单位）。
    private static final PDRectangle REPORT_PAPER = new PDRectangle(76f / 25.4f * 72f, 130f / 25.4f * 72f);
    private static final int WIDTH = 900, HEIGHT = 1539, PAD = 54, BOTTOM = 1450;
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

    public static byte[] render(ZycckRecord record, List<ZycckCareerQuestion> careers, Map<Long,String> categoryNames) throws IOException {
        return render(record, careers, careers, categoryNames);
    }
    public static byte[] render(ZycckRecord record, List<ZycckCareerQuestion> careers, List<ZycckCareerQuestion> further, Map<Long,String> categoryNames) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ZycckReportPdfRenderer writer = new ZycckReportPdfRenderer(document);
            try {
                writer.newPage();
                writer.text("我的未来职业探索报告", 44, true);
                writer.text("今天了解的职业：" + careers.size() + " 个", 30, true);
                writer.pie(further, categoryNames);
                if (careers.isEmpty()) {
                    writer.text("本次没有记录到职业信息。", 28, false);
                }
                for (int i = 0; i < careers.size(); i++) {
                    ZycckCareerQuestion career = careers.get(i);
                    writer.careerLine(i + 1, career);
                }
                writer.flushPage();
                document.getDocumentInformation().setTitle("我的未来职业探索报告");
                document.save(out);
                return out.toByteArray();
            } finally { if (writer.graphics != null) writer.graphics.dispose(); }
        }
    }
    /** 兼容旧调用方；正式报告由服务层传入职业大类名称。 */
    public static byte[] render(ZycckRecord record, List<ZycckCareerQuestion> careers) throws IOException {
        return render(record, careers, java.util.Collections.emptyMap());
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
        PDPage page = new PDPage(REPORT_PAPER); document.addPage(page);
        try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
            stream.drawImage(LosslessFactory.createFromImage(document, image), 0, 0, REPORT_PAPER.getWidth(), REPORT_PAPER.getHeight());
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
    private void pie(List<ZycckCareerQuestion> careers, Map<Long,String> names) throws IOException {
        if (careers.isEmpty()) return;
        Map<String,Integer> counts = new java.util.LinkedHashMap<>();
        for (ZycckCareerQuestion c : careers) { String n = names.get(c.getCategoryId()); counts.put(n == null ? "其他" : n, counts.getOrDefault(n == null ? "其他" : n, 0) + 1); }
        ensure(430); text("职业大类比例", 27, true);
        int cx = WIDTH / 2, cy = y + 155, radius = 125; double start = -Math.PI / 2;
        Color[] colors = {new Color(78,141,247), new Color(82,183,136), new Color(246,173,85), new Color(231,111,81), new Color(155,135,245)}; int index = 0;
        for (Map.Entry<String,Integer> e : counts.entrySet()) { double end = start + Math.PI * 2 * e.getValue() / careers.size(); graphics.setColor(colors[index++ % colors.length]); graphics.fillArc(cx-radius, cy-radius, radius*2, radius*2, (int)Math.toDegrees(-end), (int)Math.toDegrees(end-start)); start = end; }
        y = cy + radius + 36; index = 0; for (Map.Entry<String,Integer> e : counts.entrySet()) { graphics.setColor(colors[index % colors.length]); graphics.fillOval(PAD, y-16, 16, 16); graphics.setColor(new Color(75,85,99)); graphics.setFont(font.deriveFont(20f)); graphics.drawString(e.getKey()+" "+Math.round(e.getValue()*100f/careers.size())+"%", PAD+24, y); y += 30; index++; }
    }
    private void careerLine(int index, ZycckCareerQuestion career) throws IOException {
        ensure(38); graphics.setFont(font.deriveFont(Font.BOLD, 20f)); graphics.setColor(new Color(26,44,74)); String line = index + ". " + value(career.getCareerName()) + "：" + value(career.getOneLineIntro()); while (graphics.getFontMetrics().stringWidth(line) > WIDTH - PAD * 2) line = line.substring(0, Math.max(1, line.length() - 1)); graphics.drawString(line, PAD, y); y += 34;
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
