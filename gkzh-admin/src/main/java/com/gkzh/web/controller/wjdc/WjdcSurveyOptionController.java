package com.gkzh.web.controller.wjdc;

import java.util.List;
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
import com.gkzh.wjdc.domain.WjdcSurveyOption;
import com.gkzh.wjdc.service.IWjdcSurveyOptionService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 问卷选项Controller
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@RestController
@RequestMapping("/wjdc/option")
public class WjdcSurveyOptionController extends BaseController
{
    @Autowired
    private IWjdcSurveyOptionService wjdcSurveyOptionService;

    /**
     * 查询问卷选项列表
     */
    @PreAuthorize("@ss.hasPermi('wjdc:option:list')")
    @GetMapping("/list")
    public TableDataInfo list(WjdcSurveyOption wjdcSurveyOption)
    {
        startPage();
        List<WjdcSurveyOption> list = wjdcSurveyOptionService.selectWjdcSurveyOptionList(wjdcSurveyOption);
        return getDataTable(list);
    }

    /**
     * 导出问卷选项列表
     */
    @PreAuthorize("@ss.hasPermi('wjdc:option:export')")
    @Log(title = "问卷选项", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(WjdcSurveyOption wjdcSurveyOption)
    {
        List<WjdcSurveyOption> list = wjdcSurveyOptionService.selectWjdcSurveyOptionList(wjdcSurveyOption);
        ExcelUtil<WjdcSurveyOption> util = new ExcelUtil<WjdcSurveyOption>(WjdcSurveyOption.class);
        return util.exportExcel(list, "option");
    }

    /**
     * 获取问卷选项详细信息
     */
    @PreAuthorize("@ss.hasPermi('wjdc:option:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Integer id)
    {
        return success(wjdcSurveyOptionService.selectWjdcSurveyOptionById(id));
    }

    /**
     * 根据问题ID获取选项列表
     */
    @PreAuthorize("@ss.hasPermi('wjdc:option:query')")
    @GetMapping(value = "/question/{questionId}")
    public AjaxResult getOptionsByQuestionId(@PathVariable("questionId") Integer questionId)
    {
        List<WjdcSurveyOption> list = wjdcSurveyOptionService.selectWjdcSurveyOptionByQuestionId(questionId);
        return success(list);
    }

    /**
     * 新增问卷选项
     */
    @PreAuthorize("@ss.hasPermi('wjdc:option:add')")
    @Log(title = "问卷选项", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WjdcSurveyOption wjdcSurveyOption)
    {
        return toAjax(wjdcSurveyOptionService.insertWjdcSurveyOption(wjdcSurveyOption));
    }

    /**
     * 修改问卷选项
     */
    @PreAuthorize("@ss.hasPermi('wjdc:option:edit')")
    @Log(title = "问卷选项", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WjdcSurveyOption wjdcSurveyOption)
    {
        return toAjax(wjdcSurveyOptionService.updateWjdcSurveyOption(wjdcSurveyOption));
    }

    /**
     * 删除问卷选项
     */
    @PreAuthorize("@ss.hasPermi('wjdc:option:remove')")
    @Log(title = "问卷选项", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids)
    {
        return toAjax(wjdcSurveyOptionService.deleteWjdcSurveyOptionByIds(ids));
    }
} 