package com.gkzh.school.utils;

import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 部门祖级路径工具类
 * 用于初始化现有部门数据的ancestors字段
 * 
 * @author gkzh
 */
@Component
public class DepartmentAncestorsUtil {

    @Autowired
    private GkzhSchoolDepartmentMapper departmentMapper;

    /**
     * 初始化指定学校的所有部门ancestors字段
     * 
     * @param schoolId 学校ID
     */
    public void initAncestorsBySchoolId(Long schoolId) {
        // 获取该学校的所有部门
        GkzhSchoolDepartment query = new GkzhSchoolDepartment();
        query.setSchoolId(schoolId);
        List<GkzhSchoolDepartment> departments = departmentMapper.selectDepartmentList(query);
        
        // 递归更新每个部门的ancestors
        for (GkzhSchoolDepartment dept : departments) {
            updateDepartmentAncestors(dept, departments);
        }
    }

    /**
     * 递归更新部门的ancestors字段
     * 
     * @param dept 当前部门
     * @param allDepts 所有部门列表
     */
    private void updateDepartmentAncestors(GkzhSchoolDepartment dept, List<GkzhSchoolDepartment> allDepts) {
        if (dept.getParentId() == null || dept.getParentId() == 0L) {
            // 根部门
            dept.setAncestors("0");
        } else {
            // 查找父部门
            GkzhSchoolDepartment parentDept = findDepartmentById(dept.getParentId(), allDepts);
            if (parentDept != null) {
                // 如果父部门的ancestors还没有设置，先设置父部门
                if (parentDept.getAncestors() == null || parentDept.getAncestors().isEmpty()) {
                    updateDepartmentAncestors(parentDept, allDepts);
                }
                // 设置当前部门的ancestors
                dept.setAncestors(parentDept.getAncestors() + "," + parentDept.getDepartmentId());
            } else {
                // 父部门不存在，设置为根部门
                dept.setAncestors("0");
            }
        }
        
        // 更新数据库
        departmentMapper.updateDepartment(dept);
    }

    /**
     * 在部门列表中查找指定ID的部门
     * 
     * @param departmentId 部门ID
     * @param departments 部门列表
     * @return 找到的部门，如果没找到返回null
     */
    private GkzhSchoolDepartment findDepartmentById(Long departmentId, List<GkzhSchoolDepartment> departments) {
        for (GkzhSchoolDepartment dept : departments) {
            if (dept.getDepartmentId().equals(departmentId)) {
                return dept;
            }
        }
        return null;
    }

    /**
     * 验证部门ancestors字段的正确性
     * 
     * @param schoolId 学校ID
     * @return 验证结果
     */
    public boolean validateAncestors(Long schoolId) {
        GkzhSchoolDepartment query = new GkzhSchoolDepartment();
        query.setSchoolId(schoolId);
        List<GkzhSchoolDepartment> departments = departmentMapper.selectDepartmentList(query);
        
        for (GkzhSchoolDepartment dept : departments) {
            if (dept.getAncestors() == null || dept.getAncestors().isEmpty()) {
                return false;
            }
            
            // 验证ancestors格式是否正确
            String[] ancestorIds = dept.getAncestors().split(",");
            for (String ancestorId : ancestorIds) {
                if (!ancestorId.equals("0") && !isValidDepartmentId(Long.valueOf(ancestorId), departments)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查部门ID是否在部门列表中存在
     * 
     * @param departmentId 部门ID
     * @param departments 部门列表
     * @return 是否存在
     */
    private boolean isValidDepartmentId(Long departmentId, List<GkzhSchoolDepartment> departments) {
        for (GkzhSchoolDepartment dept : departments) {
            if (dept.getDepartmentId().equals(departmentId)) {
                return true;
            }
        }
        return false;
    }
} 