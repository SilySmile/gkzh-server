package com.gkzh.lottery.service.impl;

import java.util.List;
import com.gkzh.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.lottery.mapper.GkzhZyxxzStudentChoiceMapper;
import com.gkzh.lottery.domain.GkzhZyxxzStudentChoice;
import com.gkzh.lottery.service.IGkzhZyxxzStudentChoiceService;

/**
 * 职业信息站-学生选择记录Service业务层处理
 * 
 * @author gkzh
 * @date 2026-06-04
 */
@Service
public class GkzhZyxxzStudentChoiceServiceImpl implements IGkzhZyxxzStudentChoiceService 
{
    @Autowired
    private GkzhZyxxzStudentChoiceMapper gkzhZyxxzStudentChoiceMapper;

    /**
     * 查询职业信息站-学生选择记录
     * 
     * @param choiceId 职业信息站-学生选择记录主键
     * @return 职业信息站-学生选择记录
     */
    @Override
    public GkzhZyxxzStudentChoice selectGkzhZyxxzStudentChoiceByChoiceId(Long choiceId)
    {
        return gkzhZyxxzStudentChoiceMapper.selectGkzhZyxxzStudentChoiceByChoiceId(choiceId);
    }

    /**
     * 查询职业信息站-学生选择记录列表
     * 
     * @param gkzhZyxxzStudentChoice 职业信息站-学生选择记录
     * @return 职业信息站-学生选择记录
     */
    @Override
    public List<GkzhZyxxzStudentChoice> selectGkzhZyxxzStudentChoiceList(GkzhZyxxzStudentChoice gkzhZyxxzStudentChoice)
    {
        return gkzhZyxxzStudentChoiceMapper.selectGkzhZyxxzStudentChoiceList(gkzhZyxxzStudentChoice);
    }

    /**
     * 新增职业信息站-学生选择记录
     * 
     * @param gkzhZyxxzStudentChoice 职业信息站-学生选择记录
     * @return 结果
     */
    @Override
    public int insertGkzhZyxxzStudentChoice(GkzhZyxxzStudentChoice gkzhZyxxzStudentChoice)
    {
        gkzhZyxxzStudentChoice.setCreateTime(DateUtils.getNowDate());
        return gkzhZyxxzStudentChoiceMapper.insertGkzhZyxxzStudentChoice(gkzhZyxxzStudentChoice);
    }

    /**
     * 修改职业信息站-学生选择记录
     * 
     * @param gkzhZyxxzStudentChoice 职业信息站-学生选择记录
     * @return 结果
     */
    @Override
    public int updateGkzhZyxxzStudentChoice(GkzhZyxxzStudentChoice gkzhZyxxzStudentChoice)
    {
        return gkzhZyxxzStudentChoiceMapper.updateGkzhZyxxzStudentChoice(gkzhZyxxzStudentChoice);
    }

    /**
     * 批量删除职业信息站-学生选择记录
     * 
     * @param choiceIds 需要删除的职业信息站-学生选择记录主键
     * @return 结果
     */
    @Override
    public int deleteGkzhZyxxzStudentChoiceByChoiceIds(Long[] choiceIds)
    {
        return gkzhZyxxzStudentChoiceMapper.deleteGkzhZyxxzStudentChoiceByChoiceIds(choiceIds);
    }

    /**
     * 删除职业信息站-学生选择记录信息
     * 
     * @param choiceId 职业信息站-学生选择记录主键
     * @return 结果
     */
    @Override
    public int deleteGkzhZyxxzStudentChoiceByChoiceId(Long choiceId)
    {
        return gkzhZyxxzStudentChoiceMapper.deleteGkzhZyxxzStudentChoiceByChoiceId(choiceId);
    }
}
