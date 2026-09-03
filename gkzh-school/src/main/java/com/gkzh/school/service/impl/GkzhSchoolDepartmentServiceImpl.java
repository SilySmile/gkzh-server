package com.gkzh.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gkzh.common.core.domain.entity.SysUser;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.common.utils.SecurityUtils;
import com.gkzh.common.utils.bean.BeanValidators;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.school.service.IGkzhSchoolDepartmentService;
import com.gkzh.common.constant.UserConstants;
import com.gkzh.common.core.text.Convert;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.StringUtils;
import com.gkzh.school.vo.GkzhSchoolDepartmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Service
public class GkzhSchoolDepartmentServiceImpl implements IGkzhSchoolDepartmentService {
    @Autowired
    private GkzhSchoolDepartmentMapper mapper;
    @Autowired
    private GkzhSchoolMapper gkzhSchoolMapper;
    @Autowired
    private GkzhStudentMapper gkzhStudentMapper;

    @Override
    public GkzhSchoolDepartment selectDepartmentById(Long departmentId) {
        return mapper.selectDepartmentById(departmentId);
    }

    @Override
    public List<GkzhSchoolDepartment> selectDepartmentList(GkzhSchoolDepartment dept) {
        return mapper.selectDepartmentList(dept);
    }

    @Override
    public List<GkzhSchoolDepartment> selectDepartmentTreeBySchoolId(Long schoolId) {
        return mapper.selectDepartmentTreeBySchoolId(schoolId);
    }

    @Override
    public List<GkzhSchoolDepartment> buildDepartmentTree(List<GkzhSchoolDepartment> departments) {
        List<GkzhSchoolDepartment> returnList = new ArrayList<GkzhSchoolDepartment>();
        List<Long> tempList = departments.stream().map(GkzhSchoolDepartment::getDepartmentId).collect(Collectors.toList());
        for (GkzhSchoolDepartment department : departments) {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(department.getParentId())) {
                recursionFn(departments, department);
                returnList.add(department);
            }
        }
        if (returnList.isEmpty()) {
            returnList = departments;
        }
        return returnList;
    }

    @Override
    public int selectNormalChildrenDepartmentById(Long departmentId) {
        return mapper.selectNormalChildrenDepartmentById(departmentId);
    }

    @Override
    public boolean hasChildByDepartmentId(Long departmentId) {
        int result = mapper.hasChildByDepartmentId(departmentId);
        return result > 0;
    }

    @Override
    public boolean checkDepartmentNameUnique(GkzhSchoolDepartment department) {
        Long departmentId = StringUtils.isNull(department.getDepartmentId()) ? -1L : department.getDepartmentId();
        GkzhSchoolDepartment info = mapper.checkDepartmentNameUnique(department.getTitle(), department.getParentId(), department.getSchoolId());
        if (StringUtils.isNotNull(info) && info.getDepartmentId().longValue() != departmentId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

      @Override
    public int insertDepartment(GkzhSchoolDepartment dept) {
        if(dept.getParentId() == null || dept.getParentId() == -1) {
            dept.setParentId(0L);
        }
        
        // 设置ancestors字段
        if (dept.getParentId() == 0L) {
            dept.setAncestors("0");
        } else {
            GkzhSchoolDepartment parentDept = mapper.selectDepartmentById(dept.getParentId());
            if (parentDept == null) {
                throw new ServiceException("父部门不存在");
            }
            // 如果父节点不为正常状态,则不允许新增子节点
            if (!UserConstants.DEPT_NORMAL.equals(parentDept.getStatus())) {
                throw new ServiceException("部门停用，不允许新增");
            }
            dept.setAncestors(parentDept.getAncestors() + "," + dept.getParentId());
        }
        dept.setCreateTime(DateUtils.getNowDate());
        dept.setDelFlag("0");
        return mapper.insertDepartment(dept);
    }

    @Override
    public int updateDepartment(GkzhSchoolDepartment dept) {
        if(dept.getParentId() == null || dept.getParentId() == -1) {
            dept.setParentId(0L);
        }
        dept.setUpdateTime(DateUtils.getNowDate());
        GkzhSchoolDepartment newParentDept = mapper.selectDepartmentById(dept.getParentId());
        GkzhSchoolDepartment oldDept = mapper.selectDepartmentById(dept.getDepartmentId());
        if (StringUtils.isNotNull(newParentDept) && StringUtils.isNotNull(oldDept)) {
            String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getDepartmentId();
            String oldAncestors = oldDept.getAncestors();
            dept.setAncestors(newAncestors);
            updateDepartmentChildren(dept.getDepartmentId(), newAncestors, oldAncestors);
        }
        
        dept.setDelFlag("0");
        int result = mapper.updateDepartment(dept);
        if (UserConstants.DEPT_NORMAL.equals(dept.getStatus()) && StringUtils.isNotEmpty(dept.getAncestors())
                && !StringUtils.equals("0", dept.getAncestors())) {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentDepartmentStatusNormal(dept);
        }
        return result;
    }

    @Override
    public int deleteDepartmentById(Long departmentId) {
        //判断是否有子元素存在
        if(mapper.hasChildByDepartmentId(departmentId) > 0){
            throw new ServiceException("存在专业,不允许删除");
        }
        //判断是否有学生存在
        QueryWrapper<GkzhStudent> query = Wrappers.query();
        if(departmentId != null){
            query.eq("department_id", departmentId);
            Long count = gkzhStudentMapper.selectCount(query);
            if(count > 0){
                throw new ServiceException("存在学生,不允许删除");
            }
        }
        return mapper.deleteDepartmentById(departmentId);
    }

    @Override
    public int deleteDepartmentByIds(Long[] departmentIds) {
        return mapper.deleteDepartmentByIds(departmentIds);
    }

    /**
     * 修改子元素关系
     * 
     * @param departmentId 被修改的部门ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    public void updateDepartmentChildren(Long departmentId, String newAncestors, String oldAncestors) {
        List<GkzhSchoolDepartment> children = mapper.selectChildrenDepartmentById(departmentId);
        for (GkzhSchoolDepartment child : children) {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (children.size() > 0) {
            mapper.updateDepartmentChildren(children);
        }
    }

    /**
     * 修改该部门的父级部门状态
     * 
     * @param department 当前部门
     */
    private void updateParentDepartmentStatusNormal(GkzhSchoolDepartment department) {
        String ancestors = department.getAncestors();
        Long[] departmentIds = Convert.toLongArray(ancestors);
        mapper.updateDepartmentStatusNormal(departmentIds);
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<GkzhSchoolDepartment> list, GkzhSchoolDepartment t) {
        // 得到子节点列表
        List<GkzhSchoolDepartment> childList = getChildList(list, t);
        t.setChildren(childList);
        for (GkzhSchoolDepartment tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<GkzhSchoolDepartment> getChildList(List<GkzhSchoolDepartment> list, GkzhSchoolDepartment t) {
        List<GkzhSchoolDepartment> tlist = new ArrayList<GkzhSchoolDepartment>();
        Iterator<GkzhSchoolDepartment> it = list.iterator();
        while (it.hasNext()) {
            GkzhSchoolDepartment n = (GkzhSchoolDepartment) it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getDepartmentId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<GkzhSchoolDepartment> list, GkzhSchoolDepartment t) {
        return getChildList(list, t).size() > 0;
    }
    @Override
    public String importDept(List<GkzhSchoolDepartmentVO> deptList, Boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(deptList) || deptList.size() == 0) {
            throw new ServiceException("导入学生数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        for (GkzhSchoolDepartmentVO deptVO : deptList) {
            try {
                // 1. 根据学校名称查找学校ID
                Long schoolId = getSchoolIdByName(deptVO.getSchoolName());
                if (schoolId == null) {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、数据（学校：" + deptVO.getSchoolName() +
                            "，院系：" + deptVO.getDepartmentName() +
                            "，专业：" + deptVO.getMajorName() + "）学校不存在");
                    continue;
                }

                // 2. 处理院系（一级部门）
                GkzhSchoolDepartment collegeDept = null;
                if (StringUtils.isNotEmpty(deptVO.getDepartmentName())) {
                    collegeDept = getDepartmentByNameAndSchoolId(deptVO.getDepartmentName(), schoolId);
                    if (collegeDept == null) {
                        // 新增院系
                        GkzhSchoolDepartment newCollegeDept = new GkzhSchoolDepartment();
                        newCollegeDept.setTitle(deptVO.getDepartmentName());
                        newCollegeDept.setSchoolId(schoolId);
                        newCollegeDept.setParentId(0L);
                        newCollegeDept.setAncestors("0");
                        newCollegeDept.setSortNum(0);
                        newCollegeDept.setStatus("0");
                        newCollegeDept.setDelFlag("0");
                        newCollegeDept.setCreateBy(operName);
                        newCollegeDept.setCreateTime(DateUtils.getNowDate());
                        mapper.insertDepartment(newCollegeDept);
                        collegeDept = newCollegeDept;
                        successNum++;
                        successMsg.append("<br/>" + successNum + "、院系 " + deptVO.getDepartmentName() + " 导入成功");
                    } else if (isUpdateSupport) {
                        // 更新院系信息（如果需要）
                        collegeDept.setUpdateBy(operName);
                        collegeDept.setUpdateTime(DateUtils.getNowDate());
                        mapper.updateDepartment(collegeDept);
                        successNum++;
                        successMsg.append("<br/>" + successNum + "、院系 " + deptVO.getDepartmentName() + " 更新成功");
                    }
                }

                // 3. 处理专业（二级部门）
                if (StringUtils.isNotEmpty(deptVO.getMajorName()) && collegeDept != null) {
                    GkzhSchoolDepartment majorDept = getDepartmentByNameAndParentId(deptVO.getMajorName(), collegeDept.getDepartmentId());
                    if (majorDept == null) {
                        // 新增专业
                        GkzhSchoolDepartment newMajorDept = new GkzhSchoolDepartment();
                        newMajorDept.setTitle(deptVO.getMajorName());
                        newMajorDept.setSchoolId(schoolId);
                        newMajorDept.setParentId(collegeDept.getDepartmentId());
                        newMajorDept.setAncestors("0," + collegeDept.getDepartmentId());
                        newMajorDept.setSortNum(0);
                        newMajorDept.setStatus("0");
                        newMajorDept.setDelFlag("0");
                        newMajorDept.setCreateBy(operName);
                        newMajorDept.setCreateTime(DateUtils.getNowDate());
                        mapper.insertDepartment(newMajorDept);
                        successNum++;
                        successMsg.append("<br/>" + successNum + "、专业 " + deptVO.getMajorName() + " 导入成功");
                    } else if (isUpdateSupport) {
                        // 更新专业信息（如果需要）
                        majorDept.setUpdateBy(operName);
                        majorDept.setUpdateTime(DateUtils.getNowDate());
                        mapper.updateDepartment(majorDept);
                        successNum++;
                        successMsg.append("<br/>" + successNum + "、专业 " + deptVO.getMajorName() + " 更新成功");
                    }
                } else if (StringUtils.isNotEmpty(deptVO.getMajorName()) && collegeDept == null) {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、专业 " + deptVO.getMajorName() + " 未找到对应院系");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、数据（学校：" + deptVO.getSchoolName() +
                        "，院系：" + deptVO.getDepartmentName() +
                        "，专业：" + deptVO.getMajorName() + "）导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }

        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    @Override
    public Map<Long, String> getDepartmentMajorsByIds(Set<Long> departmentIds) {
        QueryWrapper<GkzhSchoolDepartment> query = Wrappers.query();
        if(departmentIds != null && departmentIds.size() > 0){
            query.in("department_id", departmentIds);
        }
        List<GkzhSchoolDepartment> gkzhSchoolDepartments = mapper.selectList(query);
        HashMap<Long, String> ret = new HashMap<>();
        for (GkzhSchoolDepartment gkzhSchoolDepartment : gkzhSchoolDepartments) {
            ret.put(gkzhSchoolDepartment.getDepartmentId(), gkzhSchoolDepartment.getTitle());
        }
        return ret;
    }

    /**
     * 根据学校名称获取学校ID
     * @param schoolName 学校名称
     * @return 学校ID
     */
    private Long getSchoolIdByName(String schoolName) {
        QueryWrapper<GkzhSchool> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT school_id");
        queryWrapper.eq("title", schoolName);
        GkzhSchool school = gkzhSchoolMapper.selectOne(queryWrapper);
        return school != null ? school.getSchoolId() : null;
    }

    /**
     * 根据部门名称和学校ID获取部门
     * @param deptName 部门名称
     * @param schoolId 学校ID
     * @return 部门信息
     */
    private GkzhSchoolDepartment getDepartmentByNameAndSchoolId(String deptName, Long schoolId) {
        QueryWrapper<GkzhSchoolDepartment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title", deptName);
        queryWrapper.eq("school_id", schoolId);
        return mapper.selectOne(queryWrapper);
    }

    /**
     * 根据部门名称和父ID获取部门
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @return 部门信息
     */
    private GkzhSchoolDepartment getDepartmentByNameAndParentId(String deptName, Long parentId) {
        QueryWrapper<GkzhSchoolDepartment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title", deptName);
        queryWrapper.eq("parent_id", parentId);
        return mapper.selectOne(queryWrapper);
    }


    /**
     * 批量获取部门及其所有父级部门
     */
    public Map<Long, List<GkzhSchoolDepartment>> batchGetDepartmentsWithAncestors(List<Long> departmentIds) {
        if (CollectionUtils.isEmpty(departmentIds)) {
            return new HashMap<>();
        }

        // 1. 批量查询所有目标部门
        QueryWrapper<GkzhSchoolDepartment> query = Wrappers.query();
        query.in("department_id", departmentIds);
        List<GkzhSchoolDepartment> targetDepartments = mapper.selectList(query);
        if (CollectionUtils.isEmpty(targetDepartments)) {
            return new HashMap<>();
        }

        // 2. 收集所有需要查询的部门ID（包括ancestors中的ID）
        Set<Long> allDeptIds = new HashSet<>();
        Map<Long, Set<Long>> deptToAncestorIds = new HashMap<>();

        for (GkzhSchoolDepartment dept : targetDepartments) {
            Set<Long> ancestorIds = parseAncestorsToSet(dept.getAncestors());
            ancestorIds.add(dept.getDepartmentId()); // 包含自身

            deptToAncestorIds.put(dept.getDepartmentId(), ancestorIds);
            allDeptIds.addAll(ancestorIds);
        }

        // 3. 批量查询所有相关部门信息
        QueryWrapper<GkzhSchoolDepartment> queryAll = Wrappers.query();
        queryAll.in("department_id", allDeptIds);
        List<GkzhSchoolDepartment> allRelatedDepartments = mapper.selectList(queryAll);
        Map<Long, GkzhSchoolDepartment> deptMap = allRelatedDepartments.stream()
                .collect(Collectors.toMap(GkzhSchoolDepartment::getDepartmentId, Function.identity()));

        // 4. 构建结果
        Map<Long, List<GkzhSchoolDepartment>> result = new HashMap<>();
        for (Map.Entry<Long, Set<Long>> entry : deptToAncestorIds.entrySet()) {
            Long deptId = entry.getKey();
            Set<Long> ancestorIds = entry.getValue();

            List<GkzhSchoolDepartment> deptList = ancestorIds.stream()
                    .map(deptMap::get)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(dept ->
                            StringUtils.isBlank(dept.getAncestors()) ? 0 : dept.getAncestors().split(",").length
                    ))
                    .collect(Collectors.toList());

            result.put(deptId, deptList);
        }

        return result;
    }

    /**
     * 解析ancestors字符串为部门ID集合
     */
    private Set<Long> parseAncestorsToSet(String ancestors) {
        Set<Long> ids = new HashSet<>();
        if (StringUtils.isNotBlank(ancestors)) {
            String[] idArray = ancestors.split(",");
            for (String idStr : idArray) {
                if (StringUtils.isNotBlank(idStr)) {
                    try {
                        ids.add(Long.parseLong(idStr.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("部门ancestors字段包含无效的ID: {}", idStr);
                    }
                }
            }
        }
        return ids;
    }

    /**
     * 批量获取部门的完整路径
     */
    public Map<Long, String> batchGetDepartmentFullPaths(List<Long> departmentIds) {
        Map<Long, List<GkzhSchoolDepartment>> deptPaths = batchGetDepartmentsWithAncestors(departmentIds);

        return deptPaths.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(GkzhSchoolDepartment::getTitle)
                                .collect(Collectors.joining(" > "))
                ));
    }
}