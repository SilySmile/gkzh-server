package com.gkzh.zdyp.service;

import com.gkzh.zdyp.domain.ZdypCareer;
import com.gkzh.zdyp.domain.dto.ZdypRecordQuery;
import com.gkzh.zdyp.mapper.ZdypRecordMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职得一拼统计分析服务（结果输出-学校端）。
 *
 * <p>统计口径：参与人数按参与记录去重；通关人数指已通关人数；维度错误、首填偏好与行为分型
 * 只统计首次尝试，避免学生看到答案后重闯的数据污染认知判断；院系取专业部门的父部门，
 * 未填写专业或部门缺失统一归入“未填写院系”。后台统计页、活动查看页和 Excel 导出共用同一套计算。</p>
 */
@Service
public class ZdypStatisticsService {

    private final ZdypRecordMapper recordMapper;
    private final ZdypConfigService configService;

    public ZdypStatisticsService(ZdypRecordMapper recordMapper, ZdypConfigService configService) {
        this.recordMapper = recordMapper;
        this.configService = configService;
    }

    /** 整体数据：参与、通关、首次通关、重闯、超时与用时指标。 */
    public Map<String, Object> summary(ZdypRecordQuery query) {
        Map<String, Object> raw = recordMapper.selectAdminSummary(query);
        if (raw == null) {
            raw = new LinkedHashMap<>();
        }
        long total = number(raw.get("total"));
        long finished = number(raw.get("finished"));
        long started = number(raw.get("started"));
        long firstPass = number(raw.get("firstPass"));
        long reattemptTotal = number(raw.get("reattemptTotal"));
        long timeoutPlayers = number(raw.get("timeoutPlayers"));
        long timeoutTotal = number(raw.get("timeoutTotal"));
        long avgFirstMs = Math.round(decimal(raw.get("avgFirstMs")).doubleValue());
        long avgBestMs = Math.round(decimal(raw.get("avgBestMs")).doubleValue());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("participantCount", total);
        data.put("startedCount", started);
        data.put("finishedCount", finished);
        data.put("inProgressCount", Math.max(0L, total - finished));
        data.put("finishRate", percent(finished, total));
        data.put("firstPassCount", firstPass);
        data.put("firstPassRate", percent(firstPass, total));
        data.put("reattemptTotal", reattemptTotal);
        data.put("avgAttempts", started == 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf((started + reattemptTotal) * 1D / started).setScale(2, RoundingMode.HALF_UP));
        data.put("timeoutPlayers", timeoutPlayers);
        data.put("timeoutCount", timeoutTotal);
        data.put("timeoutRate", percent(timeoutPlayers, total));
        data.put("avgFirstMs", avgFirstMs);
        data.put("avgFirstSeconds", seconds(avgFirstMs));
        data.put("avgBestMs", avgBestMs);
        data.put("avgBestSeconds", seconds(avgBestMs));
        data.put("minBestSeconds", seconds(Math.round(decimal(raw.get("minBestMs")).doubleValue())));
        data.put("maxBestSeconds", seconds(Math.round(decimal(raw.get("maxBestMs")).doubleValue())));
        return data;
    }

    /**
     * 四类行为分型人数与占比。
     * 四个类型固定输出（含 0 人），未出现的类型也展示，便于学校端对比。
     */
    public List<Map<String, Object>> behaviorStats(ZdypRecordQuery query, long finished) {
        Map<String, Map<String, Object>> raw = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectBehaviorStats(query)) {
            raw.put(String.valueOf(row.get("behaviorType")), row);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String type : new String[]{ZdypBehavior.BALANCED, ZdypBehavior.ADVANTAGE,
                ZdypBehavior.WEAK, ZdypBehavior.RUSHED}) {
            Map<String, Object> stat = raw.remove(type);
            long count = stat == null ? 0L : number(stat.get("playerCount"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("behaviorType", type);
            row.put("behaviorName", ZdypBehavior.name(type));
            row.put("playerCount", count);
            row.put("rate", percent(count, finished));
            row.put("conclusion", ZdypBehavior.conclusion(type));
            row.put("suggestion", ZdypBehavior.suggestion(type));
            rows.add(row);
        }
        for (Map.Entry<String, Map<String, Object>> entry : raw.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("behaviorType", entry.getKey());
            row.put("behaviorName", entry.getValue().get("behaviorName"));
            row.put("playerCount", number(entry.getValue().get("playerCount")));
            row.put("rate", percent(number(entry.getValue().get("playerCount")), finished));
            row.put("conclusion", null);
            row.put("suggestion", null);
            rows.add(row);
        }
        return rows;
    }

    /**
     * 首次尝试各卡槽的放对与放错次数。
     * 四个卡槽固定输出（含 0 次），并给出首次正确率与未填写人数。
     */
    public List<Map<String, Object>> slotStats(ZdypRecordQuery query, long started) {
        Map<String, Map<String, Object>> raw = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectFirstAttemptSlotStats(query)) {
            raw.put(String.valueOf(row.get("slotCode")), row);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, String> slot : ZdypConfigService.SLOTS.entrySet()) {
            Map<String, Object> stat = raw.remove(slot.getKey());
            long placed = stat == null ? 0L : number(stat.get("placedCount"));
            long correct = stat == null ? 0L : number(stat.get("correctCount"));
            long wrong = stat == null ? 0L : number(stat.get("wrongCount"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("slotCode", slot.getKey());
            row.put("slotName", slot.getValue());
            row.put("placedCount", placed);
            row.put("correctCount", correct);
            row.put("wrongCount", wrong);
            row.put("unfilledCount", Math.max(0L, started - placed));
            row.put("correctRate", percent(correct, placed));
            row.put("wrongRate", percent(wrong, placed));
            rows.add(row);
        }
        return rows;
    }

    /** 首次尝试首选卡槽的分布：判断学生先看优势板块还是先看职业缺点板块。 */
    public List<Map<String, Object>> firstSlotStats(ZdypRecordQuery query, long started) {
        Map<String, Long> raw = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectFirstSlotStats(query)) {
            raw.put(String.valueOf(row.get("slotCode")), number(row.get("playerCount")));
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, String> slot : ZdypConfigService.SLOTS.entrySet()) {
            long count = raw.getOrDefault(slot.getKey(), 0L);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("slotCode", slot.getKey());
            row.put("slotName", slot.getValue());
            row.put("playerCount", count);
            row.put("rate", percent(count, started));
            rows.add(row);
        }
        return rows;
    }

    /**
     * 各岗位的选择人数与通关人数。
     * 以当前配置的岗位为行（含停用，保证历史数据可见），未出现的岗位补 0；
     * 统计结果中出现但配置已删除的编码追加在末尾，避免历史记录丢失。
     */
    public List<Map<String, Object>> careerStats(ZdypRecordQuery query) {
        Map<String, Map<String, Object>> raw = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectCareerStats(query)) {
            raw.put(String.valueOf(row.get("careerCode")), row);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ZdypCareer career : configService.listAllCareers()) {
            Map<String, Object> stat = raw.remove(career.getCareerCode());
            long selected = stat == null ? 0L : number(stat.get("selectedCount"));
            long finished = stat == null ? 0L : number(stat.get("finishedCount"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("careerCode", career.getCareerCode());
            row.put("careerName", career.getCareerName());
            row.put("sortOrder", career.getSortOrder());
            row.put("status", career.getStatus());
            row.put("selectedCount", selected);
            row.put("finishedCount", finished);
            row.put("finishRate", percent(finished, selected));
            rows.add(row);
        }
        for (Map.Entry<String, Map<String, Object>> entry : raw.entrySet()) {
            Map<String, Object> stat = entry.getValue();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("careerCode", entry.getKey());
            row.put("careerName", stat.get("careerName"));
            row.put("sortOrder", 9999);
            row.put("status", "1");
            row.put("selectedCount", number(stat.get("selectedCount")));
            row.put("finishedCount", number(stat.get("finishedCount")));
            row.put("finishRate", percent(number(stat.get("finishedCount")), number(stat.get("selectedCount"))));
            rows.add(row);
        }
        return rows;
    }

    /** 各院系参与与通关人数、通关率。 */
    public List<Map<String, Object>> departmentStats(ZdypRecordQuery query) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> raw : recordMapper.selectDepartmentSummary(query)) {
            long participants = number(raw.get("participantCount"));
            long finished = number(raw.get("finishedCount"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("departmentName", String.valueOf(raw.get("departmentName")));
            row.put("participantCount", participants);
            row.put("finishedCount", finished);
            row.put("finishRate", percent(finished, participants));
            rows.add(row);
        }
        return rows;
    }

    /** 学校端简要分析：整体通关情况、最集中的认知短板与四类分型主流。 */
    public String analysis(Map<String, Object> summary, List<Map<String, Object>> behaviorStats,
                           List<Map<String, Object>> slotStats) {
        long total = number(summary.get("participantCount"));
        long finished = number(summary.get("finishedCount"));
        if (total == 0) {
            return "当前活动尚无学生扫码参与。学生扫码并完成一次拼图挑战后，"
                    + "这里将展示通关情况、各维度正确率与四类职业认知分型占比。";
        }
        if (finished == 0) {
            return "当前共 " + total + " 人扫码进入，尚无学生通关；"
                    + "学生完成一次拼图后即可看到通关率、维度错误与行为分型分布。";
        }
        StringBuilder text = new StringBuilder();
        text.append("本次共 ").append(total).append(" 人扫码参与，").append(finished)
                .append(" 人通关，通关率 ").append(summary.get("finishRate")).append("%；其中 ")
                .append(summary.get("firstPassCount")).append(" 人首次即通关（")
                .append(summary.get("firstPassRate")).append("%），平均重闯 ")
                .append(summary.get("avgAttempts")).append(" 次。");

        Map<String, Object> topBehavior = null;
        for (Map<String, Object> row : behaviorStats) {
            if (number(row.get("playerCount")) > 0
                    && (topBehavior == null || number(row.get("playerCount")) > number(topBehavior.get("playerCount")))) {
                topBehavior = row;
            }
        }
        if (topBehavior != null) {
            text.append("职业认知分型中占比最高的是「").append(topBehavior.get("behaviorName"))
                    .append("」（").append(topBehavior.get("playerCount")).append(" 人，占 ")
                    .append(topBehavior.get("rate")).append("%）。");
        }

        Map<String, Object> worst = null;
        for (Map<String, Object> row : slotStats) {
            if (number(row.get("placedCount")) == 0) {
                continue;
            }
            if (worst == null || number(row.get("wrongCount")) > number(worst.get("wrongCount"))) {
                worst = row;
            }
        }
        if (worst != null && number(worst.get("wrongCount")) > 0) {
            text.append("首次尝试中错配最多的是「").append(worst.get("slotName")).append("」板块（错 ")
                    .append(worst.get("wrongCount")).append(" 次，正确率 ")
                    .append(worst.get("correctRate")).append("%）；");
            if ("weakness".equals(worst.get("slotCode"))) {
                text.append("说明学生普遍回避职业缺点与岗位风险，存在“只看优势”的择业认知偏差，")
                        .append("建议在生涯指导中补强职业风险与行业痛点的辩证教育。");
            } else {
                text.append("建议按该板块补充职业信息科普，帮助学生建立完整的四维职业认知。");
            }
        }
        long timeoutPlayers = number(summary.get("timeoutPlayers"));
        if (timeoutPlayers > 0) {
            text.append("另有 ").append(timeoutPlayers).append(" 人出现过超时未完成（占 ")
                    .append(summary.get("timeoutRate")).append("%），可作为后续生涯探索节奏的观察对象。");
        }
        return text.toString();
    }

    /** 活动查看页汇总数据：学校端两页内容 + 最近参与记录。 */
    public Map<String, Object> gameViewStats(Long gameId) {
        ZdypRecordQuery query = new ZdypRecordQuery();
        query.setGameId(gameId);
        Map<String, Object> summary = summary(query);
        long finished = number(summary.get("finishedCount"));
        long started = number(summary.get("startedCount"));
        List<Map<String, Object>> behaviors = behaviorStats(query, finished);
        List<Map<String, Object>> slots = slotStats(query, started);
        List<Map<String, Object>> firstSlots = firstSlotStats(query, started);
        List<Map<String, Object>> careers = careerStats(query);
        List<Map<String, Object>> departments = departmentStats(query);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("behaviorStats", behaviors);
        data.put("slotStats", slots);
        data.put("firstSlotStats", firstSlots);
        data.put("careerStats", careers);
        data.put("departmentStats", departments);
        data.put("analysis", analysis(summary, behaviors, slots));
        return data;
    }

    /** 百分比统一保留两位小数；分母为 0 时返回 0.00。 */
    public static BigDecimal percent(long value, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value * 100D / total).setScale(2, RoundingMode.HALF_UP);
    }

    /** 毫秒转秒，保留一位小数。 */
    static BigDecimal seconds(long millis) {
        return BigDecimal.valueOf(millis / 1000D).setScale(1, RoundingMode.HALF_UP);
    }

    private static BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static long number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? 0L : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
