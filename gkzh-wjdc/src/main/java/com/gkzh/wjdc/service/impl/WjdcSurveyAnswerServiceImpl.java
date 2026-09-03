package com.gkzh.wjdc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.wjdc.mapper.WjdcSurveyAnswerMapper;
import com.gkzh.wjdc.domain.WjdcSurveyAnswer;
import com.gkzh.wjdc.service.IWjdcSurveyAnswerService;

/**
 * 用户答题Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Service
public class WjdcSurveyAnswerServiceImpl implements IWjdcSurveyAnswerService 
{
    @Autowired
    private WjdcSurveyAnswerMapper wjdcSurveyAnswerMapper;

    /**
     * 查询用户答题
     * 
     * @param answerId 用户答题主键
     * @return 用户答题
     */
    @Override
    public WjdcSurveyAnswer selectWjdcSurveyAnswerByAnswerId(Long answerId)
    {
        return wjdcSurveyAnswerMapper.selectWjdcSurveyAnswerByAnswerId(answerId);
    }

    /**
     * 查询用户答题列表
     * 
     * @param wjdcSurveyAnswer 用户答题
     * @return 用户答题
     */
    @Override
    public List<WjdcSurveyAnswer> selectWjdcSurveyAnswerList(WjdcSurveyAnswer wjdcSurveyAnswer)
    {
        return wjdcSurveyAnswerMapper.selectWjdcSurveyAnswerList(wjdcSurveyAnswer);
    }

    /**
     * 新增用户答题
     * 
     * @param wjdcSurveyAnswer 用户答题
     * @return 结果
     */
    @Override
    public int insertWjdcSurveyAnswer(WjdcSurveyAnswer wjdcSurveyAnswer)
    {
        return wjdcSurveyAnswerMapper.insertWjdcSurveyAnswer(wjdcSurveyAnswer);
    }

    /**
     * 修改用户答题
     * 
     * @param wjdcSurveyAnswer 用户答题
     * @return 结果
     */
    @Override
    public int updateWjdcSurveyAnswer(WjdcSurveyAnswer wjdcSurveyAnswer)
    {
        return wjdcSurveyAnswerMapper.updateWjdcSurveyAnswer(wjdcSurveyAnswer);
    }

    /**
     * 批量删除用户答题
     * 
     * @param answerIds 需要删除的用户答题主键
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyAnswerByAnswerIds(Long[] answerIds)
    {
        return wjdcSurveyAnswerMapper.deleteWjdcSurveyAnswerByAnswerIds(answerIds);
    }

    /**
     * 删除用户答题信息
     * 
     * @param answerId 用户答题主键
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyAnswerByAnswerId(Long answerId)
    {
        return wjdcSurveyAnswerMapper.deleteWjdcSurveyAnswerByAnswerId(answerId);
    }

    /**
     * 根据答卷ID查询答题列表
     * 
     * @param responseId 答卷ID
     * @return 答题列表
     */
    @Override
    public List<WjdcSurveyAnswer> selectWjdcSurveyAnswerByResponseId(Long responseId)
    {
        return wjdcSurveyAnswerMapper.selectWjdcSurveyAnswerByResponseId(responseId);
    }

    /**
     * 根据答卷ID删除答题
     * 
     * @param responseId 答卷ID
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyAnswerByResponseId(Long responseId)
    {
        return wjdcSurveyAnswerMapper.deleteWjdcSurveyAnswerByResponseId(responseId);
    }
} 