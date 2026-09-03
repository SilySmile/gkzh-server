package com.gkzh.activity.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gkzh.activity.domain.GkzhActivity;

/**
 * 活动举办Service接口
 * 
 * @author gkzh
 * @date 2025-06-22
 */
public interface IGkzhActivityService 
{
    /**
     * 查询活动举办
     * 
     * @param activityId 活动举办主键
     * @return 活动举办
     */
    public GkzhActivity selectGkzhActivityByActivityId(Long activityId);

    /**
     * 查询活动举办列表
     * 
     * @param gkzhActivity 活动举办
     * @return 活动举办集合
     */
    public List<GkzhActivity> selectGkzhActivityList(GkzhActivity gkzhActivity);

    /**
     * 新增活动举办
     * 
     * @param gkzhActivity 活动举办
     * @return 结果
     */
    public int insertGkzhActivity(GkzhActivity gkzhActivity);

    /**
     * 修改活动举办
     * 
     * @param gkzhActivity 活动举办
     * @return 结果
     */
    public int updateGkzhActivity(GkzhActivity gkzhActivity);

    /**
     * 批量删除活动举办
     * 
     * @param activityIds 需要删除的活动举办主键集合
     * @return 结果
     */
    public int deleteGkzhActivityByActivityIds(Long[] activityIds);

    /**
     * 删除活动举办信息
     * 
     * @param activityId 活动举办主键
     * @return 结果
     */
    public int deleteGkzhActivityByActivityId(Long activityId);

    /**
     * 查询活动的所有环节（从JSON配置中解析）
     * 
     * @param activityId 活动ID
     * @return 活动环节集合
     */
    public List selectActivityModulesByActivityId(Long activityId);

    Map<Long, String> getActivityTitlesByIds(Set<Long> activityIds);
}
