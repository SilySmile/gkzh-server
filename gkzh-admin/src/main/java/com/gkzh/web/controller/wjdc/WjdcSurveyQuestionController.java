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
import com.gkzh.wjdc.domain.WjdcSurveyQuestion;
import com.gkzh.wjdc.service.IWjdcSurveyQuestionService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 问卷问题Controller
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@RestController
@RequestMapping("/wjdc/question")
public class WjdcSurveyQuestionController extends BaseController
{
    @Autowired
    private IWjdcSurveyQuestionService wjdcSurveyQuestionService;

    /**
     * 查询问卷问题列表
     */
    @PreAuthorize("@ss.hasPermi('wjdc:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(WjdcSurveyQuestion wjdcSurveyQuestion)
    {
        startPage();
        List<WjdcSurveyQuestion> list = wjdcSurveyQuestionService.selectWjdcSurveyQuestionList(wjdcSurveyQuestion);
        return getDataTable(list);
    }

    /**
     * 导出问卷问题列表
     */
    @PreAuthorize("@ss.hasPermi('wjdc:question:export')")
    @Log(title = "问卷问题", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(WjdcSurveyQuestion wjdcSurveyQuestion)
    {
        List<WjdcSurveyQuestion> list = wjdcSurveyQuestionService.selectWjdcSurveyQuestionList(wjdcSurveyQuestion);
        ExcelUtil<WjdcSurveyQuestion> util = new ExcelUtil<WjdcSurveyQuestion>(WjdcSurveyQuestion.class);
        return util.exportExcel(list, "question");
    }

    /**
     * 获取问卷问题详细信息
     */
    @PreAuthorize("@ss.hasPermi('wjdc:question:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Integer id)
    {
        return success(wjdcSurveyQuestionService.selectWjdcSurveyQuestionById(id));
    }

    /**
     * 根据问卷ID获取问题列表
     */
    @PreAuthorize("@ss.hasPermi('wjdc:question:query')")
    @GetMapping(value = "/survey/{surveyId}")
    public AjaxResult getQuestionsBySurveyId(@PathVariable("surveyId") Integer surveyId)
    {
        List<WjdcSurveyQuestion> list = wjdcSurveyQuestionService.selectWjdcSurveyQuestionBySurveyId(surveyId);
        return success(list);
    }

    /**
     * 新增问卷问题
     */
    @PreAuthorize("@ss.hasPermi('wjdc:question:add')")
    @Log(title = "问卷问题", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WjdcSurveyQuestion wjdcSurveyQuestion)
    {
        Long l = wjdcSurveyQuestionService.insertWjdcSurveyQuestion(wjdcSurveyQuestion);
        if (l > 0){
            return success(l);
        }
        return error();
    }

    /**
     * 修改问卷问题
     */
    @PreAuthorize("@ss.hasPermi('wjdc:question:edit')")
    @Log(title = "问卷问题", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WjdcSurveyQuestion wjdcSurveyQuestion)
    {
        return toAjax(wjdcSurveyQuestionService.updateWjdcSurveyQuestion(wjdcSurveyQuestion));
    }

    /**
     * 删除问卷问题
     */
    @PreAuthorize("@ss.hasPermi('wjdc:question:remove')")
    @Log(title = "问卷问题", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids)
    {
        return toAjax(wjdcSurveyQuestionService.deleteWjdcSurveyQuestionByIds(ids));
    }
}
