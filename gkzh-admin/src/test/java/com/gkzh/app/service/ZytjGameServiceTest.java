package com.gkzh.app.service;

import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zytj.domain.ZytjItem;
import com.gkzh.zytj.domain.ZytjRecord;
import com.gkzh.zytj.domain.ZytjSetting;
import com.gkzh.zytj.domain.ZytjType;
import com.gkzh.zytj.mapper.ZytjRecordMapper;
import com.gkzh.zytj.service.ZytjConfigService;
import com.gkzh.zytj.service.ZytjGameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 职愿探究学生端业务回归：
 * 四列必须各选一件并按列序拼成四位代码；未选中的列按该列首个启用物品补全并标记超时；
 * 提交后默认只读；允许重新探究时才覆盖并累加次数；跨学校扫码必须被拒绝。
 */
public class ZytjGameServiceTest {

    private ZytjRecordMapper recordMapper;
    private ZytjConfigService configService;
    private IActivityWeekService activityWeekService;
    private ZytjGameService service;
    private ZytjRecord record;
    private ZytjSetting setting;
    private List<ZytjItem> items;
    private ZytjType enfp;

    @BeforeEach
    public void setUp() {
        recordMapper = mock(ZytjRecordMapper.class);
        configService = mock(ZytjConfigService.class);
        activityWeekService = mock(IActivityWeekService.class);
        service = new ZytjGameService(recordMapper, configService, activityWeekService);

        items = new ArrayList<>();
        items.add(item(1L, "E", "派对气球", 1, 1));
        items.add(item(2L, "I", "单人帐篷", 1, 2));
        items.add(item(3L, "N", "水晶球", 2, 1));
        items.add(item(4L, "S", "地质锤", 2, 2));
        items.add(item(5L, "T", "逻辑拼图", 3, 1));
        items.add(item(6L, "F", "手工编织围巾", 3, 2));
        items.add(item(7L, "P", "沙滩躺椅", 4, 1));
        items.add(item(8L, "J", "台历", 4, 2));

        enfp = new ZytjType();
        enfp.setTypeId(11L);
        enfp.setTypeCode("ENFP");
        enfp.setFeature("乐观、富有想象力、善于激励他人");
        enfp.setCareerField("广告创意、培训、人力资源");
        enfp.setTypicalCareers("广告策划师、培训师、招聘主管");
        enfp.setStatus("0");

        setting = new ZytjSetting();
        setting.setSettingId(1);
        setting.setTimeLimit(30);
        setting.setAllowReselect("0");
        setting.setEnabled("1");

        record = new ZytjRecord();
        record.setRecordId(9L);
        record.setSchoolId(5L);
        record.setInstanceId(21L);
        record.setGameId(18L);
        record.setUserId(7L);
        record.setGameType("zytj");
        record.setStatus("participating");
        record.setAttemptNo(1);
        record.setStartTime(new Date(System.currentTimeMillis() - 5000L));

        when(recordMapper.selectOne(any())).thenReturn(record);
        when(configService.getSetting()).thenReturn(setting);
        when(configService.listActiveItems()).thenReturn(items);
        when(configService.listItems()).thenReturn(items);
        when(configService.findType("ENFP")).thenReturn(enfp);
        for (ZytjItem item : items) {
            when(configService.findActiveItem(item.getItemCode())).thenReturn(item);
        }
        for (int column = 1; column <= 4; column++) {
            List<ZytjItem> columnItems = new ArrayList<>();
            for (ZytjItem item : items) {
                if (Integer.valueOf(column).equals(item.getColumnIndex())) {
                    columnItems.add(item);
                }
            }
            when(configService.listActiveItemsByColumn(column)).thenReturn(columnItems);
        }
        when(activityWeekService.getLatestParticipation(anyLong(), anyLong())).thenReturn(null);
    }

    @Test
    public void submitAssemblesTypeCodeInColumnOrder() {
        Map<String, Object> view = service.submit(9L, 7L, Arrays.asList("E", "N", "F", "P"), false);

        assertEquals("finished", view.get("status"));
        Map<?, ?> result = (Map<?, ?>) view.get("result");
        assertEquals("ENFP", result.get("typeCode"));
        assertEquals(Arrays.asList("E", "N", "F", "P"), result.get("letters"));
        assertEquals("广告策划师、培训师、招聘主管", result.get("typicalCareers"));
        assertFalse((Boolean) result.get("isTimeout"));
        verify(recordMapper, times(1)).update(any(), any());
    }

    @Test
    public void missingColumnsAreFilledFromColumnHeadAndMarkedTimeout() {
        // 到点自动提交：只选了第一列，其余三列由服务端按行序首个物品（水晶球/逻辑拼图/沙滩躺椅）补全。
        when(configService.findType("ENTP")).thenReturn(type("ENTP"));

        Map<String, Object> view = service.submit(9L, 7L, Arrays.asList("E", null, null, null), true);

        Map<?, ?> result = (Map<?, ?>) view.get("result");
        assertEquals("ENTP", result.get("typeCode"));
        assertTrue((Boolean) result.get("isTimeout"));
        assertEquals("1", record.getIsTimeout());
        assertEquals("E,N,T,P", record.getItemCodes());
    }

    @Test
    public void itemFromWrongColumnIsReplacedInsteadOfRejected() {
        // 第二列提交了第一列的气球：按该列首个启用物品（水晶球）补全，并标记超时。
        Map<String, Object> view = service.submit(9L, 7L, Arrays.asList("E", "E", "F", "P"), false);

        Map<?, ?> result = (Map<?, ?>) view.get("result");
        assertEquals("ENFP", result.get("typeCode"));
        assertTrue((Boolean) result.get("isTimeout"));
    }

    @Test
    public void unknownTypeIsRejected() {
        when(configService.findType("ENFP")).thenReturn(null);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.submit(9L, 7L, Arrays.asList("E", "N", "F", "P"), false));
        assertEquals("该人格类型尚未配置，请稍后重试", error.getMessage());
        verify(recordMapper, never()).update(any(), any());
    }

    @Test
    public void finishedCardIsReadOnlyByDefault() {
        record.setStatus("finished");
        record.setTypeCode("ENFP");
        when(configService.findType("ISTJ")).thenReturn(type("ISTJ"));

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.submit(9L, 7L, Arrays.asList("I", "S", "T", "J"), false));
        assertEquals("职业探索卡已生成，不可修改", error.getMessage());
        verify(recordMapper, never()).update(any(), any());
    }

    @Test
    public void sameTypeResubmitIsIdempotent() {
        record.setStatus("finished");
        record.setTypeCode("ENFP");

        Map<String, Object> view = service.submit(9L, 7L, Arrays.asList("E", "N", "F", "P"), false);

        assertEquals("finished", view.get("status"));
        verify(recordMapper, never()).update(any(), any());
    }

    @Test
    public void reselectIsAllowedWhenSettingEnablesIt() {
        setting.setAllowReselect("1");
        record.setStatus("finished");
        record.setTypeCode("ENFP");
        record.setAttemptNo(1);
        when(configService.findType("ISTJ")).thenReturn(type("ISTJ"));

        service.submit(9L, 7L, Arrays.asList("I", "S", "T", "J"), false);

        assertEquals(2, record.getAttemptNo());
        assertEquals("ISTJ", record.getTypeCode());
        verify(recordMapper, times(1)).update(any(), any());
    }

    @Test
    public void disabledGameIsRejectedWithReadableMessage() {
        setting.setEnabled("0");

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.submit(9L, 7L, Arrays.asList("E", "N", "F", "P"), false));
        assertEquals("职愿探究暂未开放，请稍后再试", error.getMessage());
    }

    @Test
    public void slowSubmissionBeyondLimitIsMarkedTimeout() {
        // 服务端按进入选择页时间判定：超过限时即使客户端未标记也要记为超时。
        record.setStartTime(new Date(System.currentTimeMillis() - 65_000L));

        Map<String, Object> view = service.submit(9L, 7L, Arrays.asList("E", "N", "F", "P"), false);

        Map<?, ?> result = (Map<?, ?>) view.get("result");
        assertTrue((Boolean) result.get("isTimeout"));
    }

    @Test
    public void submitCompletesActivityParticipation() {
        GkzhGameParticipation participation = new GkzhGameParticipation();
        participation.setParticipationId(1L);
        participation.setStatus("0");
        when(activityWeekService.getLatestParticipation(anyLong(), anyLong())).thenReturn(participation);

        service.submit(9L, 7L, Arrays.asList("E", "N", "F", "P"), false);

        assertEquals("1", participation.getStatus());
        assertNotNull(participation.getResultJson());
        assertTrue(participation.getResultJson().contains("ENFP"));
        verify(activityWeekService, times(1)).updateGameParticipation(participation);
    }

    @Test
    public void enterRejectsActivityBelongingToAnotherSchool() {
        GkzhActivityGame game = new GkzhActivityGame();
        game.setGameId(18L);
        game.setInstanceId(21L);
        game.setAreaId(21L);
        game.setGameType("zytj");
        when(activityWeekService.getGame(18L)).thenReturn(game);
        when(activityWeekService.getGameSchoolId(game)).thenReturn(99L);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.enter(5L, 21L, 18L, 7L, 1L, "20260001", "张三", 10L, "软件工程", "男"));
        assertEquals("该活动不属于当前学校，无法进入", error.getMessage());
        verify(recordMapper, never()).insert(any(ZytjRecord.class));
    }

    private static ZytjType type(String code) {
        ZytjType type = new ZytjType();
        type.setTypeId(12L);
        type.setTypeCode(code);
        type.setFeature("实际、有条理、注重细节、坚守传统");
        type.setCareerField("金融、法律、行政后勤");
        type.setTypicalCareers("审计师、法务专员、办公室主任");
        type.setStatus("0");
        return type;
    }

    private static ZytjItem item(Long id, String code, String name, int column, int row) {
        ZytjItem item = new ZytjItem();
        item.setItemId(id);
        item.setItemCode(code);
        item.setItemName(name);
        item.setColumnIndex(column);
        item.setRowIndex(row);
        item.setSortOrder(row - 1);
        item.setDimensionCode(ZytjConfigService.COLUMN_DIMENSIONS[column - 1]);
        item.setStatus("0");
        return item;
    }
}
