package com.gkzh.activity.service;

import java.util.List;
import com.gkzh.activity.domain.GkzhActivityParticipationRecord;

/**
 * 活动参与记录Service接口
 * 
 * @author gkzh
 * @date 2025-06-27
 */
public interface IGkzhActivityParticipationRecordService 
{
    /**
     * 查询活动参与记录
     * 
     * @param recordId 活动参与记录主键
     * @return 活动参与记录
     */
    public GkzhActivityParticipationRecord selectGkzhActivityParticipationRecordByRecordId(Long recordId);

    /**
     * 查询活动参与记录列表
     * 
     * @param gkzhActivityParticipationRecord 活动参与记录
     * @return 活动参与记录集合
     */
    public List<GkzhActivityParticipationRecord> selectGkzhActivityParticipationRecordList(GkzhActivityParticipationRecord gkzhActivityParticipationRecord);

    /**
     * 查询用户最新参与记录
     * 
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 用户参与记录
     */
    public GkzhActivityParticipationRecord selectLatestParticipationRecordByUserIdAndActivityId(Long userId, Long activityId);

    /**
     * 新增活动参与记录
     * 
     * @param gkzhActivityParticipationRecord 活动参与记录
     * @return 结果
     */
    public int insertGkzhActivityParticipationRecord(GkzhActivityParticipationRecord gkzhActivityParticipationRecord);

    /**
     * 修改活动参与记录
     * 
     * @param gkzhActivityParticipationRecord 活动参与记录
     * @return 结果
     */
    public int updateGkzhActivityParticipationRecord(GkzhActivityParticipationRecord gkzhActivityParticipationRecord);

    /**
     * 批量删除活动参与记录
     * 
     * @param recordIds 需要删除的活动参与记录主键集合
     * @return 结果
     */
    public int deleteGkzhActivityParticipationRecordByRecordIds(Long[] recordIds);

    /**
     * 删除活动参与记录信息
     * 
     * @param recordId 活动参与记录主键
     * @return 结果
     */
    public int deleteGkzhActivityParticipationRecordByRecordId(Long recordId);


    public GkzhActivityParticipationRecord selectGkzhActivityParticipationRecordByUserIdAndActivityId(Long userId,Long activityId,Integer participationType);



    public List<GkzhActivityParticipationRecord> selectLParticipationRecord(Long userId,Long activityId);


    public Boolean isParticipated(Long userId,Long activityId,Integer participationType);
}
