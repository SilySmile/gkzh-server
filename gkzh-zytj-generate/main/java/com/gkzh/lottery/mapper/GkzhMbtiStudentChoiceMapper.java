package com.gkzh.lottery.mapper;

import java.util.List;
import com.gkzh.lottery.domain.GkzhMbtiStudentChoice;

/**
 * 职愿探究-学生选择记录Mapper接口
 * 
 * @author gkzh
 * @date 2026-06-02
 */
public interface GkzhMbtiStudentChoiceMapper 
{
    /**
     * 查询职愿探究-学生选择记录
     * 
     * @param choiceId 职愿探究-学生选择记录主键
     * @return 职愿探究-学生选择记录
     */
    public GkzhMbtiStudentChoice selectGkzhMbtiStudentChoiceByChoiceId(Long choiceId);

    /**
     * 查询职愿探究-学生选择记录列表
     * 
     * @param gkzhMbtiStudentChoice 职愿探究-学生选择记录
     * @return 职愿探究-学生选择记录集合
     */
    public List<GkzhMbtiStudentChoice> selectGkzhMbtiStudentChoiceList(GkzhMbtiStudentChoice gkzhMbtiStudentChoice);

    /**
     * 新增职愿探究-学生选择记录
     * 
     * @param gkzhMbtiStudentChoice 职愿探究-学生选择记录
     * @return 结果
     */
    public int insertGkzhMbtiStudentChoice(GkzhMbtiStudentChoice gkzhMbtiStudentChoice);

    /**
     * 修改职愿探究-学生选择记录
     * 
     * @param gkzhMbtiStudentChoice 职愿探究-学生选择记录
     * @return 结果
     */
    public int updateGkzhMbtiStudentChoice(GkzhMbtiStudentChoice gkzhMbtiStudentChoice);

    /**
     * 删除职愿探究-学生选择记录
     * 
     * @param choiceId 职愿探究-学生选择记录主键
     * @return 结果
     */
    public int deleteGkzhMbtiStudentChoiceByChoiceId(Long choiceId);

    /**
     * 批量删除职愿探究-学生选择记录
     * 
     * @param choiceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGkzhMbtiStudentChoiceByChoiceIds(Long[] choiceIds);
}
