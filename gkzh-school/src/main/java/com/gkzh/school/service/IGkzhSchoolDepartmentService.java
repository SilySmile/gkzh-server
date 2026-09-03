package com.gkzh.school.service;

import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.vo.GkzhSchoolDepartmentVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IGkzhSchoolDepartmentService {
    GkzhSchoolDepartment selectDepartmentById(Long departmentId);
    List<GkzhSchoolDepartment> selectDepartmentList(GkzhSchoolDepartment dept);
    List<GkzhSchoolDepartment> selectDepartmentTreeBySchoolId(Long schoolId);
    int insertDepartment(GkzhSchoolDepartment dept);
    int updateDepartment(GkzhSchoolDepartment dept);
    int deleteDepartmentById(Long departmentId);
    int deleteDepartmentByIds(Long[] departmentIds);
    
    /**
     * 构建前端所需要树结构
     * 
     * @param departments 部门列表
     * @return 树结构列表
     */
    List<GkzhSchoolDepartment> buildDepartmentTree(List<GkzhSchoolDepartment> departments);
    
    /**
     * 根据ID查询所有子部门（正常状态）
     * 
     * @param departmentId 部门ID
     * @return 子部门数
     */
    int selectNormalChildrenDepartmentById(Long departmentId);
    
    /**
     * 是否存在部门子节点
     * 
     * @param departmentId 部门ID
     * @return 结果
     */
    boolean hasChildByDepartmentId(Long departmentId);
    
    /**
     * 校验部门名称是否唯一
     * 
     * @param department 部门信息
     * @return 结果
     */
    boolean checkDepartmentNameUnique(GkzhSchoolDepartment department);

    /**
     * 导入学生数据
     *
     * @param deptList 院系数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    public String importDept(List<GkzhSchoolDepartmentVO> deptList, Boolean isUpdateSupport, String operName);


    Map<Long, String> getDepartmentMajorsByIds(Set<Long> departmentIds);

    Map<Long, List<GkzhSchoolDepartment>> batchGetDepartmentsWithAncestors(List<Long> departmentIds);

    Map<Long, String> batchGetDepartmentFullPaths(List<Long> departmentIds);
} 