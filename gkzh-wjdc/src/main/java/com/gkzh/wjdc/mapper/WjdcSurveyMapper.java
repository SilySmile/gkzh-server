package com.gkzh.wjdc.mapper;

import java.util.List;
import com.gkzh.wjdc.domain.WjdcSurvey;

/**
 * 问卷管理Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface WjdcSurveyMapper 
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
     * @return 问卷管理集合
     */
    public List<WjdcSurvey> selectWjdcSurveyList(WjdcSurvey wjdcSurvey);

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
     * 删除问卷管理
     * 
     * @param id 问卷管理主键
     * @return 结果
     */
    public int deleteWjdcSurveyById(Long id);

    /**
     * 批量删除问卷管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWjdcSurveyByIds(Long[] ids);
}
