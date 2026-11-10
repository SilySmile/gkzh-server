package com.gkzh.app.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报告条目格式化工具。
 *
 * <p>报告里的「职业方向」「工作环境偏好」等列表既可能是 Map，也可能是
 * {@code Career}、{@code WorkEnv} 这类只有 getTitle() 的实体对象。
 * 早期实现直接调用 {@code String.valueOf(item)}，会把 Apache Commons
 * ToStringBuilder 的多行结构（类名、careerId、title 等）原样印进 PDF，
 * 学生看到的就是一段“代码”。这里统一按标题/说明取值，取不到就跳过，
 * 绝不再回退到 toString()。</p>
 */
public final class ReportValueFormatter {

    private ReportValueFormatter() {
    }

    /**
     * 把列表条目转换为「标题, 说明」二元组；说明可能为空。
     *
     * @param rawItems 列表或单值，非 Iterable 时返回空结果
     * @return 每项为 [标题, 说明]，标题不会为空
     */
    public static List<String[]> titleValues(Object rawItems) {
        List<String[]> rows = new ArrayList<>();
        if (!(rawItems instanceof Iterable)) {
            return rows;
        }
        for (Object item : (Iterable<?>) rawItems) {
            if (item == null) {
                continue;
            }
            String title;
            String description;
            if (item instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) item;
                title = text(map.get("title"));
                if (title == null) {
                    title = text(map.get("name"));
                }
                description = text(map.get("description"));
            } else {
                title = text(read(item, "getTitle"));
                if (title == null) {
                    title = text(read(item, "getName"));
                }
                description = text(read(item, "getDescription"));
            }
            if (title == null && description == null) {
                continue;
            }
            rows.add(new String[]{title == null ? "项目" : title, description});
        }
        return rows;
    }

    /** 读取无参 getter；方法不存在或调用失败时返回 null，不抛异常。 */
    private static Object read(Object bean, String getter) {
        try {
            Method method = bean.getClass().getMethod(getter);
            return method.invoke(bean);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
