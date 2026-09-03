package com.gkzh.app.controller.xycc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.config.GkzhConfig;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.xycc.service.IPatternComboService;
import com.gkzh.xycc.service.IHollandCodeService;
import com.gkzh.xycc.domain.HollandCode;
import com.gkzh.activity.domain.week.GkzhActivityArea;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.service.IGkzhStudentService;
import com.gkzh.sszctop.domain.SszctopCareer;
import com.gkzh.sszctop.domain.SszctopDimension;
import com.gkzh.sszctop.domain.SszctopDimensionRank;
import com.gkzh.sszctop.domain.SszctopStudentReport;
import com.gkzh.sszctop.mapper.SszctopStudentReportMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.text.SimpleDateFormat;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

/** 生成可在浏览器打开的临时报告文件，默认缓存 24 小时。 */
@RestController
@RequestMapping("/api/xycc/report")
public class ReportCacheController extends FrontBaseController {
    private final IPatternComboService patternComboService;
    private final IHollandCodeService hollandCodeService;
    private final IActivityWeekService activityWeekService;
    private final IGkzhStudentService studentService;
    private final SszctopStudentReportMapper sszctopReportMapper;
    private static final long CACHE_MILLIS = 24 * 60 * 60 * 1000L;

    public ReportCacheController(IPatternComboService patternComboService, IHollandCodeService hollandCodeService,
                                 IActivityWeekService activityWeekService, IGkzhStudentService studentService,
                                 SszctopStudentReportMapper sszctopReportMapper) {
        this.patternComboService = patternComboService;
        this.hollandCodeService = hollandCodeService;
        this.activityWeekService = activityWeekService;
        this.studentService = studentService;
        this.sszctopReportMapper = sszctopReportMapper;
    }

    @PostMapping("/cache")
    public AjaxResult cache(@RequestBody Map<String, Object> body) throws Exception {
        Long activityId = Long.valueOf(String.valueOf(body.get("activityId")));
        Map result = patternComboService.getXyccResult(activityId, getCurrentStudent().getUserId());
        String token = UUID.randomUUID().toString().replace("-", "");
        File dir = new File(GkzhConfig.getProfile(), "report-cache");
        if (!dir.exists()) dir.mkdirs();
        Files.write(new File(dir, token + ".pdf").toPath(), createPdf(result));
        return AjaxResult.success("报告生成成功", "/profile/report-cache/" + token + ".pdf");
    }

    @PostMapping("/cache/all")
    public AjaxResult cacheAll(@RequestBody Map<String, Object> body) throws Exception {
        Long activityId = Long.valueOf(String.valueOf(body.get("activityId")));
        // 不使用前端传入的 sections 作为数据来源，防止不同活动实例的游戏被混入报告包。
        GkzhActivityWeekInstance instance = activityWeekService.getInstance(activityId);
        if (instance == null) return AjaxResult.error("活动不存在");
        Long userId = getCurrentStudent().getUserId();
        GkzhStudent student = studentService.selectGkzhStudentByStudentId(getCurrentStudent().getStuId());
        if (student == null) return AjaxResult.error("未找到学生信息");
        String token = UUID.randomUUID().toString().replace("-", "");
        File dir = new File(GkzhConfig.getProfile(), "report-cache"); if (!dir.exists()) dir.mkdirs();
        File zipFile = new File(dir, token + ".zip");
        int reportCount = 0;
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            // 只取当前活动实例与当前学校的区域，确保生涯/就业活动报告严格隔离。
            for (GkzhActivityArea area : activityWeekService.listAreas(activityId, student.getSchoolId())) {
                for (GkzhActivityGame game : activityWeekService.listGames(area.getAreaId())) {
                    GkzhGameParticipation participation = activityWeekService.getLatestParticipation(game.getGameId(), userId);
                    if (participation == null || !("1".equals(participation.getStatus()) || "2".equals(participation.getStatus()))) continue;
                    byte[] pdf;
                    if ("mind-window".equals(game.getGameType())) {
                        pdf = createPdf(patternComboService.getXyccResult(activityId, userId));
                    } else if ("sszctop".equals(game.getGameType())) {
                        // 职场 TOP 使用结算时保存的个人快照，内容与游戏结束后查看的个人报告保持一致。
                        pdf = createSszctopPdf(activityId, game.getGameId(), userId);
                        if (pdf == null) pdf = createGamePdf(instance, area, game, participation);
                    } else {
                        pdf = createGamePdf(instance, area, game, participation);
                    }
                    String entry = safeName(area.getTitle()) + "/" + safeName(game.getTitle()) + "/" + safeName(game.getTitle()) + "个人报告.pdf";
                    zip.putNextEntry(new ZipEntry(entry));
                    zip.write(pdf);
                    zip.closeEntry();
                    reportCount++;
                }
            }
        }
        if (reportCount == 0) { zipFile.delete(); return AjaxResult.error("当前活动暂无可下载的游戏报告"); }
        return AjaxResult.success("报告压缩包生成成功", "/profile/report-cache/" + token + ".zip");
    }

    /**
     * 除心愿橱窗外，活动中的每个终局游戏均生成独立 PDF，避免下载全部报告时被静默忽略。
     * 具体游戏后续增加专属 PDF 模板时，只需在 cacheAll 的分支中替换此通用报告即可。
     */
    private byte[] createGamePdf(GkzhActivityWeekInstance instance, GkzhActivityArea area,
                                 GkzhActivityGame game, GkzhGameParticipation participation) throws Exception {
        List<String> lines = new java.util.ArrayList<>();
        lines.add("个人游戏报告");
        lines.add("活动：" + text(instance.getTitle(), "当前活动"));
        lines.add("活动类型：" + activityTypeName(instance.getBizType()));
        lines.add("区域：" + text(area.getTitle(), "未分类区域"));
        lines.add("游戏：" + text(game.getTitle(), gameTypeName(game.getGameType())));
        lines.add("游戏类别：" + gameTypeName(game.getGameType()));
        lines.add("游戏状态：" + ("1".equals(participation.getStatus()) ? "已完成" : "未通过"));
        lines.add("完成时间：" + formatDate(participation.getFinishTime()));
        lines.add("说明：本报告记录本次活动中该游戏的个人完成结果。具体游戏的专属报告内容将按其自身规则展示。 ");
        return createTextPdf(lines);
    }

    /**
     * 根据职场 TOP 的个人结算快照生成 PDF。展示字段与 pages/sszctop/report.vue 一致：
     * 维度说明、个人三项排序及职业解释、正确项绿色标记、失败时的正确排序。
     */
    private byte[] createSszctopPdf(Long activityId, Long gameId, Long userId) throws Exception {
        SszctopStudentReport report = sszctopReportMapper.selectOne(new QueryWrapper<SszctopStudentReport>()
                .eq("instance_id", activityId).eq("game_id", gameId).eq("user_id", userId)
                .orderByDesc("report_id").last("limit 1"));
        if (report == null) return null;

        SszctopDimension dimension = parseObject(report.getDimensionSnapshot(), SszctopDimension.class);
        List<SszctopCareer> careers = parseList(report.getCareersSnapshot(), SszctopCareer.class);
        List<Long> selectedIds = parseIds(report.getSharedOrderSnapshot());
        List<Long> standardIds = parseIds(report.getStandardOrderSnapshot());
        List<SszctopDimensionRank> rankDetails = parseRankDetails(report.getReportJson());
        boolean passed = "passed".equals(report.getResult());

        final int width = 1080, pad = 48;
        // 中文字体按保守字数换行，保证每行都留在 PDF 卡片内容区内。
        int height = 500 + textLines(dimension == null ? "" : dimension.getDescription(), 28).size() * 44;
        for (Long careerId : selectedIds) height += 220 + textLines(rankDescription(rankDetails, careerId), 28).size() * 44;
        if (!passed) height += 190 + standardIds.size() * 104;
        height = Math.max(height, 1180);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(245, 247, 251)); g.fillRect(0, 0, width, height);
        int y = 72;
        g.setColor(new Color(36, 86, 166)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 46)); drawCentered(g, "谁是职场TOP", width, y); y += 58;
        g.setColor(passed ? new Color(32, 166, 106) : new Color(217, 83, 79));
        g.setFont(new Font("Microsoft YaHei", Font.BOLD, 30)); drawCentered(g, passed ? "挑战成功" : "本局未通过", width, y); y += 54;

        int dimensionHeight = 230 + textLines(dimension == null ? "暂无维度说明" : dimension.getDescription(), 28).size() * 44;
        drawCard(g, pad, y, width - pad * 2, dimensionHeight, Color.WHITE, new Color(227, 233, 242));
        y += 46;
        g.setColor(new Color(37, 56, 88)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 32)); g.drawString("本次选择的维度", pad + 28, y); y += 54;
        g.setColor(new Color(36, 86, 166)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 40)); g.drawString(dimension == null ? "未记录维度" : text(dimension.getName(), "未记录维度"), pad + 28, y); y += 48;
        g.setColor(new Color(83, 101, 126)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 27)); g.drawString("维度解释", pad + 28, y); y += 38;
        y = drawWrappedText(g, dimension == null ? "暂无维度说明" : dimension.getDescription(), pad + 28, y, 28, 28, new Color(102, 117, 139), 44) + 44;

        g.setColor(new Color(37, 56, 88)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 34)); g.drawString("你选择的职业与个人排序", pad, y); y += 42;
        for (int i = 0; i < selectedIds.size(); i++) {
            Long careerId = selectedIds.get(i); SszctopCareer career = career(careers, careerId);
            boolean correct = i < standardIds.size() && Objects.equals(careerId, standardIds.get(i));
            String description = rankDescription(rankDetails, careerId);
            int cardHeight = 180 + textLines(description, 28).size() * 44;
            Color border = correct ? new Color(53, 183, 122) : new Color(227, 233, 242);
            drawCard(g, pad, y, width - pad * 2, cardHeight, correct ? new Color(241, 251, 246) : Color.WHITE, border);
            g.setColor(correct ? new Color(53, 183, 122) : new Color(36, 86, 166)); g.fillOval(pad + 26, y + 28, 50, 50);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 28)); drawCenteredAt(g, String.valueOf(i + 1), pad + 51, y + 63);
            g.setColor(new Color(43, 58, 82)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 32)); g.drawString(career == null ? "未记录职业" : text(career.getName(), "未记录职业"), pad + 98, y + 63);
            if (correct) { g.setColor(new Color(32, 166, 106)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 24)); g.drawString("排序正确", width - pad - 138, y + 62); }
            g.setColor(new Color(102, 117, 139)); g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 27));
            drawWrappedText(g, description, pad + 28, y + 124, 28, 27, new Color(102, 117, 139), 44);
            y += cardHeight + 34;
        }
        if (!passed) {
            int standardHeight = 140 + standardIds.size() * 104;
            drawCard(g, pad, y, width - pad * 2, standardHeight, new Color(255, 248, 236), new Color(242, 162, 58)); y += 48;
            g.setColor(new Color(37, 56, 88)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 34)); g.drawString("正确排序", pad + 28, y); y += 38;
            g.setColor(new Color(125, 137, 155)); g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 25)); g.drawString("以下为本次选取职业的正确排序：", pad + 28, y); y += 44;
            for (int i = 0; i < standardIds.size(); i++) { SszctopCareer career = career(careers, standardIds.get(i)); g.setColor(new Color(233, 154, 40)); g.fillOval(pad + 28, y - 28, 42, 42); g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 24)); drawCenteredAt(g, String.valueOf(i + 1), pad + 49, y + 2); g.setColor(new Color(43, 58, 82)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 29)); g.drawString(career == null ? "未记录职业" : text(career.getName(), "未记录职业"), pad + 90, y + 1); y += 92; }
        }
        g.dispose();
        return createImagePdf(image);
    }

    private <T> T parseObject(String value, Class<T> type) { try { return JSON.parseObject(value, type); } catch (Exception ignored) { return null; } }
    private <T> List<T> parseList(String value, Class<T> type) { try { List<T> list = JSON.parseArray(value, type); return list == null ? new java.util.ArrayList<>() : list; } catch (Exception ignored) { return new java.util.ArrayList<>(); } }
    private List<Long> parseIds(String value) { List<Long> ids = new java.util.ArrayList<>(); if (value == null || value.trim().isEmpty()) return ids; for (String id : value.split(",")) try { ids.add(Long.valueOf(id)); } catch (NumberFormatException ignored) {} return ids; }
    private List<SszctopDimensionRank> parseRankDetails(String reportJson) { try { JSONObject json = JSON.parseObject(reportJson); List<SszctopDimensionRank> list = JSON.parseArray(JSON.toJSONString(json.get("rankDetails")), SszctopDimensionRank.class); return list == null ? new java.util.ArrayList<>() : list; } catch (Exception ignored) { return new java.util.ArrayList<>(); } }
    private SszctopCareer career(List<SszctopCareer> careers, Long id) { for (SszctopCareer career : careers) if (Objects.equals(career.getCareerId(), id)) return career; return null; }
    private String rankDescription(List<SszctopDimensionRank> ranks, Long careerId) { for (SszctopDimensionRank rank : ranks) if (Objects.equals(rank.getCareerId(), careerId)) return text(rank.getDescription(), "暂无该维度下的职业说明"); return "暂无该维度下的职业说明"; }
    private int drawWrappedText(Graphics2D g, String value, int x, int y, int maxChars, int fontSize, Color color, int lineHeight) { g.setColor(color); g.setFont(new Font("Microsoft YaHei", Font.PLAIN, fontSize)); for (String line : textLines(value, maxChars)) { g.drawString(line, x, y); y += lineHeight; } return y; }
    private void drawCard(Graphics2D g, int x, int y, int width, int height, Color background, Color border) { g.setColor(background); g.fillRoundRect(x, y, width, height, 24, 24); g.setColor(border); g.setStroke(new BasicStroke(3)); g.drawRoundRect(x + 1, y + 1, width - 2, height - 2, 24, 24); }
    private byte[] createImagePdf(BufferedImage image) throws Exception { try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) { PDRectangle pageSize = PDRectangle.A4; float scale = pageSize.getWidth() / image.getWidth(); int slice = (int) (pageSize.getHeight() / scale); for (int top = 0; top < image.getHeight(); top += slice) { BufferedImage part = image.getSubimage(0, top, image.getWidth(), Math.min(slice, image.getHeight() - top)); PDPage page = new PDPage(pageSize); doc.addPage(page); try (PDPageContentStream cs = new PDPageContentStream(doc, page)) { cs.drawImage(LosslessFactory.createFromImage(doc, part), 0, pageSize.getHeight() - part.getHeight() * scale, pageSize.getWidth(), part.getHeight() * scale); } } doc.save(out); return out.toByteArray(); } }

    /** 使用图片分页生成通用 PDF，保证中文字体在移动端下载后可正常显示。 */
    private byte[] createTextPdf(List<String> sections) throws Exception {
        final int width = 1080, height = 1520, pad = 76;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(245, 247, 250)); g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE); g.fillRoundRect(pad, 72, width - pad * 2, height - 144, 30, 30);
        int y = 170;
        for (int i = 0; i < sections.size(); i++) {
            g.setColor(i == 0 ? new Color(26, 44, 74) : new Color(52, 73, 94));
            g.setFont(new Font("Microsoft YaHei", i == 0 ? Font.BOLD : Font.PLAIN, i == 0 ? 44 : 30));
            for (String line : textLines(sections.get(i), 28)) { g.drawString(line, pad + 42, y); y += i == 0 ? 68 : 52; }
            y += i == 0 ? 30 : 14;
        }
        g.dispose();
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDRectangle pageSize = PDRectangle.A4; PDPage page = new PDPage(pageSize); doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(LosslessFactory.createFromImage(doc, image), 0, 0, pageSize.getWidth(), pageSize.getHeight());
            }
            doc.save(out); return out.toByteArray();
        }
    }

    private String activityTypeName(String type) { return "job_week".equals(type) ? "就业活动" : "生涯活动"; }
    private String gameTypeName(String type) {
        Map<String, String> names = new HashMap<>();
        names.put("mind-window", "心愿橱窗"); names.put("zytj", "职愿探究"); names.put("sszctop", "谁是职场TOP");
        names.put("zyxxz", "职业信息站"); names.put("wjyd", "职场危机应对"); names.put("cyzs", "创业知识");
        names.put("survey", "问卷调查"); names.put("lottery", "抽奖"); names.put("check-in", "签到"); names.put("check-out", "签退");
        return names.getOrDefault(type, text(type, "游戏"));
    }
    private String formatDate(java.util.Date date) { return date == null ? "未记录" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date); }
    private String text(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value; }

    @Scheduled(fixedDelay = 60 * 60 * 1000L)
    public void cleanExpired() {
        File dir = new File(GkzhConfig.getProfile(), "report-cache");
        File[] files = dir.listFiles((d, n) -> n.endsWith(".html") || n.endsWith(".pdf") || n.endsWith(".zip"));
        if (files != null) for (File file : files) if (System.currentTimeMillis() - file.lastModified() > CACHE_MILLIS) file.delete();
    }

    private String escape(String value) { if (value == null) return ""; return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    /** 用真实报告数据绘制结果页长图，再分页写入 PDF，避免 HTML 引擎与小程序 CSS 渲染差异。 */
    private byte[] createPdf(Map result) throws Exception {
        int width = 1080, pad = 48, y = 42;
        String code = result.get("code") == null ? "" : String.valueOf(result.get("code"));
        Map<String,HollandCode> details = new HashMap<>();
        for (HollandCode item : hollandCodeService.listCodes()) details.put(item.getCode(), item);
        int height = 250 + code.length() * 330;
        List<String> lines = new java.util.ArrayList<>();
        for (int i=0; i<code.length(); i++) {
            HollandCode item = details.get(String.valueOf(code.charAt(i)));
            if (item != null) { height += 430 + textLines(item.getSummary(), 40).size() * 34 + textLines(item.getLife(), 40).size() * 34; }
        }
        height += 430 + (int)(PDRectangle.A4.getHeight()/PDRectangle.A4.getWidth()*width);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(245,247,250)); g.fillRect(0,0,width,height);
        g.setColor(new Color(26,44,74)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 42)); drawCentered(g,"您的职业兴趣类型为",width,y+44); y += 80;
        int badgeW = 300, badgeH = 220, gap = 18, start = (width - (code.length()*badgeW + (code.length()-1)*gap))/2;
        for (int i=0; i<code.length(); i++) { HollandCode item=details.get(String.valueOf(code.charAt(i))); if(item==null)continue; int x=start+i*(badgeW+gap); Color c=Color.decode(color(String.valueOf(code.charAt(i)))); g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),55)); g.fillRoundRect(x,y,badgeW,badgeH,28,28); g.setColor(c); g.setStroke(new BasicStroke(8)); g.drawRoundRect(x+4,y+4,badgeW-8,badgeH-8,28,28); g.setFont(new Font("Arial",Font.BOLD,62)); drawCenteredAt(g,String.valueOf(code.charAt(i)),x+badgeW/2,y+92); g.setFont(new Font("Microsoft YaHei",Font.PLAIN,28)); drawCenteredAt(g,item.getName(),x+badgeW/2,y+145); }
        y += badgeH + 55; g.setColor(new Color(44,62,80)); g.setFont(new Font("Microsoft YaHei",Font.BOLD,34)); g.drawString("下述是你的结论",pad,y); y += 28;
        for (int i=0; i<code.length(); i++) { HollandCode item=details.get(String.valueOf(code.charAt(i))); if(item==null)continue; Color c=Color.decode(color(String.valueOf(code.charAt(i)))); int cardH=250+textLines(item.getSummary(),40).size()*34+textLines(item.getLife(),40).size()*34; g.setColor(Color.WHITE); g.fillRoundRect(pad,y,width-pad*2,cardH,24,24); g.setColor(c); g.fillRoundRect(pad,y,width-pad*2,12,24,24); g.setColor(new Color(26,44,74)); g.setFont(new Font("Microsoft YaHei",Font.BOLD,30)); g.drawString(icon(String.valueOf(code.charAt(i)))+"  "+item.getName()+"（"+code.charAt(i)+"）",pad+28,y+58); y=drawSection(g,"特点",item.getSummary(),y+88,pad,width,c); y=drawSection(g,"生活事例",item.getLife(),y+18,pad,width,c)+34; }
        int pageSlice=(int)(PDRectangle.A4.getHeight()/PDRectangle.A4.getWidth()*width);
        int comboHeight=430;
        int pageOffset=y%pageSlice;
        if(pageOffset>pageSlice-180){ y += pageSlice-pageOffset+24; }
        g.setColor(Color.WHITE); g.fillRoundRect(pad,y,width-pad*2,comboHeight,24,24); g.setColor(new Color(26,44,74)); g.setFont(new Font("Microsoft YaHei",Font.BOLD,30)); drawCentered(g,"你的职业方向与工作环境偏好",width,y+58); g.setColor(new Color(24,86,209)); g.setFont(new Font("Microsoft YaHei",Font.BOLD,23)); g.drawString("职业方向",pad+28,y+108); y=drawTags(g,result.get("careers"),y+158,pad,width,new Color(238,244,255),new Color(24,86,209)); g.setColor(new Color(39,174,96)); g.drawString("工作环境偏好",pad+28,y+58); drawTags(g,result.get("workEnvs"),y+98,pad,width,new Color(234,250,241),new Color(39,174,96)); g.dispose();
        try (PDDocument doc=new PDDocument(); ByteArrayOutputStream out=new ByteArrayOutputStream()) { PDRectangle pageSize=PDRectangle.A4; float scale=pageSize.getWidth()/width; int slice=(int)(pageSize.getHeight()/scale); for(int top=0;top<height;top+=slice){ BufferedImage part=image.getSubimage(0,top,width,Math.min(slice,height-top)); PDPage page=new PDPage(pageSize); doc.addPage(page); try(PDPageContentStream cs=new PDPageContentStream(doc,page)){ cs.drawImage(LosslessFactory.createFromImage(doc,part),0,pageSize.getHeight()-part.getHeight()*scale,pageSize.getWidth(),part.getHeight()*scale); } } doc.save(out); return out.toByteArray(); }
    }
    private int drawSection(Graphics2D g,String title,String text,int y,int pad,int width,Color c){g.setColor(c);g.setFont(new Font("Microsoft YaHei",Font.BOLD,23));g.drawString(title,pad+28,y);g.setColor(new Color(52,73,94));g.setFont(new Font("Microsoft YaHei",Font.PLAIN,22));int yy=y+36;for(String line:textLines(text,40)){g.drawString(line,pad+28,yy);yy+=34;}return yy;}
    private int drawTags(Graphics2D g,Object items,int y,int pad,int width,Color bg,Color fg){if(items instanceof Iterable){int x=pad+28;g.setFont(new Font("Microsoft YaHei",Font.PLAIN,20));for(Object item:(Iterable)items){String t=String.valueOf(value(item,"title"));int w=g.getFontMetrics().stringWidth(t)+34;if(x+w>width-pad){x=pad+28;y+=44;}g.setColor(bg);g.fillRoundRect(x,y-25,w,34,20,20);g.setColor(fg);g.drawString(t,x+17,y);x+=w+12;}}return y;}
    private List<String> textLines(String text,int max){List<String> out=new java.util.ArrayList<>();if(text==null) return out;for(int i=0;i<text.length();i+=max)out.add(text.substring(i,Math.min(text.length(),i+max)));return out;}
    private void drawCentered(Graphics2D g,String s,int width,int y){drawCenteredAt(g,s,width/2,y);} private void drawCenteredAt(Graphics2D g,String s,int x,int y){FontMetrics fm=g.getFontMetrics();g.drawString(s,x-fm.stringWidth(s)/2,y);}

    private String buildHtml(Map result) {
        String code = result.get("code") == null ? "" : String.valueOf(result.get("code"));
        Map<String,HollandCode> details = new HashMap<>();
        for (HollandCode item : hollandCodeService.listCodes()) details.put(item.getCode(), item);
        StringBuilder badges = new StringBuilder();
        StringBuilder conclusions = new StringBuilder();
        for (int i=0; i<code.length(); i++) {
            String c = String.valueOf(code.charAt(i)); HollandCode item = details.get(c); if (item == null) continue;
            String color = color(c); String icon = icon(c);
            badges.append("<div class='badge' style='border-color:").append(color).append("'><b>").append(escape(c)).append("</b><span>").append(escape(item.getName())).append("</span></div>");
            conclusions.append("<div class='type-card' style='border-top-color:").append(color).append("'><h3>").append(icon).append(" ").append(escape(item.getName())).append("（").append(escape(c)).append("）</h3><h4>特点</h4><p>").append(escape(item.getSummary())).append("</p><h4>生活事例</h4><p>").append(escape(item.getLife())).append("</p></div>");
        }
        return "<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE html><html xmlns='http://www.w3.org/1999/xhtml' lang='zh-CN'><head><meta charset='UTF-8' /><title>职业兴趣测评报告</title><style type='text/css'>body{font-family:Arial,'Microsoft YaHei';max-width:900px;margin:0 auto;padding:24px;color:#263b55;background:#f5f7fa}.header{text-align:center;padding:24px 18px 12px}.header h1{margin:0;color:#1a2c4a;font-size:28px}.badges{margin:22px auto 12px;text-align:center}.badge{display:inline-block;vertical-align:top;width:108px;height:98px;margin:6px;padding:12px 4px;border:5px solid #8bbce0;border-radius:18px;background:#eef7ff;box-sizing:border-box}.badge b{display:block;font-size:30px;color:#1677ff;line-height:38px}.badge span{display:block;margin-top:4px;font-size:14px}.type-explain-title{font-size:22px;font-weight:bold;color:#2c3e50;margin:22px 4px 12px}.type-card,.combo{margin:18px 0;padding:20px 22px;background:#fff;border-radius:14px;border-top:7px solid #1677ff;box-shadow:0 4px 12px #dfe6ef}.type-card h3{margin:0 0 16px;font-size:20px;color:#1a2c4a}.type-card h4{color:#1856d1;margin:12px 0 4px;font-size:15px}.type-card p{line-height:1.8;margin:0;font-size:15px;color:#34495e}.combo{border-top:0}.combo h2{color:#1a2c4a;font-size:20px;text-align:center;margin:0 0 20px}.combo h3{font-size:15px;color:#1856d1;margin:14px 0 6px}.item{display:inline-block;margin:4px 8px 4px 0;padding:8px 13px;background:#eef4ff;color:#1856d1;border-radius:18px;font-size:14px}.secondary{background:#eafaf1;color:#27ae60}</style></head><body><div class='header'><h1>您的职业兴趣类型为</h1><div class='badges'>" + badges + "</div></div><div class='type-explain-title'>下述是你的结论</div>" + conclusions + "<div class='combo'><h2>你的职业方向与工作环境偏好</h2><h3>职业方向</h3>" + listHtml(result,"careers","title", false) + "<h3>工作环境偏好</h3>" + listHtml(result,"workEnvs","title", true) + "</div></body></html>";
    }

    private String listHtml(Map result, String key, String field) { return listHtml(result, key, field, false); }
    private String listHtml(Map result, String key, String field, boolean secondary) {
        Object value = result.get(key);
        if (!(value instanceof Iterable)) return "";
        StringBuilder html = new StringBuilder();
        for (Object item : (Iterable) value) {
            Object text = value(item, field);
            if (text != null && !String.valueOf(text).trim().isEmpty()) html.append("<div class='item").append(secondary ? " secondary" : "").append("'>").append(escape(String.valueOf(text))).append("</div>");
        }
        return html.toString();
    }

    private Object value(Object bean, String field) { if (bean == null) return ""; if (bean instanceof Map) return ((Map) bean).get(field); try { return bean.getClass().getMethod("get" + Character.toUpperCase(field.charAt(0)) + field.substring(1)).invoke(bean); } catch (Exception e) { return ""; } }
    private String safeName(String value) { return value == null || value.trim().isEmpty() ? "未分类区域" : value.replaceAll("[\\\\/:*?\"<>|]", "_"); }
    private String color(String c) { return "I".equals(c)?"#2980b9":"R".equals(c)?"#e67e22":"S".equals(c)?"#27ae60":"E".equals(c)?"#c0392b":"A".equals(c)?"#8e44ad":"#7f8c8d"; }
    private String icon(String c) { return "I".equals(c)?"🔬":"R".equals(c)?"🛠️":"S".equals(c)?"🤝":"E".equals(c)?"📈":"A".equals(c)?"🎨":"📋"; }
}
