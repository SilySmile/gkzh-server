package com.gkzh.wjdc.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.gkzh.wjdc.domain.WjdcSurveyQuestionStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.wjdc.mapper.WjdcSurveyStatisticsMapper;
import com.gkzh.wjdc.domain.WjdcSurveyStatistics;
import com.gkzh.wjdc.service.IWjdcSurveyStatisticsService;

/**
 * 问卷统计Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Service
public class WjdcSurveyStatisticsServiceImpl implements IWjdcSurveyStatisticsService 
{
    @Autowired
    private WjdcSurveyStatisticsMapper wjdcSurveyStatisticsMapper;

    /**
     * 查询问卷答卷统计
     * 
     * @param surveyId 问卷ID
     * @return 统计结果
     */
    @Override
    public List<WjdcSurveyStatistics> selectSurveyResponseStatistics(Long surveyId)
    {
        return wjdcSurveyStatisticsMapper.selectSurveyResponseStatistics(surveyId);
    }

    /**
     * 查询问题选项统计
     * 
     * @param questionId 问题ID
     * @return 统计结果
     */
    @Override
    public List<WjdcSurveyStatistics> selectQuestionOptionStatistics(Long questionId)
    {
        return wjdcSurveyStatisticsMapper.selectQuestionOptionStatistics(questionId);
    }

    /**
     * 查询填空题答案统计
     * 
     * @param questionId 问题ID
     * @return 统计结果
     */
    @Override
    public List<WjdcSurveyStatistics> selectQuestionAnswerStatistics(Long questionId)
    {
        return wjdcSurveyStatisticsMapper.selectQuestionAnswerStatistics(questionId);
    }

    /**
     * 查询选项打分题统计
     *
     * @param questionId 问题ID
     * @return 统计结果
     */
    @Override
    public List<WjdcSurveyStatistics> selectQuestionOptionScoreStatistics(Long questionId)
    {
        return wjdcSurveyStatisticsMapper.selectQuestionOptionScoreStatistics(questionId);
    }

    /**
     * 查询所有问卷的答卷数量统计
     * 
     * @return 统计结果
     */
    @Override
    public List<WjdcSurveyStatistics> selectAllSurveyResponseCount()
    {
        return wjdcSurveyStatisticsMapper.selectAllSurveyResponseCount();
    }

    /**
     * 查询指定问卷的所有问题统计
     * 
     * @param surveyId 问卷ID
     * @return 统计结果
     */
    @Override
    public List<WjdcSurveyStatistics> selectSurveyQuestionStatistics(Long surveyId)
    {
//        return wjdcSurveyStatisticsMapper.selectSurveyQuestionStatistics(surveyId);
        return wjdcSurveyStatisticsMapper.selectSurveyResponseStatistics(surveyId);
    }

    /**
     * 获取问卷完整统计信息
     */
    @Override
    public List<WjdcSurveyQuestionStatistics> getSurveyFullStatistics(Long surveyId) {
        // 获取问卷基本信息
        List<WjdcSurveyStatistics> surveyStats = selectSurveyResponseStatistics(surveyId);
        String surveyTitle = surveyStats.isEmpty() ? "" : surveyStats.get(0).getSurveyTitle();
        Long totalResponses = surveyStats.isEmpty() ? 0L : surveyStats.get(0).getTotalResponses().longValue();

        // 获取所有问题统计
        List<WjdcSurveyStatistics> questionStats = selectSurveyQuestionStatistics(surveyId);

        List<WjdcSurveyQuestionStatistics> result = new ArrayList<>();

        // 遍历每个问题，获取详细统计信息
        for (WjdcSurveyStatistics question : questionStats) {
            WjdcSurveyQuestionStatistics questionStat = new WjdcSurveyQuestionStatistics();
            questionStat.setQuestionId(question.getQuestionId());
            questionStat.setQuestionTitle(question.getQuestionTitle());
            questionStat.setQuestionType(question.getQuestionType());
            questionStat.setTotalResponses(question.getTotalResponses().longValue());

            // 根据问题类型获取不同统计信息
            if ("1".equals(question.getQuestionType()) || "2".equals(question.getQuestionType())) {
                // 单选或多选题，获取选项统计
                List<WjdcSurveyStatistics> optionStats = selectQuestionOptionStatistics(question.getQuestionId());
                questionStat.setOptions(optionStats);
            } else if ("3".equals(question.getQuestionType()) || "4".equals(question.getQuestionType())) {
                // 填空题或打分题，获取答案统计
                List<WjdcSurveyStatistics> answerStats = selectQuestionAnswerStatistics(question.getQuestionId());
                questionStat.setAnswers(answerStats);
            } else if ("5".equals(question.getQuestionType())) {
                // 选项打分题，获取选项统计（平均分和标准差）
                List<WjdcSurveyStatistics> optionStats = selectQuestionOptionScoreStatistics(question.getQuestionId());
                questionStat.setAnswers(optionStats);
            }

            result.add(questionStat);
        }

        return result;
    }
} 