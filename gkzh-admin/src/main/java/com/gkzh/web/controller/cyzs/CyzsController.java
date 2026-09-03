package com.gkzh.web.controller.cyzs;

import com.gkzh.common.annotation.Log;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.enums.BusinessType;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.cyzs.domain.CyzsQuestion;
import com.gkzh.cyzs.service.ICyzsQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 创业知识Controller
 * 
 * @author gkzh
 * @date 2025-10-13
 */
@RestController
@RequestMapping("/cyzs/question")
public class CyzsController extends BaseController
{
    @Autowired
    private ICyzsQuestionService cyzsQuestionService;

    /**
     * 查询创业知识列表
     */
    @PreAuthorize("@ss.hasPermi('cyzs:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(CyzsQuestion cyzsQuestion)
    {
        startPage();
        List<CyzsQuestion> list = cyzsQuestionService.selectCyzsQuestionList(cyzsQuestion);
        return getDataTable(list);
    }

    /**
     * 导出创业知识列表
     */
    @PreAuthorize("@ss.hasPermi('cyzs:question:export')")
    @Log(title = "创业知识", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CyzsQuestion cyzsQuestion)
    {
        List<CyzsQuestion> list = cyzsQuestionService.selectCyzsQuestionList(cyzsQuestion);
        ExcelUtil<CyzsQuestion> util = new ExcelUtil<CyzsQuestion>(CyzsQuestion.class);
        util.exportExcel(response, list, "创业知识数据");
    }

    /**
     * 获取创业知识详细信息
     */
    @PreAuthorize("@ss.hasPermi('cyzs:question:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(cyzsQuestionService.selectCyzsQuestionById(id));
    }

    /**
     * 新增创业知识
     */
    @PreAuthorize("@ss.hasPermi('cyzs:question:add')")
    @Log(title = "创业知识", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CyzsQuestion cyzsQuestion)
    {
        return toAjax(cyzsQuestionService.insertCyzsQuestion(cyzsQuestion));
    }

    /**
     * 修改创业知识
     */
    @PreAuthorize("@ss.hasPermi('cyzs:question:edit')")
    @Log(title = "创业知识", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CyzsQuestion cyzsQuestion)
    {
        return toAjax(cyzsQuestionService.updateCyzsQuestion(cyzsQuestion));
    }

    /**
     * 删除创业知识
     */
    @PreAuthorize("@ss.hasPermi('cyzs:question:remove')")
    @Log(title = "创业知识", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(cyzsQuestionService.deleteCyzsQuestionByIds(ids));
    }
}
