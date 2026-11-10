package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.gkzh.zytj.domain.dto.ZytjRecordQuery;
import com.gkzh.zytj.mapper.ZytjRecordMapper;
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
 * 职愿探究 Mapper XML 解析回归。
 * 动态 SQL 中的日期比较必须使用 XML 实体写法，人格类型与状态条件必须可选，
 * 院系统计必须回退父部门且名称字段取 title，维度分布必须按四列位置拆分 item_codes。
 */
public class ZytjRecordMapperParseTest {

    private static final String NAMESPACE = ZytjRecordMapper.class.getName();
    private static final String RESOURCE = "mapper/zytj/ZytjRecordMapper.xml";

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
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectTypeStats"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectDimensionStats"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectDepartmentSummary"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectDepartmentLetterCounts"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectGameViewRecords"));
    }

    @Test
    public void dateConditionsUseXmlSafeComparison() throws Exception {
        MybatisConfiguration configuration = configuration();
        ZytjRecordQuery query = new ZytjRecordQuery();
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
    public void typeAndStatusFiltersAreOptional() throws Exception {
        MybatisConfiguration configuration = configuration();
        ZytjRecordQuery query = new ZytjRecordQuery();
        query.setTypeCode("ENFP");
        query.setStatus("finished");
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectAdminRecords")
                .getBoundSql(params).getSql();
        assertTrue(sql.contains("r.type_code = ?"), sql);
        assertTrue(sql.contains("r.status = 'finished'"), sql);
        assertFalse(sql.contains("r.instance_id = ?"), sql);
        assertFalse(sql.contains("r.school_id = ?"), sql);
    }

    @Test
    public void typeStatsOnlyCountFinishedRecords() throws Exception {
        MybatisConfiguration configuration = configuration();
        ZytjRecordQuery query = new ZytjRecordQuery();
        query.setInstanceId(21L);
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectTypeStats")
                .getBoundSql(params).getSql();
        assertTrue(sql.contains("r.status = 'finished'"), sql);
        assertTrue(sql.contains("GROUP BY r.type_code"), sql);
    }

    @Test
    public void dimensionStatsSplitItemCodesByColumnPosition() throws Exception {
        MybatisConfiguration configuration = configuration();
        Map<String, Object> params = new HashMap<>();
        params.put("q", new ZytjRecordQuery());

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectDimensionStats")
                .getBoundSql(params).getSql();
        // 四列分别取第 1/2/3/4 段，最后一列直接取最后一段。
        assertTrue(sql.contains("SUBSTRING_INDEX(r.item_codes, ',', 1)"), sql);
        assertTrue(sql.contains("SUBSTRING_INDEX(SUBSTRING_INDEX(r.item_codes, ',', 2), ',', -1)"), sql);
        assertTrue(sql.contains("SUBSTRING_INDEX(SUBSTRING_INDEX(r.item_codes, ',', 3), ',', -1)"), sql);
        assertTrue(sql.contains("SUBSTRING_INDEX(r.item_codes, ',', -1)"), sql);
        assertTrue(sql.contains("UNION ALL"), sql);
    }

    @Test
    public void departmentStatsFallBackToParentDepartment() throws Exception {
        MybatisConfiguration configuration = configuration();
        Map<String, Object> params = new HashMap<>();
        params.put("q", new ZytjRecordQuery());

        String summary = configuration.getMappedStatement(NAMESPACE + ".selectDepartmentSummary")
                .getBoundSql(params).getSql();
        assertTrue(summary.contains("LEFT JOIN gkzh_school_department p ON p.department_id = d.parent_id"), summary);
        assertTrue(summary.contains("未填写院系"), summary);
        // gkzh_school_department 的名称字段是 title；写成 name 会在运行时报 Unknown column。
        assertTrue(summary.contains("COALESCE(p.title"), summary);
        assertFalse(summary.contains("p.name"), summary);

        String letters = configuration.getMappedStatement(NAMESPACE + ".selectDepartmentLetterCounts")
                .getBoundSql(params).getSql();
        assertTrue(letters.contains("COALESCE(p.title"), letters);
        assertFalse(letters.contains("p.name"), letters);
    }
}
