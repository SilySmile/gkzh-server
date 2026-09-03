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
import com.gkzh.xycc.domain.Pattern;
import com.gkzh.xycc.service.IPatternService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 心愿橱窗Controller
 * 
 * @author gkzh
 * @date 2025-06-12
 */
@RestController
@RequestMapping("/xycc/pattern")
public class PatternController extends BaseController
{
    @Autowired
    private IPatternService patternService;

    /**
     * 查询心愿橱窗列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:pattern:list')")
    @GetMapping("/list")
    public TableDataInfo list(Pattern pattern)
    {
        startPage();
        List<Pattern> list = patternService.selectPatternList(pattern);
        return getDataTable(list);
    }

    /**
     * 导出心愿橱窗列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:pattern:export')")
    @Log(title = "心愿橱窗", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Pattern pattern)
    {
        List<Pattern> list = patternService.selectPatternList(pattern);
        ExcelUtil<Pattern> util = new ExcelUtil<Pattern>(Pattern.class);
        util.exportExcel(response, list, "心愿橱窗数据");
    }

    /**
     * 获取心愿橱窗详细信息
     */
    @PreAuthorize("@ss.hasPermi('xycc:pattern:query')")
    @GetMapping(value = "/{patternId}")
    public AjaxResult getInfo(@PathVariable("patternId") Long patternId)
    {
        return success(patternService.selectPatternByPatternId(patternId));
    }

    /**
     * 新增心愿橱窗
     */
    @PreAuthorize("@ss.hasPermi('xycc:pattern:add')")
    @Log(title = "心愿橱窗", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Pattern pattern)
    {
        return toAjax(patternService.insertPattern(pattern));
    }

    /**
     * 修改心愿橱窗
     */
    @PreAuthorize("@ss.hasPermi('xycc:pattern:edit')")
    @Log(title = "心愿橱窗", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Pattern pattern)
    {
        return toAjax(patternService.updatePattern(pattern));
    }

    /**
     * 删除心愿橱窗
     */
    @PreAuthorize("@ss.hasPermi('xycc:pattern:remove')")
    @Log(title = "心愿橱窗", businessType = BusinessType.DELETE)
	@DeleteMapping("/{patternIds}")
    public AjaxResult remove(@PathVariable Long[] patternIds)
    {
        return toAjax(patternService.deletePatternByPatternIds(patternIds));
    }
}
