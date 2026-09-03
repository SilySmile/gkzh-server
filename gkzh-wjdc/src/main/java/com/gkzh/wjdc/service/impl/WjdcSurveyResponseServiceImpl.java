package com.gkzh.wjdc.service.impl;

import java.util.List;

import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.wjdc.domain.WjdcSurveyAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.wjdc.mapper.WjdcSurveyResponseMapper;
import com.gkzh.wjdc.domain.WjdcSurveyResponse;
import com.gkzh.wjdc.service.IWjdcSurveyResponseService;
import com.gkzh.wjdc.mapper.WjdcSurveyAnswerMapper;

/**
 * 用户答卷Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Service
public class WjdcSurveyResponseServiceImpl implements IWjdcSurveyResponseService 
{
    @Autowired
    private WjdcSurveyResponseMapper wjdcSurveyResponseMapper;

    @Autowired
    private WjdcSurveyAnswerMapper wjdcSurveyAnswerMapper;

    @Autowired
    private IGkzhActivityParticipationRecordService gkzhActivityParticipationRecordService;
    /**
     * 查询用户答卷
     * 
     * @param responseId 用户答卷主键
     * @return 用户答卷
     */
    @Override
    public WjdcSurveyResponse selectWjdcSurveyResponseByResponseId(Long responseId)
    {
        return wjdcSurveyResponseMapper.selectWjdcSurveyResponseByResponseId(responseId);
    }

    /**
     * 查询用户答卷列表
     * 
     * @param wjdcSurveyResponse 用户答卷
     * @return 用户答卷
     */
    @Override
    public List<WjdcSurveyResponse> selectWjdcSurveyResponseList(WjdcSurveyResponse wjdcSurveyResponse)
    {
        return wjdcSurveyResponseMapper.selectWjdcSurveyResponseList(wjdcSurveyResponse);
    }

    /**
     * 新增用户答卷
     * 
     * @param wjdcSurveyResponse 用户答卷
     * @return 结果
     */
    @Override
    public int insertWjdcSurveyResponse(WjdcSurveyResponse wjdcSurveyResponse)
    {
        return wjdcSurveyResponseMapper.insertWjdcSurveyResponse(wjdcSurveyResponse);
    }

    /**
     * 修改用户答卷
     * 
     * @param wjdcSurveyResponse 用户答卷
     * @return 结果
     */
    @Override
    public int updateWjdcSurveyResponse(WjdcSurveyResponse wjdcSurveyResponse)
    {
        return wjdcSurveyResponseMapper.updateWjdcSurveyResponse(wjdcSurveyResponse);
    }

    /**
     * 批量删除用户答卷
     * 
     * @param responseIds 需要删除的用户答卷主键
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyResponseByResponseIds(Long[] responseIds)
    {
        return wjdcSurveyResponseMapper.deleteWjdcSurveyResponseByResponseIds(responseIds);
    }

    /**
     * 删除用户答卷信息
     * 
     * @param responseId 用户答卷主键
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyResponseByResponseId(Long responseId)
    {
        return wjdcSurveyResponseMapper.deleteWjdcSurveyResponseByResponseId(responseId);
    }

    /**
     * 查询用户答卷详情（包含答案）
     * 
     * @param responseId 用户答卷主键
     * @return 用户答卷详情
     */
    @Override
    public WjdcSurveyResponse selectWjdcSurveyResponseDetailByResponseId(Long responseId)
    {
        return wjdcSurveyResponseMapper.selectWjdcSurveyResponseDetailByResponseId(responseId);
    }

    @Override
    public boolean hasUserSubmitted(Long surveyId, Long userId) {
        return wjdcSurveyResponseMapper.countBySurveyIdAndUserId(surveyId, userId) > 0;
    }

    @Override
    public Long saveSurveyResponse(StudentCheckin checkin, Long activityId, WjdcSurveyResponse response) {
        // 1. 保存主表
        wjdcSurveyResponseMapper.insertWjdcSurveyResponse(response);
        Long responseId = response.getResponseId();
        // 2. 保存明细
        for (WjdcSurveyAnswer ans : response.getAnswers()) {
            ans.setResponseId(response.getResponseId());
            wjdcSurveyAnswerMapper.insertWjdcSurveyAnswer(ans);
        }
        // 3.存入参与活动表
        if(response.getSurveyId() == 4L){
            return responseId;
        }
        GkzhActivityParticipationRecord record = new GkzhActivityParticipationRecord();
        record.setUserId(checkin.getUserId());
        record.setUserName(checkin.getStuName());
        record.setUserCode(checkin.getStuNo());
        record.setParticipationTime(DateUtils.getNowDate());
        record.setParticipationType(5);
        record.setActivityId(activityId);
        record.setModuleId(responseId);
        record.setCreateTime(DateUtils.getNowDate());
        record.setResult("问卷完成");
        gkzhActivityParticipationRecordService.insertGkzhActivityParticipationRecord(record);
        return responseId;
    }
} 