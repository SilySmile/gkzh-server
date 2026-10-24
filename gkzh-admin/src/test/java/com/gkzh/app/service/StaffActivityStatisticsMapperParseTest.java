package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.gkzh.activity.mapper.staff.GkzhStaffActivityStatisticsMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作人员统计 Mapper 注解 SQL 解析回归。
 * <p>
 * 注解 SQL 会被 MyBatis 当成 XML 解析，不能出现裸 `<`；也不要把 `<` 写成 `&lt;`，
 * 注解文本不会像 XML 文件那样被还原成 `<`，最终会把 `&lt;` 原样传给 MySQL。
 * 日期上界统一用等价的 `#{endTime} > scan_time`。
 */
public class StaffActivityStatisticsMapperParseTest {

    private static final String NAMESPACE = GkzhStaffActivityStatisticsMapper.class.getName();

    private MybatisConfiguration configuration() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.addMapper(GkzhStaffActivityStatisticsMapper.class);
        return configuration;
    }

    @Test
    public void annotationSqlParsesAtStartup() {
        MybatisConfiguration configuration = configuration();
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectActivityGames"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectDimensionRows"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectStudentRows"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectCollegeOptions"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectMajorOptions"));
    }

    @Test
    public void activityGamesSqlHasNoXmlEntity() {
        MybatisConfiguration configuration = configuration();
        Map<String, Object> params = new HashMap<>();
        params.put("instanceId", 21L);
        params.put("schoolId", 5L);
        params.put("startTime", LocalDateTime.of(1970, 1, 1, 0, 0));
        params.put("endTime", LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectActivityGames")
                .getBoundSql(params).getSql();
        assertFalse(sql.contains("&lt;"), sql);
        assertTrue(sql.contains("? > scan_time"), sql);
    }

    @Test
    public void studentRowsSqlHasNoXmlEntity() {
        MybatisConfiguration configuration = configuration();
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", 18L);
        params.put("schoolId", 5L);
        params.put("collegeId", null);
        params.put("majorId", null);
        params.put("gender", null);
        params.put("startTime", LocalDateTime.of(1970, 1, 1, 0, 0));
        params.put("endTime", LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectStudentRows")
                .getBoundSql(params).getSql();
        assertFalse(sql.contains("&lt;"), sql);
        assertTrue(sql.contains("? > scan_time"), sql);
    }
}
