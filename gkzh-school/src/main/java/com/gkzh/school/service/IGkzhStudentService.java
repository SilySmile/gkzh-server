package com.gkzh.school.service;

import java.util.List;

import com.gkzh.common.core.domain.entity.SysUser;
import com.gkzh.school.domain.GkzhStudent;

/**
 * 学生Service接口
 * 
 * @author gkzh
 * @date 2025-06-19
 */
public interface IGkzhStudentService {
    /**
     * 查询学生
     * 
     * @param studentId 学生主键
     * @return 学生
     */
    public GkzhStudent selectGkzhStudentByStudentId(Long studentId);

    /**
     * 查询学生列表
     * 
     * @param gkzhStudent 学生
     * @return 学生集合
     */
    public List<GkzhStudent> selectGkzhStudentList(GkzhStudent gkzhStudent);

    /**
     * 新增学生
     * 
     * @param gkzhStudent 学生
     * @return 结果
     */
    public int insertGkzhStudent(GkzhStudent gkzhStudent);

    /**
     * 修改学生
     * 
     * @param gkzhStudent 学生
     * @return 结果
     */
    public int updateGkzhStudent(GkzhStudent gkzhStudent);

    /**
     * 批量删除学生
     * 
     * @param studentIds 需要删除的学生主键集合
     * @return 结果
     */
    public int deleteGkzhStudentByStudentIds(Long[] studentIds);

    /**
     * 删除学生信息
     * 
     * @param studentId 学生主键
     * @return 结果
     */
    public int deleteGkzhStudentByStudentId(Long studentId);

    /**
     * 根据签到信息查询学生
     * 
     * @param gkzhStudent 学生信息
     * @return 学生
     */
    public GkzhStudent selectGkzhStudentByCheckinInfo(GkzhStudent gkzhStudent);

    /**
     * 导入学生数据
     *
     * @param stuList 用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    public String importStudent(List<GkzhStudent> stuList, Boolean isUpdateSupport, String operName);
} 