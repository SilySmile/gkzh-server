package com.gkzh.app.service;

import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.gkzh.zycck.service.ZycckRecordService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 猜猜看扫码入口必须拒绝其他学校的活动。 */
class ZycckSchoolEntryTest {

    @Test
    void rejectsGameFromAnotherSchool() {
        ZycckRecordMapper records = mock(ZycckRecordMapper.class);
        IActivityWeekService activities = mock(IActivityWeekService.class);
        GkzhActivityGame game = new GkzhActivityGame();
        game.setGameId(5L);
        game.setInstanceId(4L);
        game.setAreaId(19L);
        game.setGameType("zycck");
        when(activities.getGame(5L)).thenReturn(game);
        when(activities.getGameSchoolId(game)).thenReturn(3L);

        ZycckRecordService service = new ZycckRecordService(records, activities, null, null);
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.enter(1L, 4L, 5L, 3L, 21L, 101L, "软件工程", "0"));

        assertEquals("该活动不属于当前学校，无法进入", error.getMessage());
        verify(records, never()).insert(any(ZycckRecord.class));
        verify(activities, never()).recordGameEnter(any());
    }
}
