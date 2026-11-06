package com.gkzh.app.service;

import com.gkzh.nlld.domain.NlldAnswer;
import com.gkzh.nlld.domain.NlldDimension;
import com.gkzh.nlld.support.NlldScoring;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 能量雷达计分规则回归：
 * 15 个维度各出现 2 次、单维度最高 2 分，恰好 2 个维度得 2 分为 TOP2，其余为多元开放型。
 */
public class NlldScoringTest {

    private static NlldAnswer answer(String code) {
        NlldAnswer answer = new NlldAnswer();
        answer.setDimensionCode(code);
        return answer;
    }

    private static List<NlldAnswer> answers(String... codes) {
        List<NlldAnswer> list = new ArrayList<>();
        for (String code : codes) {
            list.add(answer(code));
        }
        return list;
    }

    private static List<NlldDimension> dimensions() {
        List<NlldDimension> list = new ArrayList<>();
        int sort = 1;
        for (String code : NlldScoring.DIMENSION_CODES) {
            NlldDimension dimension = new NlldDimension();
            dimension.setDimensionId((long) sort);
            dimension.setDimensionCode(code);
            dimension.setDimensionName(code);
            dimension.setSortOrder(sort);
            list.add(dimension);
            sort++;
        }
        return list;
    }

    @Test
    public void dimensionCodesAreFifteenAndUnique() {
        assertEquals(15, NlldScoring.DIMENSION_CODES.size());
        assertEquals(15, NlldScoring.DIMENSION_CODES.stream().distinct().count());
        assertTrue(NlldScoring.isValidCode("security"));
        assertFalse(NlldScoring.isValidCode("unknown"));
    }

    @Test
    public void emptyScoresAreAllZero() {
        Map<String, Integer> scores = NlldScoring.emptyScores();
        assertEquals(15, scores.size());
        assertEquals(0, scores.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    public void exactlyTwoDimensionsAtTwoPointsIsTop2() {
        List<NlldAnswer> list = answers(
                "security", "security", "management", "management",
                "altruism", "creativity", "intellect", "aesthetics",
                "salary", "colleague", "autonomy", "lifestyle");
        Map<String, Integer> scores = NlldScoring.scoreOf(list);
        assertEquals(12, scores.values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(NlldScoring.RESULT_TOP2, NlldScoring.resultType(scores));
        List<NlldDimension> top = NlldScoring.twoPointDimensions(scores, dimensions());
        assertEquals(2, top.size());
        assertEquals("security", top.get(0).getDimensionCode());
        assertEquals("management", top.get(1).getDimensionCode());
    }

    @Test
    public void threeDimensionsAtTwoPointsIsOpen() {
        List<NlldAnswer> list = answers(
                "altruism", "altruism", "security", "security", "autonomy", "autonomy",
                "achievement", "aesthetics", "environment", "salary", "colleague", "lifestyle");
        Map<String, Integer> scores = NlldScoring.scoreOf(list);
        assertEquals(NlldScoring.RESULT_OPEN, NlldScoring.resultType(scores));
    }

    @Test
    public void onlyOneDimensionAtTwoPointsIsOpen() {
        List<NlldAnswer> list = answers(
                "intellect", "intellect",
                "achievement", "creativity", "autonomy", "variety", "salary",
                "security", "prestige", "aesthetics", "environment", "management");
        Map<String, Integer> scores = NlldScoring.scoreOf(list);
        assertEquals(NlldScoring.RESULT_OPEN, NlldScoring.resultType(scores));
    }

    @Test
    public void noDimensionAtTwoPointsIsOpen() {
        List<NlldAnswer> list = answers(
                "intellect", "achievement", "creativity", "autonomy", "variety", "salary",
                "security", "prestige", "aesthetics", "environment", "management", "colleague");
        Map<String, Integer> scores = NlldScoring.scoreOf(list);
        assertEquals(NlldScoring.RESULT_OPEN, NlldScoring.resultType(scores));
    }

    @Test
    public void unknownDimensionCodesAreIgnored() {
        Map<String, Integer> scores = NlldScoring.scoreOf(answers("security", "not-exists"));
        assertEquals(1, scores.get("security"));
        assertEquals(15, scores.size());
        assertEquals(1, scores.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    public void topDimensionsFollowConfiguredOrder() {
        NlldDimension first = new NlldDimension();
        first.setDimensionId(2L);
        first.setDimensionCode("security");
        first.setSortOrder(2);
        NlldDimension second = new NlldDimension();
        second.setDimensionId(1L);
        second.setDimensionCode("management");
        second.setSortOrder(1);
        Map<String, Integer> scores = NlldScoring.emptyScores();
        scores.put("security", 2);
        scores.put("management", 2);
        List<NlldDimension> top = NlldScoring.twoPointDimensions(scores,
                java.util.Arrays.asList(first, second));
        assertEquals("management", top.get(0).getDimensionCode());
        assertEquals("security", top.get(1).getDimensionCode());
    }

    @Test
    public void openTextIsPresent() {
        assertFalse(NlldScoring.OPEN_TEXT.trim().isEmpty());
        assertTrue(NlldScoring.OPEN_TEXT.contains("开放态度"));
        assertTrue(NlldScoring.OPEN_TEXT.contains("职业价值观排序"));
    }
}
