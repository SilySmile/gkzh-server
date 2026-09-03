package com.gkzh.web.controller.xycc;

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
import com.gkzh.xycc.domain.PatternCombo;
import com.gkzh.xycc.service.IPatternComboService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 编码组合Controller
 * 
 * @author gkzh
 * @date 2025-06-15
 */
@RestController
@RequestMapping("/xycc/combo")
public class PatternComboController extends BaseController
{
    @Autowired
    private IPatternComboService patternComboService;

    /**
     * 查询编码组合列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:combo:list')")
    @GetMapping("/list")
    public TableDataInfo list(PatternCombo patternCombo)
    {
        startPage();
        List<PatternCombo> list = patternComboService.selectPatternComboList(patternCombo);
        return getDataTable(list);
    }

    /**
     * 导出编码组合列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:combo:export')")
    @Log(title = "编码组合", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PatternCombo patternCombo)
    {
        List<PatternCombo> list = patternComboService.selectPatternComboList(patternCombo);
        ExcelUtil<PatternCombo> util = new ExcelUtil<PatternCombo>(PatternCombo.class);
        util.exportExcel(response, list, "编码组合数据");
    }

    /**
     * 获取编码组合详细信息
     */
    @PreAuthorize("@ss.hasPermi('xycc:combo:query')")
    @GetMapping(value = "/{patternComboId}")
    public AjaxResult getInfo(@PathVariable("patternComboId") Long patternComboId)
    {
        return success(patternComboService.selectPatternComboByPatternComboId(patternComboId));
    }

    /**
     * 新增编码组合
     */
    @PreAuthorize("@ss.hasPermi('xycc:combo:add')")
    @Log(title = "编码组合", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PatternCombo patternCombo)
    {
        return toAjax(patternComboService.insertPatternCombo(patternCombo));
    }

    /**
     * 修改编码组合
     */
    @PreAuthorize("@ss.hasPermi('xycc:combo:edit')")
    @Log(title = "编码组合", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PatternCombo patternCombo)
    {
        return toAjax(patternComboService.updatePatternCombo(patternCombo));
    }

    /**
     * 删除编码组合
     */
    @PreAuthorize("@ss.hasPermi('xycc:combo:remove')")
    @Log(title = "编码组合", businessType = BusinessType.DELETE)
	@DeleteMapping("/{patternComboIds}")
    public AjaxResult remove(@PathVariable Long[] patternComboIds)
    {
        return toAjax(patternComboService.deletePatternComboByPatternComboIds(patternComboIds));
    }
}
