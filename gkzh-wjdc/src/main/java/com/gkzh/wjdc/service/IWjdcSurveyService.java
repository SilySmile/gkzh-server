package com.gkzh.wjdc.service;

import java.util.List;
import com.gkzh.wjdc.domain.WjdcSurvey;

/**
 * 问卷管理Service接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface IWjdcSurveyService 
{
    /**
     * 查询问卷管理
     * 
     * @param id 问卷管理主键
     * @return 问卷管理
     */
    public WjdcSurvey selectWjdcSurveyById(Long id);

    /**
     * 查询问卷管理列表
     * 
     * @param wjdcSurvey 问卷管理
     * @return 问卷管理
     */
    public List<WjdcSurvey> selectWjdcSurveyList(WjdcSurvey wjdcSurvey);

    /**
     * 查询问卷管理列表（包含问题和选项）
     * 
     * @param wjdcSurvey 问卷管理
     * @return 问卷管理
     */
    public List<WjdcSurvey> selectWjdcSurveyListWithDetails(WjdcSurvey wjdcSurvey);

    /**
     * 新增问卷管理
     * 
     * @param wjdcSurvey 问卷管理
     * @return 结果
     */
    public int insertWjdcSurvey(WjdcSurvey wjdcSurvey);

    /**
     * 修改问卷管理
     * 
     * @param wjdcSurvey 问卷管理
     * @return 结果
     */
    public int updateWjdcSurvey(WjdcSurvey wjdcSurvey);

    /**
     * 批量删除问卷管理
     * 
     * @param ids 需要删除的问卷管理主键
     * @return 结果
     */
    public int deleteWjdcSurveyByIds(Long[] ids);

    /**
     * 删除问卷管理信息
     * 
     * @param id 问卷管理主键
     * @return 结果
     */
    public int deleteWjdcSurveyById(Long id);


    public WjdcSurvey selectWjdcSurveyWithQuestions(Long surveyId);

    /**
     * 复制问卷
     *
     * @param surveyId 原始问卷ID
     * @return 新问卷ID
     */
    public Long copySurvey(Long surveyId);
}
