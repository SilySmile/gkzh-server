package com.gkzh.app.service;

import com.gkzh.zytj.domain.ZytjItem;
import com.gkzh.zytj.domain.ZytjType;
import com.gkzh.zytj.domain.dto.ZytjRecordQuery;
import com.gkzh.zytj.mapper.ZytjRecordMapper;
import com.gkzh.zytj.service.ZytjConfigService;
import com.gkzh.zytj.service.ZytjStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 职愿探究统计口径回归：
 * 16 型分布必须以全部配置型别为行并补 0；维度倾向固定四组八个字母并补 0；
 * 分母为 0 时占比返回 0，不能出现 NaN。
 */
public class ZytjStatisticsServiceTest {

    private ZytjRecordMapper recordMapper;
    private ZytjConfigService configService;
    private ZytjStatisticsService service;

    @BeforeEach
    public void setUp() {
        recordMapper = mock(ZytjRecordMapper.class);
        configService = mock(ZytjConfigService.class);
        service = new ZytjStatisticsService(recordMapper, configService);
        when(configService.listTypes()).thenReturn(types());
        when(configService.listItems()).thenReturn(items());
    }

    @Test
    public void typeStatsFillAllConfiguredTypesWithZero() {
        when(recordMapper.selectTypeStats(any())).thenReturn(Collections.emptyList());

        List<Map<String, Object>> rows = service.typeStats(new ZytjRecordQuery(), 0L);

        assertEquals(16, rows.size());
        for (Map<String, Object> row : rows) {
            assertEquals(0L, ((Number) row.get("selectedCount")).longValue());
            assertEquals(new BigDecimal("0.0"), row.get("rate"));
        }
    }

    @Test
    public void typeStatsAppendConfiguredMissingType() {
        Map<String, Object> stat = new LinkedHashMap<>();
        stat.put("typeCode", "ENFP");
        stat.put("feature", "乐观");
        stat.put("careerField", "广告创意");
        stat.put("typicalCareers", "广告策划师");
        stat.put("selectedCount", 3L);
        when(recordMapper.selectTypeStats(any())).thenReturn(Collections.singletonList(stat));

        List<Map<String, Object>> rows = service.typeStats(new ZytjRecordQuery(), 3L);

        Map<String, Object> enfp = rows.stream()
                .filter(row -> "ENFP".equals(row.get("typeCode")))
                .findFirst().orElse(null);
        assertEquals(3L, ((Number) enfp.get("selectedCount")).longValue());
        assertEquals(new BigDecimal("100.0"), enfp.get("rate"));
    }

    @Test
    public void dimensionStatsAlwaysReturnFourGroups() {
        when(recordMapper.selectDimensionStats(any())).thenReturn(Collections.emptyList());

        List<Map<String, Object>> rows = service.dimensionStats(new ZytjRecordQuery(), 0L);

        assertEquals(8, rows.size());
        List<String> letters = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            letters.add(String.valueOf(row.get("letter")));
            assertEquals(0L, ((Number) row.get("selectedCount")).longValue());
        }
        assertEquals(java.util.Arrays.asList("E", "I", "S", "N", "T", "F", "J", "P"), letters);
    }

    @Test
    public void dimensionStatsMapItemNamesAndRates() {
        Map<String, Object> stat = new LinkedHashMap<>();
        stat.put("dimensionCode", "EI");
        stat.put("letter", "E");
        stat.put("selectedCount", 4L);
        when(recordMapper.selectDimensionStats(any())).thenReturn(Collections.singletonList(stat));

        List<Map<String, Object>> rows = service.dimensionStats(new ZytjRecordQuery(), 8L);

        Map<String, Object> e = rows.stream()
                .filter(row -> "E".equals(row.get("letter")))
                .findFirst().orElse(null);
        assertEquals("派对气球", e.get("itemName"));
        assertEquals(new BigDecimal("50.0"), e.get("rate"));
    }

    @Test
    public void summaryUsesZeroRatesWhenNoParticipants() {
        when(recordMapper.selectAdminSummary(any())).thenReturn(new LinkedHashMap<>());
        when(recordMapper.selectTypeStats(any())).thenReturn(Collections.emptyList());

        Map<String, Object> summary = service.summary(new ZytjRecordQuery());

        assertEquals(0L, ((Number) summary.get("participantCount")).longValue());
        assertEquals(new BigDecimal("0.0"), summary.get("finishRate"));
        assertEquals(new BigDecimal("0.0"), summary.get("timeoutRate"));
        assertTrue(summary.containsKey("topTypeCode"));
    }

    private static List<ZytjType> types() {
        List<ZytjType> list = new ArrayList<>();
        String[] codes = {"ISTJ", "ISFJ", "INFJ", "INTJ", "ISTP", "ISFP", "INFP", "INTP",
                "ESTP", "ESFP", "ENFP", "ENTP", "ESTJ", "ESFJ", "ENFJ", "ENTJ"};
        for (int i = 0; i < codes.length; i++) {
            ZytjType type = new ZytjType();
            type.setTypeId((long) (i + 1));
            type.setTypeCode(codes[i]);
            type.setFeature("特点" + i);
            type.setCareerField("领域" + i);
            type.setTypicalCareers("职业" + i);
            type.setSortOrder(i + 1);
            type.setStatus("0");
            list.add(type);
        }
        return list;
    }

    private static List<ZytjItem> items() {
        List<ZytjItem> list = new ArrayList<>();
        list.add(item("E", "派对气球", 1));
        list.add(item("I", "单人帐篷", 1));
        list.add(item("S", "地质锤", 2));
        list.add(item("N", "水晶球", 2));
        list.add(item("T", "逻辑拼图", 3));
        list.add(item("F", "手工编织围巾", 3));
        list.add(item("J", "台历", 4));
        list.add(item("P", "沙滩躺椅", 4));
        return list;
    }

    private static ZytjItem item(String code, String name, int column) {
        ZytjItem item = new ZytjItem();
        item.setItemCode(code);
        item.setItemName(name);
        item.setColumnIndex(column);
        item.setRowIndex(1);
        item.setStatus("0");
        return item;
    }
}
