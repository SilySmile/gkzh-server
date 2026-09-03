package com.gkzh.wjdc.service;

import java.util.List;

import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.wjdc.domain.WjdcSurveyResponse;

/**
 * 用户答卷Service接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface IWjdcSurveyResponseService 
{
    /**
     * 查询用户答卷
     * 
     * @param responseId 用户答卷主键
     * @return 用户答卷
     */
    public WjdcSurveyResponse selectWjdcSurveyResponseByResponseId(Long responseId);

    /**
     * 查询用户答卷列表
     * 
     * @param wjdcSurveyResponse 用户答卷
     * @return 用户答卷集合
     */
    public List<WjdcSurveyResponse> selectWjdcSurveyResponseList(WjdcSurveyResponse wjdcSurveyResponse);

    /**
     * 新增用户答卷
     * 
     * @param wjdcSurveyResponse 用户答卷
     * @return 结果
     */
    public int insertWjdcSurveyResponse(WjdcSurveyResponse wjdcSurveyResponse);

    /**
     * 修改用户答卷
     * 
     * @param wjdcSurveyResponse 用户答卷
     * @return 结果
     */
    public int updateWjdcSurveyResponse(WjdcSurveyResponse wjdcSurveyResponse);

    /**
     * 批量删除用户答卷
     * 
     * @param responseIds 需要删除的用户答卷主键集合
     * @return 结果
     */
    public int deleteWjdcSurveyResponseByResponseIds(Long[] responseIds);

    /**
     * 删除用户答卷信息
     * 
     * @param responseId 用户答卷主键
     * @return 结果
     */
    public int deleteWjdcSurveyResponseByResponseId(Long responseId);

    /**
     * 查询用户答卷详情（包含答案）
     * 
     * @param responseId 用户答卷主键
     * @return 用户答卷详情
     */
    public WjdcSurveyResponse selectWjdcSurveyResponseDetailByResponseId(Long responseId);

    boolean hasUserSubmitted(Long surveyId, Long userId);
    Long saveSurveyResponse(StudentCheckin checkin,Long activityId,WjdcSurveyResponse response);
} 