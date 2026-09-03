package com.gkzh.activity.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.activity.domain.GkzhActivity;

import org.apache.ibatis.annotations.Mapper;

/**
 * 活动举办Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-22
 */
@Mapper
public interface GkzhActivityMapper extends BaseMapper<GkzhActivity> {

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
     * 删除活动举办
     * 
     * @param activityId 活动举办主键
     * @return 结果
     */
    public int deleteGkzhActivityByActivityId(Long activityId);

    /**
     * 批量删除活动举办
     * 
     * @param activityIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGkzhActivityByActivityIds(Long[] activityIds);

    /**
     * 查询活动的所有环节
     * 
     * @param activityId 活动ID
     * @return 活动环节集合
     */
    // 已移除，改用JSON解析方式获取活动环节

}
