package com.gkzh.zcyltzj.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 骰子局计分规则。
 *
 * <p>颜色口径按需求方确认执行（与常见“A/B 为绿”的直觉相反）：
 * A、B 选项计入红色一侧，C、D 选项计入绿色一侧。红色答案占比即页面展示的“压力值”。
 * 压力值大于 50 显示红色，小于 50 显示绿色，等于 50 显示黄色。
 * 如需调整口径，只改 {@link #RED_OPTIONS} 一处即可。</p>
 */
public final class ZcyltzjScoring {

    /** 计入红色一侧的选项；按需求方要求 A、B 为红色。 */
    private static final Set<String> RED_OPTIONS = Collections.unmodifiableSet(new HashSet<>(
            java.util.Arrays.asList("A", "B")));

    private ZcyltzjScoring() {
    }

    /**
     * 根据每道题选择的选项计算红色数量、绿色数量、压力值和结果颜色。
     *
     * @param options 每题选项，元素为 A、B、C、D
     * @return 计算结果
     */
    public static Outcome compute(List<String> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("缺少答案，无法计算压力值");
        }
        int red = 0;
        int green = 0;
        for (String option : options) {
            String value = option == null ? "" : option.trim().toUpperCase();
            if (!"A".equals(value) && !"B".equals(value) && !"C".equals(value) && !"D".equals(value)) {
                throw new IllegalArgumentException("选项必须为 A、B、C 或 D");
            }
            if (RED_OPTIONS.contains(value)) {
                red++;
            } else {
                green++;
            }
        }
        int pressure = Math.round(red * 100f / options.size());
        return new Outcome(red, green, pressure, colorOf(pressure));
    }

    /** 压力值大于 50 红色，小于 50 绿色，等于 50 黄色。 */
    public static String colorOf(int pressureValue) {
        if (pressureValue > 50) {
            return "red";
        }
        if (pressureValue < 50) {
            return "green";
        }
        return "yellow";
    }

    /** 一次计分的结果。 */
    public static final class Outcome {
        private final int redCount;
        private final int greenCount;
        private final int pressureValue;
        private final String color;

        public Outcome(int redCount, int greenCount, int pressureValue, String color) {
            this.redCount = redCount;
            this.greenCount = greenCount;
            this.pressureValue = pressureValue;
            this.color = color;
        }

        /** 红色（压力高）答案数量。 */
        public int getRedCount() {
            return redCount;
        }

        /** 绿色（压力低）答案数量。 */
        public int getGreenCount() {
            return greenCount;
        }

        /** 压力值（红色答案占比），0-100。 */
        public int getPressureValue() {
            return pressureValue;
        }

        /** 结果颜色：red、green 或 yellow。 */
        public String getColor() {
            return color;
        }
    }
}
