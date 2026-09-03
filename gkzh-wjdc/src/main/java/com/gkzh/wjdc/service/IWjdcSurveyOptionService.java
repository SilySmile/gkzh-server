package com.gkzh.wjdc.service;

import java.util.List;
import com.gkzh.wjdc.domain.WjdcSurveyOption;

/**
 * 问卷选项Service接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface IWjdcSurveyOptionService 
{
    /**
     * 查询问卷选项
     * 
     * @param id 问卷选项主键
     * @return 问卷选项
     */
    public WjdcSurveyOption selectWjdcSurveyOptionById(Integer id);

    /**
     * 查询问卷选项列表
     * 
     * @param wjdcSurveyOption 问卷选项
     * @return 问卷选项集合
     */
    public List<WjdcSurveyOption> selectWjdcSurveyOptionList(WjdcSurveyOption wjdcSurveyOption);

    /**
     * 根据问题ID查询选项列表
     * 
     * @param questionId 问题ID
     * @return 问卷选项集合
     */
    public List<WjdcSurveyOption> selectWjdcSurveyOptionByQuestionId(Integer questionId);

    /**
     * 新增问卷选项
     * 
     * @param wjdcSurveyOption 问卷选项
     * @return 结果
     */
    public int insertWjdcSurveyOption(WjdcSurveyOption wjdcSurveyOption);

    /**
     * 修改问卷选项
     * 
     * @param wjdcSurveyOption 问卷选项
     * @return 结果
     */
    public int updateWjdcSurveyOption(WjdcSurveyOption wjdcSurveyOption);

    /**
     * 批量删除问卷选项
     * 
     * @param ids 需要删除的问卷选项主键集合
     * @return 结果
     */
    public int deleteWjdcSurveyOptionByIds(Integer[] ids);

    /**
     * 删除问卷选项信息
     * 
     * @param id 问卷选项主键
     * @return 结果
     */
    public int deleteWjdcSurveyOptionById(Integer id);

    /**
     * 根据问题ID删除选项
     * 
     * @param questionId 问题ID
     * @return 结果
     */
    public int deleteWjdcSurveyOptionByQuestionId(Integer questionId);

}