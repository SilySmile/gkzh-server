package com.gkzh.zyfxq.service;

import com.gkzh.zyfxq.domain.dto.ZyfxqRecordQuery;
import com.gkzh.zyfxq.mapper.ZyfxqRecordMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职业飞行棋统计分析服务（结果输出-学校端）。
 *
 * <p>统计口径与需求文档一致：第一页为参与总人数、参与者平均单局耗时、累计掷骰总次数和整体通过人数；
 * 第二页为各知识点（职业问答 / 情景任务卡）正确率、超时答题占比、各题目正确率、每题平均作答用时
 * 和高频错题。后台统计页、活动查看页与 Excel 导出共用同一套计算，避免同一活动出现多个数字。</p>
 */
@Service
public class ZyfxqStatisticsService {

    private final ZyfxqRecordMapper recordMapper;

    public ZyfxqStatisticsService(ZyfxqRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    /** 第一页汇总：参与人数、结束局数、通关人数、平均耗时、累计掷骰次数与整体正确率。 */
    public Map<String, Object> summary(ZyfxqRecordQuery query) {
        Map<String, Object> raw = recordMapper.selectAdminSummary(query);
        long total = number(raw == null ? null : raw.get("total"));
        long finished = number(raw == null ? null : raw.get("finishedCount"));
        long win = number(raw == null ? null : raw.get("winCount"));
        long lose = number(raw == null ? null : raw.get("loseCount"));
        long playing = number(raw == null ? null : raw.get("playingCount"));
        long rollTotal = number(raw == null ? null : raw.get("rollTotal"));
        long stepTotal = number(raw == null ? null : raw.get("stepTotal"));
        long answerTotal = number(raw == null ? null : raw.get("answerTotal"));
        long correctTotal = number(raw == null ? null : raw.get("correctTotal"));
        long timeoutTotal = number(raw == null ? null : raw.get("timeoutTotal"));
        long avgDurationMs = Math.round(decimal(raw == null ? null : raw.get("avgDurationMs")).doubleValue());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("participantCount", total);
        data.put("finishedCount", finished);
        data.put("winCount", win);
        data.put("loseCount", lose);
        data.put("playingCount", playing);
        data.put("passRate", percent(win, finished));
        data.put("finishRate", percent(finished, total));
        data.put("avgDurationMs", avgDurationMs);
        data.put("avgDurationSeconds", seconds(avgDurationMs));
        data.put("rollTotal", rollTotal);
        data.put("stepTotal", stepTotal);
        data.put("answerTotal", answerTotal);
        data.put("correctTotal", correctTotal);
        data.put("timeoutTotal", timeoutTotal);
        data.put("correctRate", percent(correctTotal, answerTotal));
        data.put("timeoutRate", percent(timeoutTotal, answerTotal));
        return data;
    }

    /** 第二页知识点正确率：职业问答与情景任务卡两类，缺失的题型补 0 行。 */
    public List<Map<String, Object>> questionTypeStats(ZyfxqRecordQuery query) {
        Map<String, Map<String, Object>> raw = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectQuestionTypeStats(query)) {
            raw.put(String.valueOf(row.get("questionType")), row);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        int serial = 1;
        for (String type : new String[]{ZyfxqConfigService.TYPE_CAREER, ZyfxqConfigService.TYPE_SCENARIO}) {
            Map<String, Object> row = raw.get(type);
            long answerCount = number(row == null ? null : row.get("answerCount"));
            long correctCount = number(row == null ? null : row.get("correctCount"));
            long timeoutCount = number(row == null ? null : row.get("timeoutCount"));
            long avgCostMs = Math.round(decimal(row == null ? null : row.get("avgCostMs")).doubleValue());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("serialNo", serial++);
            item.put("questionType", type);
            item.put("questionTypeName", ZyfxqConfigService.questionTypeName(type));
            item.put("answerCount", answerCount);
            item.put("correctCount", correctCount);
            item.put("correctRate", percent(correctCount, answerCount));
            item.put("timeoutCount", timeoutCount);
            item.put("timeoutRate", percent(timeoutCount, answerCount));
            item.put("avgCostMs", avgCostMs);
            item.put("avgCostSeconds", seconds(avgCostMs));
            rows.add(item);
        }
        return rows;
    }

    /** 第二页题目明细：每题作答次数、正确率、超时次数与平均作答用时。 */
    public List<Map<String, Object>> questionStats(ZyfxqRecordQuery query) {
        List<Map<String, Object>> rows = new ArrayList<>();
        int serial = 1;
        for (Map<String, Object> row : recordMapper.selectQuestionStats(query)) {
            long answerCount = number(row.get("answerCount"));
            long correctCount = number(row.get("correctCount"));
            long timeoutCount = number(row.get("timeoutCount"));
            long avgCostMs = Math.round(decimal(row.get("avgCostMs")).doubleValue());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("serialNo", serial++);
            item.put("questionId", row.get("questionId"));
            item.put("questionType", row.get("questionType"));
            item.put("questionTypeName", ZyfxqConfigService.questionTypeName(
                    row.get("questionType") == null ? null : String.valueOf(row.get("questionType"))));
            item.put("title", row.get("questionTitle"));
            item.put("answerCount", answerCount);
            item.put("correctCount", correctCount);
            item.put("wrongCount", Math.max(0L, answerCount - correctCount));
            item.put("correctRate", percent(correctCount, answerCount));
            item.put("timeoutCount", timeoutCount);
            item.put("timeoutRate", percent(timeoutCount, answerCount));
            item.put("avgCostMs", avgCostMs);
            item.put("avgCostSeconds", seconds(avgCostMs));
            rows.add(item);
        }
        return rows;
    }

    /** 高频错题：按答错次数（含超时）降序取前 N 题，供页面与 PDF 的“高频错题统计”。 */
    public List<Map<String, Object>> wrongQuestions(List<Map<String, Object>> questionStats, int limit) {
        List<Map<String, Object>> ranked = new ArrayList<>();
        for (Map<String, Object> row : questionStats) {
            if (number(row.get("wrongCount")) > 0) {
                ranked.add(row);
            }
        }
        // 先按答错次数降序，错题次数相同时按作答次数降序，保证同一份数据每次排序结果一致。
        ranked.sort((left, right) -> {
            int compare = Long.compare(number(right.get("wrongCount")), number(left.get("wrongCount")));
            if (compare != 0) {
                return compare;
            }
            return Long.compare(number(right.get("answerCount")), number(left.get("answerCount")));
        });
        if (ranked.size() > limit) {
            return new ArrayList<>(ranked.subList(0, limit));
        }
        return ranked;
    }

    /** 学校端简要分析文本，用于统计页和 PDF 第二页。 */
    public String analysis(Map<String, Object> summary, List<Map<String, Object>> typeStats,
                           List<Map<String, Object>> wrongQuestions) {
        long finished = number(summary.get("finishedCount"));
        if (finished == 0) {
            return "当前活动尚无学生完成对局。学生掷骰前进并在 3 分钟内抵达终点后，"
                    + "这里将展示通过率、平均耗时、各知识点正确率与高频错题。";
        }
        StringBuilder text = new StringBuilder();
        text.append("本次共 ").append(number(summary.get("participantCount"))).append(" 人扫码参与，")
                .append(finished).append(" 人对局结束，其中 ").append(number(summary.get("winCount")))
                .append(" 人通关，通关率 ").append(summary.get("passRate")).append("%；")
                .append("平均单局耗时 ").append(summary.get("avgDurationSeconds")).append(" 秒，")
                .append("累计掷骰 ").append(number(summary.get("rollTotal"))).append(" 次。");
        text.append("整体答题正确率 ").append(summary.get("correctRate")).append("%，超时答题占比 ")
                .append(summary.get("timeoutRate")).append("%。");
        for (Map<String, Object> row : typeStats) {
            text.append("「").append(row.get("questionTypeName")).append("」正确率 ")
                    .append(row.get("correctRate")).append("%（作答 ")
                    .append(number(row.get("answerCount"))).append(" 次）；");
        }
        if (!wrongQuestions.isEmpty()) {
            Map<String, Object> worst = wrongQuestions.get(0);
            text.append("错得最多的是“").append(worst.get("title")).append("”，答错 ")
                    .append(number(worst.get("wrongCount"))).append(" 次，正确率仅 ")
                    .append(worst.get("correctRate")).append("%。");
            text.append("建议针对该知识点安排现场讲解或复习，再让学生重新挑战。");
        } else {
            text.append("当前各题正确率正常，未出现集中错题。");
        }
        return text.toString();
    }

    /** 活动查看页汇总数据：结果输出-学校端两页内容 + 最近参与记录。 */
    public Map<String, Object> gameViewStats(Long gameId) {
        ZyfxqRecordQuery query = new ZyfxqRecordQuery();
        query.setGameId(gameId);
        Map<String, Object> summary = summary(query);
        List<Map<String, Object>> typeStats = questionTypeStats(query);
        List<Map<String, Object>> questions = questionStats(query);
        List<Map<String, Object>> wrong = wrongQuestions(questions, 5);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("questionTypeStats", typeStats);
        data.put("questionStats", questions);
        data.put("wrongQuestions", wrong);
        data.put("analysis", analysis(summary, typeStats, wrong));
        return data;
    }

    /** 毫秒转秒，保留一位小数，用于“平均单局耗时”“每题平均作答用时”。 */
    static BigDecimal seconds(long millis) {
        return BigDecimal.valueOf(millis / 1000D).setScale(1, RoundingMode.HALF_UP);
    }

    /** 百分比统一保留两位小数；分母为 0 时返回 0.00。 */
    static BigDecimal percent(long value, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value * 100D / total).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static long number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? 0L : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
