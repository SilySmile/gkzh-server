package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.gkzh.zmwl.domain.dto.ZmwlRecordQuery;
import com.gkzh.zmwl.mapper.ZmwlRecordMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 职面未来 Mapper XML 解析回归。
 * 动态 SQL 中的日期比较必须使用 XML 实体写法，方向与状态条件必须可选，院系统计必须回退父部门。
 */
public class ZmwlRecordMapperParseTest {

    private static final String NAMESPACE = ZmwlRecordMapper.class.getName();
    private static final String RESOURCE = "mapper/zmwl/ZmwlRecordMapper.xml";

    private MybatisConfiguration configuration() throws Exception {
        MybatisConfiguration configuration = new MybatisConfiguration();
        try (InputStream input = Resources.getResourceAsStream(RESOURCE)) {
            XMLMapperBuilder builder = new XMLMapperBuilder(input, configuration, RESOURCE,
                    configuration.getSqlFragments());
            builder.parse();
        }
        return configuration;
    }

    @Test
    public void xmlMapperParsesAtStartup() throws Exception {
        MybatisConfiguration configuration = configuration();
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectAdminRecords"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectAdminSummary"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectDirectionStats"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectDepartmentSummary"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectDepartmentDirectionCounts"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectGameViewRecords"));
    }

    @Test
    public void dateConditionsUseXmlSafeComparison() throws Exception {
        MybatisConfiguration configuration = configuration();
        ZmwlRecordQuery query = new ZmwlRecordQuery();
        query.setInstanceId(21L);
        query.setSchoolId(5L);
        query.setStartDate(LocalDate.of(2026, 11, 1));
        query.setEndDate(LocalDate.of(2026, 11, 30));
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectAdminRecords")
                .getBoundSql(params).getSql();
        assertFalse(sql.contains("&lt;"), sql);
        assertTrue(sql.contains("r.scan_time >= ?"), sql);
        assertTrue(sql.contains("r.scan_time < ?"), sql);
    }

    @Test
    public void statusAndDirectionFiltersAreOptional() throws Exception {
        MybatisConfiguration configuration = configuration();
        ZmwlRecordQuery query = new ZmwlRecordQuery();
        query.setDirectionCode("undecided");
        query.setStatus("finished");
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectAdminRecords")
                .getBoundSql(params).getSql();
        assertTrue(sql.contains("r.direction_code = ?"), sql);
        assertTrue(sql.contains("r.status = 'finished'"), sql);
        assertFalse(sql.contains("r.instance_id = ?"), sql);
        assertFalse(sql.contains("r.school_id = ?"), sql);
    }

    @Test
    public void directionStatsOnlyCountFinishedRecords() throws Exception {
        MybatisConfiguration configuration = configuration();
        ZmwlRecordQuery query = new ZmwlRecordQuery();
        query.setInstanceId(21L);
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectDirectionStats")
                .getBoundSql(params).getSql();
        assertTrue(sql.contains("r.status = 'finished'"), sql);
        assertTrue(sql.contains("GROUP BY r.direction_code"), sql);
    }

    @Test
    public void departmentStatsFallBackToParentDepartment() throws Exception {
        MybatisConfiguration configuration = configuration();
        ZmwlRecordQuery query = new ZmwlRecordQuery();
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String summary = configuration.getMappedStatement(NAMESPACE + ".selectDepartmentSummary")
                .getBoundSql(params).getSql();
        assertTrue(summary.contains("LEFT JOIN gkzh_school_department p ON p.department_id = d.parent_id"), summary);
        assertTrue(summary.contains("未填写院系"), summary);
    }
}
