package com.gkzh.app.service;

import com.gkzh.xycc.domain.Career;
import com.gkzh.xycc.domain.WorkEnv;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 报告条目格式化回归。
 * 心愿橱窗的「职业方向」「工作环境偏好」传入的是 Career / WorkEnv 实体，
 * 必须取标题展示，不能把 toString() 的多行结构（类名、字段名）印进 PDF。
 */
public class ReportValueFormatterTest {

    @Test
    void mapItemsUseTitleAndDescription() {
        Map<String, Object> item = new HashMap<>();
        item.put("title", "数据分析师");
        item.put("description", "负责数据采集与分析");

        List<String[]> rows = ReportValueFormatter.titleValues(Arrays.asList(item));

        assertEquals(1, rows.size());
        assertEquals("数据分析师", rows.get(0)[0]);
        assertEquals("负责数据采集与分析", rows.get(0)[1]);
    }

    @Test
    void careerEntityUsesTitleInsteadOfToString() {
        Career career = new Career();
        career.setCareerId(3L);
        career.setTitle("人工智能训练师");

        List<String[]> rows = ReportValueFormatter.titleValues(Arrays.asList(career));

        assertEquals(1, rows.size());
        assertEquals("人工智能训练师", rows.get(0)[0]);
        assertNull(rows.get(0)[1]);
        // 旧实现会把 "com.gkzh.xycc.domain.Career@xxxx[" 这类内容打进报告。
        String joined = rows.get(0)[0] + "|" + (rows.get(0)[1] == null ? "" : rows.get(0)[1]);
        assertFalse(joined.contains("@"), joined);
        assertFalse(joined.contains("careerId"), joined);
        assertFalse(joined.contains("["), joined);
    }

    @Test
    void workEnvEntityUsesTitle() {
        WorkEnv env = new WorkEnv();
        env.setWorkEnvId(9L);
        env.setTitle("室内办公环境");

        List<String[]> rows = ReportValueFormatter.titleValues(Arrays.asList(env));

        assertEquals(1, rows.size());
        assertEquals("室内办公环境", rows.get(0)[0]);
    }

    @Test
    void unsupportedItemsAreSkippedNotPrinted() {
        List<String[]> rows = ReportValueFormatter.titleValues(Arrays.asList(new Object(), null));
        assertTrue(rows.isEmpty(), "无法识别的元素应跳过，不能回退到 toString()");
    }

    @Test
    void nonIterableReturnsEmpty() {
        assertTrue(ReportValueFormatter.titleValues("纯文本").isEmpty());
        assertTrue(ReportValueFormatter.titleValues(null).isEmpty());
    }
}
