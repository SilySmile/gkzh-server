package com.gkzh.app.service;

import com.gkzh.zyfxq.support.ZyfxqBoard;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 职业飞行棋棋盘规则回归：
 * 19 格固定环形布局、起点位置、超过终点按抵达处理、后退最低到起点、终点判定。
 */
public class ZyfxqBoardTest {

    @Test
    public void layoutMatchesDesign() {
        // 顶排 11-16 从左到右。
        assertArrayEquals(new int[]{0, 0}, ZyfxqBoard.slotOf(11));
        assertArrayEquals(new int[]{0, 5}, ZyfxqBoard.slotOf(16));
        // 左列向下依次是 10、9、8、7、6。
        assertArrayEquals(new int[]{1, 0}, ZyfxqBoard.slotOf(10));
        assertArrayEquals(new int[]{5, 0}, ZyfxqBoard.slotOf(6));
        // 底排向左依次是 5、4、3、2、1。
        assertArrayEquals(new int[]{6, 0}, ZyfxqBoard.slotOf(5));
        assertArrayEquals(new int[]{6, 4}, ZyfxqBoard.slotOf(1));
        // 右列向下依次是 17、18、19（终点）。
        assertArrayEquals(new int[]{1, 5}, ZyfxqBoard.slotOf(17));
        assertArrayEquals(new int[]{3, 5}, ZyfxqBoard.slotOf(19));
        // 起点占第 7 行最右一格。
        assertArrayEquals(new int[]{6, 5}, ZyfxqBoard.START_SLOT);
    }

    @Test
    public void allCellsFitInsideBoardWithoutOverlap() {
        Map<Integer, int[]> slots = ZyfxqBoard.slots();
        assertEquals(ZyfxqBoard.CELL_TOTAL, slots.size());
        Set<String> used = new HashSet<>();
        used.add(ZyfxqBoard.START_SLOT[0] + "," + ZyfxqBoard.START_SLOT[1]);
        for (int cellNo = 1; cellNo <= ZyfxqBoard.END_CELL; cellNo++) {
            int[] slot = slots.get(cellNo);
            assertNotNull(slot, "格号 " + cellNo + " 缺少布局坐标");
            assertTrue(slot[0] >= 0 && slot[0] < ZyfxqBoard.ROWS, "格号 " + cellNo + " 行越界");
            assertTrue(slot[1] >= 0 && slot[1] < ZyfxqBoard.COLUMNS, "格号 " + cellNo + " 列越界");
            assertTrue(used.add(slot[0] + "," + slot[1]), "格号 " + cellNo + " 坐标与已有格子重叠");
        }
        assertNull(ZyfxqBoard.slotOf(0));
        assertNull(ZyfxqBoard.slotOf(20));
    }

    @Test
    public void forwardMovesAndStopsAtEnd() {
        assertEquals(6, ZyfxqBoard.forward(0, 6));
        assertEquals(13, ZyfxqBoard.forward(10, 3));
        // 超过终点按终点处理，不回弹。
        assertEquals(ZyfxqBoard.END_CELL, ZyfxqBoard.forward(18, 3));
        assertEquals(ZyfxqBoard.END_CELL, ZyfxqBoard.forward(19, 1));
        assertEquals(0, ZyfxqBoard.forward(0, 0));
        // 异常负数按 0 处理，避免脏数据把棋子推到非法格号。
        assertEquals(0, ZyfxqBoard.forward(-5, 0));
        // 负步数视为不移动，棋子停在原地。
        assertEquals(0, ZyfxqBoard.forward(0, -3));
        assertEquals(4, ZyfxqBoard.forward(4, -3));
    }

    @Test
    public void backwardStopsAtStart() {
        assertEquals(3, ZyfxqBoard.backward(5, 2));
        // 最低退到起点，不会出现负数格号。
        assertEquals(0, ZyfxqBoard.backward(1, 2));
        assertEquals(0, ZyfxqBoard.backward(0, 2));
        // 后退格数为 0 时位置不变，用于后台把后退格数配置为 0 的场景。
        assertEquals(19, ZyfxqBoard.backward(19, 0));
    }

    @Test
    public void reachedEndOnlyAtEndCell() {
        assertFalse(ZyfxqBoard.reachedEnd(0));
        assertFalse(ZyfxqBoard.reachedEnd(18));
        assertTrue(ZyfxqBoard.reachedEnd(19));
    }
}
