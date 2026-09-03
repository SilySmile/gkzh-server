package com.gkzh.web.controller.activity;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.zxing.WriterException;
import com.gkzh.activity.domain.week.GkzhActivityArea;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekDefinition;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhActivityWeekSchool;
import com.gkzh.activity.domain.week.GkzhGameType;
import com.gkzh.activity.domain.week.GkzhGameConfig;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.annotation.Anonymous;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.utils.QRCodeUtils;
import com.gkzh.xycc.domain.HollandCode;
import com.gkzh.xycc.mapper.UserSelectionMapper;
import com.gkzh.xycc.service.IHollandCodeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.alibaba.fastjson2.JSON;
import com.gkzh.sszctop.domain.SszctopDimension;
import com.gkzh.sszctop.domain.SszctopStudentReport;
import com.gkzh.sszctop.mapper.SszctopDimensionMapper;
import com.gkzh.sszctop.mapper.SszctopStudentReportMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

/**
 * 活动/区域/游戏管理接口
 */
@RestController
@RequestMapping("/activity/week")
public class GkzhActivityWeekController extends BaseController {

    @Autowired
    private IActivityWeekService activityWeekService;

    @Autowired
    private UserSelectionMapper userSelectionMapper;

    @Autowired
    private IHollandCodeService hollandCodeService;

    /** 职场 TOP 的七个维度定义，用于活动游戏查看页。 */
    @Autowired
    private SszctopDimensionMapper sszctopDimensionMapper;

    /** 职场 TOP 的个人结算快照；统计按每位用户的独立报告计算。 */
    @Autowired
    private SszctopStudentReportMapper sszctopStudentReportMapper;

    @Value("${app.qr-code.base-url}")
    private String qrCodeBaseUrl;

    @GetMapping("/gameTypes")
    public AjaxResult gameTypes() {
        return AjaxResult.success(activityWeekService.listGameTypes());
    }

    @GetMapping("/gameConfigs")
    public AjaxResult gameConfigs(@RequestParam(required = false) String gameType) {
        return AjaxResult.success(activityWeekService.listGameConfigs(gameType));
    }

    @PostMapping("/gameConfig")
    public AjaxResult saveGameConfig(@RequestBody GkzhGameConfig config) {
        return toAjax(activityWeekService.saveGameConfig(config));
    }

    @DeleteMapping("/gameConfig/{configId}")
    public AjaxResult deleteGameConfig(@PathVariable Long configId) {
        return toAjax(activityWeekService.deleteGameConfig(configId));
    }

    @GetMapping("/definitions")
    public AjaxResult definitions() {
        return AjaxResult.success(activityWeekService.listDefinitions());
    }

    @PostMapping("/definition")
    public AjaxResult saveDefinition(@RequestBody GkzhActivityWeekDefinition definition) {
        return toAjax(activityWeekService.saveDefinition(definition));
    }

    @GetMapping("/instances")
    public AjaxResult instances(@RequestParam(required = false) String bizType) {
        return AjaxResult.success(activityWeekService.listInstances(bizType));
    }

    @GetMapping("/instance/{instanceId}")
    public AjaxResult instance(@PathVariable Long instanceId) {
        return AjaxResult.success(activityWeekService.getInstance(instanceId));
    }

    @PostMapping("/instance")
    public AjaxResult saveInstance(@RequestBody GkzhActivityWeekInstance instance) {
        return toAjax(activityWeekService.saveInstance(instance));
    }

    @DeleteMapping("/instance/{instanceId}")
    public AjaxResult deleteInstance(@PathVariable Long instanceId) {
        return toAjax(activityWeekService.deleteInstance(instanceId));
    }

    @GetMapping("/instance/{instanceId}/schools")
    public AjaxResult instanceSchools(@PathVariable Long instanceId) {
        return AjaxResult.success(activityWeekService.listInstanceSchools(instanceId));
    }

    @PostMapping("/instance/{instanceId}/schools")
    public AjaxResult saveInstanceSchools(@PathVariable Long instanceId, @RequestBody List<GkzhActivityWeekSchool> schoolConfigs) {
        return toAjax(activityWeekService.saveInstanceSchools(instanceId, schoolConfigs));
    }

    @GetMapping("/areas")
    public AjaxResult areas(@RequestParam Long instanceId, @RequestParam(required = false) Long schoolId) {
        return AjaxResult.success(activityWeekService.listAreas(instanceId, schoolId));
    }

    @PostMapping("/area")
    public AjaxResult saveArea(@RequestBody GkzhActivityArea area) {
        return toAjax(activityWeekService.saveArea(area));
    }

    @DeleteMapping("/area/{areaId}")
    public AjaxResult deleteArea(@PathVariable Long areaId) {
        return toAjax(activityWeekService.deleteArea(areaId));
    }

    @GetMapping("/games")
    public AjaxResult games(@RequestParam Long areaId) {
        return AjaxResult.success(activityWeekService.listGames(areaId));
    }

    @GetMapping("/game/{gameId}")
    public AjaxResult game(@PathVariable Long gameId) {
        return AjaxResult.success(activityWeekService.getGame(gameId));
    }

    /** 获取游戏查看数据，viewType 用于区分不同游戏的展示模板。 */
    @GetMapping("/game/{gameId}/view")
    public AjaxResult gameView(@PathVariable Long gameId) {
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null) return AjaxResult.error("游戏不存在");
        return AjaxResult.success(buildGameView(game));
    }

    /** 导出与查看页同源数据的 PDF。 */
    @GetMapping("/game/{gameId}/view/export")
    public ResponseEntity<byte[]> exportGameView(@PathVariable Long gameId) {
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null) return ResponseEntity.notFound().build();
        try {
            Map<String, Object> gameView = buildGameView(game);
            String viewType = String.valueOf(gameView.get("viewType"));
            if (!"mind-window".equals(viewType) && !"sszctop".equals(viewType)) return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
            byte[] bytes = createGameViewPdf(gameView);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("游戏查看报告-" + safeFileName(game.getTitle()) + ".pdf", java.nio.charset.StandardCharsets.UTF_8).build());
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Map<String, Object> buildGameView(GkzhActivityGame game) {
        Map<String, Object> view = new LinkedHashMap<>();
        String type = game.getGameType() == null ? "" : game.getGameType();
        String route = resolveGameRoute(game);
        String viewType = "generic";
        for (GkzhGameConfig config : activityWeekService.listGameConfigs(null)) {
            if ((route != null && route.equals(config.getRoute())) || type.equals(config.getGameType())) {
                viewType = config.getViewType() == null || config.getViewType().trim().isEmpty()
                        ? "generic" : config.getViewType();
                break;
            }
        }
        // 职场 TOP 的查看页固定使用专属三页模板，避免后台配置缺失时降级为通用空白页。
        if ("sszctop".equals(type)) viewType = "sszctop";
        view.put("gameId", game.getGameId());
        view.put("gameTitle", game.getTitle());
        view.put("gameType", type);
        view.put("viewType", viewType);
        if ("sszctop".equals(viewType)) return buildSszctopGameView(game, view);
        if (!"mind-window".equals(viewType)) {
            view.put("message", "该游戏暂未配置查看模板");
            view.put("pages", new ArrayList<>());
            return view;
        }
        List<HollandCode> definitions = hollandCodeService.listCodes();
        Map<String, HollandCode> definitionMap = new HashMap<>();
        for (HollandCode definition : definitions) definitionMap.put(definition.getCode(), definition);
        List<Map<String, Object>> codeRows = new ArrayList<>();
        List<Map<String, Object>> stats = userSelectionMapper.selectCodeStatsByGameId(game.getGameId());
        long total = 0L;
        for (Map<String, Object> stat : stats) total = Math.max(total, number(stat.get("total_count")));
        int serial = 1;
        for (Map<String, Object> stat : stats) {
            String code = String.valueOf(stat.get("code"));
            long count = number(stat.get("user_count"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("serialNo", serial++);
            row.put("code", code);
            row.put("codeSummary", codeSummary(code, definitionMap));
            row.put("userCount", count);
            row.put("probability", total == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(count * 100D / total).setScale(2, RoundingMode.HALF_UP));
            codeRows.add(row);
        }
        view.put("hollandCodes", definitions);
        view.put("codeStats", codeRows);
        view.put("totalParticipants", total);
        view.put("analysis", buildAnalysis(codeRows, total, definitionMap));
        return view;
    }

    /**
     * 构建“谁是职场TOP”活动查看数据：
     * 第一页提供七个维度解释，第二页按维度统计个人结算正确率，第三页输出简要活动分析。
     */
    private Map<String, Object> buildSszctopGameView(GkzhActivityGame game, Map<String, Object> view) {
        List<SszctopDimension> dimensions = sszctopDimensionMapper.selectList(
                new QueryWrapper<SszctopDimension>().orderByAsc("sort_order").orderByAsc("dimension_id"));
        List<SszctopStudentReport> reports = sszctopStudentReportMapper.selectList(
                new QueryWrapper<SszctopStudentReport>().eq("game_id", game.getGameId()));

        // 先创建完整的七维度统计行，确保即使某维度尚无人游玩也会在第二页显示为 0。
        Map<Long, Map<String, Object>> rowsByDimensionId = new LinkedHashMap<>();
        int serial = 1;
        for (SszctopDimension dimension : dimensions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("serialNo", serial++);
            row.put("dimensionId", dimension.getDimensionId());
            row.put("dimensionName", dimension.getName());
            row.put("dimensionDescription", dimension.getDescription());
            row.put("participantCount", 0L);
            row.put("correctCount", 0L);
            row.put("correctRate", BigDecimal.ZERO);
            rowsByDimensionId.put(dimension.getDimensionId(), row);
        }

        long totalParticipants = 0L;
        long totalCorrect = 0L;
        for (SszctopStudentReport report : reports) {
            SszctopDimension selectedDimension = reportDimension(report);
            if (selectedDimension == null || selectedDimension.getDimensionId() == null) continue;
            Map<String, Object> row = rowsByDimensionId.get(selectedDimension.getDimensionId());
            if (row == null) continue;
            long participantCount = number(row.get("participantCount")) + 1;
            row.put("participantCount", participantCount);
            totalParticipants++;
            if ("passed".equals(report.getResult())) {
                row.put("correctCount", number(row.get("correctCount")) + 1);
                totalCorrect++;
            }
        }

        List<Map<String, Object>> dimensionStats = new ArrayList<>(rowsByDimensionId.values());
        for (Map<String, Object> row : dimensionStats) {
            long participantCount = number(row.get("participantCount"));
            long correctCount = number(row.get("correctCount"));
            row.put("correctRate", participantCount == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(correctCount * 100D / participantCount).setScale(2, RoundingMode.HALF_UP));
        }
        view.put("dimensions", dimensions);
        view.put("dimensionStats", dimensionStats);
        view.put("totalParticipants", totalParticipants);
        view.put("totalCorrect", totalCorrect);
        view.put("overallCorrectRate", totalParticipants == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalCorrect * 100D / totalParticipants).setScale(2, RoundingMode.HALF_UP));
        view.put("analysis", buildSszctopAnalysis(dimensionStats, totalParticipants, totalCorrect));
        return view;
    }

    /** 从学生结算快照中还原其本次选择的维度；历史损坏数据直接跳过，不影响其他统计。 */
    private SszctopDimension reportDimension(SszctopStudentReport report) {
        try {
            return JSON.parseObject(report.getDimensionSnapshot(), SszctopDimension.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** 根据七维度的完成及正确情况生成简要、可读的活动复盘说明。 */
    private String buildSszctopAnalysis(List<Map<String, Object>> rows, long totalParticipants, long totalCorrect) {
        if (totalParticipants == 0) return "当前游戏尚无完成记录。完成游戏后，这里将展示七个职业认知维度的参与及正确率分析。";
        Map<String, Object> mostSelected = null;
        Map<String, Object> highestRate = null;
        Map<String, Object> lowestRate = null;
        for (Map<String, Object> row : rows) {
            long participantCount = number(row.get("participantCount"));
            if (mostSelected == null || participantCount > number(mostSelected.get("participantCount"))) mostSelected = row;
            if (participantCount == 0) continue;
            if (highestRate == null || decimal(row.get("correctRate")).compareTo(decimal(highestRate.get("correctRate"))) > 0) {
                highestRate = row;
            }
            if (lowestRate == null || decimal(row.get("correctRate")).compareTo(decimal(lowestRate.get("correctRate"))) < 0) {
                lowestRate = row;
            }
        }
        BigDecimal overallRate = BigDecimal.valueOf(totalCorrect * 100D / totalParticipants).setScale(2, RoundingMode.HALF_UP);
        String analysis = "本次共 " + totalParticipants + " 人完成谁是职场TOP，整体正确率为 " + overallRate + "% 。";
        if (mostSelected != null && number(mostSelected.get("participantCount")) > 0)
            analysis += "参与人数最多的维度是“" + mostSelected.get("dimensionName") + "”（" + mostSelected.get("participantCount") + " 人）。";
        if (highestRate != null)
            analysis += "正确率最高的是“" + highestRate.get("dimensionName") + "”（" + highestRate.get("correctRate") + "%）；";
        if (lowestRate != null)
            analysis += "正确率较低的是“" + lowestRate.get("dimensionName") + "”（" + lowestRate.get("correctRate") + "%），可作为活动讲解与职业认知讨论的重点。";
        return analysis;
    }

    /** 将统计字段统一转换为 BigDecimal，便于比较百分比。 */
    private BigDecimal decimal(Object value) {
        try { return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value)); }
        catch (Exception ignored) { return BigDecimal.ZERO; }
    }

    private long number(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        try { return value == null ? 0L : Long.parseLong(String.valueOf(value)); } catch (Exception e) { return 0L; }
    }

    private String codeSummary(String code, Map<String, HollandCode> definitions) {
        StringBuilder summary = new StringBuilder();
        if (code != null) for (int i = 0; i < code.length(); i++) {
            HollandCode item = definitions.get(String.valueOf(code.charAt(i)));
            if (item != null) { if (summary.length() > 0) summary.append('+'); summary.append(item.getName()); }
        }
        return summary.toString();
    }

    private String buildAnalysis(List<Map<String, Object>> rows, long total, Map<String, HollandCode> definitions) {
        if (total == 0 || rows.isEmpty()) return "AI分析：当前游戏暂未产生有效测评数据，暂无法进行代码统计分析。";
        Map<String, Long> typeCounts = new HashMap<>(); Map<String, Object> top = rows.get(0);
        for (Map<String, Object> row : rows) { String code = String.valueOf(row.get("code")); long count = number(row.get("userCount")); for (int i = 0; i < code.length(); i++) typeCounts.merge(String.valueOf(code.charAt(i)), count, Long::sum); }
        String topType = null; long topCount = -1L;
        for (Map.Entry<String, Long> entry : typeCounts.entrySet()) if (entry.getValue() > topCount) { topType = entry.getKey(); topCount = entry.getValue(); }
        HollandCode type = definitions.get(topType);
        return "AI分析：本次共统计 " + total + " 名完成用户，出现频率最高的复合代码为 " + top.get("code") + "（" + top.get("codeSummary") + "），占比 " + top.get("probability") + "%。" +
                " 从单类型倾向看，" + (type == null ? topType : type.getName()) + "最为突出，说明参与者整体更偏向该类型的兴趣特征。" +
                " 建议结合各专业、性别等维度继续观察差异，并将本页结果作为活动复盘和生涯指导的参考，不作为单独的人才判断依据。";
    }

    @PostMapping("/game")
    public AjaxResult saveGame(@RequestBody GkzhActivityGame game) {
        return toAjax(activityWeekService.saveGame(game));
    }

    @DeleteMapping("/game/{gameId}")
    public AjaxResult deleteGame(@PathVariable Long gameId) {
        return toAjax(activityWeekService.deleteGame(gameId));
    }

    @Anonymous
    @GetMapping("/game/qrcode/preview/{gameId}")
    public ResponseEntity<byte[]> previewGameQrCode(@PathVariable Long gameId) {
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            String content = buildGameQrContent(game);
            byte[] bytes = QRCodeUtils.generateQRCode(content, 400, 400);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setCacheControl("no-cache");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (WriterException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/game/qrcode/regenerate/{gameId}")
    public AjaxResult regenerateGameQrCode(@PathVariable Long gameId) {
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null) {
            return AjaxResult.error("游戏不存在");
        }
        game.setQrCode(buildGameQrContent(game));
        return toAjax(activityWeekService.saveGame(game));
    }

    private byte[] createGameViewPdf(Map<String, Object> view) throws Exception {
        List<BufferedImage> pages = new ArrayList<>();
        String title = String.valueOf(view.get("gameTitle"));
        if ("sszctop".equals(view.get("viewType"))) {
            pages.add(drawSszctopDimensionsPage((List<SszctopDimension>) view.get("dimensions"), title));
            pages.add(drawSszctopStatsPage((List<Map<String, Object>>) view.get("dimensionStats"), title,
                    number(view.get("totalParticipants")), decimal(view.get("overallCorrectRate"))));
            pages.add(drawTextAnalysisPage(String.valueOf(view.get("analysis")), title, "维度正确率简要分析"));
        } else {
            pages.add(drawHollandPage((List<HollandCode>) view.get("hollandCodes"), title));
            pages.add(drawCodeStatsPage((List<Map<String, Object>>) view.get("codeStats"), title));
            pages.add(drawAnalysisPage(String.valueOf(view.get("analysis")), title));
        }
        int width = 1080;
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDRectangle pageSize = PDRectangle.A4; float scale = pageSize.getWidth() / width; int slice = (int) (pageSize.getHeight() / scale);
            for (BufferedImage image : pages) for (int top = 0; top < image.getHeight(); top += slice) {
                int h = Math.min(slice, image.getHeight() - top); BufferedImage part = image.getSubimage(0, top, width, h);
                PDPage page = new PDPage(pageSize); doc.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(doc, page)) { stream.drawImage(LosslessFactory.createFromImage(doc, part), 0, pageSize.getHeight() - h * scale, pageSize.getWidth(), h * scale); }
            }
            doc.save(out); return out.toByteArray();
        }
    }

    private BufferedImage newViewImage(int height) {
        BufferedImage image = new BufferedImage(1080, height, BufferedImage.TYPE_INT_RGB); Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); g.setColor(new Color(245, 247, 250)); g.fillRect(0, 0, image.getWidth(), image.getHeight()); g.dispose(); return image;
    }

    private BufferedImage drawHollandPage(List<HollandCode> items, String title) {
        BufferedImage image = newViewImage(1527); Graphics2D g = image.createGraphics();
        g.setColor(new Color(26, 44, 74)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 42)); g.drawString(title + "—职业兴趣类型解释", 48, 70); int y = 108;
        for (HollandCode item : items) {
            g.setColor(Color.WHITE); g.fillRoundRect(48, y, 984, 202, 20, 20); g.setColor(new Color(22, 119, 255)); g.fillRoundRect(48, y, 12, 202, 12, 12);
            g.setColor(new Color(26, 44, 74)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 26)); g.drawString(item.getCode() + "  " + item.getName() + (item.getFullName() == null ? "" : "（" + item.getFullName() + "）"), 84, y + 38);
            g.setColor(new Color(52, 73, 94)); g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 19)); int next = drawWrapped(g, "解释：" + (item.getSummary() == null ? "暂无解释" : item.getSummary()), 84, y + 74, 900, 27); drawWrapped(g, "特征：" + (item.getTraits() == null ? "暂无" : item.getTraits()), 84, next + 8, 900, 27); y += 216;
        }
        g.dispose(); return image;
    }

    private BufferedImage drawCodeStatsPage(List<Map<String, Object>> rows, String title) {
        int height = Math.max(1527, 130 + rows.size() * 42); BufferedImage image = newViewImage(height); Graphics2D g = image.createGraphics();
        g.setColor(new Color(26, 44, 74)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 42)); g.drawString(title + "—复合代码统计", 48, 72); int y = 122;
        g.setColor(new Color(22, 119, 255)); g.fillRect(48, y, 984, 46); g.setColor(Color.WHITE); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 20)); g.drawString("序号", 70, y + 30); g.drawString("复合类型代码", 180, y + 30); g.drawString("代码简称", 410, y + 30); g.drawString("人数", 790, y + 30); g.drawString("概率", 900, y + 30); y += 46;
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 19));
        for (Map<String, Object> row : rows) { g.setColor(y % 84 == 0 ? new Color(248, 250, 253) : Color.WHITE); g.fillRect(48, y, 984, 42); g.setColor(new Color(52, 73, 94)); g.drawString(String.valueOf(row.get("serialNo")), 76, y + 28); g.drawString(String.valueOf(row.get("code")), 190, y + 28); g.drawString(String.valueOf(row.get("codeSummary")), 410, y + 28); g.drawString(String.valueOf(row.get("userCount")), 800, y + 28); g.drawString(String.valueOf(row.get("probability")) + "%", 900, y + 28); y += 42; }
        g.dispose(); return image;
    }

    private BufferedImage drawAnalysisPage(String analysis, String title) {
        BufferedImage image = newViewImage(1527); Graphics2D g = image.createGraphics(); g.setColor(new Color(26, 44, 74)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 42)); g.drawString(title + "—代码统计分析", 48, 72); g.setColor(Color.WHITE); g.fillRoundRect(48, 128, 984, 520, 24, 24); g.setColor(new Color(52, 73, 94)); g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 28)); drawWrapped(g, analysis, 88, 210, 900, 48); g.dispose(); return image;
    }

    /** 生成职场 TOP 第一页：七个职业认知维度及解释。 */
    private BufferedImage drawSszctopDimensionsPage(List<SszctopDimension> items, String title) {
        int height = Math.max(1800, 130 + items.size() * 238);
        BufferedImage image = newViewImage(height); Graphics2D g = image.createGraphics();
        g.setColor(new Color(26, 44, 74)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 42)); g.drawString(title + "—七个职业认知维度解释", 48, 72); int y = 108;
        for (SszctopDimension item : items) {
            g.setColor(Color.WHITE); g.fillRoundRect(48, y, 984, 220, 20, 20); g.setColor(new Color(103, 194, 58)); g.fillRoundRect(48, y, 12, 220, 12, 12);
            g.setColor(new Color(26, 44, 74)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 26)); g.drawString(item.getSortOrder() + "  " + item.getName(), 84, y + 42);
            g.setColor(new Color(52, 73, 94)); g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 20)); drawWrapped(g, "维度解释：" + (item.getDescription() == null ? "暂无解释" : item.getDescription()), 84, y + 82, 900, 29); y += 234;
        }
        g.dispose(); return image;
    }

    /** 生成职场 TOP 第二页：七个维度的个人结算正确率统计。 */
    private BufferedImage drawSszctopStatsPage(List<Map<String, Object>> rows, String title, long total, BigDecimal overallRate) {
        int height = Math.max(900, 210 + rows.size() * 56); BufferedImage image = newViewImage(height); Graphics2D g = image.createGraphics();
        g.setColor(new Color(26, 44, 74)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 42)); g.drawString(title + "—七个维度正确率统计", 48, 72);
        g.setColor(new Color(52, 73, 94)); g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 23)); g.drawString("完成用户数：" + total + "    整体正确率：" + overallRate + "%", 48, 112); int y = 148;
        g.setColor(new Color(103, 194, 58)); g.fillRect(48, y, 984, 50); g.setColor(Color.WHITE); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        g.drawString("序号", 72, y + 32); g.drawString("职业认知维度", 180, y + 32); g.drawString("完成人数", 610, y + 32); g.drawString("正确人数", 760, y + 32); g.drawString("正确率", 900, y + 32); y += 50;
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 20));
        for (Map<String, Object> row : rows) { g.setColor(y % 112 == 0 ? new Color(248, 250, 253) : Color.WHITE); g.fillRect(48, y, 984, 56); g.setColor(new Color(52, 73, 94)); g.drawString(String.valueOf(row.get("serialNo")), 76, y + 35); g.drawString(String.valueOf(row.get("dimensionName")), 180, y + 35); g.drawString(String.valueOf(row.get("participantCount")), 630, y + 35); g.drawString(String.valueOf(row.get("correctCount")), 780, y + 35); g.drawString(String.valueOf(row.get("correctRate")) + "%", 900, y + 35); y += 56; }
        g.dispose(); return image;
    }

    /** 生成职场 TOP 第三页：与 Web 查看页相同的简要分析文本。 */
    private BufferedImage drawTextAnalysisPage(String analysis, String title, String pageTitle) {
        BufferedImage image = newViewImage(1527); Graphics2D g = image.createGraphics(); g.setColor(new Color(26, 44, 74)); g.setFont(new Font("Microsoft YaHei", Font.BOLD, 42)); g.drawString(title + "—" + pageTitle, 48, 72); g.setColor(Color.WHITE); g.fillRoundRect(48, 128, 984, 520, 24, 24); g.setColor(new Color(52, 73, 94)); g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 28)); drawWrapped(g, analysis, 88, 210, 900, 48); g.dispose(); return image;
    }

    private int drawWrapped(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) { FontMetrics fm = g.getFontMetrics(); StringBuilder line = new StringBuilder(); for (int i = 0; text != null && i < text.length(); i++) { String candidate = line.toString() + text.charAt(i); if (fm.stringWidth(candidate) > maxWidth && line.length() > 0) { g.drawString(line.toString(), x, y); y += lineHeight; line.setLength(0); } line.append(text.charAt(i)); } if (line.length() > 0) { g.drawString(line.toString(), x, y); y += lineHeight; } return y; }
    private String safeFileName(String value) { return value == null || value.trim().isEmpty() ? "游戏查看报告" : value.replaceAll("[\\\\/:*?\"<>|]", "_"); }

    private String buildGameQrContent(GkzhActivityGame game) {
        List<GkzhActivityArea> areas = activityWeekService.listAreas(game.getInstanceId(), null);
        Long schoolId = null;
        for (GkzhActivityArea area : areas) {
            if (area.getAreaId().equals(game.getAreaId())) {
                schoolId = area.getSchoolId();
                break;
            }
        }
        String gameType = resolveGameRoute(game);
        // 二维码只编码小程序内部路由文本，不再放置 http(s) 网页地址，避免微信相机直接跳转 Web 页面。
        return "GKZH_MP:/pages/activity/week"
                + "?schoolId=" + (schoolId == null ? "" : schoolId)
                + "&instanceId=" + game.getInstanceId()
                + "&areaId=" + game.getAreaId()
                + "&gameId=" + game.getGameId()
                + "&gameType=" + gameType;
    }

    /**
     * 二维码必须携带可执行的前端路由。历史数据中的 game_type 可能是中文分类，
     * 或保存时只写入了 config.route，因此优先从 config 中取 route，避免二维码只得到分类文本。
     */
    private String resolveGameRoute(GkzhActivityGame game) {
        String config = game.getConfig();
        if (config != null) {
            Matcher matcher = Pattern.compile("\\\"route\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(config);
            if (matcher.find()) {
                return matcher.group(1);
            }
            // 历史活动游戏有时只保存 configId；从配置表补回具体路由，避免二维码缺少游戏类型。
            Matcher configIdMatcher = Pattern.compile("\\\"configId\\\"\\s*:\\s*(\\d+)").matcher(config);
            if (configIdMatcher.find()) {
                Long configId = Long.valueOf(configIdMatcher.group(1));
                for (GkzhGameConfig item : activityWeekService.listGameConfigs(null)) {
                    if (configId.equals(item.getConfigId())) {
                        return item.getRoute() == null || item.getRoute().trim().isEmpty()
                                ? item.getGameType() : item.getRoute();
                    }
                }
            }
        }
        return game.getGameType() == null ? "" : game.getGameType();
    }
}
