package com.gkzh.nlld.service;

import com.gkzh.nlld.domain.NlldDimension;
import com.gkzh.nlld.domain.dto.NlldRecordQuery;
import com.gkzh.nlld.mapper.NlldRecordMapper;
import com.gkzh.nlld.support.NlldScoring;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 能量雷达统计分析服务。
 * 后台统计、活动查看页和 Excel 导出共用同一套参与人数、完成率和 15 维偏好口径。
 */
@Service
public class NlldStatisticsService {

    private final NlldRecordMapper recordMapper;
    private final NlldConfigService configService;

    public NlldStatisticsService(NlldRecordMapper recordMapper, NlldConfigService configService) {
        this.recordMapper = recordMapper;
        this.configService = configService;
    }

    /** 汇总参与人数、完成人数、TOP2/多元开放型人数和完成率。 */
    public Map<String, Object> summary(NlldRecordQuery query) {
        Map<String, Object> raw = recordMapper.selectAdminSummary(query);
        long total = number(raw == null ? null : raw.get("total"));
        long started = number(raw == null ? null : raw.get("startedCount"));
        long finished = number(raw == null ? null : raw.get("finished"));
        long top2 = number(raw == null ? null : raw.get("top2Count"));
        long open = number(raw == null ? null : raw.get("openCount"));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enteredCount", total);
        data.put("startedCount", started);
        data.put("completedCount", finished);
        data.put("top2Count", top2);
        data.put("openCount", open);
        data.put("completionRate", percent(finished, total));
        data.put("top2Rate", percent(top2, finished));
        data.put("openRate", percent(open, finished));
        return data;
    }

    /** 15 个维度的被选次数、选择占比、得 2 分人数和进入 TOP2 人数。 */
    public List<Map<String, Object>> dimensionStats(NlldRecordQuery query, Map<String, Object> summary) {
        Map<String, Long> selectedMap = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectDimensionSelectionCounts(query)) {
            selectedMap.put(String.valueOf(row.get("dimensionCode")),
                    number(row.get("selectedCount")));
        }
        Map<String, Long> twoPointMap = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectDimensionTwoPointCounts(query)) {
            twoPointMap.put(String.valueOf(row.get("dimensionCode")),
                    number(row.get("recordCount")));
        }
        Map<Long, Long> top1Map = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectTop1Counts(query)) {
            top1Map.put(longValue(row.get("dimensionId")), number(row.get("recordCount")));
        }
        Map<Long, Long> top2Map = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectTop2Counts(query)) {
            top2Map.put(longValue(row.get("dimensionId")), number(row.get("recordCount")));
        }

        long selectedTotal = selectedMap.values().stream().mapToLong(Long::longValue).sum();
        long finished = number(summary == null ? null : summary.get("completedCount"));

        List<Map<String, Object>> rows = new ArrayList<>();
        int serial = 1;
        for (NlldDimension dimension : configService.listDimensions()) {
            long selected = selectedMap.getOrDefault(dimension.getDimensionCode(), 0L);
            long twoPoint = twoPointMap.getOrDefault(dimension.getDimensionCode(), 0L);
            long topCount = top1Map.getOrDefault(dimension.getDimensionId(), 0L)
                    + top2Map.getOrDefault(dimension.getDimensionId(), 0L);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("serialNo", serial++);
            row.put("dimensionId", dimension.getDimensionId());
            row.put("dimensionCode", dimension.getDimensionCode());
            row.put("dimensionName", dimension.getDimensionName());
            row.put("dimensionGroup", dimension.getDimensionGroup());
            row.put("selectedCount", selected);
            row.put("selectedRate", percent(selected, selectedTotal));
            row.put("twoPointCount", twoPoint);
            row.put("twoPointRate", percent(twoPoint, finished));
            row.put("top2Count", topCount);
            row.put("top2Rate", percent(topCount, finished));
            rows.add(row);
        }
        return rows;
    }

    /** TOP2 与多元开放型两类结果分布；未完成的记录不计入。 */
    public List<Map<String, Object>> resultTypeStats(Map<String, Object> summary) {
        long top2 = number(summary == null ? null : summary.get("top2Count"));
        long open = number(summary == null ? null : summary.get("openCount"));
        long finished = top2 + open;
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(resultRow(1, NlldScoring.RESULT_TOP2, "TOP2 类型", top2, finished));
        rows.add(resultRow(2, NlldScoring.RESULT_OPEN, "多元开放型", open, finished));
        return rows;
    }

    /** 生成学校端偏好分析文本，用于统计页和 PDF。 */
    public String analysis(Map<String, Object> summary, List<Map<String, Object>> dimensionStats,
                           List<Map<String, Object>> resultTypeStats) {
        long finished = number(summary.get("completedCount"));
        if (finished == 0) {
            return "当前游戏尚无学生完成测评。学生完成 6 关选择后，这里将展示职业价值观维度偏好、TOP2 与多元开放型分布。";
        }
        List<Map<String, Object>> ranked = new ArrayList<>(dimensionStats);
        ranked.sort((left, right) -> {
            int compare = Long.compare(number(right.get("twoPointCount")), number(left.get("twoPointCount")));
            if (compare != 0) {
                return compare;
            }
            return Long.compare(number(right.get("selectedCount")), number(left.get("selectedCount")));
        });
        StringBuilder text = new StringBuilder();
        text.append("本次共 ").append(number(summary.get("enteredCount"))).append(" 人扫码参与，")
                .append(finished).append(" 人完成测评，完成率为 ")
                .append(summary.get("completionRate")).append("%。");
        long top2 = number(summary.get("top2Count"));
        long open = number(summary.get("openCount"));
        text.append("TOP2 类型 ").append(top2).append(" 人（").append(summary.get("top2Rate"))
                .append("%），多元开放型 ").append(open).append(" 人（").append(summary.get("openRate"))
                .append("%）。");
        if (!ranked.isEmpty()) {
            Map<String, Object> first = ranked.get(0);
            text.append("最突出的价值观是“").append(first.get("dimensionName")).append("”，")
                    .append(number(first.get("twoPointCount"))).append(" 人将其选满 2 次，占完成人数的 ")
                    .append(first.get("twoPointRate")).append("%；");
            if (ranked.size() > 1) {
                Map<String, Object> second = ranked.get(1);
                text.append("其次是“").append(second.get("dimensionName")).append("”（")
                        .append(second.get("twoPointRate")).append("%）。");
            }
        }
        text.append("建议结合院系、专业和性别差异继续观察，把价值观偏好作为生涯讨论的切入口，而不是对个人能力的判断。");
        return text.toString();
    }

    /** 活动查看页汇总数据；记录明细由后台补全学生与学校字段后另行返回。 */
    public Map<String, Object> gameViewStats(Long gameId) {
        NlldRecordQuery query = new NlldRecordQuery();
        query.setGameId(gameId);
        Map<String, Object> summary = summary(query);
        List<Map<String, Object>> dimensions = dimensionStats(query, summary);
        List<Map<String, Object>> resultTypes = resultTypeStats(summary);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("totalParticipants", summary.get("enteredCount"));
        data.put("completedCount", summary.get("completedCount"));
        data.put("completionRate", summary.get("completionRate"));
        data.put("top2Count", summary.get("top2Count"));
        data.put("openCount", summary.get("openCount"));
        data.put("dimensionStats", dimensions);
        data.put("resultTypeStats", resultTypes);
        data.put("analysis", analysis(summary, dimensions, resultTypes));
        return data;
    }

    private Map<String, Object> resultRow(int serialNo, String resultType, String resultName,
                                          long count, long total) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("serialNo", serialNo);
        row.put("resultType", resultType);
        row.put("resultName", resultName);
        row.put("userCount", count);
        row.put("probability", percent(count, total));
        return row;
    }

    /** 百分比统一保留两位小数；分母为 0 时返回 0.00。 */
    private static BigDecimal percent(long value, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value * 100D / total).setScale(2, RoundingMode.HALF_UP);
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

    private static long longValue(Object value) {
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
