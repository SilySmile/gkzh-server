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
import com.gkzh.activity.mapper.week.GkzhGameParticipationMapper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.nlld.domain.NlldRecord;
import com.gkzh.nlld.mapper.NlldRecordMapper;
import com.gkzh.nlld.service.NlldGameService;
import com.gkzh.zcyltzj.domain.ZcyltzjRecord;
import com.gkzh.zcyltzj.mapper.ZcyltzjRecordMapper;
import com.gkzh.zcyltzj.service.ZcyltzjRecordService;
import com.gkzh.zyfxq.domain.ZyfxqRecord;
import com.gkzh.zyfxq.mapper.ZyfxqRecordMapper;
import com.gkzh.zyfxq.service.ZyfxqGameService;
import com.gkzh.jlcz.domain.JlczRecord;
import com.gkzh.jlcz.mapper.JlczRecordMapper;
import com.gkzh.jlcz.service.JlczGameService;
import com.gkzh.zmwl.domain.ZmwlRecord;
import com.gkzh.zmwl.mapper.ZmwlRecordMapper;
import com.gkzh.zmwl.service.ZmwlGameService;
import com.gkzh.zyjdc.domain.ZyjdcRecord;
import com.gkzh.zyjdc.domain.ZyjdcRole;
import com.gkzh.zyjdc.mapper.ZyjdcRecordMapper;
import com.gkzh.zyjdc.service.ZyjdcGameService;
import com.gkzh.zytj.domain.ZytjRecord;
import com.gkzh.zytj.mapper.ZytjRecordMapper;
import com.gkzh.zdyp.domain.ZdypCard;
import com.gkzh.zdyp.domain.ZdypCareer;
import com.gkzh.zdyp.domain.ZdypRecord;
import com.gkzh.zdyp.mapper.ZdypRecordMapper;
import com.gkzh.zdyp.service.ZdypConfigService;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.service.IGkzhStudentService;
import com.gkzh.sszctop.domain.SszctopCareer;
import com.gkzh.sszctop.domain.SszctopDimension;
import com.gkzh.sszctop.domain.SszctopDimensionRank;
import com.gkzh.sszctop.domain.SszctopStudentReport;
import com.gkzh.sszctop.mapper.SszctopStudentReportMapper;
import com.gkzh.app.service.ZycckReportPdfService;
import com.gkzh.app.service.ReportPdfFont;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.text.SimpleDateFormat;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

/** 生成可在浏览器打开的临时报告文件，默认缓存 24 小时。 */
@RestController
@RequestMapping("/api/xycc/report")
public class ReportCacheController extends FrontBaseController {
    private final IPatternComboService patternComboService;
    private final IHollandCodeService hollandCodeService;
    private final IActivityWeekService activityWeekService;
    private final GkzhGameParticipationMapper gameParticipationMapper;
    private final NlldRecordMapper nlldRecordMapper;
    private final NlldGameService nlldGameService;
    private final ZcyltzjRecordMapper zcyltzjRecordMapper;
    private final ZcyltzjRecordService zcyltzjRecordService;
    private final ZyfxqRecordMapper zyfxqRecordMapper;
    private final ZyfxqGameService zyfxqGameService;
    private final JlczRecordMapper jlczRecordMapper;
    private final JlczGameService jlczGameService;
    private final ZmwlRecordMapper zmwlRecordMapper;
    private final ZmwlGameService zmwlGameService;
    private final ZyjdcRecordMapper zyjdcRecordMapper;
    private final ZyjdcGameService zyjdcGameService;
    private final ZytjRecordMapper zytjRecordMapper;
    private final ZdypRecordMapper zdypRecordMapper;
    private final ZdypConfigService zdypConfigService;
    private final IGkzhStudentService studentService;
    private final SszctopStudentReportMapper sszctopReportMapper;
    private final ZycckReportPdfService zycckReportPdfService;
    private static final long CACHE_MILLIS = 24 * 60 * 60 * 1000L;

    public ReportCacheController(IPatternComboService patternComboService, IHollandCodeService hollandCodeService,
                                 IActivityWeekService activityWeekService, IGkzhStudentService studentService,
                                 SszctopStudentReportMapper sszctopReportMapper, ZycckReportPdfService zycckReportPdfService,
                                 GkzhGameParticipationMapper gameParticipationMapper,
                                 NlldRecordMapper nlldRecordMapper, NlldGameService nlldGameService,
                                 ZcyltzjRecordMapper zcyltzjRecordMapper, ZcyltzjRecordService zcyltzjRecordService,
                                 ZyfxqRecordMapper zyfxqRecordMapper, ZyfxqGameService zyfxqGameService,
                                 JlczRecordMapper jlczRecordMapper, JlczGameService jlczGameService,
                                 ZmwlRecordMapper zmwlRecordMapper, ZmwlGameService zmwlGameService,
                                 ZyjdcRecordMapper zyjdcRecordMapper, ZyjdcGameService zyjdcGameService,
                                 ZytjRecordMapper zytjRecordMapper,
                                 ZdypRecordMapper zdypRecordMapper, ZdypConfigService zdypConfigService) {
        this.patternComboService = patternComboService;
        this.hollandCodeService = hollandCodeService;
        this.activityWeekService = activityWeekService;
        this.gameParticipationMapper = gameParticipationMapper;
        this.nlldRecordMapper = nlldRecordMapper;
        this.nlldGameService = nlldGameService;
        this.zcyltzjRecordMapper = zcyltzjRecordMapper;
        this.zcyltzjRecordService = zcyltzjRecordService;
        this.zyfxqRecordMapper = zyfxqRecordMapper;
        this.zyfxqGameService = zyfxqGameService;
        this.jlczRecordMapper = jlczRecordMapper;
        this.jlczGameService = jlczGameService;
        this.zmwlRecordMapper = zmwlRecordMapper;
        this.zmwlGameService = zmwlGameService;
        this.zyjdcRecordMapper = zyjdcRecordMapper;
        this.zyjdcGameService = zyjdcGameService;
        this.zytjRecordMapper = zytjRecordMapper;
        this.zdypRecordMapper = zdypRecordMapper;
        this.zdypConfigService = zdypConfigService;
        this.studentService = studentService;
        this.sszctopReportMapper = sszctopReportMapper;
        this.zycckReportPdfService = zycckReportPdfService;
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
                    GkzhGameParticipation participation = findFinishedParticipation(activityId, game.getGameId(), userId);
                    if (participation == null || !("1".equals(participation.getStatus()) || "2".equals(participation.getStatus()))) continue;
                    byte[] pdf;
                    if ("mind-window".equals(game.getGameType())) {
                        pdf = createPdf(patternComboService.getXyccResult(activityId, userId));
                    } else if ("nlld".equals(game.getGameType())) {
                        pdf = createNlldPdf(activityId, game.getGameId(), userId);
                        if (pdf == null) pdf = createGamePdf(instance, area, game, participation);
                    } else if ("zcyltzj".equals(game.getGameType())) {
                        pdf = createZcyltzjPdf(activityId, game.getGameId(), userId);
                        if (pdf == null) pdf = createGamePdf(instance, area, game, participation);
                    } else if ("zyfxq".equals(game.getGameType())) {
                        pdf = createZyfxqPdf(activityId, game.getGameId(), userId);
                        if (pdf == null) pdf = createGamePdf(instance, area, game, participation);
                    } else if ("jlcz".equals(game.getGameType())) {
                        pdf = createJlczPdf(activityId, game.getGameId(), userId);
                        if (pdf == null) pdf = createGamePdf(instance, area, game, participation);
                    } else if ("zmwl".equals(game.getGameType())) {
                        pdf = createZmwlPdf(activityId, game.getGameId(), userId);
                        if (pdf == null) pdf = createGamePdf(instance, area, game, participation);
                    } else if ("zyjdc".equals(game.getGameType())) {
                        pdf = createZyjdcPdf(activityId, game.getGameId(), userId);
                        if (pdf == null) pdf = createGamePdf(instance, area, game, participation);
                    } else if ("zytj".equals(game.getGameType())) {
                        pdf = createZytjPdf(activityId, game.getGameId(), userId);
                        if (pdf == null) pdf = createGamePdf(instance, area, game, participation);
                    } else if ("zdyp".equals(game.getGameType())) {
                        pdf = createZdypPdf(activityId, game.getGameId(), userId);
                        if (pdf == null) pdf = createGamePdf(instance, area, game, participation);
                    } else if ("zycck".equals(game.getGameType())) {
                        pdf = zycckReportPdfService.createForActivity(student.getSchoolId(), activityId, game.getGameId(), userId);
                    } else if ("sszctop".equals(game.getGameType())) {
                        // 职场 TOP 使用结算时保存的个人快照，内容与游戏结束后查看的个人报告保持一致。
                        pdf = createSszctopTextPdf(activityId, game.getGameId(), userId);
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
        return AjaxResult.success("报告压缩包生成成功", "/api/common/report-cache/" + token + ".zip");
    }

    /** 报告包必须使用当前活动实例的终局记录，避免同一游戏在别的活动中的记录遮住本场结果。 */
    private GkzhGameParticipation findFinishedParticipation(Long activityId, Long gameId, Long userId) {
        if (activityId == null || gameId == null || userId == null) return null;
        return gameParticipationMapper.selectOne(new QueryWrapper<GkzhGameParticipation>()
                .eq("instance_id", activityId)
                .eq("game_id", gameId)
                .eq("user_id", userId)
                .in("status", "1", "2")
                .last("ORDER BY CASE status WHEN '1' THEN 0 WHEN '2' THEN 1 ELSE 2 END, create_time DESC LIMIT 1"));
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
        lines.add("本局结果");
        int detailCount = appendResultDetails(lines, parseResult(participation.getResultJson()), "", 0);
        if (detailCount == 0) lines.add("该游戏未保存可下载的个人结果明细。");
        return createTextPdf(lines);
    }

    /** 使用能量雷达结束页同一份游戏记录和结果组装逻辑生成可检索文字 PDF。 */
    @SuppressWarnings("unchecked")
    private byte[] createNlldPdf(Long activityId, Long gameId, Long userId) throws Exception {
        NlldRecord record = nlldRecordMapper.selectOne(new QueryWrapper<NlldRecord>()
                .eq("instance_id", activityId).eq("game_id", gameId).eq("user_id", userId)
                .eq("status", "finished").last("LIMIT 1"));
        if (record == null) return null;

        Map<String, Object> view = nlldGameService.viewRecord(record.getRecordId(), userId);
        Object rawResult = view.get("result");
        Map<String, Object> result = rawResult instanceof Map ? (Map<String, Object>) rawResult : new HashMap<>();
        List<String> lines = new java.util.ArrayList<>();
        lines.add("我的职业能量雷达报告");
        lines.add("结果类型：" + ("top2".equals(result.get("resultType")) ? "TOP2 类型" : "多元开放型"));
        if ("top2".equals(result.get("resultType"))) {
            appendNlldDimension(lines, "第一推荐维度", result.get("top1"));
            appendNlldDimension(lines, "第二推荐维度", result.get("top2"));
        } else {
            addLine(lines, "结果说明", result.get("openText"));
        }
        return createTextPdf(lines);
    }

    private void appendNlldDimension(List<String> lines, String title, Object rawDimension) {
        if (!(rawDimension instanceof Map)) return;
        Map<?, ?> dimension = (Map<?, ?>) rawDimension;
        Object name = dimension.get("name");
        lines.add(title + "：" + text(name == null ? null : String.valueOf(name), "未记录"));
        addLine(lines, "维度特点", dimension.get("summary"));
        addLine(lines, "职业参考", dimension.get("careerReference"));
    }

    private void addLine(List<String> lines, String label, Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) return;
        lines.add(label + "：" + value);
    }

    private byte[] createZcyltzjPdf(Long activityId, Long gameId, Long userId) throws Exception {
        ZcyltzjRecord record = zcyltzjRecordMapper.selectOne(new QueryWrapper<ZcyltzjRecord>()
                .eq("instance_id", activityId).eq("game_id", gameId).eq("user_id", userId)
                .eq("status", "finished").last("LIMIT 1"));
        if (record == null) return null;
        Map<String, Object> view = zcyltzjRecordService.view(record);
        Object rawResult = view.get("result");
        Map<?, ?> result = rawResult instanceof Map ? (Map<?, ?>) rawResult : java.util.Collections.emptyMap();
        List<String> lines = new java.util.ArrayList<>();
        lines.add("我的压力接受度报告");
        Object rawPressure = result.get("pressureValue");
        int pressure = rawPressure instanceof Number ? ((Number) rawPressure).intValue() : 0;
        pressure = Math.max(0, Math.min(100, pressure));
        addLine(lines, "我的压力值是", pressure + "%");
        addLine(lines, "测评项目", view.get("pointName"));
        String color = String.valueOf(result.get("color"));
        addLine(lines, "结果区间", "red".equals(color) ? "红色区间" : "yellow".equals(color) ? "临界区间" : "绿色区间");
        addLine(lines, "压力主题", view.get("theme"));
        addLine(lines, "职场投射", view.get("projection"));
        addLine(lines, "红色答案", result.get("redCount"));
        addLine(lines, "绿色答案", result.get("greenCount"));
        addLine(lines, "答题总数", result.get("total"));
        return createTextPdf(lines);
    }

    private byte[] createZyfxqPdf(Long activityId, Long gameId, Long userId) throws Exception {
        ZyfxqRecord record = zyfxqRecordMapper.selectOne(new QueryWrapper<ZyfxqRecord>()
                .eq("instance_id", activityId).eq("game_id", gameId).eq("user_id", userId)
                .eq("status", "finished").last("LIMIT 1"));
        if (record == null) return null;
        Map<String, Object> view = zyfxqGameService.view(record);
        List<String> lines = new java.util.ArrayList<>();
        lines.add("职业飞行棋对局报告");
        String result = String.valueOf(view.get("result"));
        boolean win = "win".equalsIgnoreCase(result);
        lines.add(win ? "恭喜你完成了本次挑战" : "很遗憾，本局未通过");
        lines.add(win
                ? "你在 " + zyfxqDuration(view.get("durationMs")) + " 内抵达终点，本局成绩已计入活动统计。"
                : "3 分钟内没有抵达终点，可以重新开始一局，成绩以最新一局为准。");
        lines.add("本局数据");
        lines.add("本局总时长：" + zyfxqDuration(view.get("durationMs")));
        lines.add("掷骰总次数：" + numberOrZero(view.get("rollCount")) + " 次");
        lines.add("累计行进总步数：" + numberOrZero(view.get("stepTotal")) + " 步");
        lines.add("抵达格号：" + numberOrZero(view.get("position")) + " / 19");
        lines.add("作答题数：" + numberOrZero(view.get("answerCount")) + " 题");
        lines.add("答对题数：" + numberOrZero(view.get("correctCount")) + " 题");
        lines.add("超时题数：" + numberOrZero(view.get("timeoutCount")) + " 题");
        lines.add("第几局：第 " + numberOrDefault(view.get("attemptNo"), 1) + " 局");
        lines.add("本局开始：" + zyfxqTime(view.get("startTime")));
        lines.add("本局结束：" + zyfxqTime(view.get("endTime")));
        lines.add("答对题目原地继续摇骰，答错或超时后退 2 格；3 分钟内抵达第 19 格即通关。");
        lines.add("答题明细");
        Object rawAnswers = view.get("answers");
        if (!(rawAnswers instanceof List) || ((List<?>) rawAnswers).isEmpty()) {
            lines.add("本局还没有触发过题目，下次落点停在答题格就会弹出题目。");
        } else {
            for (Object rawAnswer : (List<?>) rawAnswers) {
                if (!(rawAnswer instanceof Map)) continue;
                Map<?, ?> answer = (Map<?, ?>) rawAnswer;
                String cell = textValue(answer.get("cellNo"), "-");
                String type = textValue(answer.get("questionTypeName"), "题目");
                String answerResult = textValue(answer.get("result"), "-");
                lines.add("第 " + cell + " 格 · " + type + " · " + answerResult);
                lines.add("题目：" + textValue(answer.get("title"), "-"));
                lines.add("题目限时：" + textValue(answer.get("limitSeconds"), "-") + " 秒");
                lines.add("实际答题：" + zyfxqCost(answer.get("costMs")));
                lines.add("是否超时：" + (Boolean.TRUE.equals(answer.get("timeout")) ? "是" : "否"));
                String option = textValue(answer.get("optionContent"), "超时未作答");
                String optionLabel = textValue(answer.get("optionLabel"), "");
                lines.add("我的选择：" + (optionLabel.isEmpty() || option.startsWith("超时") ? option : optionLabel + ". " + option));
                String correct = textValue(answer.get("correctContent"), "-");
                String correctLabel = textValue(answer.get("correctLabel"), "");
                lines.add("正确答案：" + (correctLabel.isEmpty() ? correct : correctLabel + ". " + correct));
            }
        }
        return createTextPdf(lines);
    }

    /** 简历称重个人报告：与小程序结果页同源，输出总重、分类小计、缺口与建议。 */
    private byte[] createJlczPdf(Long activityId, Long gameId, Long userId) throws Exception {
        JlczRecord record = jlczRecordMapper.selectOne(new QueryWrapper<JlczRecord>()
                .eq("instance_id", activityId).eq("game_id", gameId).eq("user_id", userId)
                .eq("status", "finished").last("LIMIT 1"));
        if (record == null) return null;
        Map<String, Object> view = jlczGameService.view(record);
        List<String> lines = new java.util.ArrayList<>();
        Map<?, ?> result = view.get("result") instanceof Map ? (Map<?, ?>) view.get("result") : new java.util.HashMap<>();
        Map<?, ?> setting = view.get("setting") instanceof Map ? (Map<?, ?>) view.get("setting") : new java.util.HashMap<>();
        boolean pass = Boolean.TRUE.equals(result.get("pass"));
        boolean balanced = Boolean.TRUE.equals(result.get("balanced"));
        lines.add("简历称重报告");
        lines.add(pass ? (balanced ? "你的简历分量刚好达到通关标准" : "你的简历分量达到了通关标准")
                : "你的简历分量还差一点");
        lines.add("总重量：" + numberOrZero(result.get("totalWeight")) + "g（标准 "
                + numberOrZero(result.get("passWeight")) + "g）");
        if (!pass) {
            lines.add("距通关还差：" + numberOrZero(result.get("gap")) + "g");
        }
        lines.add("本次勾选：" + numberOrZero(result.get("selectedCount")) + " 项");
        lines.add("称重用时：" + zyfxqDuration(result.get("durationMs")));
        lines.add("称重次数：第 " + numberOrDefault(result.get("attemptNo"), 1) + " 次");
        lines.add("左侧标准砝码：" + numberOrZero(setting.get("leftWeightG")) + "g");
        lines.add("分类小计");
        Object rawCategories = result.get("categoryTotals");
        if (rawCategories instanceof List) {
            for (Object raw : (List<?>) rawCategories) {
                if (!(raw instanceof Map)) continue;
                Map<?, ?> category = (Map<?, ?>) raw;
                lines.add(textValue(category.get("categoryName"), "类别") + "："
                        + numberOrZero(category.get("totalWeight")) + "g");
            }
        }
        lines.add("本次勾选的砝码");
        Object rawSelected = view.get("selected");
        if (!(rawSelected instanceof List) || ((List<?>) rawSelected).isEmpty()) {
            lines.add("本次没有勾选任何砝码，可以重新称重后再试。");
        } else {
            for (Object raw : (List<?>) rawSelected) {
                if (!(raw instanceof Map)) continue;
                Map<?, ?> weight = (Map<?, ?>) raw;
                lines.add("第 " + textValue(weight.get("weightNo"), "-") + " 号 · "
                        + textValue(weight.get("label"), "-") + " · " + numberOrZero(weight.get("weightG")) + "g");
            }
        }
        Object rawSuggestions = result.get("suggestions");
        if (!pass && rawSuggestions instanceof List && !((List<?>) rawSuggestions).isEmpty()) {
            lines.add("优先补充建议");
            for (Object raw : (List<?>) rawSuggestions) {
                if (!(raw instanceof Map)) continue;
                Map<?, ?> weight = (Map<?, ?>) raw;
                lines.add("第 " + textValue(weight.get("weightNo"), "-") + " 号 · "
                        + textValue(weight.get("label"), "-") + "（" + numberOrZero(weight.get("weightG")) + "g）");
            }
        }
        lines.add("提示：通关标准为总重达到 " + numberOrZero(setting.get("passWeightG"))
                + "g；本结果为简历自查，仅用于活动体验与简历改进参考。");
        return createTextPdf(lines);
    }

    /** 职面未来个人报告：与小程序选择卡同源，输出方向、标签、方向解读与寄语。 */
    private byte[] createZdypPdf(Long activityId, Long gameId, Long userId) throws Exception {
        ZdypRecord record = zdypRecordMapper.selectOne(new QueryWrapper<ZdypRecord>()
                .eq("instance_id", activityId).eq("game_id", gameId).eq("user_id", userId)
                .eq("status", "finished").last("LIMIT 1"));
        if (record == null) return null;
        List<String> lines = new java.util.ArrayList<>();
        lines.add("我的职业拼图报告");
        lines.add("岗位：" + textValue(record.getCareerName(), "-"));
        lines.add("职业认知分型：" + textValue(record.getBehaviorName(), "-"));
        lines.add("闯关次数：" + numberOrZero(record.getAttemptNo()) + " 次");
        if (record.getBestMs() != null) {
            lines.add("最快通关用时：" + String.format(java.util.Locale.ROOT, "%.1f 秒", record.getBestMs() / 1000d));
        }
        lines.add("个人结论");
        lines.add(textValue(record.getConclusion(), "暂无个人结论"));
        lines.add("提升建议");
        lines.add(textValue(record.getSuggestion(), "暂无提升建议"));

        lines.add("这张岗位拼图");
        if (record.getCareerId() != null) {
            for (ZdypCard card : zdypConfigService.listAllCards(record.getCareerId())) {
                String dimension = ZdypConfigService.SLOTS.get(card.getDimensionCode());
                lines.add("【" + textValue(dimension, "岗位信息") + "】" + textValue(card.getCardText(), "-"));
            }
        }

        ZdypCareer career = null;
        for (ZdypCareer item : zdypConfigService.listAllCareers()) {
            if (record.getCareerId() != null && record.getCareerId().equals(item.getCareerId())) {
                career = item;
                break;
            }
        }
        lines.add("岗位解析");
        lines.add("岗位定义：" + textValue(career == null ? null : career.getIntro(), "暂无岗位定义"));
        lines.add("行业价值：" + textValue(career == null ? null : career.getIndustryValue(), "暂无行业价值说明"));
        lines.add("新生入门建议：" + textValue(career == null ? null : career.getNewcomerAdvice(), "暂无入门建议"));
        lines.add("提示：拼图结果只反映本次职业信息加工表现，不代表能力评价，"
                + "建议结合岗位真实要求继续探索，并主动了解职业缺点与行业风险。");
        return createTextPdf(lines);
    }

    private byte[] createZmwlPdf(Long activityId, Long gameId, Long userId) throws Exception {
        ZmwlRecord record = zmwlRecordMapper.selectOne(new QueryWrapper<ZmwlRecord>()
                .eq("instance_id", activityId).eq("game_id", gameId).eq("user_id", userId)
                .eq("status", "finished").last("LIMIT 1"));
        if (record == null) return null;
        List<String> lines = new java.util.ArrayList<>();
        lines.add("我的未来选择卡");
        lines.add("这是我选择的未来发展方向");
        lines.add("发展方向：" + textValue(record.getDirectionName(), "-"));
        if (record.getTagName() != null && !record.getTagName().trim().isEmpty()) {
            lines.add("方向标签：" + record.getTagName());
        }
        lines.add("方向解读");
        lines.add(textValue(record.getInterpretation(), "暂无方向解读"));
        lines.add("寄语");
        lines.add(textValue(record.getMessage(), "暂无寄语"));
        if (record.getFinishTime() != null) {
            lines.add("确认时间：" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                    .format(record.getFinishTime()));
        }
        lines.add("提示：方向选择没有对错，本结果仅用于帮助你思考未来的发展方向，"
                + "可结合自身兴趣、能力和实际情况继续探索。");
        return createTextPdf(lines);
    }

    private String numberOrZero(Object value) {
        return value == null ? "0" : String.valueOf(value);
    }

    private String numberOrDefault(Object value, int fallback) {
        return value == null ? String.valueOf(fallback) : String.valueOf(value);
    }

    private String textValue(Object value, String fallback) {
        if (value == null || String.valueOf(value).trim().isEmpty()) return fallback;
        return String.valueOf(value);
    }

    private String zyfxqDuration(Object value) {
        if (value == null) return "-";
        try {
            double seconds = Double.parseDouble(String.valueOf(value)) / 1000d;
            return String.format(java.util.Locale.ROOT, "%.1f 秒", seconds);
        } catch (NumberFormatException ignored) {
            return String.valueOf(value);
        }
    }

    private String zyfxqCost(Object value) {
        if (value == null) return "-";
        try {
            double seconds = Double.parseDouble(String.valueOf(value)) / 1000d;
            return String.format(java.util.Locale.ROOT, "%.1f 秒", seconds);
        } catch (NumberFormatException ignored) {
            return String.valueOf(value);
        }
    }

    private String zyfxqTime(Object value) {
        if (value == null) return "-";
        if (value instanceof java.util.Date) return new SimpleDateFormat("yyyy-MM-dd HH:mm").format((java.util.Date) value);
        return String.valueOf(value);
    }

    private byte[] createZyjdcPdf(Long activityId, Long gameId, Long userId) throws Exception {
        ZyjdcRecord record = zyjdcRecordMapper.selectOne(new QueryWrapper<ZyjdcRecord>()
                .eq("instance_id", activityId).eq("game_id", gameId).eq("user_id", userId)
                .in("result", "win", "lose", "draw").last("LIMIT 1"));
        if (record == null) return null;
        Map<String, Object> view = zyjdcGameService.viewRecord(record.getRecordId(), userId);
        List<String> lines = new java.util.ArrayList<>();
        lines.add("职业角斗场对局报告");
        String result = textValue(view.get("result"), "draw");
        boolean win = "win".equalsIgnoreCase(result);
        String resultTitle = win ? "你赢下了这一局" : ("lose".equalsIgnoreCase(result) ? "这一局没有获胜" : "这一局打成平局");
        lines.add(resultTitle);
        lines.add(zyjdcResultSub(result, textValue(view.get("resultReason"), "")));
        lines.add("角色：" + textValue(view.get("roleName"), "—"));
        lines.add("尝试：" + numberOrDefault(view.get("attemptNo"), 1) + " 次");
        lines.add("步数：" + numberOrZero(view.get("moveCount")));

        Object rawRole = view.get("role");
        ZyjdcRole role = rawRole instanceof ZyjdcRole ? (ZyjdcRole) rawRole : null;
        lines.add("角色档案");
        lines.add("角色名称：" + textValue(view.get("roleName"), "—"));
        lines.add("职业名称：" + textValue(view.get("jobTitle"), "—"));
        lines.add("角色解读：" + roleText(role == null ? null : role.getRoleInterpretation(), "这是你在本局选择的角色。"));
        lines.add("职场定位");
        lines.add(roleText(role == null ? null : role.getPositioning(), role == null ? textValue(view.get("jobTitle"), "暂无内容") : "暂无内容"));
        lines.add("能力优势");
        lines.add(roleText(role == null ? null : role.getAdvantage(), role == null ? textValue(view.get("jobTitle"), "暂无内容") : "暂无内容"));
        lines.add("生存法则");
        lines.add(roleText(role == null ? null : role.getSurvivalRule(), "暂无内容"));
        lines.add("职场能力");
        lines.add(roleText(role == null ? null : role.getCareerAbility(), "暂无内容"));
        return createTextPdf(lines);
    }

    private String roleText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String zyjdcResultSub(String result, String reason) {
        boolean win = "win".equalsIgnoreCase(result);
        if ("den".equals(reason)) return win ? "你先一步踏入对方兽穴，赢下本局并获得通关印戳。" : "对方先一步踏入你的兽穴，本局失利，可以重新挑战。";
        if ("capture_all".equals(reason)) return win ? "你吃光了对方全部棋子，获得通关印戳。" : "你的棋子被对方全部吃掉，可以重新挑战。";
        if ("no_move".equals(reason)) return win ? "对方无子可动，你获得胜利。" : "我方无子可动，本局判负。";
        if ("timeout".equals(reason)) return win ? "时间到，你剩余棋子的最高等级占优，获得胜利。" : "时间到，按剩余棋子等级与数量结算。";
        return win ? "恭喜完成本局对弈。" : "调整策略后再试一次。";
    }

    /** 职业探索卡 PDF：使用提交时写入的人格类型与三段文案快照，与结果页保持一致。 */
    private byte[] createZytjPdf(Long activityId, Long gameId, Long userId) throws Exception {
        ZytjRecord record = zytjRecordMapper.selectOne(new QueryWrapper<ZytjRecord>()
                .eq("instance_id", activityId).eq("game_id", gameId).eq("user_id", userId)
                .eq("game_type", "zytj").orderByDesc("record_id").last("limit 1"));
        if (record == null || record.getTypeCode() == null) return null;
        List<String> lines = new java.util.ArrayList<>();
        lines.add("职愿探究 · 职业探索卡");
        addLine(lines, "你的人格类型是", record.getTypeCode());
        addLine(lines, "性格特点", record.getFeature());
        addLine(lines, "适合职业领域", record.getCareerField());
        addLine(lines, "典型职业", record.getTypicalCareers());
        addLine(lines, "用时（秒）", record.getDurationMs() == null ? null : record.getDurationMs() / 1000L);
        if ("1".equals(record.getIsTimeout())) {
            addLine(lines, "提示", "本局到点自动提交，未选满的列已按该列首个物品补全。");
        }
        return createTextPdf(lines);
    }

    private byte[] createSszctopTextPdf(Long activityId, Long gameId, Long userId) throws Exception {
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
        List<String> lines = new java.util.ArrayList<>();
        lines.add("谁是职场 TOP 个人报告");
        lines.add("对局结果：" + (passed ? "挑战成功" : "本局未通过"));
        addLine(lines, "本次选择的维度", dimension == null ? null : dimension.getName());
        addLine(lines, "维度解释", dimension == null ? null : dimension.getDescription());
        lines.add("你选择的职业与个人排序");
        for (int i = 0; i < selectedIds.size(); i++) {
            Long id = selectedIds.get(i);
            SszctopCareer career = career(careers, id);
            addLine(lines, "第 " + (i + 1) + " 名", career == null ? "未记录职业" : career.getName());
            addLine(lines, "职业排序说明", rankDescription(rankDetails, id));
            if (i < standardIds.size() && Objects.equals(id, standardIds.get(i))) addLine(lines, "排序校验", "正确");
        }
        if (!passed) {
            lines.add("正确排序");
            for (int i = 0; i < standardIds.size(); i++) {
                SszctopCareer career = career(careers, standardIds.get(i));
                addLine(lines, "第 " + (i + 1) + " 名", career == null ? "未记录职业" : career.getName());
            }
        }
        return createTextPdf(lines);
    }

    /** 仅展开报告页会展示的字段，避免把棋盘配置等内部状态写进报告。 */
    private void appendReportFields(List<String> lines, Map<String, Object> data,
                                    java.util.Set<String> keys, int depth) {
        if (data == null) return;
        for (String key : keys) {
            Object value = data.get(key);
            if (value == null) continue;
            if (value instanceof Map || value instanceof List) {
                lines.add(resultLabel(key));
                appendReportValue(lines, value, resultLabel(key), depth + 1);
            } else {
                addLine(lines, resultLabel(key), value);
            }
        }
    }

    private void appendReportValue(List<String> lines, Object value, String parent, int depth) {
        if (depth > 4 || lines.size() >= 120 || value == null) return;
        if (value instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (lines.size() >= 120) break;
                String key = resultLabel(String.valueOf(entry.getKey()));
                Object child = entry.getValue();
                String label = parent + " - " + key;
                if (child instanceof Map || child instanceof List) appendReportValue(lines, child, label, depth + 1);
                else addLine(lines, label, child);
            }
        } else if (value instanceof List) {
            int i = 1;
            for (Object child : (List<?>) value) {
                if (lines.size() >= 120) break;
                appendReportValue(lines, child, parent + " " + i++, depth + 1);
            }
        } else {
            addLine(lines, parent, value);
        }
    }

    private JSONObject parseResult(String resultJson) {
        if (resultJson == null || resultJson.trim().isEmpty()) return null;
        try {
            return JSON.parseObject(resultJson);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** 递归展开统一终局快照，让没有专属 PDF 模板的新旧游戏也能下载个人结果。 */
    private int appendResultDetails(List<String> lines, Object value, String parent, int depth) {
        if (depth > 5 || lines.size() >= 120 || value == null) return 0;
        int count = 0;
        if (value instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (lines.size() >= 120) break;
                String key = String.valueOf(entry.getKey());
                String label = resultLabel(key);
                Object child = entry.getValue();
                if (child == null) continue;
                if (child instanceof Map || child instanceof List) {
                    count += appendResultDetails(lines, child, parent.isEmpty() ? label : parent + " - " + label, depth + 1);
                } else {
                    String text = String.valueOf(child).trim();
                    if (text.isEmpty()) continue;
                    if ("result".equals(key)) text = resultName(text);
                    if (text.length() > 3000) text = text.substring(0, 2997) + "...";
                    lines.add((parent.isEmpty() ? "" : parent + " - ") + label + "：" + text);
                    count++;
                }
            }
        } else if (value instanceof List) {
            int index = 1;
            for (Object item : (List<?>) value) {
                if (lines.size() >= 120) break;
                count += appendResultDetails(lines, item, parent + " " + index++, depth + 1);
            }
        }
        return count;
    }

    private String resultLabel(String key) {
        Map<String, String> labels = new HashMap<>();
        labels.put("result", "对局结果"); labels.put("resultType", "结果类型");
        labels.put("pointNo", "骰子点数"); labels.put("pointName", "测评项目");
        labels.put("pressureValue", "压力值"); labels.put("color", "结果等级");
        labels.put("attemptNo", "局数"); labels.put("rollCount", "掷骰次数");
        labels.put("durationMs", "本局时长（毫秒）"); labels.put("roleName", "职业角色");
        labels.put("jobTitle", "职业名称"); labels.put("reason", "结束原因");
        labels.put("top1", "第一推荐"); labels.put("top2", "第二推荐");
        labels.put("openText", "结果说明"); labels.put("scores", "测评得分");
        labels.put("roomCode", "房间编号");
        labels.put("resultTypeName", "结果类型"); labels.put("redCount", "红色答案数");
        labels.put("greenCount", "绿色答案数"); labels.put("total", "答题总数");
        labels.put("theme", "压力主题"); labels.put("projection", "职场投射");
        labels.put("summary", "维度特点"); labels.put("careerReference", "职业参考");
        labels.put("correctCount", "答对题数"); labels.put("timeoutCount", "超时题数");
        labels.put("answerCount", "作答题数"); labels.put("stepTotal", "累计行进步数");
        labels.put("position", "抵达格号"); labels.put("durationMs", "本局时长（毫秒）");
        labels.put("finishTime", "完成时间"); labels.put("endTime", "本局结束时间");
        labels.put("startTime", "本局开始时间"); labels.put("attemptNo", "局数");
        return labels.getOrDefault(key, key);
    }

    private String resultName(String result) {
        if ("win".equalsIgnoreCase(result) || "passed".equalsIgnoreCase(result)) return "已通过";
        if ("lose".equalsIgnoreCase(result) || "failed".equalsIgnoreCase(result)) return "未通过";
        return result;
    }

    private <T> T parseObject(String value, Class<T> type) { try { return JSON.parseObject(value, type); } catch (Exception ignored) { return null; } }
    private <T> List<T> parseList(String value, Class<T> type) { try { List<T> list = JSON.parseArray(value, type); return list == null ? new java.util.ArrayList<>() : list; } catch (Exception ignored) { return new java.util.ArrayList<>(); } }
    private List<Long> parseIds(String value) { List<Long> ids = new java.util.ArrayList<>(); if (value == null || value.trim().isEmpty()) return ids; for (String id : value.split(",")) try { ids.add(Long.valueOf(id)); } catch (NumberFormatException ignored) {} return ids; }
    private List<SszctopDimensionRank> parseRankDetails(String reportJson) { try { JSONObject json = JSON.parseObject(reportJson); List<SszctopDimensionRank> list = JSON.parseArray(JSON.toJSONString(json.get("rankDetails")), SszctopDimensionRank.class); return list == null ? new java.util.ArrayList<>() : list; } catch (Exception ignored) { return new java.util.ArrayList<>(); } }
    private SszctopCareer career(List<SszctopCareer> careers, Long id) { for (SszctopCareer career : careers) if (Objects.equals(career.getCareerId(), id)) return career; return null; }
    private String rankDescription(List<SszctopDimensionRank> ranks, Long careerId) { for (SszctopDimensionRank rank : ranks) if (Objects.equals(rank.getCareerId(), careerId)) return text(rank.getDescription(), "暂无该维度下的职业说明"); return "暂无该维度下的职业说明"; }
    /** 使用 HTML 排版输出文字 PDF，避免将报告内容栅格化为截图。 */
    private byte[] createTextPdf(List<String> sections) throws Exception {
        StringBuilder html = new StringBuilder("<!DOCTYPE html><html><head><meta charset='UTF-8'/><style>")
                .append("@page{size:A4;margin:22mm 18mm}body{font-family:GKZH,sans-serif;color:#24364b;font-size:12pt;line-height:1.65}")
                .append("h1{font-size:23pt;color:#244e87;border-bottom:2px solid #78a9d8;padding-bottom:12px;margin:0 0 24px}")
                .append("h2{font-size:15pt;color:#244e87;margin:22px 0 8px;page-break-after:avoid}")
                .append(".row{padding:9px 12px;border-bottom:1px solid #e5eaf0;page-break-inside:avoid}")
                .append(".label{font-weight:bold;color:#334b66}.value{white-space:pre-wrap;overflow-wrap:anywhere}")
                .append("</style></head><body>");
        for (int i = 0; i < sections.size(); i++) {
            String section = sections.get(i) == null ? "" : sections.get(i);
            if (i == 0) {
                html.append("<h1>").append(escape(section)).append("</h1>");
            } else if (section.matches("(?:本局数据|答题明细|本局结果|15 项维度得分|兴趣类型说明|职业方向|工作环境偏好|你选择的职业与个人排序|正确排序)")) {
                html.append("<h2>").append(escape(section)).append("</h2>");
            } else {
                int split = section.indexOf('：');
                if (split < 0) split = section.indexOf(':');
                html.append("<div class='row'>");
                if (split > 0) {
                    html.append("<span class='label'>").append(escape(section.substring(0, split + 1))).append("</span> ")
                            .append("<span class='value'>").append(escape(section.substring(split + 1))).append("</span>");
                } else {
                    html.append("<span class='value'>").append(escape(section)).append("</span>");
                }
                html.append("</div>");
            }
        }
        html.append("</body></html>");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder().withHtmlContent(html.toString(), null).toStream(out);
            ReportPdfFont.register(builder);
            builder.run();
            return out.toByteArray();
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

    /** 使用结束页报告数据生成可检索文字 PDF。 */
    private byte[] createPdf(Map result) throws Exception {
        String code = result.get("code") == null ? "" : String.valueOf(result.get("code"));
        Map<String,HollandCode> details = new HashMap<>();
        for (HollandCode item : hollandCodeService.listCodes()) details.put(item.getCode(), item);
        List<String> lines = new java.util.ArrayList<>();
        lines.add("职业兴趣测评报告");
        addLine(lines, "霍兰德代码", code);
        lines.add("兴趣类型说明");
        for (int i=0; i<code.length(); i++) {
            HollandCode item = details.get(String.valueOf(code.charAt(i)));
            if (item != null) {
                addLine(lines, item.getName() + "（" + item.getCode() + "）特点", item.getSummary());
                addLine(lines, "生活事例", item.getLife());
            }
        }
        lines.add("职业方向");
        appendTitleValues(lines, result.get("careers"));
        lines.add("工作环境偏好");
        appendTitleValues(lines, result.get("workEnvs"));
        return createTextPdf(lines);
    }

    private void appendTitleValues(List<String> lines, Object rawItems) {
        if (!(rawItems instanceof Iterable)) return;
        for (Object item : (Iterable<?>) rawItems) {
            if (item instanceof Map) {
                Object title = ((Map<?, ?>) item).get("title");
                addLine(lines, title == null ? "项目" : String.valueOf(title), ((Map<?, ?>) item).get("description"));
            } else if (item != null) lines.add(String.valueOf(item));
        }
    }
    private String safeName(String value) { return value == null || value.trim().isEmpty() ? "未分类区域" : value.replaceAll("[\\\\/:*?\"<>|]", "_"); }
}
