package com.gkzh.zyjdc.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.zyjdc.domain.ZyjdcRole;
import com.gkzh.zyjdc.domain.dto.ZyjdcRecordQuery;
import com.gkzh.zyjdc.mapper.ZyjdcRecordMapper;
import com.gkzh.zyjdc.mapper.ZyjdcRoleMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职业角斗场统计分析服务。
 * 后台统计、活动查看页和 Excel 导出共用同一套人数、完成率和角色分布口径。
 */
@Service
public class ZyjdcStatisticsService {

    private final ZyjdcRecordMapper recordMapper;
    private final ZyjdcRoleMapper roleMapper;

    public ZyjdcStatisticsService(ZyjdcRecordMapper recordMapper, ZyjdcRoleMapper roleMapper) {
        this.recordMapper = recordMapper;
        this.roleMapper = roleMapper;
    }

    /** 汇总参与人数、选择人数、完成人数、胜负分布和平均尝试次数。 */
    public Map<String, Object> summary(ZyjdcRecordQuery query) {
        Map<String, Object> raw = recordMapper.selectAdminSummary(query);
        Map<String, Object> data = new LinkedHashMap<>();
        long total = number(raw == null ? null : raw.get("total"));
        long selected = number(raw == null ? null : raw.get("selectedCount"));
        long finished = number(raw == null ? null : raw.get("finished"));
        long win = number(raw == null ? null : raw.get("winCount"));
        long lose = number(raw == null ? null : raw.get("loseCount"));
        long draw = number(raw == null ? null : raw.get("drawCount"));
        data.put("enteredCount", total);
        data.put("selectedCount", selected);
        data.put("completedCount", finished);
        data.put("winCount", win);
        data.put("loseCount", lose);
        data.put("drawCount", draw);
        data.put("averageAttempts", raw == null || raw.get("avgAttempts") == null ? BigDecimal.ZERO : raw.get("avgAttempts"));
        data.put("completionRate", percent(finished, total));
        data.put("selectedCompletionRate", percent(finished, selected));
        return data;
    }

    /** 8 个固定角色的选择人数、通关人数、偏好占比和完成率。 */
    public List<Map<String, Object>> roleStats(ZyjdcRecordQuery query) {
        List<ZyjdcRole> roles = roleMapper.selectList(new QueryWrapper<ZyjdcRole>()
                .orderByAsc("sort_order").orderByAsc("role_id"));
        Map<Long, Map<String, Object>> countMap = new LinkedHashMap<>();
        for (Map<String, Object> row : recordMapper.selectRoleCounts(query)) {
            Long roleId = longValue(row.get("roleId") == null ? row.get("role_id") : row.get("roleId"));
            if (roleId != null) {
                countMap.put(roleId, row);
            }
        }
        long selectedTotal = 0L;
        for (Map<String, Object> row : countMap.values()) {
            selectedTotal += number(row.get("userCount") == null ? row.get("user_count") : row.get("userCount"));
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        int serial = 1;
        for (ZyjdcRole role : roles) {
            Map<String, Object> count = countMap.get(role.getRoleId());
            long userCount = count == null ? 0L : number(count.get("userCount") == null ? count.get("user_count") : count.get("userCount"));
            long finishedCount = count == null ? 0L : number(count.get("finishedCount") == null ? count.get("finished_count") : count.get("finishedCount"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("serialNo", serial++);
            row.put("roleId", role.getRoleId());
            row.put("roleCode", role.getRoleCode());
            row.put("roleName", role.getRoleName());
            row.put("jobTitle", role.getJobTitle());
            row.put("imageUrl", role.getImageUrl());
            row.put("rankLevel", role.getRankLevel());
            row.put("userCount", userCount);
            row.put("finishedCount", finishedCount);
            row.put("probability", percent(userCount, selectedTotal));
            row.put("completionRate", percent(finishedCount, userCount));
            rows.add(row);
        }
        return rows;
    }

    /** 胜负平三类结果分布；未结束的记录不计入。 */
    public List<Map<String, Object>> resultStats(ZyjdcRecordQuery query) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("win", 0L);
        counts.put("lose", 0L);
        counts.put("draw", 0L);
        for (Map<String, Object> row : recordMapper.selectResultCounts(query)) {
            String result = row.get("result") == null ? null : String.valueOf(row.get("result"));
            if (result != null && counts.containsKey(result)) {
                counts.put(result, number(row.get("userCount") == null ? row.get("user_count") : row.get("userCount")));
            }
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        List<Map<String, Object>> rows = new ArrayList<>();
        int serial = 1;
        String[][] definitions = {{"win", "胜利"}, {"lose", "失败"}, {"draw", "平局"}};
        for (String[] definition : definitions) {
            long count = counts.getOrDefault(definition[0], 0L);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("serialNo", serial++);
            row.put("result", definition[0]);
            row.put("resultName", definition[1]);
            row.put("userCount", count);
            row.put("probability", percent(count, total));
            rows.add(row);
        }
        return rows;
    }

    /** 生成学校端第三页的整体偏好分析文本。 */
    public String analysis(Map<String, Object> summary, List<Map<String, Object>> roleStats,
                           List<Map<String, Object>> resultStats) {
        long selected = number(summary.get("selectedCount"));
        if (selected == 0) {
            return "当前游戏尚无学生选择角色。学生完成角色选择后，这里将展示动物角色偏好、对局结果和完成情况分析。";
        }
        Map<String, Object> top = null;
        Map<String, Object> second = null;
        for (Map<String, Object> row : roleStats) {
            long count = number(row.get("userCount"));
            if (count <= 0) {
                continue;
            }
            if (top == null || count > number(top.get("userCount"))) {
                second = top;
                top = row;
            } else if (second == null || count > number(second.get("userCount"))) {
                second = row;
            }
        }
        StringBuilder text = new StringBuilder();
        text.append("本次共 ").append(number(summary.get("enteredCount"))).append(" 人扫码参与，")
                .append(selected).append(" 人完成角色选择，")
                .append(number(summary.get("completedCount"))).append(" 人获得通关印戳，完成率为 ")
                .append(summary.get("completionRate")).append("%。");
        if (top != null) {
            text.append("选择最多的是“").append(top.get("roleName")).append("（").append(top.get("jobTitle"))
                    .append("）”，共 ").append(number(top.get("userCount"))).append(" 人，占比 ")
                    .append(top.get("probability")).append("%；");
        }
        if (second != null) {
            text.append("其次是“").append(second.get("roleName")).append("（").append(second.get("jobTitle"))
                    .append("）”，占比 ").append(second.get("probability")).append("%。");
        }
        long win = 0L;
        long lose = 0L;
        long draw = 0L;
        for (Map<String, Object> row : resultStats) {
            String result = String.valueOf(row.get("result"));
            long count = number(row.get("userCount"));
            if ("win".equals(result)) {
                win = count;
            } else if ("lose".equals(result)) {
                lose = count;
            } else if ("draw".equals(result)) {
                draw = count;
            }
        }
        text.append("对局结果方面，胜利 ").append(win).append(" 人、失败 ").append(lose)
                .append(" 人、平局 ").append(draw).append(" 人，平均每局尝试 ")
                .append(summary.get("averageAttempts")).append(" 次。");
        if (top != null && "mouse".equals(String.valueOf(top.get("roleCode")))) {
            text.append("选择老鼠的学生最多，说明不少参与者更认同以巧劲和观察力破局的成长路径；建议在活动复盘中重点讨论“弱势位置如何创造价值”。");
        } else if (top != null && ("elephant".equals(String.valueOf(top.get("roleCode")))
                || "lion".equals(String.valueOf(top.get("roleCode"))))) {
            text.append("高位角色更受青睐，说明参与者整体更向往承担决策和资源整合责任；建议同时引导学生理解高位角色背后的压力与担当。");
        } else {
            text.append("角色偏好分布可以作为生涯讨论的切入口，建议结合院系、专业和性别差异继续观察，帮助学生把角色偏好转化为具体的职业探索行动。");
        }
        return text.toString();
    }

    /** 活动查看页汇总数据；记录明细由后台补全学生与学校字段后另行返回。 */
    public Map<String, Object> gameViewStats(Long gameId) {
        ZyjdcRecordQuery query = new ZyjdcRecordQuery();
        query.setGameId(gameId);
        Map<String, Object> summary = summary(query);
        List<Map<String, Object>> roles = roleStats(query);
        List<Map<String, Object>> results = resultStats(query);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("totalParticipants", summary.get("enteredCount"));
        data.put("completedCount", summary.get("completedCount"));
        data.put("completionRate", summary.get("completionRate"));
        data.put("averageAttempts", summary.get("averageAttempts"));
        data.put("roleStats", roles);
        data.put("resultStats", results);
        data.put("analysis", analysis(summary, roles, results));
        return data;
    }

    /** 百分比统一保留两位小数；分母为 0 时返回 0.00。 */
    private static BigDecimal percent(long value, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value * 100D / total).setScale(2, RoundingMode.HALF_UP);
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

    private static Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
