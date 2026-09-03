package com.gkzh.school.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhStudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.service.IGkzhSchoolService;

/**
 * 学校Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-19
 */
@Service
public class GkzhSchoolServiceImpl implements IGkzhSchoolService 
{
    @Autowired
    private GkzhSchoolMapper gkzhSchoolMapper;

    @Autowired
    private GkzhSchoolDepartmentMapper gkzhSchoolDepartmentMapper;

    @Autowired
    private GkzhStudentMapper gkzhStudentMapper;

    /**
     * 查询学校
     * 
     * @param schoolId 学校主键
     * @return 学校
     */
    @Override
    public GkzhSchool selectGkzhSchoolBySchoolId(Long schoolId)
    {
        return gkzhSchoolMapper.selectGkzhSchoolBySchoolId(schoolId);
    }

    /**
     * 查询学校列表
     * 
     * @param gkzhSchool 学校
     * @return 学校
     */
    @Override
    public List<GkzhSchool> selectGkzhSchoolList(GkzhSchool gkzhSchool)
    {
        return gkzhSchoolMapper.selectGkzhSchoolList(gkzhSchool);
    }

    /**
     * 新增学校
     * 
     * @param gkzhSchool 学校
     * @return 结果
     */
    @Override
    public int insertGkzhSchool(GkzhSchool gkzhSchool)
    {
        gkzhSchool.setCreateTime(DateUtils.getNowDate());
        return gkzhSchoolMapper.insertGkzhSchool(gkzhSchool);
    }

    /**
     * 修改学校
     * 
     * @param gkzhSchool 学校
     * @return 结果
     */
    @Override
    public int updateGkzhSchool(GkzhSchool gkzhSchool)
    {
        gkzhSchool.setUpdateTime(DateUtils.getNowDate());
        return gkzhSchoolMapper.updateGkzhSchool(gkzhSchool);
    }

    /**
     * 批量删除学校
     * 
     * @param schoolIds 需要删除的学校主键
     * @return 结果
     */
    @Override
    public int deleteGkzhSchoolBySchoolIds(Long[] schoolIds)
    {
        //检查要删除的学校下是否有部门或学生
        QueryWrapper<GkzhSchoolDepartment> query = Wrappers.query();
        if(schoolIds.length > 0){
            query.in("school_id", schoolIds);
            Long count = gkzhSchoolDepartmentMapper.selectCount(query);
            if(count > 0){
                throw new RuntimeException("该学校下有院系，请先删除院系");
            }
            QueryWrapper<GkzhStudent> queryStu = Wrappers.query();
            queryStu.in("school_id", schoolIds);
            Long studentCount = gkzhStudentMapper.selectCount(queryStu);
            if(studentCount > 0){
                throw new RuntimeException("该学校下有学生，请先删除学生");
            }
        }
        return gkzhSchoolMapper.deleteGkzhSchoolBySchoolIds(schoolIds);
    }

    /**
     * 删除学校信息
     * 
     * @param schoolId 学校主键
     * @return 结果
     */
    @Override
    public int deleteGkzhSchoolBySchoolId(Long schoolId)
    {
        return gkzhSchoolMapper.deleteGkzhSchoolBySchoolId(schoolId);
    }

    @Override
    public Map<Long, String> getSchoolNamesByDepartmentIds(Set<Long> departmentIds) {
        List<GkzhSchool> gkzhSchools = gkzhSchoolMapper.selectSchoolNamesByDepartmentIds(departmentIds);

        return gkzhSchools.stream()
                .collect(java.util.stream.Collectors.toMap(GkzhSchool::getSchoolId, GkzhSchool::getTitle));

    }
}
