package com.gkzh.web.controller.school;

import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.service.IGkzhSchoolDepartmentService;
import com.gkzh.common.annotation.Log;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.enums.BusinessType;
import com.gkzh.school.vo.GkzhSchoolDepartmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/school/department")
public class GkzhSchoolDepartmentController extends BaseController {

    @Autowired
    private IGkzhSchoolDepartmentService service;

    /**
     * 获取部门树结构
     */
    @GetMapping("/tree/{schoolId}")
    public AjaxResult getDepartmentTree(@PathVariable Long schoolId) {
        List<GkzhSchoolDepartment> departments = service.selectDepartmentTreeBySchoolId(schoolId);
        return AjaxResult.success(service.buildDepartmentTree(departments));
    }

    /**
     * 获取部门列表
     */
    @GetMapping("/list")
    public AjaxResult list(GkzhSchoolDepartment department) {
        List<GkzhSchoolDepartment> list = service.selectDepartmentList(department);
        return AjaxResult.success(list);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @GetMapping("/{departmentId}")
    public AjaxResult getInfo(@PathVariable Long departmentId) {
        return AjaxResult.success(service.selectDepartmentById(departmentId));
    }

    /**
     * 新增部门
     */
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GkzhSchoolDepartment department) {
        if (!service.checkDepartmentNameUnique(department)) {
            return AjaxResult.error("新增部门'" + department.getTitle() + "'失败，部门名称已存在");
        }
        department.setCreateBy(getUsername());
        return toAjax(service.insertDepartment(department));
    }

    /**
     * 修改部门
     */
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GkzhSchoolDepartment department) {
        if (!service.checkDepartmentNameUnique(department)) {
            return AjaxResult.error("修改部门'" + department.getTitle() + "'失败，部门名称已存在");
        }
        department.setUpdateBy(getUsername());
        return toAjax(service.updateDepartment(department));
    }

    /**
     * 删除部门
     */
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{departmentId}")
    public AjaxResult remove(@PathVariable Long departmentId) {
        if (service.hasChildByDepartmentId(departmentId)) {
            return AjaxResult.error("存在下级,不允许删除");
        }
        if (service.selectNormalChildrenDepartmentById(departmentId) > 0) {
            return AjaxResult.error("存在下级,不允许删除");
        }
        return toAjax(service.deleteDepartmentById(departmentId));
    }

    /**
     * 获取部门下拉树列表
     */
    @GetMapping("/treeselect/{schoolId}")
    public AjaxResult treeselect(@PathVariable Long schoolId) {
        List<GkzhSchoolDepartment> departments = service.selectDepartmentTreeBySchoolId(schoolId);
        return AjaxResult.success(service.buildDepartmentTree(departments));
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<GkzhSchoolDepartmentVO> util = new ExcelUtil<GkzhSchoolDepartmentVO>(GkzhSchoolDepartmentVO.class);
        util.importTemplateExcel(response, "院系数据");
    }

    @Log(title = "院系管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('school:department:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<GkzhSchoolDepartmentVO> util = new ExcelUtil<GkzhSchoolDepartmentVO>(GkzhSchoolDepartmentVO.class);
        List<GkzhSchoolDepartmentVO> deptList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = service.importDept(deptList, updateSupport, operName);
        return success(message);
    }
} 