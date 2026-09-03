package com.gkzh.zytj.service.impl;

import java.util.List;
import com.gkzh.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.zytj.mapper.GkzhMbtiStudentChoiceMapper;
import com.gkzh.zytj.domain.GkzhMbtiStudentChoice;
import com.gkzh.zytj.service.IGkzhMbtiStudentChoiceService;

/**
 * 职愿探究-学生选择记录Service业务层处理
 * 
 * @author gkzh
 * @date 2026-06-02
 */
@Service
public class GkzhMbtiStudentChoiceServiceImpl implements IGkzhMbtiStudentChoiceService 
{
    @Autowired
    private GkzhMbtiStudentChoiceMapper gkzhMbtiStudentChoiceMapper;

    /**
     * 查询职愿探究-学生选择记录
     * 
     * @param choiceId 职愿探究-学生选择记录主键
     * @return 职愿探究-学生选择记录
     */
    @Override
    public GkzhMbtiStudentChoice selectGkzhMbtiStudentChoiceByChoiceId(Long choiceId)
    {
        return gkzhMbtiStudentChoiceMapper.selectGkzhMbtiStudentChoiceByChoiceId(choiceId);
    }

    /**
     * 查询职愿探究-学生选择记录列表
     * 
     * @param gkzhMbtiStudentChoice 职愿探究-学生选择记录
     * @return 职愿探究-学生选择记录
     */
    @Override
    public List<GkzhMbtiStudentChoice> selectGkzhMbtiStudentChoiceList(GkzhMbtiStudentChoice gkzhMbtiStudentChoice)
    {
        return gkzhMbtiStudentChoiceMapper.selectGkzhMbtiStudentChoiceList(gkzhMbtiStudentChoice);
    }

    /**
     * 新增职愿探究-学生选择记录
     * 
     * @param gkzhMbtiStudentChoice 职愿探究-学生选择记录
     * @return 结果
     */
    @Override
    public int insertGkzhMbtiStudentChoice(GkzhMbtiStudentChoice gkzhMbtiStudentChoice)
    {
        gkzhMbtiStudentChoice.setCreateTime(DateUtils.getNowDate());
        return gkzhMbtiStudentChoiceMapper.insertGkzhMbtiStudentChoice(gkzhMbtiStudentChoice);
    }

    /**
     * 修改职愿探究-学生选择记录
     * 
     * @param gkzhMbtiStudentChoice 职愿探究-学生选择记录
     * @return 结果
     */
    @Override
    public int updateGkzhMbtiStudentChoice(GkzhMbtiStudentChoice gkzhMbtiStudentChoice)
    {
        return gkzhMbtiStudentChoiceMapper.updateGkzhMbtiStudentChoice(gkzhMbtiStudentChoice);
    }

    /**
     * 批量删除职愿探究-学生选择记录
     * 
     * @param choiceIds 需要删除的职愿探究-学生选择记录主键
     * @return 结果
     */
    @Override
    public int deleteGkzhMbtiStudentChoiceByChoiceIds(Long[] choiceIds)
    {
        return gkzhMbtiStudentChoiceMapper.deleteGkzhMbtiStudentChoiceByChoiceIds(choiceIds);
    }

    /**
     * 删除职愿探究-学生选择记录信息
     * 
     * @param choiceId 职愿探究-学生选择记录主键
     * @return 结果
     */
    @Override
    public int deleteGkzhMbtiStudentChoiceByChoiceId(Long choiceId)
    {
        return gkzhMbtiStudentChoiceMapper.deleteGkzhMbtiStudentChoiceByChoiceId(choiceId);
    }
}
