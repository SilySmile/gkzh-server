package com.gkzh.wjdc.mapper;

import java.util.List;
import com.gkzh.wjdc.domain.WjdcSurveyAnswer;

/**
 * 用户答题Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface WjdcSurveyAnswerMapper 
{
    /**
     * 查询用户答题
     * 
     * @param answerId 用户答题主键
     * @return 用户答题
     */
    public WjdcSurveyAnswer selectWjdcSurveyAnswerByAnswerId(Long answerId);

    /**
     * 查询用户答题列表
     * 
     * @param wjdcSurveyAnswer 用户答题
     * @return 用户答题集合
     */
    public List<WjdcSurveyAnswer> selectWjdcSurveyAnswerList(WjdcSurveyAnswer wjdcSurveyAnswer);

    /**
     * 新增用户答题
     * 
     * @param wjdcSurveyAnswer 用户答题
     * @return 结果
     */
    public int insertWjdcSurveyAnswer(WjdcSurveyAnswer wjdcSurveyAnswer);

    /**
     * 修改用户答题
     * 
     * @param wjdcSurveyAnswer 用户答题
     * @return 结果
     */
    public int updateWjdcSurveyAnswer(WjdcSurveyAnswer wjdcSurveyAnswer);

    /**
     * 删除用户答题
     * 
     * @param answerId 用户答题主键
     * @return 结果
     */
    public int deleteWjdcSurveyAnswerByAnswerId(Long answerId);

    /**
     * 批量删除用户答题
     * 
     * @param answerIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWjdcSurveyAnswerByAnswerIds(Long[] answerIds);

    /**
     * 根据答卷ID查询答题列表
     * 
     * @param responseId 答卷ID
     * @return 答题列表
     */
    public List<WjdcSurveyAnswer> selectWjdcSurveyAnswerByResponseId(Long responseId);

    /**
     * 根据答卷ID删除答题
     * 
     * @param responseId 答卷ID
     * @return 结果
     */
    public int deleteWjdcSurveyAnswerByResponseId(Long responseId);

}