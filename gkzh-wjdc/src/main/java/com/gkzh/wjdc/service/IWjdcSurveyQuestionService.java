package com.gkzh.wjdc.service;

import java.util.List;
import com.gkzh.wjdc.domain.WjdcSurveyQuestion;

/**
 * 问卷问题Service接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface IWjdcSurveyQuestionService 
{
    /**
     * 查询问卷问题
     * 
     * @param id 问卷问题主键
     * @return 问卷问题
     */
    public WjdcSurveyQuestion selectWjdcSurveyQuestionById(Integer id);

    /**
     * 查询问卷问题列表
     * 
     * @param wjdcSurveyQuestion 问卷问题
     * @return 问卷问题集合
     */
    public List<WjdcSurveyQuestion> selectWjdcSurveyQuestionList(WjdcSurveyQuestion wjdcSurveyQuestion);

    /**
     * 根据问卷ID查询问题列表
     * 
     * @param surveyId 问卷ID
     * @return 问卷问题集合
     */
    public List<WjdcSurveyQuestion> selectWjdcSurveyQuestionBySurveyId(Integer surveyId);

    /**
     * 新增问卷问题
     * 
     * @param wjdcSurveyQuestion 问卷问题
     * @return 结果
     */
    public Long insertWjdcSurveyQuestion(WjdcSurveyQuestion wjdcSurveyQuestion);

    /**
     * 修改问卷问题
     * 
     * @param wjdcSurveyQuestion 问卷问题
     * @return 结果
     */
    public int updateWjdcSurveyQuestion(WjdcSurveyQuestion wjdcSurveyQuestion);

    /**
     * 批量删除问卷问题
     * 
     * @param ids 需要删除的问卷问题主键集合
     * @return 结果
     */
    public int deleteWjdcSurveyQuestionByIds(Integer[] ids);

    /**
     * 删除问卷问题信息
     * 
     * @param id 问卷问题主键
     * @return 结果
     */
    public int deleteWjdcSurveyQuestionById(Integer id);

    /**
     * 根据问卷ID删除问题
     * 
     * @param surveyId 问卷ID
     * @return 结果
     */
    public int deleteWjdcSurveyQuestionBySurveyId(Integer surveyId);

}
