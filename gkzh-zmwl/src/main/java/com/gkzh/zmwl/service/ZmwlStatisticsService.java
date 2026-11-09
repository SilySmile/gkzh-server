package com.gkzh.zmwl.service;

import com.gkzh.zmwl.domain.ZmwlDirection;
import com.gkzh.zmwl.domain.dto.ZmwlRecordQuery;
import com.gkzh.zmwl.mapper.ZmwlRecordMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职面未来统计分析服务（结果输出-学校端）。
 *
 * <p>统计口径：参与人数按参与记录去重；已完成人数指已确认发展方向的人数；
 * 方向偏好占比以已完成人数为分母，同一学生只计一次；院系取专业部门的父部门，
 * 未填写专业或部门缺失统一归入“未填写院系”。后台统计页、活动查看页和 Excel 导出共用同一套计算。</p>
 */
@Service
public class ZmwlStatisticsService {

    private final ZmwlRecordMapper recordMapper;
    private final ZmwlConfigService configService;

    public ZmwlStatisticsService(ZmwlRecordMapper recordMapper, ZmwlConfigService configService) {
        this.recordMapper = recordMapper;
        this.configService = configService;
    }

    /** 第一页整体数据：参与人数、已完成人数、完成率、暂未确定人数与平均选择用时。 */
    public Map<String, Object> summary(ZmwlRecordQuery query) {
        Map<String, Object> raw = recordMapper.selectAdminSummary(query);
        if (raw == null) {
            raw = new LinkedHashMap<>();
        }
        long total = number(raw.get("total"));
        long finished = number(raw.get("finished"));
        long chosen = number(raw.get("chosen"));
        long undecided = number(raw.get("undecided"));
        long avgDurationMs = Math.round(decimal(raw.get("avgDurationMs")).doubleValue());

        List<Map<String, Object>> directions = directionStats(query, finished);
        Map<String, Object> top = null;
        for (Map<String, Object> direction : directions) {
            long count = number(direction.get("selectedCount"));
            if (count > 0 && (top == null || count > number(top.get("selectedCount")))) {
                top = direction;
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("participantCount", total);
        data.put("finishedCount", finished);
        data.put("chosenCount", chosen);
        data.put("inProgressCount", Math.max(0L, total - finished));
        data.put("undecidedCount", undecided);
        data.put("finishRate", percent(finished, total));
        data.put("directionCount", directions.size());
        data.put("avgDurationMs", avgDurationMs);
        data.put("avgDurationSeconds", seconds(avgDurationMs));
        if (top != null) {
            data.put("topDirectionCode", top.get("directionCode"));
            data.put("topDirectionName", top.get("name"));
            data.put("topDirectionCount", top.get("selectedCount"));
            data.put("topDirectionRate", top.get("rate"));
        } else {
            data.put("topDirectionCode", null);
            data.put("topDirectionName", null);
            data.put("topDirectionCount", 0L);
            data.put("topDirectionRate", percent(0, finished));
        }
        return data;
    }

    /**
     * 各发展方向偏好占比。
     * 以当前配置的全部方向为行（含停用，保证历史数据仍可见），未出现的方向补 0；
     * 统计结果中出现但配置已删除的编码追加在末尾，避免历史记录丢失。
     */
    public List<Map<String, Object>> directionStats(ZmwlRecordQuery query, long finished) {
        Map<String, Map<String, Object>> raw = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectDirectionStats(query)) {
            raw.put(String.valueOf(row.get("directionCode")), row);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ZmwlDirection direction : configService.listAll()) {
            Map<String, Object> stat = raw.remove(direction.getDirectionCode());
            long selected = stat == null ? 0L : number(stat.get("selectedCount"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("directionCode", direction.getDirectionCode());
            row.put("name", direction.getName());
            row.put("shortIntro", direction.getShortIntro());
            row.put("tagName", direction.getTagName());
            row.put("iconUrl", direction.getIconUrl());
            row.put("sortOrder", direction.getSortOrder());
            row.put("status", direction.getStatus());
            row.put("selectedCount", selected);
            row.put("rate", percent(selected, finished));
            rows.add(row);
        }
        for (Map.Entry<String, Map<String, Object>> entry : raw.entrySet()) {
            Map<String, Object> stat = entry.getValue();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("directionCode", entry.getKey());
            row.put("name", stat.get("directionName"));
            row.put("shortIntro", null);
            row.put("tagName", null);
            row.put("iconUrl", null);
            row.put("sortOrder", 9999);
            row.put("status", "1");
            row.put("selectedCount", number(stat.get("selectedCount")));
            row.put("rate", percent(number(stat.get("selectedCount")), finished));
            rows.add(row);
        }
        return rows;
    }

    /** 各发展方向排序后的偏好列表，最多前 N 项，用于简要分析。 */
    public List<Map<String, Object>> topDirections(List<Map<String, Object>> directionStats, int limit) {
        List<Map<String, Object>> sorted = new ArrayList<>();
        for (Map<String, Object> row : directionStats) {
            if (number(row.get("selectedCount")) > 0) {
                sorted.add(row);
            }
        }
        sorted.sort((left, right) -> Long.compare(number(right.get("selectedCount")), number(left.get("selectedCount"))));
        return sorted.size() > limit ? new ArrayList<>(sorted.subList(0, limit)) : sorted;
    }

    /**
     * 院系偏好：每个院系的参与人数、完成人数、完成率和最受欢迎方向。
     * 院系维度人数来自单独的分组查询，避免在 MySQL 5.7 中写窗口函数。
     */
    public List<Map<String, Object>> departmentStats(ZmwlRecordQuery query) {
        Map<String, Map<String, Long>> directionCounts = new LinkedHashMap<>();
        Map<String, String> directionNames = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectDepartmentDirectionCounts(query)) {
            String department = String.valueOf(row.get("departmentName"));
            String code = String.valueOf(row.get("directionCode"));
            directionCounts.computeIfAbsent(department, key -> new LinkedHashMap<>())
                    .merge(code, number(row.get("selectedCount")), Long::sum);
            directionNames.putIfAbsent(code, row.get("directionName") == null ? code : String.valueOf(row.get("directionName")));
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> raw : recordMapper.selectDepartmentSummary(query)) {
            String department = String.valueOf(raw.get("departmentName"));
            long participants = number(raw.get("participantCount"));
            long finished = number(raw.get("finishedCount"));
            Map<String, Long> counts = directionCounts.getOrDefault(department, new LinkedHashMap<>());
            String topCode = null;
            long topCount = 0L;
            for (Map.Entry<String, Long> entry : counts.entrySet()) {
                if (entry.getValue() > topCount) {
                    topCode = entry.getKey();
                    topCount = entry.getValue();
                }
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("departmentName", department);
            row.put("participantCount", participants);
            row.put("finishedCount", finished);
            row.put("finishRate", percent(finished, participants));
            row.put("topDirectionCode", topCode);
            row.put("topDirectionName", topCode == null ? null : directionNames.getOrDefault(topCode, topCode));
            row.put("topDirectionCount", topCount);
            row.put("topDirectionRate", percent(topCount, finished));
            rows.add(row);
        }
        return rows;
    }

    /** 学校端简要分析：整体完成情况、最受欢迎方向与“暂未确定”占比。 */
    public String analysis(Map<String, Object> summary, List<Map<String, Object>> directionStats) {
        long total = number(summary.get("participantCount"));
        long finished = number(summary.get("finishedCount"));
        if (total == 0) {
            return "当前活动尚无学生扫码参与。学生扫码并确认未来发展方向后，"
                    + "这里将展示参与人数、完成情况与各发展方向的偏好占比。";
        }
        if (finished == 0) {
            return "当前共 " + total + " 人扫码进入，尚无学生确认未来发展方向；"
                    + "学生完成选择后即可看到方向偏好占比与院系分布。";
        }
        StringBuilder text = new StringBuilder();
        text.append("本次共 ").append(total).append(" 人扫码参与，")
                .append(finished).append(" 人完成方向选择，完成率 ")
                .append(summary.get("finishRate")).append("%；");
        List<Map<String, Object>> top = topDirections(directionStats, 3);
        if (!top.isEmpty()) {
            text.append("偏好最高的方向是「").append(top.get(0).get("name")).append("」（")
                    .append(top.get(0).get("selectedCount")).append(" 人，占 ")
                    .append(top.get(0).get("rate")).append("%）");
            if (top.size() > 1) {
                text.append("，其次为「").append(top.get(1).get("name")).append("」（")
                        .append(top.get(1).get("rate")).append("%）");
            }
            text.append("。");
        }
        long undecided = number(summary.get("undecidedCount"));
        if (undecided > 0) {
            text.append("另有 ").append(undecided).append(" 人选择「暂未确定」（占 ")
                    .append(percent(undecided, finished)).append("%），")
                    .append("可作为后续生涯咨询与方向探索的重点对象。");
        }
        if (number(summary.get("avgDurationSeconds")) > 0) {
            text.append("平均选择用时 ").append(summary.get("avgDurationSeconds")).append(" 秒。");
        }
        return text.toString();
    }

    /** 活动查看页汇总数据：学校端两页内容 + 最近参与记录。 */
    public Map<String, Object> gameViewStats(Long gameId) {
        ZmwlRecordQuery query = new ZmwlRecordQuery();
        query.setGameId(gameId);
        Map<String, Object> summary = summary(query);
        long finished = number(summary.get("finishedCount"));
        List<Map<String, Object>> directions = directionStats(query, finished);
        List<Map<String, Object>> departments = departmentStats(query);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("directionStats", directions);
        data.put("departmentStats", departments);
        data.put("analysis", analysis(summary, directions));
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
