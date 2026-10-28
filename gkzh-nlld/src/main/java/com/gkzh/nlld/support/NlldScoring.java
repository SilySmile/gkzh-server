package com.gkzh.nlld.support;

import com.gkzh.nlld.domain.NlldAnswer;
import com.gkzh.nlld.domain.NlldDimension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 能量雷达计分与结果判定规则。
 *
 * <p>规则：6 关，每关从 5 件物品中选择 2 件；15 个价值观维度在全表 30 件物品中
 * 各出现 2 次，因此单维度得分只能是 0/1/2，全部维度得分合计固定为 12。
 * 恰好 2 个维度得 2 分为 TOP2；只有 1 个维度得 2 分、3 个及以上维度并列 2 分、
 * 或没有任何维度得 2 分时，统一判定为“多元开放型”。</p>
 */
public final class NlldScoring {

    /** 结果类型：恰好两个维度并列最高分。 */
    public static final String RESULT_TOP2 = "top2";

    /** 结果类型：多元开放型。 */
    public static final String RESULT_OPEN = "open";

    /** 维度编码顺序，与数据库 sort_order 一致。 */
    public static final List<String> DIMENSION_CODES = Collections.unmodifiableList(Arrays.asList(
            "intellect", "achievement", "creativity", "autonomy", "variety",
            "salary", "security", "prestige", "aesthetics", "environment",
            "management", "colleague", "supervisor", "altruism", "lifestyle"));

    /** 多元开放型统一解读文案，来自《能量雷达(2).docx》第四部分。 */
    public static final String OPEN_TEXT = "你对多种工作特点都持开放态度，当前的选择尚未呈现出足以明确区分优先顺序的单一偏好。"
            + "你可能愿意尝试不同的工作方式与环境。随着学习、实践和职业探索的深入，你可以逐步发现哪些因素更能激发你的热情与动力，"
            + "并形成更清晰的职业价值观排序。";

    private NlldScoring() {
    }

    /** 初始化 15 个维度的 0 分得分表。 */
    public static Map<String, Integer> emptyScores() {
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (String code : DIMENSION_CODES) {
            scores.put(code, 0);
        }
        return scores;
    }

    /**
     * 按选择明细累计维度得分。
     * 未知维度编码忽略，避免历史脏数据把参评人数算错。
     */
    public static Map<String, Integer> scoreOf(List<NlldAnswer> answers) {
        Map<String, Integer> scores = emptyScores();
        if (answers == null) {
            return scores;
        }
        for (NlldAnswer answer : answers) {
            String code = answer == null ? null : answer.getDimensionCode();
            if (code != null && scores.containsKey(code)) {
                scores.put(code, scores.get(code) + 1);
            }
        }
        return scores;
    }

    /** 结果类型：恰好 2 个维度得 2 分为 top2，其余全部为 open。 */
    public static String resultType(Map<String, Integer> scores) {
        int twoPointCount = 0;
        if (scores != null) {
            for (Integer score : scores.values()) {
                if (score != null && score == 2) {
                    twoPointCount++;
                }
            }
        }
        return twoPointCount == 2 ? RESULT_TOP2 : RESULT_OPEN;
    }

    /** 返回得 2 分的维度，按配置顺序排列；TOP2 场景固定返回 2 个。 */
    public static List<NlldDimension> twoPointDimensions(Map<String, Integer> scores, List<NlldDimension> dimensions) {
        List<NlldDimension> result = new ArrayList<>();
        if (scores == null || dimensions == null) {
            return result;
        }
        for (NlldDimension dimension : dimensions) {
            Integer score = scores.get(dimension.getDimensionCode());
            if (score != null && score == 2) {
                result.add(dimension);
            }
        }
        result.sort(Comparator
                .comparing((NlldDimension d) -> d.getSortOrder() == null ? Integer.MAX_VALUE : d.getSortOrder())
                .thenComparing(d -> d.getDimensionId() == null ? Long.MAX_VALUE : d.getDimensionId()));
        return result;
    }

    /** 生成报告页使用的 15 维得分明细，按配置顺序输出。 */
    public static List<Map<String, Object>> scoreRows(Map<String, Integer> scores, List<NlldDimension> dimensions) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (scores == null || dimensions == null) {
            return rows;
        }
        for (NlldDimension dimension : dimensions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", dimension.getDimensionCode());
            row.put("name", dimension.getDimensionName());
            row.put("group", dimension.getDimensionGroup());
            row.put("score", scores.getOrDefault(dimension.getDimensionCode(), 0));
            rows.add(row);
        }
        return rows;
    }

    /** 校验维度编码是否属于 15 个固定维度。 */
    public static boolean isValidCode(String code) {
        return code != null && DIMENSION_CODES.contains(code);
    }
}
