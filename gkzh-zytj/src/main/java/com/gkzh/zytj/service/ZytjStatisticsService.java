package com.gkzh.zytj.service;

import com.gkzh.zytj.domain.ZytjItem;
import com.gkzh.zytj.domain.ZytjType;
import com.gkzh.zytj.domain.dto.ZytjRecordQuery;
import com.gkzh.zytj.mapper.ZytjRecordMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职愿探究统计分析服务（结果输出-学校端）。
 *
 * <p>统计口径：参与人数按参与记录去重；已提交人数指已生成职业探索卡的人数；
 * 16 型占比以已提交人数为分母，同一学生只计一次；维度倾向按 item_codes 的四列位置
 * 拆分统计；院系取专业部门的父部门，缺失统一归入“未填写院系”。
 * 后台统计页、活动查看页和 Excel 导出共用同一套计算。</p>
 */
@Service
public class ZytjStatisticsService {

    /** 四个维度及每个维度的两个字母，顺序与选择页列序一致。 */
    private static final String[][] DIMENSIONS = {
            {"EI", "E", "I"},
            {"SN", "S", "N"},
            {"TF", "T", "F"},
            {"JP", "J", "P"}
    };

    private final ZytjRecordMapper recordMapper;
    private final ZytjConfigService configService;

    public ZytjStatisticsService(ZytjRecordMapper recordMapper, ZytjConfigService configService) {
        this.recordMapper = recordMapper;
        this.configService = configService;
    }

    /** 第一页整体数据：参与人数、已提交人数、完成率、超时人数与平均用时。 */
    public Map<String, Object> summary(ZytjRecordQuery query) {
        Map<String, Object> raw = recordMapper.selectAdminSummary(query);
        if (raw == null) {
            raw = new LinkedHashMap<>();
        }
        long total = number(raw.get("total"));
        long finished = number(raw.get("finished"));
        long timeout = number(raw.get("timeoutCount"));
        long typed = number(raw.get("typed"));
        long avgDurationMs = Math.round(decimal(raw.get("avgDurationMs")).doubleValue());

        List<Map<String, Object>> types = typeStats(query, finished);
        Map<String, Object> top = null;
        for (Map<String, Object> type : types) {
            long count = number(type.get("selectedCount"));
            if (count > 0 && (top == null || count > number(top.get("selectedCount")))) {
                top = type;
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("participantCount", total);
        data.put("finishedCount", finished);
        data.put("inProgressCount", Math.max(0L, total - finished));
        data.put("typedCount", typed);
        data.put("finishRate", percent(finished, total));
        data.put("timeoutCount", timeout);
        data.put("timeoutRate", percent(timeout, finished));
        data.put("typeCount", types.size());
        data.put("avgDurationMs", avgDurationMs);
        data.put("avgDurationSeconds", seconds(avgDurationMs));
        if (top != null) {
            data.put("topTypeCode", top.get("typeCode"));
            data.put("topTypeFeature", top.get("feature"));
            data.put("topTypeCount", top.get("selectedCount"));
            data.put("topTypeRate", top.get("rate"));
        } else {
            data.put("topTypeCode", null);
            data.put("topTypeFeature", null);
            data.put("topTypeCount", 0L);
            data.put("topTypeRate", percent(0, finished));
        }
        return data;
    }

    /**
     * 16 型分布（第二页“各人格类型对应的解读”）。
     * 以当前配置的全部型别为行，未出现的类型补 0；统计中出现但配置已删除的类型追加在末尾。
     */
    public List<Map<String, Object>> typeStats(ZytjRecordQuery query, long finished) {
        Map<String, Map<String, Object>> raw = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectTypeStats(query)) {
            raw.put(String.valueOf(row.get("typeCode")), row);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ZytjType type : configService.listTypes()) {
            Map<String, Object> stat = raw.remove(type.getTypeCode());
            long selected = stat == null ? 0L : number(stat.get("selectedCount"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("typeCode", type.getTypeCode());
            row.put("feature", type.getFeature());
            row.put("careerField", type.getCareerField());
            row.put("typicalCareers", type.getTypicalCareers());
            row.put("sortOrder", type.getSortOrder());
            row.put("status", type.getStatus());
            row.put("selectedCount", selected);
            row.put("rate", percent(selected, finished));
            rows.add(row);
        }
        for (Map.Entry<String, Map<String, Object>> entry : raw.entrySet()) {
            Map<String, Object> stat = entry.getValue();
            long selected = number(stat.get("selectedCount"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("typeCode", entry.getKey());
            row.put("feature", stat.get("feature"));
            row.put("careerField", stat.get("careerField"));
            row.put("typicalCareers", stat.get("typicalCareers"));
            row.put("sortOrder", null);
            row.put("status", "2");
            row.put("selectedCount", selected);
            row.put("rate", percent(selected, finished));
            rows.add(row);
        }
        return rows;
    }

    /**
     * 四个维度的字母倾向分布，用于学校端“人格类型整体数据”页的倾向条形图。
     * 每个维度固定两行（如 E/I），未出现的字母补 0，并补充对应物品名称便于展示。
     */
    public List<Map<String, Object>> dimensionStats(ZytjRecordQuery query, long finished) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectDimensionStats(query)) {
            String dimension = String.valueOf(row.get("dimensionCode"));
            String letter = String.valueOf(row.get("letter"));
            counts.merge(dimension + ":" + letter, number(row.get("selectedCount")), Long::sum);
        }
        List<ZytjItem> items = configService.listItems();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String[] dimension : DIMENSIONS) {
            for (int index = 1; index < dimension.length; index++) {
                String letter = dimension[index];
                long count = counts.getOrDefault(dimension[0] + ":" + letter, 0L);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("dimensionCode", dimension[0]);
                row.put("letter", letter);
                row.put("itemName", itemNameOf(items, letter));
                row.put("selectedCount", count);
                row.put("rate", percent(count, finished));
                rows.add(row);
            }
        }
        return rows;
    }

    /** 各院系参与与提交人数；并补充该院系占比。 */
    public List<Map<String, Object>> departmentStats(ZytjRecordQuery query, long total) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : recordMapper.selectDepartmentSummary(query)) {
            long participants = number(row.get("participantCount"));
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("departmentName", row.get("departmentName"));
            node.put("participantCount", participants);
            node.put("finishedCount", number(row.get("finishedCount")));
            node.put("rate", percent(participants, total));
            rows.add(node);
        }
        return rows;
    }

    /** 各院系在每个维度字母上的人数，用于院系倾向明细与 Excel。 */
    public List<Map<String, Object>> departmentLetterStats(ZytjRecordQuery query) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : recordMapper.selectDepartmentLetterCounts(query)) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("departmentName", row.get("departmentName"));
            node.put("dimensionCode", row.get("dimensionCode"));
            node.put("letter", row.get("letter"));
            node.put("selectedCount", number(row.get("selectedCount")));
            rows.add(node);
        }
        return rows;
    }

    /** 活动查看页使用的精简统计：汇总 + 16 型分布 + 维度倾向 + 院系分布。 */
    public Map<String, Object> gameViewStats(ZytjRecordQuery query) {
        Map<String, Object> summary = summary(query);
        long finished = number(summary.get("finishedCount"));
        long total = number(summary.get("participantCount"));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("types", typeStats(query, finished));
        data.put("dimensions", dimensionStats(query, finished));
        data.put("departments", departmentStats(query, total));
        return data;
    }

    private static String itemNameOf(List<ZytjItem> items, String letter) {
        for (ZytjItem item : items) {
            if (letter.equals(item.getItemCode())) {
                return item.getItemName();
            }
        }
        return null;
    }

    private static long number(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /** 占比：保留一位小数，分母为 0 时返回 0，避免出现 NaN。 */
    private static BigDecimal percent(long part, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(part).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private static long seconds(long millis) {
        return millis <= 0 ? 0L : millis / 1000L;
    }
}
