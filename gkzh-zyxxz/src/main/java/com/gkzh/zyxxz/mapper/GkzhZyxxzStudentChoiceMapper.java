package com.gkzh.zyxxz.mapper;

import java.util.List;
import com.gkzh.zyxxz.domain.GkzhZyxxzStudentChoice;

/**
 * 职业信息站-学生选择记录Mapper接口
 * 
 * @author gkzh
 * @date 2026-06-04
 */
public interface GkzhZyxxzStudentChoiceMapper 
{
    /**
     * 查询职业信息站-学生选择记录
     * 
     * @param choiceId 职业信息站-学生选择记录主键
     * @return 职业信息站-学生选择记录
     */
    public GkzhZyxxzStudentChoice selectGkzhZyxxzStudentChoiceByChoiceId(Long choiceId);

    /**
     * 查询职业信息站-学生选择记录列表
     * 
     * @param gkzhZyxxzStudentChoice 职业信息站-学生选择记录
     * @return 职业信息站-学生选择记录集合
     */
    public List<GkzhZyxxzStudentChoice> selectGkzhZyxxzStudentChoiceList(GkzhZyxxzStudentChoice gkzhZyxxzStudentChoice);

    /**
     * 新增职业信息站-学生选择记录
     * 
     * @param gkzhZyxxzStudentChoice 职业信息站-学生选择记录
     * @return 结果
     */
    public int insertGkzhZyxxzStudentChoice(GkzhZyxxzStudentChoice gkzhZyxxzStudentChoice);

    /**
     * 修改职业信息站-学生选择记录
     * 
     * @param gkzhZyxxzStudentChoice 职业信息站-学生选择记录
     * @return 结果
     */
    public int updateGkzhZyxxzStudentChoice(GkzhZyxxzStudentChoice gkzhZyxxzStudentChoice);

    /**
     * 删除职业信息站-学生选择记录
     * 
     * @param choiceId 职业信息站-学生选择记录主键
     * @return 结果
     */
    public int deleteGkzhZyxxzStudentChoiceByChoiceId(Long choiceId);

    /**
     * 批量删除职业信息站-学生选择记录
     * 
     * @param choiceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGkzhZyxxzStudentChoiceByChoiceIds(Long[] choiceIds);
}
