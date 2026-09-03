package com.gkzh.web.controller.school;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.gkzh.common.utils.DateUtils;
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
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.service.IGkzhSchoolService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 学校Controller
 * 
 * @author gkzh
 * @date 2025-06-19
 */
@RestController
@RequestMapping("/school/school")
public class GkzhSchoolController extends BaseController
{
    @Autowired
    private IGkzhSchoolService gkzhSchoolService;

    /**
     * 查询学校列表
     */
    @PreAuthorize("@ss.hasPermi('school:school:list')")
    @GetMapping("/list")
    public TableDataInfo list(GkzhSchool gkzhSchool)
    {
        startPage();
        List<GkzhSchool> list = gkzhSchoolService.selectGkzhSchoolList(gkzhSchool);
        return getDataTable(list);
    }

    /**
     * 导出学校列表
     */
    @PreAuthorize("@ss.hasPermi('school:school:export')")
    @Log(title = "学校", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GkzhSchool gkzhSchool)
    {
        List<GkzhSchool> list = gkzhSchoolService.selectGkzhSchoolList(gkzhSchool);
        ExcelUtil<GkzhSchool> util = new ExcelUtil<GkzhSchool>(GkzhSchool.class);
        util.exportExcel(response, list, "学校数据");
    }

    /**
     * 获取学校详细信息
     */
    @PreAuthorize("@ss.hasPermi('school:school:query')")
    @GetMapping(value = "/{schoolId}")
    public AjaxResult getInfo(@PathVariable("schoolId") Long schoolId)
    {
        return success(gkzhSchoolService.selectGkzhSchoolBySchoolId(schoolId));
    }

    /**
     * 新增学校
     */
    @PreAuthorize("@ss.hasPermi('school:school:add')")
    @Log(title = "学校", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GkzhSchool gkzhSchool)
    {
        gkzhSchool.setCreateTime(DateUtils.getNowDate());
        gkzhSchool.setCreateBy(getUsername());
        return toAjax(gkzhSchoolService.insertGkzhSchool(gkzhSchool));
    }

    /**
     * 修改学校
     */
    @PreAuthorize("@ss.hasPermi('school:school:edit')")
    @Log(title = "学校", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GkzhSchool gkzhSchool)
    {
        gkzhSchool.setUpdateBy(getUsername());
        gkzhSchool.setUpdateTime(DateUtils.getNowDate());
        return toAjax(gkzhSchoolService.updateGkzhSchool(gkzhSchool));
    }

    /**
     * 删除学校
     */
    @PreAuthorize("@ss.hasPermi('school:school:remove')")
    @Log(title = "学校", businessType = BusinessType.DELETE)
	@DeleteMapping("/{schoolIds}")
    public AjaxResult remove(@PathVariable Long[] schoolIds)
    {
        return toAjax(gkzhSchoolService.deleteGkzhSchoolBySchoolIds(schoolIds));
    }
}
