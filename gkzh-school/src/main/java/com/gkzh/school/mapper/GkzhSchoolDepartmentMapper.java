package com.gkzh.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface GkzhSchoolDepartmentMapper extends BaseMapper<GkzhSchoolDepartment> {
    GkzhSchoolDepartment selectDepartmentById(Long departmentId);
    List<GkzhSchoolDepartment> selectDepartmentList(GkzhSchoolDepartment dept);
    List<GkzhSchoolDepartment> selectDepartmentTreeBySchoolId(Long schoolId);
    int insertDepartment(GkzhSchoolDepartment dept);
    int updateDepartment(GkzhSchoolDepartment dept);
    int deleteDepartmentById(Long departmentId);
    int deleteDepartmentByIds(Long[] departmentIds);
    
    /**
     * 根据ID查询所有子部门
     * 
     * @param departmentId 部门ID
     * @return 部门列表
     */
    List<GkzhSchoolDepartment> selectChildrenDepartmentById(Long departmentId);
    
    /**
     * 根据ID查询所有子部门（正常状态）
     * 
     * @param departmentId 部门ID
     * @return 子部门数
     */
    int selectNormalChildrenDepartmentById(Long departmentId);
    
    /**
     * 是否存在子节点
     * 
     * @param departmentId 部门ID
     * @return 结果
     */
    int hasChildByDepartmentId(Long departmentId);
    
    /**
     * 校验部门名称是否唯一
     * 
     * @param title 部门名称
     * @param parentId 父部门ID
     * @param schoolId 学校ID
     * @return 结果
     */
    GkzhSchoolDepartment checkDepartmentNameUnique(@Param("title") String title, @Param("parentId") Long parentId, @Param("schoolId") Long schoolId);
    
    /**
     * 修改子元素关系
     * 
     * @param departments 子元素
     * @return 结果
     */
    int updateDepartmentChildren(List<GkzhSchoolDepartment> departments);
    
    /**
     * 修改该部门的父级部门状态
     * 
     * @param departmentIds 部门ID数组
     * @return 结果
     */
    int updateDepartmentStatusNormal(Long[] departmentIds);
} 