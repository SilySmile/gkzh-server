package com.gkzh.sszctop.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.sszctop.domain.SszctopDimension;
import com.gkzh.sszctop.domain.dto.SszctopStatisticsQuery;
import com.gkzh.sszctop.mapper.SszctopDimensionMapper;
import com.gkzh.sszctop.mapper.SszctopStatisticsMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 谁是职场TOP统计分析服务。
 * 统计汇总、学生明细和 Excel 导出共用同一份查询与口径，避免页面人数与导出不一致。
 */
@Service
public class SszctopStatisticsService {

    private final SszctopStatisticsMapper statisticsMapper;
    private final SszctopDimensionMapper dimensionMapper;
    private final IActivityWeekService weekService;

    public SszctopStatisticsService(SszctopStatisticsMapper statisticsMapper,
                                    SszctopDimensionMapper dimensionMapper,
                                    IActivityWeekService weekService) {
        this.statisticsMapper = statisticsMapper;
        this.dimensionMapper = dimensionMapper;
        this.weekService = weekService;
    }

    /** 统计汇总、七维度完成情况、结果分布、分析和当前筛选下的分页明细。 */
    public Map<String, Object> statistics(SszctopStatisticsQuery query, int pageNum, int pageSize) {
        List<Map<String, Object>> rows = rows(query);
        Map<String, Object> summary = summary(rows);
        List<Map<String, Object>> dimensionStats = dimensionStats(rows);
        List<Map<String, Object>> resultStats = resultStats(rows);

        int safePageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 100);
        int safePageNum = pageNum <= 0 ? 1 : pageNum;
        int from = Math.min((safePageNum - 1) * safePageSize, rows.size());
        int to = Math.min(from + safePageSize, rows.size());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("dimensionStats", dimensionStats);
        data.put("resultStats", resultStats);
        data.put("analysis", analysis(summary, dimensionStats));
        data.put("total", rows.size());
        data.put("records", new ArrayList<>(rows.subList(from, to)));
        return data;
    }

    /** 查询当前筛选条件下的全部明细，供统计汇总和 Excel 导出复用。 */
    public List<Map<String, Object>> rows(SszctopStatisticsQuery query) {
        Map<Long, String> instanceNames = new HashMap<>();
        for (GkzhActivityWeekInstance instance : weekService.listInstances(null)) {
            instanceNames.put(instance.getInstanceId(), instance.getTitle());
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> raw : statisticsMapper.selectReportRows(query)) {
            Map<String, Object> row = new LinkedHashMap<>();
            Long instanceId = longValue(col(raw, "instanceId", "instance_id"));
            String result = text(col(raw, "result", "result"));

            row.put("reportId", longValue(col(raw, "reportId", "report_id")));
            row.put("instanceId", instanceId);
            row.put("instanceName", instanceId == null ? null : instanceNames.get(instanceId));
            row.put("gameId", longValue(col(raw, "gameId", "game_id")));
            row.put("userId", longValue(col(raw, "userId", "user_id")));
            row.put("studentId", longValue(col(raw, "studentId", "student_id")));
            row.put("studentName", text(col(raw, "studentName", "student_name")));
            row.put("studentNo", text(col(raw, "studentNo", "student_no")));
            row.put("schoolId", longValue(col(raw, "schoolId", "school_id")));
            row.put("schoolName", text(col(raw, "schoolName", "school_name")));
            row.put("departmentId", longValue(col(raw, "departmentId", "department_id")));
            row.put("departmentName", text(col(raw, "departmentName", "department_name")));
            row.put("majorId", longValue(col(raw, "majorId", "major_id")));
            row.put("major", text(col(raw, "major", "major")));
            row.put("gender", genderText(text(col(raw, "gender", "gender"))));
            row.put("result", result);
            row.put("resultName", resultText(result));
            row.put("createTime", col(raw, "createTime", "create_time"));

            JSONObject dimension = parseObject(text(col(raw, "dimensionSnapshot", "dimension_snapshot")));
            Long dimensionId = dimension == null ? null : dimension.getLong("dimensionId");
            String dimensionName = dimension == null ? null : dimension.getString("name");
            row.put("dimensionId", dimensionId);
            row.put("dimensionName", isBlank(dimensionName) ? "未记录维度" : dimensionName);
            row.put("careers", careerNames(text(col(raw, "careersSnapshot", "careers_snapshot")),
                    text(col(raw, "sharedOrderSnapshot", "shared_order_snapshot"))));
            rows.add(row);
        }
        return rows;
    }

    /** 完成人数、成功人数、未通过人数和整体成功率。 */
    public Map<String, Object> summary(List<Map<String, Object>> rows) {
        long total = rows.size();
        long passed = 0L;
        for (Map<String, Object> row : rows) {
            if ("passed".equals(row.get("result"))) {
                passed++;
            }
        }
        long failed = total - passed;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("participantCount", total);
        data.put("passedCount", passed);
        data.put("failedCount", failed);
        data.put("passRate", percent(passed, total));
        return data;
    }

    /** 七个维度的完成人数、成功人数和成功率；历史快照缺失时单独归入“未记录维度”。 */
    public List<Map<String, Object>> dimensionStats(List<Map<String, Object>> rows) {
        Map<Long, long[]> counts = new LinkedHashMap<>();
        long unknownTotal = 0L;
        long unknownPassed = 0L;
        for (Map<String, Object> row : rows) {
            Long dimensionId = longValue(row.get("dimensionId"));
            boolean passed = "passed".equals(row.get("result"));
            if (dimensionId == null) {
                unknownTotal++;
                if (passed) {
                    unknownPassed++;
                }
                continue;
            }
            long[] count = counts.computeIfAbsent(dimensionId, key -> new long[2]);
            count[0]++;
            if (passed) {
                count[1]++;
            }
        }

        List<Map<String, Object>> stats = new ArrayList<>();
        int serial = 1;
        for (SszctopDimension dimension : dimensionMapper.selectList(new QueryWrapper<SszctopDimension>()
                .orderByAsc("sort_order").orderByAsc("dimension_id"))) {
            long[] count = counts.get(dimension.getDimensionId());
            long total = count == null ? 0L : count[0];
            long passed = count == null ? 0L : count[1];
            stats.add(dimensionRow(serial++, dimension.getDimensionId(), dimension.getName(), total, passed));
        }
        if (unknownTotal > 0) {
            stats.add(dimensionRow(serial, null, "未记录维度", unknownTotal, unknownPassed));
        }
        return stats;
    }

    /** 挑战成功与未通过的结果分布。 */
    public List<Map<String, Object>> resultStats(List<Map<String, Object>> rows) {
        long passed = 0L;
        long failed = 0L;
        for (Map<String, Object> row : rows) {
            if ("passed".equals(row.get("result"))) {
                passed++;
            } else if ("failed".equals(row.get("result"))) {
                failed++;
            }
        }
        long total = passed + failed;
        List<Map<String, Object>> stats = new ArrayList<>();
        int serial = 1;
        for (String[] definition : new String[][]{{"passed", "挑战成功"}, {"failed", "本局未通过"}}) {
            long count = "passed".equals(definition[0]) ? passed : failed;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("serialNo", serial++);
            row.put("result", definition[0]);
            row.put("resultName", definition[1]);
            row.put("userCount", count);
            row.put("probability", percent(count, total));
            stats.add(row);
        }
        return stats;
    }

    /** 按当前筛选结果生成活动复盘文案。 */
    public String analysis(Map<String, Object> summary, List<Map<String, Object>> dimensionStats) {
        long total = number(summary.get("participantCount"));
        if (total == 0) {
            return "当前筛选条件下暂无完成记录。学生完成谁是职场TOP后，这里将展示七个维度的参与情况和成功率分析。";
        }
        Map<String, Object> mostSelected = null;
        Map<String, Object> highestRate = null;
        Map<String, Object> lowestRate = null;
        for (Map<String, Object> row : dimensionStats) {
            long participants = number(row.get("participantCount"));
            if (participants <= 0) {
                continue;
            }
            if (mostSelected == null || participants > number(mostSelected.get("participantCount"))) {
                mostSelected = row;
            }
            BigDecimal rate = decimal(row.get("passRate"));
            if (highestRate == null || rate.compareTo(decimal(highestRate.get("passRate"))) > 0) {
                highestRate = row;
            }
            if (lowestRate == null || rate.compareTo(decimal(lowestRate.get("passRate"))) < 0) {
                lowestRate = row;
            }
        }
        StringBuilder text = new StringBuilder();
        text.append("本次共 ").append(total).append(" 人完成谁是职场TOP，挑战成功 ")
                .append(number(summary.get("passedCount"))).append(" 人、未通过 ")
                .append(number(summary.get("failedCount"))).append(" 人，整体成功率为 ")
                .append(summary.get("passRate")).append("%。");
        if (mostSelected != null) {
            text.append("参与人数最多的维度是“").append(mostSelected.get("dimensionName"))
                    .append("”（").append(number(mostSelected.get("participantCount"))).append(" 人）。");
        }
        if (highestRate != null && lowestRate != null && !highestRate.get("dimensionName").equals(lowestRate.get("dimensionName"))) {
            text.append("成功率最高的是“").append(highestRate.get("dimensionName")).append("”（")
                    .append(highestRate.get("passRate")).append("%），最低的是“")
                    .append(lowestRate.get("dimensionName")).append("”（")
                    .append(lowestRate.get("passRate")).append("%），可作为活动复盘和职业认知讲解的重点。");
        }
        return text.toString();
    }

    private Map<String, Object> dimensionRow(int serial, Long dimensionId, String name, long total, long passed) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("serialNo", serial);
        row.put("dimensionId", dimensionId);
        row.put("dimensionName", name);
        row.put("participantCount", total);
        row.put("passedCount", passed);
        row.put("passRate", percent(passed, total));
        return row;
    }

    /** 从职业快照和共享排序快照还原本局选择并排序的三个职业名称。 */
    private String careerNames(String careersSnapshot, String orderSnapshot) {
        JSONArray careers = parseArray(careersSnapshot);
        if (careers == null || careers.isEmpty()) {
            return "";
        }
        Map<String, String> names = new HashMap<>();
        for (int i = 0; i < careers.size(); i++) {
            JSONObject career = careers.getJSONObject(i);
            if (career == null) {
                continue;
            }
            names.put(String.valueOf(career.getLong("careerId")), career.getString("name"));
        }
        List<String> ordered = new ArrayList<>();
        for (String id : String.valueOf(orderSnapshot == null ? "" : orderSnapshot).split(",")) {
            String name = names.get(id.trim());
            if (!isBlank(name)) {
                ordered.add(name);
            }
        }
        if (ordered.isEmpty()) {
            for (String name : names.values()) {
                if (!isBlank(name)) {
                    ordered.add(name);
                }
            }
        }
        return String.join("、", ordered);
    }

    private static JSONObject parseObject(String json) {
        try {
            return isBlank(json) ? null : JSON.parseObject(json);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static JSONArray parseArray(String json) {
        try {
            return isBlank(json) ? null : JSON.parseArray(json);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** 统一把 0/1 或中文性别转为页面展示值。 */
    private static String genderText(String gender) {
        String value = gender == null ? "" : gender.trim();
        if ("0".equals(value) || "男".equals(value)) {
            return "男";
        }
        if ("1".equals(value) || "女".equals(value)) {
            return "女";
        }
        return "其他";
    }

    private static String resultText(String result) {
        if ("passed".equals(result)) {
            return "挑战成功";
        }
        if ("failed".equals(result)) {
            return "本局未通过";
        }
        return "未结束";
    }

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
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static BigDecimal decimal(Object value) {
        try {
            return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private static Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /** 兼容 MyBatis Map 结果别名可能是驼峰或下划线两种形式。 */
    private static Object col(Map<String, Object> raw, String camel, String snake) {
        Object value = raw.get(camel);
        return value != null ? value : raw.get(snake);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
