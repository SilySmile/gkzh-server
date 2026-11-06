package com.gkzh.jlcz.service;

import com.gkzh.jlcz.domain.JlczWeight;
import com.gkzh.jlcz.domain.dto.JlczRecordQuery;
import com.gkzh.jlcz.mapper.JlczRecordMapper;
import com.gkzh.jlcz.support.JlczScoring;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 简历称重统计分析服务（结果输出-学校端）。
 *
 * <p>统计口径：参与人数按参与记录去重；通关率 = 通关人数 ÷ 已称重人数；
 * 平均总重、平均勾选数和平均用时只统计已提交记录；砝码选择率以已称重人数为分母，
 * 未勾选人数即该项的“缺口”。后台统计页、活动查看页和 Excel 导出共用同一套计算。</p>
 */
@Service
public class JlczStatisticsService {

    private final JlczRecordMapper recordMapper;
    private final JlczConfigService configService;

    public JlczStatisticsService(JlczRecordMapper recordMapper, JlczConfigService configService) {
        this.recordMapper = recordMapper;
        this.configService = configService;
    }

    /** 第一页汇总：参与、已称重、通关、通关率、平均总重、平均勾选数与平均用时。 */
    public Map<String, Object> summary(JlczRecordQuery query) {
        Map<String, Object> raw = recordMapper.selectAdminSummary(query);
        if (raw == null) {
            raw = new LinkedHashMap<>();
        }
        long total = number(raw.get("total"));
        long finished = number(raw.get("finished"));
        long pass = number(raw.get("passCount"));
        long fail = number(raw.get("failCount"));
        long avgDurationMs = Math.round(decimal(raw.get("avgDurationMs")).doubleValue());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("participantCount", total);
        data.put("finishedCount", finished);
        data.put("passCount", pass);
        data.put("failCount", fail);
        data.put("inProgressCount", Math.max(0L, total - finished));
        data.put("passRate", percent(pass, finished));
        data.put("finishRate", percent(finished, total));
        data.put("avgTotalWeight", decimal(raw.get("avgTotalWeight")).setScale(1, RoundingMode.HALF_UP));
        data.put("avgSelectedCount", decimal(raw.get("avgSelectedCount")).setScale(1, RoundingMode.HALF_UP));
        data.put("avgDurationMs", avgDurationMs);
        data.put("avgDurationSeconds", seconds(avgDurationMs));
        return data;
    }

    /** 20 个砝码的勾选人数、选择率与未勾选缺口；未出现的砝码补 0。 */
    public List<Map<String, Object>> weightStats(JlczRecordQuery query, long finished) {
        Map<Integer, Map<String, Object>> raw = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectWeightStats(query)) {
            raw.put(integer(row.get("weightNo")), row);
        }
        List<JlczWeight> weights = configService.listAll();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (JlczWeight weight : weights) {
            Map<String, Object> stat = raw.get(weight.getWeightNo());
            long selected = stat == null ? 0L : number(stat.get("selectedCount"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("weightNo", weight.getWeightNo());
            row.put("label", weight.getLabel());
            row.put("icon", weight.getIcon());
            row.put("imageUrl", weight.getImageUrl());
            row.put("weightG", weight.getWeightG());
            row.put("categoryCode", weight.getCategoryCode());
            row.put("categoryName", weight.getCategoryName());
            row.put("status", weight.getStatus());
            row.put("selectedCount", selected);
            row.put("selectedRate", percent(selected, finished));
            row.put("gapCount", Math.max(0L, finished - selected));
            rows.add(row);
        }
        return rows;
    }

    /** 四类砝码小计：可获重量、被选次数、人均获得重量和类别选择率；缺失类别也补 0。 */
    public List<Map<String, Object>> categoryStats(List<Map<String, Object>> weightStats, long finished) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String code : JlczScoring.CATEGORY_ORDER) {
            long weightCount = 0;
            long available = 0;
            long selectedCount = 0;
            long selectedWeight = 0;
            String name = JlczScoring.categoryName(code);
            for (Map<String, Object> weight : weightStats) {
                if (!code.equals(String.valueOf(weight.get("categoryCode")))) {
                    continue;
                }
                long grams = number(weight.get("weightG"));
                long selected = number(weight.get("selectedCount"));
                weightCount++;
                available += grams;
                selectedCount += selected;
                selectedWeight += grams * selected;
                if (weight.get("categoryName") != null) {
                    name = String.valueOf(weight.get("categoryName"));
                }
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("categoryCode", code);
            row.put("categoryName", name);
            row.put("weightCount", weightCount);
            row.put("availableWeight", available);
            row.put("selectedCount", selectedCount);
            row.put("selectedWeight", selectedWeight);
            row.put("avgWeightPerParticipant", finished == 0 ? BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.valueOf(selectedWeight / (double) finished).setScale(1, RoundingMode.HALF_UP));
            row.put("selectionRate", percent(selectedCount, finished * weightCount));
            rows.add(row);
        }
        return rows;
    }

    /** 总重区间分布，用于展示“大多数简历落在哪个分量段”；必须沿用页面筛选条件。 */
    public List<Map<String, Object>> distribution(JlczRecordQuery query, Map<String, Object> summary) {
        String[] keys = {"lt60", "b60", "b70", "b80", "b90", "gt100"};
        String[] labels = {"60g 以下", "60-69g", "70-79g", "80-89g", "90-100g", "100g 以上"};
        long finished = number(summary.get("finishedCount"));
        Map<String, Object> raw = recordMapper.selectAdminSummary(query);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            long count = raw == null ? 0L : number(raw.get(keys[i]));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("bucket", keys[i]);
            row.put("label", labels[i]);
            row.put("count", count);
            row.put("rate", percent(count, finished));
            rows.add(row);
        }
        return rows;
    }

    /** 学校端简要分析：通关情况、平均分量、最薄弱类别与勾选率最低的砝码。 */
    public String analysis(Map<String, Object> summary, List<Map<String, Object>> categoryStats,
                           List<Map<String, Object>> weightStats) {
        long finished = number(summary.get("finishedCount"));
        if (finished == 0) {
            return "当前活动尚无学生完成称重。学生对照简历勾选砝码并提交后，"
                    + "这里将展示通关率、平均简历分量、四类砝码获得情况与高频缺口。";
        }
        StringBuilder text = new StringBuilder();
        text.append("本次共 ").append(number(summary.get("participantCount"))).append(" 人扫码参与，")
                .append(finished).append(" 人完成称重，其中 ").append(number(summary.get("passCount")))
                .append(" 人通关，通关率 ").append(summary.get("passRate")).append("%；")
                .append("平均简历分量 ").append(summary.get("avgTotalWeight")).append("g，")
                .append("平均勾选 ").append(summary.get("avgSelectedCount")).append(" 项，")
                .append("平均用时 ").append(summary.get("avgDurationSeconds")).append(" 秒。");
        Map<String, Object> weakest = null;
        for (Map<String, Object> category : categoryStats) {
            if (weakest == null || decimal(category.get("avgWeightPerParticipant"))
                    .compareTo(decimal(weakest.get("avgWeightPerParticipant"))) < 0) {
                weakest = category;
            }
        }
        if (weakest != null) {
            text.append("四类砝码中，人均获得最少的是「").append(weakest.get("categoryName")).append("」（人均 ")
                    .append(weakest.get("avgWeightPerParticipant")).append("g，可获 ")
                    .append(number(weakest.get("availableWeight"))).append("g）。");
        }
        Map<String, Object> lowest = null;
        for (Map<String, Object> weight : weightStats) {
            if (lowest == null || decimal(weight.get("selectedRate")).compareTo(decimal(lowest.get("selectedRate"))) < 0) {
                lowest = weight;
            }
        }
        if (lowest != null) {
            text.append("勾选率最低的砝码是「").append(lowest.get("label")).append("」（")
                    .append(lowest.get("selectedRate")).append("%，").append(number(lowest.get("gapCount")))
                    .append(" 人未勾选），可作为现场简历诊断与讲解的重点。");
        }
        return text.toString();
    }

    /** 活动查看页汇总数据：学校端两页内容 + 最近参与记录。 */
    public Map<String, Object> gameViewStats(Long gameId) {
        JlczRecordQuery query = new JlczRecordQuery();
        query.setGameId(gameId);
        Map<String, Object> summary = summary(query);
        long finished = number(summary.get("finishedCount"));
        List<Map<String, Object>> weights = weightStats(query, finished);
        List<Map<String, Object>> categories = categoryStats(weights, finished);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("weightStats", weights);
        data.put("categoryStats", categories);
        data.put("distribution", distribution(query, summary));
        data.put("analysis", analysis(summary, categories, weights));
        return data;
    }

    /** 百分比统一保留两位小数；分母为 0 时返回 0.00。 */
    static BigDecimal percent(long value, long total) {
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

    private static int integer(Object value) {
        return (int) number(value);
    }
}
