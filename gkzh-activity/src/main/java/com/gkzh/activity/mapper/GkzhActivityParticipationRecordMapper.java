package com.gkzh.activity.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import org.apache.ibatis.annotations.Param;

/**
 * 活动参与记录Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-27
 */
public interface GkzhActivityParticipationRecordMapper extends BaseMapper<GkzhActivityParticipationRecord>
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
     * 删除活动参与记录
     * 
     * @param recordId 活动参与记录主键
     * @return 结果
     */
    public int deleteGkzhActivityParticipationRecordByRecordId(Long recordId);

    /**
     * 批量删除活动参与记录
     * 
     * @param recordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGkzhActivityParticipationRecordByRecordIds(Long[] recordIds);

    /**
     * 查询用户最新参与记录
     * 
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 用户参与记录
     */
    public GkzhActivityParticipationRecord selectLatestParticipationRecordByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);

    /**
     * 根据用户ID和活动ID查询参与记录
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 活动参与记录
     */
    GkzhActivityParticipationRecord selectByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId, @Param("participationType") Integer participationType);

}
