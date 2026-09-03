package com.gkzh.web.controller.zyxxz;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.gkzh.zyxxz.domain.GkzhZyxxzStudentChoice;
import com.gkzh.zyxxz.service.IGkzhZyxxzStudentChoiceService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 职业信息站-学生选择记录Controller
 * 
 * @author gkzh
 * @date 2026-06-04
 */
@RestController
@RequestMapping("/zyxxz/choice")
public class GkzhZyxxzStudentChoiceController extends BaseController
{
    @Autowired
    private IGkzhZyxxzStudentChoiceService gkzhZyxxzStudentChoiceService;

    /**
     * 查询职业信息站-学生选择记录列表
     */
    @PreAuthorize("@ss.hasPermi('zyxxz:choice:list')")
    @GetMapping("/list")
    public TableDataInfo list(GkzhZyxxzStudentChoice gkzhZyxxzStudentChoice)
    {
        startPage();
        List<GkzhZyxxzStudentChoice> list = gkzhZyxxzStudentChoiceService.selectGkzhZyxxzStudentChoiceList(gkzhZyxxzStudentChoice);
        return getDataTable(list);
    }

    /**
     * 导出职业信息站-学生选择记录列表
     */
    @PreAuthorize("@ss.hasPermi('zyxxz:choice:export')")
    @Log(title = "职业信息站-学生选择记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GkzhZyxxzStudentChoice gkzhZyxxzStudentChoice)
    {
        List<GkzhZyxxzStudentChoice> list = gkzhZyxxzStudentChoiceService.selectGkzhZyxxzStudentChoiceList(gkzhZyxxzStudentChoice);
        ExcelUtil<GkzhZyxxzStudentChoice> util = new ExcelUtil<GkzhZyxxzStudentChoice>(GkzhZyxxzStudentChoice.class);
        util.exportExcel(response, list, "职业信息站-学生选择记录数据");
    }

    /**
     * 获取职业信息站-学生选择记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('zyxxz:choice:query')")
    @GetMapping(value = "/{choiceId}")
    public AjaxResult getInfo(@PathVariable("choiceId") Long choiceId)
    {
        return success(gkzhZyxxzStudentChoiceService.selectGkzhZyxxzStudentChoiceByChoiceId(choiceId));
    }

    /**
     * 新增职业信息站-学生选择记录
     */
    @PreAuthorize("@ss.hasPermi('zyxxz:choice:add')")
    @Log(title = "职业信息站-学生选择记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GkzhZyxxzStudentChoice gkzhZyxxzStudentChoice)
    {
        return toAjax(gkzhZyxxzStudentChoiceService.insertGkzhZyxxzStudentChoice(gkzhZyxxzStudentChoice));
    }

    /**
     * 修改职业信息站-学生选择记录
     */
    @PreAuthorize("@ss.hasPermi('zyxxz:choice:edit')")
    @Log(title = "职业信息站-学生选择记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GkzhZyxxzStudentChoice gkzhZyxxzStudentChoice)
    {
        return toAjax(gkzhZyxxzStudentChoiceService.updateGkzhZyxxzStudentChoice(gkzhZyxxzStudentChoice));
    }

    /**
     * 删除职业信息站-学生选择记录
     */
    @PreAuthorize("@ss.hasPermi('zyxxz:choice:remove')")
    @Log(title = "职业信息站-学生选择记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{choiceIds}")
    public AjaxResult remove(@PathVariable Long[] choiceIds)
    {
        return toAjax(gkzhZyxxzStudentChoiceService.deleteGkzhZyxxzStudentChoiceByChoiceIds(choiceIds));
    }
}
