package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.gkzh.nlld.domain.dto.NlldRecordQuery;
import com.gkzh.nlld.mapper.NlldRecordMapper;
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
 * 能量雷达 Mapper XML 解析回归。
 * 动态 SQL 中的日期比较必须使用 XML 实体写法，解析后 SQL 不能残留 {@code &lt;}。
 */
public class NlldRecordMapperParseTest {

    private static final String NAMESPACE = NlldRecordMapper.class.getName();
    private static final String RESOURCE = "mapper/nlld/NlldRecordMapper.xml";

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
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectDimensionSelectionCounts"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectDimensionTwoPointCounts"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectTop1Counts"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectTop2Counts"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectAnswerDetails"));
        assertNotNull(configuration.getMappedStatement(NAMESPACE + ".selectGameViewRecords"));
    }

    @Test
    public void dateConditionsUseXmlSafeComparison() throws Exception {
        MybatisConfiguration configuration = configuration();
        NlldRecordQuery query = new NlldRecordQuery();
        query.setInstanceId(21L);
        query.setSchoolId(5L);
        query.setStartDate(LocalDate.of(2026, 10, 1));
        query.setEndDate(LocalDate.of(2026, 10, 31));
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectAdminRecords")
                .getBoundSql(params).getSql();
        assertFalse(sql.contains("&lt;"), sql);
        assertTrue(sql.contains("r.scan_time >= ?"), sql);
        assertTrue(sql.contains("r.scan_time < ?"), sql);
    }

    @Test
    public void dimensionAndResultFiltersAreOptional() throws Exception {
        MybatisConfiguration configuration = configuration();
        NlldRecordQuery query = new NlldRecordQuery();
        query.setDimensionCode("security");
        query.setResultType("top2");
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);

        String sql = configuration.getMappedStatement(NAMESPACE + ".selectAdminSummary")
                .getBoundSql(params).getSql();
        assertTrue(sql.contains("r.result_type = ?"), sql);
        assertTrue(sql.contains("r.top1_dimension_id"), sql);
        assertTrue(sql.contains("gkzh_nlld_dimension"), sql);
    }
}
