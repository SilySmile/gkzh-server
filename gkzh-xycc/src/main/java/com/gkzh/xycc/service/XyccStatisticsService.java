package com.gkzh.xycc.service;

import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.xycc.domain.HollandCode;
import com.gkzh.xycc.domain.Pattern;
import com.gkzh.xycc.domain.dto.XyccStatisticsQuery;
import com.gkzh.xycc.mapper.PatternMapper;
import com.gkzh.xycc.mapper.XyccStatisticsMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 心愿橱窗统计分析服务。
 * 统计汇总、编码分布、学生明细和 Excel 导出共用同一份查询与口径。
 */
@Service
public class XyccStatisticsService {

    private final XyccStatisticsMapper statisticsMapper;
    private final IHollandCodeService hollandCodeService;
    private final IActivityWeekService weekService;
    private final PatternMapper patternMapper;

    public XyccStatisticsService(XyccStatisticsMapper statisticsMapper,
                                 IHollandCodeService hollandCodeService,
                                 IActivityWeekService weekService,
                                 PatternMapper patternMapper) {
        this.statisticsMapper = statisticsMapper;
        this.hollandCodeService = hollandCodeService;
        this.weekService = weekService;
        this.patternMapper = patternMapper;
    }

    /** 统计汇总、复合编码分布、分析和当前筛选下的分页明细。 */
    public Map<String, Object> statistics(XyccStatisticsQuery query, int pageNum, int pageSize) {
        List<Map<String, Object>> rows = rows(query);
        Map<String, Object> summary = summary(rows);
        List<Map<String, Object>> codeStats = codeStats(rows);

        int safePageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 100);
        int safePageNum = pageNum <= 0 ? 1 : pageNum;
        int from = Math.min((safePageNum - 1) * safePageSize, rows.size());
        int to = Math.min(from + safePageSize, rows.size());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("codeStats", codeStats);
        data.put("analysis", analysis(summary, codeStats));
        data.put("total", rows.size());
        data.put("records", new ArrayList<>(rows.subList(from, to)));
        return data;
    }

    /** 学生记录列表：与统计页共用同一份筛选和展示字段，只做分页。 */
    public Map<String, Object> records(XyccStatisticsQuery query, int pageNum, int pageSize) {
        List<Map<String, Object>> rows = rows(query);
        int safePageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 100);
        int safePageNum = pageNum <= 0 ? 1 : pageNum;
        int from = Math.min((safePageNum - 1) * safePageSize, rows.size());
        int to = Math.min(from + safePageSize, rows.size());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", rows.size());
        data.put("records", new ArrayList<>(rows.subList(from, to)));
        return data;
    }

    /** 当前筛选条件下的全部明细，供统计和 Excel 导出复用。 */
    public List<Map<String, Object>> rows(XyccStatisticsQuery query) {
        Map<String, String[]> comboMap = combos();
        Map<String, String> hollandNames = hollandNames();
        Map<Long, String> patternMap = new HashMap<>();
        for (Pattern pattern : patternMapper.selectPatternList(null)) {
            patternMap.put(pattern.getPatternId(), pattern.getDescription());
        }
        Map<Long, String> instanceNames = new HashMap<>();
        for (GkzhActivityWeekInstance instance : weekService.listInstances(null)) {
            instanceNames.put(instance.getInstanceId(), instance.getTitle());
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> raw : statisticsMapper.selectSelectionRows(query)) {
            Map<String, Object> row = new LinkedHashMap<>();
            Long instanceId = longValue(col(raw, "instanceId", "instance_id"));
            String code = text(col(raw, "patternComboCode", "pattern_combo_code"));
            String[] titles = comboMap.getOrDefault(code, new String[]{"", ""});
            row.put("userSelectionId", longValue(col(raw, "userSelectionId", "user_selection_id")));
            row.put("userId", longValue(col(raw, "userId", "user_id")));
            row.put("instanceId", instanceId);
            row.put("instanceName", instanceId == null ? null : instanceNames.get(instanceId));
            row.put("gameId", longValue(col(raw, "gameId", "game_id")));
            row.put("patternComboCode", code);
            row.put("codeSummary", codeSummary(code, hollandNames));
            row.put("careerTitles", titles[0]);
            row.put("envTitles", titles[1]);
            row.put("patternNames", patternNames(text(col(raw, "patternIds", "pattern_ids")), patternMap));
            row.put("studentName", text(col(raw, "studentName", "student_name")));
            row.put("studentNo", text(col(raw, "studentNo", "student_no")));
            row.put("gender", genderText(text(col(raw, "gender", "gender"))));
            row.put("schoolId", longValue(col(raw, "schoolId", "school_id")));
            row.put("schoolName", text(col(raw, "schoolName", "school_name")));
            row.put("departmentId", longValue(col(raw, "departmentId", "department_id")));
            row.put("departmentName", text(col(raw, "departmentName", "department_name")));
            row.put("majorId", longValue(col(raw, "majorId", "major_id")));
            row.put("major", text(col(raw, "major", "major")));
            row.put("createdAt", col(raw, "createdAt", "created_at"));
            rows.add(row);
        }
        return rows;
    }

    /** 完成人数、编码组合数、最高频编码及其占比。 */
    public Map<String, Object> summary(List<Map<String, Object>> rows) {
        long total = rows.size();
        Map<String, Long> codeCounts = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String code = text(row.get("patternComboCode"));
            if (code != null && !code.trim().isEmpty()) {
                codeCounts.merge(code, 1L, Long::sum);
            }
        }
        String topCode = null;
        long topCount = 0L;
        for (Map.Entry<String, Long> entry : codeCounts.entrySet()) {
            if (entry.getValue() > topCount) {
                topCode = entry.getKey();
                topCount = entry.getValue();
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("participantCount", total);
        data.put("codeCount", codeCounts.size());
        data.put("topCode", topCode);
        data.put("topCodeCount", topCount);
        data.put("topCodeProbability", percent(topCount, total));
        return data;
    }

    /** 复合编码分布：人数、占比、霍兰德类型、职业方向和工作环境偏好。 */
    public List<Map<String, Object>> codeStats(List<Map<String, Object>> rows) {
        Map<String, Map<String, Object>> stats = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String code = text(row.get("patternComboCode"));
            String key = code == null || code.trim().isEmpty() ? "" : code.trim();
            Map<String, Object> item = stats.computeIfAbsent(key, k -> {
                Map<String, Object> created = new LinkedHashMap<>();
                created.put("code", k.isEmpty() ? "未记录编码" : k);
                created.put("codeSummary", k.isEmpty() ? "未记录编码" : text(row.get("codeSummary")));
                created.put("careerTitles", text(row.get("careerTitles")));
                created.put("envTitles", text(row.get("envTitles")));
                created.put("userCount", 0L);
                return created;
            });
            item.put("userCount", number(item.get("userCount")) + 1);
        }
        long total = rows.size();
        List<Map<String, Object>> result = new ArrayList<>(stats.values());
        result.sort((left, right) -> {
            int compare = Long.compare(number(right.get("userCount")), number(left.get("userCount")));
            return compare != 0 ? compare : String.valueOf(left.get("code")).compareTo(String.valueOf(right.get("code")));
        });
        int serial = 1;
        for (Map<String, Object> item : result) {
            item.put("serialNo", serial++);
            item.put("probability", percent(number(item.get("userCount")), total));
        }
        return result;
    }

    /** 按当前结果生成活动复盘文案。 */
    public String analysis(Map<String, Object> summary, List<Map<String, Object>> codeStats) {
        long total = number(summary.get("participantCount"));
        if (total == 0) {
            return "当前筛选条件下暂无完成记录。学生完成心愿橱窗后，这里将展示复合编码分布和兴趣类型分析。";
        }
        Map<String, Long> typeCounts = new LinkedHashMap<>();
        for (Map<String, Object> item : codeStats) {
            String code = text(item.get("code"));
            if (code == null || code.trim().isEmpty() || "未记录编码".equals(code)) {
                continue;
            }
            long count = number(item.get("userCount"));
            for (int i = 0; i < code.length(); i++) {
                typeCounts.merge(String.valueOf(code.charAt(i)), count, Long::sum);
            }
        }
        String topType = null;
        long topTypeCount = 0L;
        for (Map.Entry<String, Long> entry : typeCounts.entrySet()) {
            if (entry.getValue() > topTypeCount) {
                topType = entry.getKey();
                topTypeCount = entry.getValue();
            }
        }
        Map<String, String> hollandNames = hollandNames();
        Map<String, Object> top = codeStats.isEmpty() ? null : codeStats.get(0);
        StringBuilder text = new StringBuilder();
        text.append("本次共 ").append(total).append(" 人完成心愿橱窗，覆盖 ")
                .append(number(summary.get("codeCount"))).append(" 种复合代码。");
        if (top != null) {
            text.append("出现最多的是 ").append(top.get("code")).append("（")
                    .append(top.get("codeSummary")).append("），共 ")
                    .append(number(top.get("userCount"))).append(" 人，占 ")
                    .append(top.get("probability")).append("%。");
        }
        if (topType != null) {
            String name = hollandNames.getOrDefault(topType, topType);
            text.append("从单类型倾向看，").append(name).append("最为突出，说明参与者整体更偏向该类型的兴趣特征。");
        }
        text.append("建议结合院系、专业和性别继续观察差异，并将统计结果作为活动复盘和生涯指导的参考。");
        return text.toString();
    }

    /** 全量复合编码对应的职业方向与工作环境偏好。 */
    private Map<String, String[]> combos() {
        Map<String, String[]> map = new HashMap<>();
        for (Map<String, Object> row : statisticsMapper.selectComboTitles()) {
            String code = text(col(row, "code", "code"));
            if (code == null) {
                continue;
            }
            map.put(code, new String[]{
                    text(col(row, "careerTitles", "career_titles")),
                    text(col(row, "envTitles", "env_titles"))
            });
        }
        return map;
    }

    private Map<String, String> hollandNames() {
        Map<String, String> names = new HashMap<>();
        for (HollandCode code : hollandCodeService.listCodes()) {
            names.put(code.getCode(), code.getName());
        }
        return names;
    }

    /** 把 "RIA" 转成“现实型+研究型+艺术型”。 */
    private static String codeSummary(String code, Map<String, String> hollandNames) {
        if (code == null || code.trim().isEmpty()) {
            return "未记录编码";
        }
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            String letter = String.valueOf(code.charAt(i));
            String name = hollandNames.get(letter);
            if (summary.length() > 0) {
                summary.append('+');
            }
            summary.append(name == null ? letter : name);
        }
        return summary.toString();
    }

    /** 把 "10,22,29" 转成所选图案的描述列表，用于学生明细展示和导出。 */
    private static String patternNames(String ids, Map<Long, String> names) {
        if (ids == null || ids.trim().isEmpty()) {
            return "";
        }
        List<String> list = new ArrayList<>();
        for (String id : ids.split(",")) {
            try {
                String name = names.get(Long.valueOf(id.trim()));
                if (name != null && !name.trim().isEmpty()) {
                    list.add(name);
                }
            } catch (NumberFormatException ignored) {
                // 历史数据可能不是数字编号，忽略即可。
            }
        }
        return String.join("、", list);
    }

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
}
