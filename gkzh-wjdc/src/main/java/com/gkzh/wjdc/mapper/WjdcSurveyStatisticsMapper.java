package com.gkzh.wjdc.mapper;

import java.util.List;
import com.gkzh.wjdc.domain.WjdcSurveyStatistics;

/**
 * 问卷统计Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface WjdcSurveyStatisticsMapper 
{
    /**
     * 查询问卷答卷统计
     * 
     * @param surveyId 问卷ID
     * @return 问卷统计
     */
    public List<WjdcSurveyStatistics> selectSurveyResponseStatistics(Long surveyId);

    /**
     * 查询问题选项统计
     * 
     * @param questionId 问题ID
     * @return 问题选项统计
     */
    public List<WjdcSurveyStatistics> selectQuestionOptionStatistics(Long questionId);

    /**
     * 查询填空题答案统计
     * 
     * @param questionId 问题ID
     * @return 填空题答案统计
     */
    public List<WjdcSurveyStatistics> selectQuestionAnswerStatistics(Long questionId);

    /**
     * 查询选项打分题统计
     *
     * @param questionId 问题ID
     * @return 选项打分题统计
     */
    public List<WjdcSurveyStatistics> selectQuestionOptionScoreStatistics(Long questionId);

    /**
     * 查询所有问卷的答卷数量统计
     * 
     * @return 问卷统计
     */
    public List<WjdcSurveyStatistics> selectAllSurveyResponseCount();

    /**
     * 查询指定问卷的所有问题统计
     * 
     * @param surveyId 问卷ID
     * @return 问题统计
     */
    public List<WjdcSurveyStatistics> selectSurveyQuestionStatistics(Long surveyId);
}