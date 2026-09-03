package com.gkzh.web.controller.wjyd;

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
import com.gkzh.wjyd.domain.BizQuestion;
import com.gkzh.wjyd.service.IBizQuestionService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 职场危机Controller
 * 
 * @author gkzh
 * @date 2025-10-13
 */
@RestController
@RequestMapping("/wjyd/question")
public class BizQuestionController extends BaseController
{
    @Autowired
    private IBizQuestionService bizQuestionService;

    /**
     * 查询职场危机列表
     */
    @PreAuthorize("@ss.hasPermi('wjyd:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizQuestion bizQuestion)
    {
        startPage();
        List<BizQuestion> list = bizQuestionService.selectBizQuestionList(bizQuestion);
        return getDataTable(list);
    }

    /**
     * 导出职场危机列表
     */
    @PreAuthorize("@ss.hasPermi('wjyd:question:export')")
    @Log(title = "职场危机", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizQuestion bizQuestion)
    {
        List<BizQuestion> list = bizQuestionService.selectBizQuestionList(bizQuestion);
        ExcelUtil<BizQuestion> util = new ExcelUtil<BizQuestion>(BizQuestion.class);
        util.exportExcel(response, list, "职场危机数据");
    }

    /**
     * 获取职场危机详细信息
     */
    @PreAuthorize("@ss.hasPermi('wjyd:question:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizQuestionService.selectBizQuestionById(id));
    }

    /**
     * 新增职场危机
     */
    @PreAuthorize("@ss.hasPermi('wjyd:question:add')")
    @Log(title = "职场危机", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizQuestion bizQuestion)
    {
        return toAjax(bizQuestionService.insertBizQuestion(bizQuestion));
    }

    /**
     * 修改职场危机
     */
    @PreAuthorize("@ss.hasPermi('wjyd:question:edit')")
    @Log(title = "职场危机", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizQuestion bizQuestion)
    {
        return toAjax(bizQuestionService.updateBizQuestion(bizQuestion));
    }

    /**
     * 删除职场危机
     */
    @PreAuthorize("@ss.hasPermi('wjyd:question:remove')")
    @Log(title = "职场危机", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizQuestionService.deleteBizQuestionByIds(ids));
    }
}
