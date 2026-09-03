package com.gkzh.zycck.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

@Service
public class ZycckRecordService {
    private final ZycckRecordMapper recordMapper;
    private final IActivityWeekService activityWeekService;

    public ZycckRecordService(ZycckRecordMapper recordMapper, IActivityWeekService activityWeekService) {
        this.recordMapper = recordMapper;
        this.activityWeekService = activityWeekService;
    }

    @Transactional
    public ZycckRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId) {
        if (schoolId == null || instanceId == null || gameId == null || userId == null) {
            throw new ServiceException("缺少活动或学校信息");
        }
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null || !instanceId.equals(game.getInstanceId()) || !"zycck".equals(game.getGameType())) {
            throw new ServiceException("该游戏不属于当前活动");
        }
        ZycckRecord existing = recordMapper.selectOne(new QueryWrapper<ZycckRecord>()
                .eq("school_id", schoolId).eq("instance_id", instanceId).eq("game_id", gameId).eq("user_id", userId));
        if (existing != null) return existing;
        Date now = DateUtils.getNowDate();
        ZycckRecord record = new ZycckRecord();
        record.setSchoolId(schoolId); record.setInstanceId(instanceId); record.setGameId(gameId); record.setUserId(userId);
        record.setGameType("zycck"); record.setStatus("participating"); record.setStage("scanned");
        record.setCurrentQuestionNo(0); record.setScanTime(now); record.setCreateTime(now); record.setUpdateTime(now);
        recordMapper.insert(record);
        return record;
    }

    public ZycckRecord get(Long recordId, Long userId) {
        ZycckRecord record = recordMapper.selectById(recordId);
        if (record == null || !userId.equals(record.getUserId())) throw new ServiceException("记录不存在");
        return record;
    }

    @Transactional
    public ZycckRecord start(Long recordId, Long userId) {
        ZycckRecord record = get(recordId, userId);
        if ("finished".equals(record.getStatus())) throw new ServiceException("本次游戏已完成");
        if (record.getStartTime() == null) record.setStartTime(DateUtils.getNowDate());
        record.setStage("question"); record.setCurrentQuestionNo(1); record.setUpdateTime(DateUtils.getNowDate());
        recordMapper.updateById(record); return record;
    }
}
