package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.gkzh.jlcz.domain.dto.JlczRecordQuery;
import com.gkzh.jlcz.mapper.JlczRecordMapper;
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
 * 简历称重 Mapper XML 解析回归。
 * 动态 SQL 中的日期比较必须使用 XML 实体写法，砝码统计必须限定当前称重次数。
 */
public class JlczRecordMapperParseTest {

    private static final String NAMESPACE = JlczRecordMapper.class.getName();
    private static final String RESOURCE = "mapper/jlcz/JlczRecordMapper.xml";

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
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectWeightStats"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectGameViewRecords"));
    }

    @Test
    public void dateConditionsUseXmlSafeComparison() throws Exception {
        MybatisConfiguration configuration = configuration();
        JlczRecordQuery query = new JlczRecordQuery();
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
    public void weightStatsJoinCurrentAttemptOnly() throws Exception {
        MybatisConfiguration configuration = configuration();
        JlczRecordQuery query = new JlczRecordQuery();
        query.setInstanceId(21L);
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectWeightStats")
                .getBoundSql(params).getSql();
        assertTrue(sql.contains("JOIN gkzh_jlcz_record r"), sql);
        assertTrue(sql.contains("c.attempt_no = r.attempt_no"), sql);
        assertTrue(sql.contains("COUNT(DISTINCT c.record_id)"), sql);
    }

    @Test
    public void resultAndStatusFiltersAreOptional() throws Exception {
        MybatisConfiguration configuration = configuration();
        JlczRecordQuery query = new JlczRecordQuery();
        query.setResultValue("pass");
        query.setStatus("finished");
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectAdminSummary")
                .getBoundSql(params).getSql();
        assertTrue(sql.contains("r.result = 'pass'"), sql);
        assertTrue(sql.contains("r.status = 'finished'"), sql);
        assertTrue(sql.contains("r.total_weight"), sql);
    }
}
