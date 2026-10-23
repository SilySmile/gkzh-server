package com.gkzh.app.service;

import com.gkzh.zcyltzj.support.ZcyltzjScoring;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 骰子局计分规则回归：按需求方确认的“A/B 计入红色”口径计算压力值和颜色。
 */
public class ZcyltzjScoringTest {

    @Test
    public void allRedOptionsProduceFullPressureAndRedColor() {
        ZcyltzjScoring.Outcome outcome = ZcyltzjScoring.compute(Arrays.asList("A", "B", "A", "B", "A", "B"));
        assertEquals(6, outcome.getRedCount());
        assertEquals(0, outcome.getGreenCount());
        assertEquals(100, outcome.getPressureValue());
        assertEquals("red", outcome.getColor());
    }

    @Test
    public void allGreenOptionsProduceZeroPressureAndGreenColor() {
        ZcyltzjScoring.Outcome outcome = ZcyltzjScoring.compute(Arrays.asList("C", "D", "C", "D", "C", "D"));
        assertEquals(0, outcome.getRedCount());
        assertEquals(6, outcome.getGreenCount());
        assertEquals(0, outcome.getPressureValue());
        assertEquals("green", outcome.getColor());
    }

    @Test
    public void halfRedAndHalfGreenProducesYellowColor() {
        ZcyltzjScoring.Outcome outcome = ZcyltzjScoring.compute(Arrays.asList("A", "B", "A", "C", "D", "D"));
        assertEquals(3, outcome.getRedCount());
        assertEquals(3, outcome.getGreenCount());
        assertEquals(50, outcome.getPressureValue());
        assertEquals("yellow", outcome.getColor());
    }

    @Test
    public void fourRedAnswersProduceRedColor() {
        ZcyltzjScoring.Outcome outcome = ZcyltzjScoring.compute(Arrays.asList("A", "A", "A", "B", "C", "D"));
        assertEquals(4, outcome.getRedCount());
        assertEquals(67, outcome.getPressureValue());
        assertEquals("red", outcome.getColor());
    }

    @Test
    public void twoRedAnswersProduceGreenColor() {
        ZcyltzjScoring.Outcome outcome = ZcyltzjScoring.compute(Arrays.asList("A", "B", "C", "C", "D", "D"));
        assertEquals(2, outcome.getRedCount());
        assertEquals(33, outcome.getPressureValue());
        assertEquals("green", outcome.getColor());
    }

    @Test
    public void optionIsCaseInsensitiveAndTrimmed() {
        ZcyltzjScoring.Outcome outcome = ZcyltzjScoring.compute(Arrays.asList(" a ", "b", "c", "D", "A", "C"));
        assertEquals(3, outcome.getRedCount());
    }

    @Test
    public void invalidOptionIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> ZcyltzjScoring.compute(Arrays.asList("A", "B", "C", "E")));
    }

    @Test
    public void emptyAnswersAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ZcyltzjScoring.compute(Arrays.asList()));
    }
}
