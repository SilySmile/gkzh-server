package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.gkzh.activity.mapper.staff.GkzhStaffActivityStatisticsMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 工作人员统计 Mapper 注解 SQL 解析回归：注解中的 `<` 必须转义为 `&lt;`，
 * 否则 MyBatis 在启动时会因 XML 解析失败而无法创建 Mapper。
 */
public class StaffActivityStatisticsMapperParseTest {

    @Test
    public void annotationSqlParsesAtStartup() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.addMapper(GkzhStaffActivityStatisticsMapper.class);

        String namespace = GkzhStaffActivityStatisticsMapper.class.getName();
        assertNotNull(configuration.getMappedStatement(namespace + ".selectActivityGames"));
        assertNotNull(configuration.getMappedStatement(namespace + ".selectDimensionRows"));
        assertNotNull(configuration.getMappedStatement(namespace + ".selectStudentRows"));
        assertNotNull(configuration.getMappedStatement(namespace + ".selectCollegeOptions"));
        assertNotNull(configuration.getMappedStatement(namespace + ".selectMajorOptions"));
    }
}
