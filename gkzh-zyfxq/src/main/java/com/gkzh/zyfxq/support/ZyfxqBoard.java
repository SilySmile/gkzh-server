package com.gkzh.zyfxq.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 职业飞行棋棋盘规则与固定布局。
 *
 * <p>棋盘共 19 格，另有起点（位置 0）。格子按行进顺序编号：
 * 起点在右下角，沿底排向左到 1-5，再沿左列向上到 6-10，
 * 再沿顶排向右到 11-16，最后沿右列向下到 17-19（终点）。</p>
 *
 * <p>布局按设计稿固定为 6 列 × 7 行的环型，由服务端与小程序共用同一套坐标，
 * 行列均从 0 开始；起点占第 7 行最右一格。</p>
 */
public final class ZyfxqBoard {

    /** 终点格号，抵达或越过该格即通关。 */
    public static final int END_CELL = 19;

    /** 棋盘总格数。 */
    public static final int CELL_TOTAL = 19;

    /** 棋盘列数。 */
    public static final int COLUMNS = 6;

    /** 棋盘行数。 */
    public static final int ROWS = 7;

    /** 起点的行列坐标（第 7 行最右一格）。 */
    public static final int[] START_SLOT = {6, 5};

    /** 格号 → 行列坐标；起点不参与编号，单独用 START_SLOT 表示。 */
    private static final Map<Integer, int[]> SLOTS;

    static {
        Map<Integer, int[]> slots = new LinkedHashMap<>();
        // 顶排：11-16 从左到右。
        slots.put(11, new int[]{0, 0});
        slots.put(12, new int[]{0, 1});
        slots.put(13, new int[]{0, 2});
        slots.put(14, new int[]{0, 3});
        slots.put(15, new int[]{0, 4});
        slots.put(16, new int[]{0, 5});
        // 左列向下：10、9、8、7、6。
        slots.put(10, new int[]{1, 0});
        slots.put(9, new int[]{2, 0});
        slots.put(8, new int[]{3, 0});
        slots.put(7, new int[]{4, 0});
        slots.put(6, new int[]{5, 0});
        // 底排向左：5、4、3、2、1。
        slots.put(5, new int[]{6, 0});
        slots.put(4, new int[]{6, 1});
        slots.put(3, new int[]{6, 2});
        slots.put(2, new int[]{6, 3});
        slots.put(1, new int[]{6, 4});
        // 右列向下：17、18、19（终点）。
        slots.put(17, new int[]{1, 5});
        slots.put(18, new int[]{2, 5});
        slots.put(19, new int[]{3, 5});
        SLOTS = Collections.unmodifiableMap(slots);
    }

    private ZyfxqBoard() {
    }

    /** 查询格号对应的行列坐标；非法格号返回 null。 */
    public static int[] slotOf(int cellNo) {
        return SLOTS.get(cellNo);
    }

    /** 返回只读的格号到坐标映射，供接口下发棋盘布局。 */
    public static Map<Integer, int[]> slots() {
        return SLOTS;
    }

    /** 前行：超过终点按终点计算，实现“超过即算抵达”。 */
    public static int forward(int position, int steps) {
        int next = Math.max(0, position) + Math.max(0, steps);
        return Math.min(END_CELL, next);
    }

    /** 后退：最低退到起点（位置 0）。 */
    public static int backward(int position, int steps) {
        return Math.max(0, Math.max(0, position) - Math.max(0, steps));
    }

    /** 是否已抵达终点。 */
    public static boolean reachedEnd(int position) {
        return position >= END_CELL;
    }
}
