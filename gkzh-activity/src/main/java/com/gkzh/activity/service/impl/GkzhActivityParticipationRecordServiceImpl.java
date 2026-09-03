package com.gkzh.activity.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gkzh.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.activity.mapper.GkzhActivityParticipationRecordMapper;
import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;

/**
 * 活动参与记录Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-27
 */
@Service
public class GkzhActivityParticipationRecordServiceImpl implements IGkzhActivityParticipationRecordService 
{
    @Autowired
    private GkzhActivityParticipationRecordMapper gkzhActivityParticipationRecordMapper;

    /**
     * 查询活动参与记录
     * 
     * @param recordId 活动参与记录主键
     * @return 活动参与记录
     */
    @Override
    public GkzhActivityParticipationRecord selectGkzhActivityParticipationRecordByRecordId(Long recordId)
    {
        return gkzhActivityParticipationRecordMapper.selectGkzhActivityParticipationRecordByRecordId(recordId);
    }

    /**
     * 查询活动参与记录列表
     * 
     * @param gkzhActivityParticipationRecord 活动参与记录
     * @return 活动参与记录
     */
    @Override
    public List<GkzhActivityParticipationRecord> selectGkzhActivityParticipationRecordList(GkzhActivityParticipationRecord gkzhActivityParticipationRecord)
    {
        return gkzhActivityParticipationRecordMapper.selectGkzhActivityParticipationRecordList(gkzhActivityParticipationRecord);
    }

    /**
     * 新增活动参与记录
     * 
     * @param gkzhActivityParticipationRecord 活动参与记录
     * @return 结果
     */
    @Override
    public int insertGkzhActivityParticipationRecord(GkzhActivityParticipationRecord gkzhActivityParticipationRecord)
    {
        gkzhActivityParticipationRecord.setCreateTime(DateUtils.getNowDate());
        return gkzhActivityParticipationRecordMapper.insertGkzhActivityParticipationRecord(gkzhActivityParticipationRecord);
    }

    /**
     * 修改活动参与记录
     * 
     * @param gkzhActivityParticipationRecord 活动参与记录
     * @return 结果
     */
    @Override
    public int updateGkzhActivityParticipationRecord(GkzhActivityParticipationRecord gkzhActivityParticipationRecord)
    {
        return gkzhActivityParticipationRecordMapper.updateGkzhActivityParticipationRecord(gkzhActivityParticipationRecord);
    }

    /**
     * 批量删除活动参与记录
     * 
     * @param recordIds 需要删除的活动参与记录主键
     * @return 结果
     */
    @Override
    public int deleteGkzhActivityParticipationRecordByRecordIds(Long[] recordIds)
    {
        return gkzhActivityParticipationRecordMapper.deleteGkzhActivityParticipationRecordByRecordIds(recordIds);
    }

    /**
     * 删除活动参与记录信息
     * 
     * @param recordId 活动参与记录主键
     * @return 结果
     */
    @Override
    public int deleteGkzhActivityParticipationRecordByRecordId(Long recordId)
    {
        return gkzhActivityParticipationRecordMapper.deleteGkzhActivityParticipationRecordByRecordId(recordId);
    }

    @Override
    public GkzhActivityParticipationRecord selectGkzhActivityParticipationRecordByUserIdAndActivityId(Long userId, Long activityId, Integer participationType) {
        return gkzhActivityParticipationRecordMapper.selectByUserIdAndActivityId(userId, activityId,participationType);
    }

    @Override
    public List<GkzhActivityParticipationRecord> selectLParticipationRecord(Long userId, Long activityId) {
        GkzhActivityParticipationRecord record = new GkzhActivityParticipationRecord();
        record.setActivityId(activityId);
        record.setUserId(userId);
        return gkzhActivityParticipationRecordMapper.selectGkzhActivityParticipationRecordList(record);
    }

    @Override
    public Boolean isParticipated(Long userId, Long activityId, Integer participationType) {
        QueryWrapper<GkzhActivityParticipationRecord> query = Wrappers.query();
        query.eq("user_id", userId);
        query.eq("activity_id", activityId);
        query.eq("participation_type", participationType);
        Long count = gkzhActivityParticipationRecordMapper.selectCount(query);
        if(count > 0){
            return true;
        }
        return false;
    }

    @Override
    public GkzhActivityParticipationRecord selectLatestParticipationRecordByUserIdAndActivityId(Long userId, Long activityId) {
        // 从数据库中查询用户最新参与记录
        return gkzhActivityParticipationRecordMapper.selectLatestParticipationRecordByUserIdAndActivityId(userId, activityId);
    }
}
