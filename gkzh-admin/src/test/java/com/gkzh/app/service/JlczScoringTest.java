package com.gkzh.app.service;

import com.gkzh.jlcz.domain.JlczSetting;
import com.gkzh.jlcz.domain.JlczWeight;
import com.gkzh.jlcz.support.JlczScoring;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 简历称重计分回归：总重计算、80g 边界、分类小计与未勾选建议顺序。
 */
public class JlczScoringTest {

    private static JlczSetting setting() {
        JlczSetting setting = new JlczSetting();
        setting.setLeftWeightG(80);
        setting.setPassWeightG(80);
        return setting;
    }

    private static JlczWeight weight(int no, int grams, String category) {
        JlczWeight weight = new JlczWeight();
        weight.setWeightNo(no);
        weight.setWeightG(grams);
        weight.setCategoryCode(category);
        weight.setCategoryName(JlczScoring.categoryName(category));
        weight.setLabel("砝码" + no);
        weight.setStatus("0");
        return weight;
    }

    @Test
    public void totalBelowThresholdFailsAndKeepsGap() {
        List<JlczWeight> selected = Arrays.asList(
                weight(1, 10, JlczScoring.CATEGORY_CORE),
                weight(2, 10, JlczScoring.CATEGORY_CORE),
                weight(6, 5, JlczScoring.CATEGORY_SOFT));
        JlczScoring.Outcome outcome = JlczScoring.compute(selected, setting());
        assertEquals(25, outcome.getTotalWeight());
        assertFalse(outcome.isPass());
        assertEquals("fail", outcome.getResult());
        assertEquals(55, outcome.getGap());
        assertFalse(outcome.isBalanced());
    }

    @Test
    public void exactlyEightyPassesAndIsBalanced() {
        List<JlczWeight> selected = new ArrayList<>();
        for (int no = 1; no <= 5; no++) {
            selected.add(weight(no, 10, JlczScoring.CATEGORY_CORE));
        }
        for (int no = 6; no <= 11; no++) {
            selected.add(weight(no, 5, JlczScoring.CATEGORY_SOFT));
        }
        JlczScoring.Outcome outcome = JlczScoring.compute(selected, setting());
        assertEquals(80, outcome.getTotalWeight());
        assertTrue(outcome.isPass());
        assertTrue(outcome.isBalanced());
        assertEquals("pass", outcome.getResult());
        assertEquals(0, outcome.getGap());
    }

    @Test
    public void oneGramAboveThresholdIsNotBalanced() {
        List<JlczWeight> selected = new ArrayList<>();
        for (int no = 1; no <= 5; no++) {
            selected.add(weight(no, 10, JlczScoring.CATEGORY_CORE));
        }
        for (int no = 6; no <= 11; no++) {
            selected.add(weight(no, 5, JlczScoring.CATEGORY_SOFT));
        }
        selected.add(weight(20, 1, JlczScoring.CATEGORY_BONUS));
        JlczScoring.Outcome outcome = JlczScoring.compute(selected, setting());
        assertEquals(81, outcome.getTotalWeight());
        assertTrue(outcome.isPass());
        assertFalse(outcome.isBalanced());
    }

    @Test
    public void emptySelectionFailsWithFullGap() {
        JlczScoring.Outcome outcome = JlczScoring.compute(new ArrayList<>(), setting());
        assertEquals(0, outcome.getTotalWeight());
        assertFalse(outcome.isPass());
        assertEquals(80, outcome.getGap());
    }

    @Test
    public void categoryTotalsAlwaysContainFourCategories() {
        List<JlczWeight> selected = Arrays.asList(
                weight(1, 10, JlczScoring.CATEGORY_CORE),
                weight(6, 5, JlczScoring.CATEGORY_SOFT),
                weight(11, 5, JlczScoring.CATEGORY_BASIC),
                weight(16, 5, JlczScoring.CATEGORY_BONUS));
        JlczScoring.Outcome outcome = JlczScoring.compute(selected, setting());
        assertEquals(4, outcome.getCategoryTotals().size());
        assertEquals(10, outcome.getCategoryTotals().get(JlczScoring.CATEGORY_CORE));
        assertEquals(5, outcome.getCategoryTotals().get(JlczScoring.CATEGORY_SOFT));
        assertEquals(5, outcome.getCategoryTotals().get(JlczScoring.CATEGORY_BASIC));
        assertEquals(5, outcome.getCategoryTotals().get(JlczScoring.CATEGORY_BONUS));
    }

    @Test
    public void suggestionsPreferHeavyUnselectedWeightsAndSkipSelected() {
        List<JlczWeight> all = Arrays.asList(
                weight(1, 10, JlczScoring.CATEGORY_CORE),
                weight(2, 10, JlczScoring.CATEGORY_CORE),
                weight(3, 10, JlczScoring.CATEGORY_CORE),
                weight(6, 5, JlczScoring.CATEGORY_SOFT));
        List<Map<String, Object>> rows = JlczScoring.suggestions(all, new HashSet<>(Arrays.asList(1)), 2);
        assertEquals(2, rows.size());
        assertEquals(2, rows.get(0).get("weightNo"));
        assertEquals(3, rows.get(1).get("weightNo"));
    }

    @Test
    public void suggestionLimitKeepsAllWhenFewerCandidates() {
        List<JlczWeight> all = Arrays.asList(weight(1, 10, JlczScoring.CATEGORY_CORE));
        List<Map<String, Object>> rows = JlczScoring.suggestions(all, new HashSet<>(), 3);
        assertEquals(1, rows.size());
    }

    @Test
    public void categoryNameFallsBackForUnknownCode() {
        assertEquals("其他", JlczScoring.categoryName("unknown"));
        assertEquals("硬通货（核心竞争力）", JlczScoring.categoryName(JlczScoring.CATEGORY_CORE));
    }
}
