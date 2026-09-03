package com.gkzh.wjdc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.wjdc.mapper.WjdcSurveyOptionMapper;
import com.gkzh.wjdc.domain.WjdcSurveyOption;
import com.gkzh.wjdc.service.IWjdcSurveyOptionService;

/**
 * 问卷选项Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Service
public class WjdcSurveyOptionServiceImpl implements IWjdcSurveyOptionService 
{
    @Autowired
    private WjdcSurveyOptionMapper wjdcSurveyOptionMapper;

    /**
     * 查询问卷选项
     * 
     * @param id 问卷选项主键
     * @return 问卷选项
     */
    @Override
    public WjdcSurveyOption selectWjdcSurveyOptionById(Integer id)
    {
        return wjdcSurveyOptionMapper.selectWjdcSurveyOptionById(id);
    }

    /**
     * 查询问卷选项列表
     * 
     * @param wjdcSurveyOption 问卷选项
     * @return 问卷选项
     */
    @Override
    public List<WjdcSurveyOption> selectWjdcSurveyOptionList(WjdcSurveyOption wjdcSurveyOption)
    {
        return wjdcSurveyOptionMapper.selectWjdcSurveyOptionList(wjdcSurveyOption);
    }

    /**
     * 根据问题ID查询选项列表
     * 
     * @param questionId 问题ID
     * @return 问卷选项集合
     */
    @Override
    public List<WjdcSurveyOption> selectWjdcSurveyOptionByQuestionId(Integer questionId)
    {
        return wjdcSurveyOptionMapper.selectWjdcSurveyOptionByQuestionId(questionId);
    }

    /**
     * 新增问卷选项
     * 
     * @param wjdcSurveyOption 问卷选项
     * @return 结果
     */
    @Override
    public int insertWjdcSurveyOption(WjdcSurveyOption wjdcSurveyOption)
    {
        return wjdcSurveyOptionMapper.insertWjdcSurveyOption(wjdcSurveyOption);
    }

    /**
     * 修改问卷选项
     * 
     * @param wjdcSurveyOption 问卷选项
     * @return 结果
     */
    @Override
    public int updateWjdcSurveyOption(WjdcSurveyOption wjdcSurveyOption)
    {
        return wjdcSurveyOptionMapper.updateWjdcSurveyOption(wjdcSurveyOption);
    }

    /**
     * 批量删除问卷选项
     * 
     * @param ids 需要删除的问卷选项主键
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyOptionByIds(Integer[] ids)
    {
        return wjdcSurveyOptionMapper.deleteWjdcSurveyOptionByIds(ids);
    }

    /**
     * 删除问卷选项信息
     * 
     * @param id 问卷选项主键
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyOptionById(Integer id)
    {
        return wjdcSurveyOptionMapper.deleteWjdcSurveyOptionById(id);
    }

    /**
     * 根据问题ID删除选项
     * 
     * @param questionId 问题ID
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyOptionByQuestionId(Integer questionId)
    {
        return wjdcSurveyOptionMapper.deleteWjdcSurveyOptionByQuestionId(questionId);
    }
} 