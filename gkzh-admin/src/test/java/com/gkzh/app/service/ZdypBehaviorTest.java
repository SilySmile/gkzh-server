package com.gkzh.app.service;

import com.gkzh.zdyp.service.ZdypBehavior;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 职得一拼行为分型与文案回归。
 * 分型只依据首次尝试：零错误=全面均衡；缺点错且最集中=优势偏好；用时不超过 20 秒且错 2 个以上=急躁粗放；
 * 其余为信息薄弱。文案必须与需求文档的四类结论一致，不能为空。
 */
public class ZdypBehaviorTest {

    @Test
    public void zeroWrongIsBalanced() {
        assertEquals(ZdypBehavior.BALANCED, ZdypBehavior.classify(0, 0, 0, 45_000L));
        assertEquals(ZdypBehavior.BALANCED, ZdypBehavior.classify(0, 0, 0, 5_000L));
    }

    @Test
    public void weaknessDominantErrorsAreAdvantage() {
        // 缺点放错、其余最多错一个：缺点板块错得最集中。
        assertEquals(ZdypBehavior.ADVANTAGE, ZdypBehavior.classify(1, 1, 0, 42_000L));
        assertEquals(ZdypBehavior.ADVANTAGE, ZdypBehavior.classify(2, 1, 1, 30_000L));
        // 缺点没放错、错误分散在两个其他维度时不属于优势偏好。
        assertFalse(ZdypBehavior.ADVANTAGE.equals(ZdypBehavior.classify(2, 0, 2, 40_000L)));
    }

    @Test
    public void fastWithManyWrongIsRushed() {
        assertEquals(ZdypBehavior.RUSHED, ZdypBehavior.classify(2, 0, 2, 12_000L));
        assertEquals(ZdypBehavior.RUSHED, ZdypBehavior.classify(3, 0, 3, 20_000L));
        // 用时超过 20 秒时即使错误多也归为信息薄弱型。
        assertEquals(ZdypBehavior.WEAK, ZdypBehavior.classify(3, 0, 3, 30_000L));
    }

    @Test
    public void everythingElseIsWeak() {
        assertEquals(ZdypBehavior.WEAK, ZdypBehavior.classify(1, 0, 1, 35_000L));
        assertEquals(ZdypBehavior.WEAK, ZdypBehavior.classify(4, 0, 4, 60_000L));
        // 优势偏好优先于急躁粗放：缺点错且快速提交仍算优势偏好。
        assertEquals(ZdypBehavior.ADVANTAGE, ZdypBehavior.classify(2, 1, 1, 10_000L));
    }

    @Test
    public void namesAndTextsAreComplete() {
        String[] types = {ZdypBehavior.BALANCED, ZdypBehavior.ADVANTAGE,
                ZdypBehavior.WEAK, ZdypBehavior.RUSHED};
        String[] names = {"全面均衡型", "优势偏好型", "信息薄弱型", "急躁粗放型"};
        for (int i = 0; i < types.length; i++) {
            assertEquals(names[i], ZdypBehavior.name(types[i]));
            assertNotNull(ZdypBehavior.conclusion(types[i]));
            assertNotNull(ZdypBehavior.suggestion(types[i]));
            assertTrue(ZdypBehavior.conclusion(types[i]).length() > 40, "个人结论不能为空泛文案");
            assertTrue(ZdypBehavior.suggestion(types[i]).length() > 15, "提升建议不能为空泛文案");
        }
        // 未知编码按全面均衡型兜底，避免历史数据出现空白报告。
        assertEquals("全面均衡型", ZdypBehavior.name("unknown"));
        assertNotNull(ZdypBehavior.conclusion("unknown"));
    }
}
