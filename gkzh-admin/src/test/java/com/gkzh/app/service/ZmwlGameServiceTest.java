package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zmwl.domain.ZmwlDirection;
import com.gkzh.zmwl.domain.ZmwlRecord;
import com.gkzh.zmwl.domain.ZmwlSetting;
import com.gkzh.zmwl.mapper.ZmwlRecordMapper;
import com.gkzh.zmwl.service.ZmwlConfigService;
import com.gkzh.zmwl.service.ZmwlGameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * 职面未来学生端业务回归：
 * 确认选择写入方向快照并完成活动参与；确认后默认只读；允许重新选择时才覆盖；
 * 跨学校扫码必须被拒绝。
 */
public class ZmwlGameServiceTest {

    private ZmwlRecordMapper recordMapper;
    private ZmwlConfigService configService;
    private IActivityWeekService activityWeekService;
    private ZmwlGameService service;
    private ZmwlRecord record;
    private ZmwlDirection employment;
    private ZmwlDirection overseas;
    private ZmwlSetting setting;

    @BeforeEach
    public void setUp() {
        recordMapper = mock(ZmwlRecordMapper.class);
        configService = mock(ZmwlConfigService.class);
        activityWeekService = mock(IActivityWeekService.class);
        service = new ZmwlGameService(recordMapper, configService, activityWeekService);

        employment = direction(1L, "employment", "就业工作", "职场启航者");
        overseas = direction(3L, "overseas_study", "出国留学", "世界探索者");
        setting = new ZmwlSetting();
        setting.setSettingId(1);
        setting.setEnabled("1");
        setting.setAllowReselect("0");

        record = new ZmwlRecord();
        record.setRecordId(9L);
        record.setSchoolId(5L);
        record.setInstanceId(21L);
        record.setGameId(18L);
        record.setUserId(7L);
        record.setGameType("zmwl");
        record.setStatus("participating");

        when(recordMapper.selectOne(any())).thenReturn(record);
        when(configService.getSetting()).thenReturn(setting);
        when(configService.findActive("employment")).thenReturn(employment);
        when(configService.findActive("overseas_study")).thenReturn(overseas);
        when(configService.listActive()).thenReturn(java.util.Arrays.asList(employment));
        when(activityWeekService.getLatestParticipation(anyLong(), anyLong())).thenReturn(null);
    }

    @Test
    public void chooseWritesDirectionSnapshotAndFinishes() {
        Map<String, Object> view = service.choose(9L, 7L, "employment");

        assertEquals("finished", view.get("status"));
        Map<?, ?> result = (Map<?, ?>) view.get("result");
        assertEquals("就业工作", result.get("directionName"));
        assertEquals("职场启航者", result.get("tagName"));
        assertTrue(String.valueOf(result.get("interpretation")).contains("进入职场"));
        verify(recordMapper, times(1)).update(any(), any());
    }

    @Test
    public void unknownDirectionIsRejected() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.choose(9L, 7L, "not_exists"));
        assertEquals("该发展方向暂不可选，请刷新后重新选择", error.getMessage());
    }

    @Test
    public void confirmedChoiceIsReadOnlyByDefault() {
        record.setStatus("finished");
        record.setDirectionCode("employment");
        record.setDirectionName("就业工作");

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.choose(9L, 7L, "overseas_study"));
        assertEquals("未来发展方向已确认，不可修改", error.getMessage());
        verify(recordMapper, never()).update(any(), any());
    }

    @Test
    public void sameDirectionResubmitIsIdempotent() {
        record.setStatus("finished");
        record.setDirectionCode("employment");
        record.setDirectionName("就业工作");

        service.choose(9L, 7L, "employment");
        verify(recordMapper, never()).update(any(), any());
    }

    @Test
    public void reselectIsAllowedWhenSettingEnablesIt() {
        setting.setAllowReselect("1");
        record.setStatus("finished");
        record.setDirectionCode("employment");
        record.setDirectionName("就业工作");
        when(configService.findActive("overseas_study")).thenReturn(overseas);
        when(configService.listActive()).thenReturn(java.util.Arrays.asList(employment, overseas));

        Map<String, Object> view = service.choose(9L, 7L, "overseas_study");

        Map<?, ?> result = (Map<?, ?>) view.get("result");
        assertEquals("出国留学", result.get("directionName"));
        verify(recordMapper, times(1)).update(any(), any());
    }

    @Test
    public void enterRejectsActivityBelongingToAnotherSchool() {
        GkzhActivityGame game = new GkzhActivityGame();
        game.setGameId(18L);
        game.setInstanceId(21L);
        game.setAreaId(21L);
        game.setGameType("zmwl");
        when(activityWeekService.getGame(18L)).thenReturn(game);
        when(activityWeekService.getGameSchoolId(game)).thenReturn(99L);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.enter(5L, 21L, 18L, 7L, 1L, "20260001", "张三", 10L, "软件工程", "男"));
        assertEquals("该活动不属于当前学校，无法进入", error.getMessage());
        verify(recordMapper, never()).insert(any(ZmwlRecord.class));
    }

    private static ZmwlDirection direction(Long id, String code, String name, String tag) {
        ZmwlDirection direction = new ZmwlDirection();
        direction.setDirectionId(id);
        direction.setDirectionCode(code);
        direction.setName(name);
        direction.setTagName(tag);
        direction.setInterpretation("你希望毕业后尽快进入职场，将大学期间积累的知识和能力运用到实际工作中。");
        direction.setMessage("职场是一段全新的旅程，每一次尝试和积累，都可能成为未来成长的起点。");
        direction.setStatus("0");
        direction.setSortOrder(1);
        return direction;
    }
}
