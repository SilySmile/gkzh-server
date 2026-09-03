package com.gkzh.activity.service.impl;

import java.util.*;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gkzh.activity.domain.GkzhActivity;
import com.gkzh.activity.mapper.GkzhActivityMapper;
import com.gkzh.activity.service.IGkzhActivityService;
import com.gkzh.common.utils.QRCodeUtils;
import com.gkzh.common.utils.DateUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 活动举办Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-22
 */
@Service
public class GkzhActivityServiceImpl implements IGkzhActivityService 
{
    private final GkzhActivityMapper gkzhActivityMapper;

    @Value("${app.qr-code.base-url}")
    private String qrCodeBaseUrl;

    @Autowired
    public GkzhActivityServiceImpl(GkzhActivityMapper gkzhActivityMapper) {
        this.gkzhActivityMapper = gkzhActivityMapper;
    }

    /**
     * 查询活动举办
     * 
     * @param activityId 活动举办主键
     * @return 活动举办
     */
    @Override
    public GkzhActivity selectGkzhActivityByActivityId(Long activityId)
    {
        return gkzhActivityMapper.selectGkzhActivityByActivityId(activityId);
    }

    /**
     * 查询活动举办列表
     * 
     * @param gkzhActivity 活动举办
     * @return 活动举办
     */
    @Override
    public List<GkzhActivity> selectGkzhActivityList(GkzhActivity gkzhActivity)
    {
        return gkzhActivityMapper.selectGkzhActivityList(gkzhActivity);
    }

    /**
     * 新增活动举办
     * 
     * @param gkzhActivity 活动举办
     * @return 结果
     */
    @Override
    public int insertGkzhActivity(GkzhActivity gkzhActivity)
    {
        gkzhActivity.setCreateTime(DateUtils.getNowDate());
        //设置结束时间要到当天的23:59:59
        Date endTime = gkzhActivity.getEndTime();
        endTime.setHours(23);
        endTime.setMinutes(59);
        endTime.setSeconds(59);
        gkzhActivity.setEndTime(endTime);
        int activityId = gkzhActivityMapper.insertGkzhActivity(gkzhActivity);
        if(activityId > 0) {
            // 生成活动二维码内容
            String qrContent = QRCodeUtils.generateActivityQRContent(gkzhActivity.getActivityId(), qrCodeBaseUrl);
            gkzhActivity.setQrCode(qrContent);
            gkzhActivityMapper.updateGkzhActivity(gkzhActivity);
        }
        return activityId;
    }

    /**
     * 修改活动举办
     * 
     * @param gkzhActivity 活动举办
     * @return 结果
     */
    @Override
    public int updateGkzhActivity(GkzhActivity gkzhActivity)
    {
        gkzhActivity.setUpdateTime(DateUtils.getNowDate());
        Date endTime = gkzhActivity.getEndTime();
        endTime.setHours(23);
        endTime.setMinutes(59);
        endTime.setSeconds(59);
        gkzhActivity.setEndTime(endTime);
        return gkzhActivityMapper.updateGkzhActivity(gkzhActivity);
    }

    /**
     * 批量删除活动举办
     * 
     * @param activityIds 需要删除的活动举办主键
     * @return 结果
     */
    @Override
    public int deleteGkzhActivityByActivityIds(Long[] activityIds)
    {
        return gkzhActivityMapper.deleteGkzhActivityByActivityIds(activityIds);
    }

    /**
     * 删除活动举办信息
     * 
     * @param activityId 活动举办主键
     * @return 结果
     */
    @Override
    public int deleteGkzhActivityByActivityId(Long activityId)
    {
        return gkzhActivityMapper.deleteGkzhActivityByActivityId(activityId);
    }

    @Override
    public List selectActivityModulesByActivityId(Long activityId) {
        GkzhActivity activity = gkzhActivityMapper.selectGkzhActivityByActivityId(activityId);
        if (activity != null && activity.getModuleConfig() != null) {
            return JSON.parseArray(activity.getModuleConfig());
        }
        return new ArrayList<>();
    }

    @Override
    public Map<Long, String> getActivityTitlesByIds(Set<Long> activityIds) {
        QueryWrapper<GkzhActivity> query = Wrappers.query();
        if(activityIds != null && activityIds.size() > 0){
            query.in("activity_id", activityIds);
        }
        List<GkzhActivity> gkzhActivities = gkzhActivityMapper.selectList(query);
        HashMap<Long, String> ret = new HashMap<>();

        for (GkzhActivity gkzhActivity : gkzhActivities) {
            ret.put(gkzhActivity.getActivityId(), gkzhActivity.getTitle());
        }
        return ret;
    }
}