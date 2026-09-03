package com.gkzh.web.controller.school;

import java.util.List;

import com.gkzh.common.core.domain.entity.SysUser;
import com.gkzh.school.dto.GkzhStudentDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gkzh.common.annotation.Log;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.enums.BusinessType;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.service.IGkzhStudentService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.SecurityUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 学生Controller
 * 
 * @author gkzh
 * @date 2025-06-19
 */
@RestController
@RequestMapping("/school/student")
public class GkzhStudentController extends BaseController {
    @Autowired
    private IGkzhStudentService gkzhStudentService;

    /**
     * 查询学生列表
     */
    @PreAuthorize("@ss.hasPermi('school:student:list')")
    @GetMapping("/list")
    public TableDataInfo list(GkzhStudent gkzhStudent) {
        startPage();
        List<GkzhStudent> list = gkzhStudentService.selectGkzhStudentList(gkzhStudent);
        return getDataTable(list);
    }

    /**
     * 导出学生列表
     */
    @PreAuthorize("@ss.hasPermi('school:student:export')")
    @Log(title = "学生", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GkzhStudent gkzhStudent) {
        List<GkzhStudent> list = gkzhStudentService.selectGkzhStudentList(gkzhStudent);
        ExcelUtil<GkzhStudent> util = new ExcelUtil<GkzhStudent>(GkzhStudent.class);
        util.exportExcel(response,list, "学生数据");
    }

    /**
     * 获取学生详细信息
     */
    @PreAuthorize("@ss.hasPermi('school:student:query')")
    @GetMapping(value = "/{studentId}")
    public AjaxResult getInfo(@PathVariable("studentId") Long studentId) {
        return success(gkzhStudentService.selectGkzhStudentByStudentId(studentId));
    }

    /**
     * 新增学生
     */
    @PreAuthorize("@ss.hasPermi('school:student:add')")
    @Log(title = "学生", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GkzhStudent gkzhStudent) {
        gkzhStudent.setCreateBy(getUsername());
        return toAjax(gkzhStudentService.insertGkzhStudent(gkzhStudent));
    }

    /**
     * 修改学生
     */
    @PreAuthorize("@ss.hasPermi('school:student:edit')")
    @Log(title = "学生", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GkzhStudent gkzhStudent) {
        gkzhStudent.setUpdateBy(getUsername());
        return toAjax(gkzhStudentService.updateGkzhStudent(gkzhStudent));
    }

    /** 将已注册学生的小程序登录密码重置为 123456。 */
    @PreAuthorize("@ss.hasPermi('school:student:edit')")
    @Log(title = "学生登录密码", businessType = BusinessType.UPDATE)
    @PutMapping("/{studentId}/reset-password")
    public AjaxResult resetPassword(@PathVariable Long studentId) {
        GkzhStudent student = gkzhStudentService.selectGkzhStudentByStudentId(studentId);
        if (student == null) throw new ServiceException("学生不存在");
        if (!Integer.valueOf(1).equals(student.getRegistered())) return error("该学生尚未注册，无需重置密码");
        student.setPassword(SecurityUtils.encryptPassword("123456"));
        student.setUpdateBy(getUsername());
        return toAjax(gkzhStudentService.updateGkzhStudent(student));
    }

    /**
     * 删除学生
     */
    @PreAuthorize("@ss.hasPermi('school:student:remove')")
    @Log(title = "学生", businessType = BusinessType.DELETE)
	@DeleteMapping("/{studentIds}")
    public AjaxResult remove(@PathVariable Long[] studentIds) {
        return toAjax(gkzhStudentService.deleteGkzhStudentByStudentIds(studentIds));
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<GkzhStudentDTO> util = new ExcelUtil<GkzhStudentDTO>(GkzhStudentDTO.class);
        util.importTemplateExcel(response, "学生数据");
    }

    @Log(title = "学生管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('school:student:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<GkzhStudent> util = new ExcelUtil<GkzhStudent>(GkzhStudent.class);
        List<GkzhStudent> stuList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = gkzhStudentService.importStudent(stuList, updateSupport, operName);
        return success(message);
    }


}
