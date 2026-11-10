package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.gkzh.zdyp.domain.dto.ZdypRecordQuery;
import com.gkzh.zdyp.mapper.ZdypRecordMapper;
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
 * 职得一拼 Mapper XML 解析回归。
 * 动态 SQL 中的日期比较必须使用 XML 实体写法，筛选条件必须可选，院系统计必须回退父部门，
 * 维度错误统计必须只取首次尝试。
 */
public class ZdypRecordMapperParseTest {

    private static final String NAMESPACE = ZdypRecordMapper.class.getName();
    private static final String RESOURCE = "mapper/zdyp/ZdypRecordMapper.xml";

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
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectBehaviorStats"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectFirstAttemptSlotStats"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectFirstSlotStats"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectCareerStats"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectDepartmentSummary"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectGameViewRecords"));
    }

    @Test
    public void dateConditionsUseXmlSafeComparison() throws Exception {
        MybatisConfiguration configuration = configuration();
        ZdypRecordQuery query = new ZdypRecordQuery();
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
    public void filtersAreOptional() throws Exception {
        MybatisConfiguration configuration = configuration();
        ZdypRecordQuery query = new ZdypRecordQuery();
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectAdminRecords")
                .getBoundSql(params).getSql();
        assertFalse(sql.contains("r.instance_id = ?"), sql);
        assertFalse(sql.contains("r.school_id = ?"), sql);
        assertFalse(sql.contains("r.career_code = ?"), sql);
        assertFalse(sql.contains("r.behavior_type = ?"), sql);
        assertTrue(sql.contains("r.game_type = 'zdyp'"), sql);
    }

    @Test
    public void careerBehaviorAndStatusFiltersApply() throws Exception {
        MybatisConfiguration configuration = configuration();
        ZdypRecordQuery query = new ZdypRecordQuery();
        query.setCareerCode("big_data_engineer");
        query.setBehaviorType("advantage");
        query.setStatus("finished");
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectAdminRecords")
                .getBoundSql(params).getSql();
        assertTrue(sql.contains("r.career_code = ?"), sql);
        assertTrue(sql.contains("r.behavior_type = ?"), sql);
        assertTrue(sql.contains("r.status = 'finished'"), sql);
    }

    @Test
    public void slotStatsOnlyCountFirstAttempt() throws Exception {
        MybatisConfiguration configuration = configuration();
        Map<String, Object> params = new HashMap<>();
        params.put("q", new ZdypRecordQuery());

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectFirstAttemptSlotStats")
                .getBoundSql(params).getSql();
        assertTrue(sql.contains("t.attempt_no = 1"), sql);
        assertTrue(sql.contains("GROUP BY a.slot_code"), sql);

        String firstSlot = configuration.getMappedStatement(NAMESPACE + ".selectFirstSlotStats")
                .getBoundSql(params).getSql();
        assertTrue(firstSlot.contains("t.attempt_no = 1"), firstSlot);
        assertTrue(firstSlot.contains("t.first_slot_code"), firstSlot);
    }

    @Test
    public void departmentStatsFallBackToParentDepartment() throws Exception {
        MybatisConfiguration configuration = configuration();
        Map<String, Object> params = new HashMap<>();
        params.put("q", new ZdypRecordQuery());

        String summary = configuration.getMappedStatement(NAMESPACE + ".selectDepartmentSummary")
                .getBoundSql(params).getSql();
        assertTrue(summary.contains("LEFT JOIN gkzh_school_department p ON p.department_id = d.parent_id"), summary);
        assertTrue(summary.contains("未填写院系"), summary);
        // gkzh_school_department 的名称字段是 title；写成 name 会在运行时报 Unknown column。
        assertTrue(summary.contains("COALESCE(p.title"), summary);
        assertFalse(summary.contains("p.name"), summary);
    }

    @Test
    public void behaviorStatsOnlyCountFinishedRecords() throws Exception {
        MybatisConfiguration configuration = configuration();
        Map<String, Object> params = new HashMap<>();
        params.put("q", new ZdypRecordQuery());

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectBehaviorStats")
                .getBoundSql(params).getSql();
        assertTrue(sql.contains("r.status = 'finished'"), sql);
        assertTrue(sql.contains("GROUP BY r.behavior_type"), sql);
    }
}
