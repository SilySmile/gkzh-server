package com.gkzh.school.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.domain.GkzhStudent;
import org.apache.ibatis.annotations.Param;

/**
 * 学校Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-19
 */
public interface GkzhSchoolMapper extends BaseMapper<GkzhSchool>
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
     * 删除学校
     * 
     * @param schoolId 学校主键
     * @return 结果
     */
    public int deleteGkzhSchoolBySchoolId(Long schoolId);

    /**
     * 批量删除学校
     * 
     * @param schoolIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGkzhSchoolBySchoolIds(Long[] schoolIds);

    List<GkzhSchool> selectSchoolNamesByDepartmentIds(@Param("departmentIds") Set<Long> departmentIds);
}
