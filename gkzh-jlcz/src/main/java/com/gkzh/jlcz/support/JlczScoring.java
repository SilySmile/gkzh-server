package com.gkzh.jlcz.support;

import com.gkzh.jlcz.domain.JlczSetting;
import com.gkzh.jlcz.domain.JlczWeight;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 简历称重计分与建议规则。
 *
 * <p>总重量为所选砝码重量之和；总重达到通关阈值即为通关，恰好等于阈值时视为“刚好达标”。
 * 未通关时按重量从大到小推荐尚未勾选的砝码，帮助学生在现场优先补充高分量项。</p>
 */
public final class JlczScoring {

    /** 硬通货（核心竞争力）大类编码。 */
    public static final String CATEGORY_CORE = "core";
    /** 软实力（发展潜力）大类编码。 */
    public static final String CATEGORY_SOFT = "soft";
    /** 基本功（职业素养）大类编码。 */
    public static final String CATEGORY_BASIC = "basic";
    /** 加分项（额外亮点）大类编码。 */
    public static final String CATEGORY_BONUS = "bonus";

    /** 四大类固定顺序，页面、统计和导出共用。 */
    public static final String[] CATEGORY_ORDER = {CATEGORY_CORE, CATEGORY_SOFT, CATEGORY_BASIC, CATEGORY_BONUS};

    /** 通关结果编码。 */
    public static final String RESULT_PASS = "pass";
    /** 未通关结果编码。 */
    public static final String RESULT_FAIL = "fail";

    private JlczScoring() {
    }

    /**
     * 计算一次称重结果。
     *
     * @param selected 本次勾选的砝码（已校验启停状态）
     * @param setting  规则参数
     * @return 总重、分类小计、通关判定与差值
     */
    public static Outcome compute(List<JlczWeight> selected, JlczSetting setting) {
        Map<String, Integer> categoryTotals = new LinkedHashMap<>();
        for (String code : CATEGORY_ORDER) {
            categoryTotals.put(code, 0);
        }
        int total = 0;
        for (JlczWeight weight : selected) {
            int grams = weight.getWeightG() == null ? 0 : weight.getWeightG();
            total += grams;
            categoryTotals.merge(weight.getCategoryCode(), grams, Integer::sum);
        }
        int passWeight = setting == null || setting.getPassWeightG() == null ? 80 : setting.getPassWeightG();
        boolean pass = total >= passWeight;
        int gap = pass ? 0 : passWeight - total;
        return new Outcome(total, categoryTotals, pass, passWeight, gap, total == passWeight);
    }

    /**
     * 未通关建议：只从未勾选的启用砝码中推荐，按重量降序、类别顺序、编号升序排列。
     *
     * @param all        当前全部启用砝码
     * @param selectedNo 已勾选的挂钩号
     * @param limit      最多返回数量
     * @return 建议列表，元素包含编号、标签、重量和类别
     */
    public static List<Map<String, Object>> suggestions(List<JlczWeight> all, Set<Integer> selectedNo, int limit) {
        List<JlczWeight> candidates = new ArrayList<>();
        for (JlczWeight weight : all) {
            if (weight.getWeightNo() == null || selectedNo.contains(weight.getWeightNo())) {
                continue;
            }
            candidates.add(weight);
        }
        candidates.sort((left, right) -> {
            int compare = Integer.compare(value(right.getWeightG()), value(left.getWeightG()));
            if (compare != 0) {
                return compare;
            }
            compare = Integer.compare(categoryIndex(left.getCategoryCode()), categoryIndex(right.getCategoryCode()));
            if (compare != 0) {
                return compare;
            }
            return Integer.compare(value(left.getWeightNo()), value(right.getWeightNo()));
        });
        List<Map<String, Object>> rows = new ArrayList<>();
        for (JlczWeight weight : candidates) {
            if (rows.size() >= limit) {
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("weightNo", weight.getWeightNo());
            row.put("label", weight.getLabel());
            row.put("icon", weight.getIcon());
            row.put("weightG", weight.getWeightG());
            row.put("categoryCode", weight.getCategoryCode());
            row.put("categoryName", weight.getCategoryName());
            row.put("conditionText", weight.getConditionText());
            rows.add(row);
        }
        return rows;
    }

    /** 把英文类别编码转为固定顺序下标，未知类别排在最后。 */
    public static int categoryIndex(String code) {
        for (int i = 0; i < CATEGORY_ORDER.length; i++) {
            if (CATEGORY_ORDER[i].equals(code)) {
                return i;
            }
        }
        return CATEGORY_ORDER.length;
    }

    /** 大类默认中文名，后台未填写时按编码回退。 */
    public static String categoryName(String code) {
        switch (code == null ? "" : code) {
            case CATEGORY_CORE:
                return "硬通货（核心竞争力）";
            case CATEGORY_SOFT:
                return "软实力（发展潜力）";
            case CATEGORY_BASIC:
                return "基本功（职业素养）";
            case CATEGORY_BONUS:
                return "加分项（额外亮点）";
            default:
                return "其他";
        }
    }

    private static int value(Integer number) {
        return number == null ? 0 : number;
    }

    /** 一次称重的计算结果。 */
    public static final class Outcome {

        /** 本次总重量（克）。 */
        private final int totalWeight;

        /** 四大类小计重量（克），始终包含四个类别。 */
        private final Map<String, Integer> categoryTotals;

        /** 是否通关。 */
        private final boolean pass;

        /** 本次使用的通关阈值（克）。 */
        private final int passWeight;

        /** 距通关还差的重量（克），通关时为 0。 */
        private final int gap;

        /** 是否恰好等于通关阈值（显示“刚好达标”）。 */
        private final boolean balanced;

        public Outcome(int totalWeight, Map<String, Integer> categoryTotals, boolean pass,
                       int passWeight, int gap, boolean balanced) {
            this.totalWeight = totalWeight;
            this.categoryTotals = categoryTotals;
            this.pass = pass;
            this.passWeight = passWeight;
            this.gap = gap;
            this.balanced = balanced;
        }

        public int getTotalWeight() {
            return totalWeight;
        }

        public Map<String, Integer> getCategoryTotals() {
            return categoryTotals;
        }

        public boolean isPass() {
            return pass;
        }

        public int getPassWeight() {
            return passWeight;
        }

        public int getGap() {
            return gap;
        }

        public boolean isBalanced() {
            return balanced;
        }

        /** 用于写入参与记录的结果编码。 */
        public String getResult() {
            return pass ? RESULT_PASS : RESULT_FAIL;
        }
    }
}
