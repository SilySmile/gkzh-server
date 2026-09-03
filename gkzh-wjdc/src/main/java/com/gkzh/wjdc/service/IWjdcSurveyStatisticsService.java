package com.gkzh.wjdc.service;

import java.util.List;

import com.gkzh.wjdc.domain.WjdcSurveyQuestionStatistics;
import com.gkzh.wjdc.domain.WjdcSurveyStatistics;

/**
 * 问卷统计Service接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface IWjdcSurveyStatisticsService 
{
    /**
     * 查询问卷答卷统计
     * 
     * @param surveyId 问卷ID
     * @return 统计结果
     */
    public List<WjdcSurveyStatistics> selectSurveyResponseStatistics(Long surveyId);

    /**
     * 查询问题选项统计
     * 
     * @param questionId 问题ID
     * @return 统计结果
     */
    public List<WjdcSurveyStatistics> selectQuestionOptionStatistics(Long questionId);

    /**
     * 查询填空题答案统计
     * 
     * @param questionId 问题ID
     * @return 统计结果
     */
    public List<WjdcSurveyStatistics> selectQuestionAnswerStatistics(Long questionId);

    /**
     * 查询选项打分题统计
     *
     * @param questionId 问题ID
     * @return 统计结果
     */
    public List<WjdcSurveyStatistics> selectQuestionOptionScoreStatistics(Long questionId);

    /**
     * 查询所有问卷的答卷数量统计
     * 
     * @return 统计结果
     */
    public List<WjdcSurveyStatistics> selectAllSurveyResponseCount();

    /**
     * 查询指定问卷的所有问题统计
     * 
     * @param surveyId 问卷ID
     * @return 统计结果
     */
    public List<WjdcSurveyStatistics> selectSurveyQuestionStatistics(Long surveyId);

    public List<WjdcSurveyQuestionStatistics> getSurveyFullStatistics(Long surveyId);
} 