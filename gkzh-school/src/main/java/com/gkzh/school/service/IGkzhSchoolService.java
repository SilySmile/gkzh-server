package com.gkzh.school.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gkzh.school.domain.GkzhSchool;

/**
 * 学校Service接口
 * 
 * @author gkzh
 * @date 2025-06-19
 */
public interface IGkzhSchoolService 
{
    /**
     * 查询学校
     * 
     * @param schoolId 学校主键
     * @return 学校
     */
    public GkzhSchool selectGkzhSchoolBySchoolId(Long schoolId);

    /**
     * 查询学校列表
     * 
     * @param gkzhSchool 学校
     * @return 学校集合
     */
    public List<GkzhSchool> selectGkzhSchoolList(GkzhSchool gkzhSchool);

    /**
     * 新增学校
     * 
     * @param gkzhSchool 学校
     * @return 结果
     */
    public int insertGkzhSchool(GkzhSchool gkzhSchool);

    /**
     * 修改学校
     * 
     * @param gkzhSchool 学校
     * @return 结果
     */
    public int updateGkzhSchool(GkzhSchool gkzhSchool);

    /**
     * 批量删除学校
     * 
     * @param schoolIds 需要删除的学校主键集合
     * @return 结果
     */
    public int deleteGkzhSchoolBySchoolIds(Long[] schoolIds);

    /**
     * 删除学校信息
     * 
     * @param schoolId 学校主键
     * @return 结果
     */
    public int deleteGkzhSchoolBySchoolId(Long schoolId);


    Map<Long, String> getSchoolNamesByDepartmentIds(Set<Long> departmentIds);
}
