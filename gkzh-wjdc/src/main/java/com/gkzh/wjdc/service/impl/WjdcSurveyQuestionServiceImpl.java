package com.gkzh.wjdc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.wjdc.mapper.WjdcSurveyQuestionMapper;
import com.gkzh.wjdc.domain.WjdcSurveyQuestion;
import com.gkzh.wjdc.service.IWjdcSurveyQuestionService;

/**
 * 问卷问题Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Service
public class WjdcSurveyQuestionServiceImpl implements IWjdcSurveyQuestionService 
{
    @Autowired
    private WjdcSurveyQuestionMapper wjdcSurveyQuestionMapper;

    /**
     * 查询问卷问题
     * 
     * @param id 问卷问题主键
     * @return 问卷问题
     */
    @Override
    public WjdcSurveyQuestion selectWjdcSurveyQuestionById(Integer id)
    {
        return wjdcSurveyQuestionMapper.selectWjdcSurveyQuestionById(id);
    }

    /**
     * 查询问卷问题列表
     * 
     * @param wjdcSurveyQuestion 问卷问题
     * @return 问卷问题
     */
    @Override
    public List<WjdcSurveyQuestion> selectWjdcSurveyQuestionList(WjdcSurveyQuestion wjdcSurveyQuestion)
    {
        return wjdcSurveyQuestionMapper.selectWjdcSurveyQuestionList(wjdcSurveyQuestion);
    }

    /**
     * 根据问卷ID查询问题列表
     * 
     * @param surveyId 问卷ID
     * @return 问卷问题集合
     */
    @Override
    public List<WjdcSurveyQuestion> selectWjdcSurveyQuestionBySurveyId(Integer surveyId)
    {
        return wjdcSurveyQuestionMapper.selectWjdcSurveyQuestionBySurveyId(surveyId);
    }

    /**
     * 新增问卷问题
     * 
     * @param wjdcSurveyQuestion 问卷问题
     * @return 结果
     */
    @Override
    public Long insertWjdcSurveyQuestion(WjdcSurveyQuestion wjdcSurveyQuestion)
    {
        int i = wjdcSurveyQuestionMapper.insertWjdcSurveyQuestion(wjdcSurveyQuestion);
        return i > 0 ? wjdcSurveyQuestion.getId() : 0L;
    }

    /**
     * 修改问卷问题
     * 
     * @param wjdcSurveyQuestion 问卷问题
     * @return 结果
     */
    @Override
    public int updateWjdcSurveyQuestion(WjdcSurveyQuestion wjdcSurveyQuestion)
    {
        return wjdcSurveyQuestionMapper.updateWjdcSurveyQuestion(wjdcSurveyQuestion);
    }

    /**
     * 批量删除问卷问题
     * 
     * @param ids 需要删除的问卷问题主键
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyQuestionByIds(Integer[] ids)
    {
        return wjdcSurveyQuestionMapper.deleteWjdcSurveyQuestionByIds(ids);
    }

    /**
     * 删除问卷问题信息
     * 
     * @param id 问卷问题主键
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyQuestionById(Integer id)
    {
        return wjdcSurveyQuestionMapper.deleteWjdcSurveyQuestionById(id);
    }

    /**
     * 根据问卷ID删除问题
     * 
     * @param surveyId 问卷ID
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyQuestionBySurveyId(Integer surveyId)
    {
        return wjdcSurveyQuestionMapper.deleteWjdcSurveyQuestionBySurveyId(surveyId);
    }
}
