package com.gkzh.web.controller.wjdc;

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
import com.gkzh.wjdc.domain.WjdcSurveyResponse;
import com.gkzh.wjdc.service.IWjdcSurveyResponseService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 用户答卷Controller
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@RestController
@RequestMapping("/wjdc/response")
public class WjdcSurveyResponseController extends BaseController
{
    @Autowired
    private IWjdcSurveyResponseService wjdcSurveyResponseService;

    /**
     * 查询用户答卷列表
     */
    @PreAuthorize("@ss.hasPermi('wjdc:response:list')")
    @GetMapping("/list")
    public TableDataInfo list(WjdcSurveyResponse wjdcSurveyResponse)
    {
        startPage();
        List<WjdcSurveyResponse> list = wjdcSurveyResponseService.selectWjdcSurveyResponseList(wjdcSurveyResponse);
        return getDataTable(list);
    }

    /**
     * 导出用户答卷列表
     */
    @PreAuthorize("@ss.hasPermi('wjdc:response:export')")
    @Log(title = "用户答卷", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WjdcSurveyResponse wjdcSurveyResponse)
    {
        List<WjdcSurveyResponse> list = wjdcSurveyResponseService.selectWjdcSurveyResponseList(wjdcSurveyResponse);
        ExcelUtil<WjdcSurveyResponse> util = new ExcelUtil<WjdcSurveyResponse>(WjdcSurveyResponse.class);
        util.exportExcel(response, list, "用户答卷数据");
    }

    /**
     * 获取用户答卷详细信息
     */
    @PreAuthorize("@ss.hasPermi('wjdc:response:query')")
    @GetMapping(value = "/{responseId}")
    public AjaxResult getInfo(@PathVariable("responseId") Long responseId)
    {
        return success(wjdcSurveyResponseService.selectWjdcSurveyResponseByResponseId(responseId));
    }

    /**
     * 获取用户答卷详情（包含答案）
     */
    @PreAuthorize("@ss.hasPermi('wjdc:response:query')")
    @GetMapping(value = "/detail/{responseId}")
    public AjaxResult getDetail(@PathVariable("responseId") Long responseId)
    {
        return success(wjdcSurveyResponseService.selectWjdcSurveyResponseDetailByResponseId(responseId));
    }

    /**
     * 新增用户答卷
     */
    @PreAuthorize("@ss.hasPermi('wjdc:response:add')")
    @Log(title = "用户答卷", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WjdcSurveyResponse wjdcSurveyResponse)
    {
        return toAjax(wjdcSurveyResponseService.insertWjdcSurveyResponse(wjdcSurveyResponse));
    }

    /**
     * 修改用户答卷
     */
    @PreAuthorize("@ss.hasPermi('wjdc:response:edit')")
    @Log(title = "用户答卷", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WjdcSurveyResponse wjdcSurveyResponse)
    {
        return toAjax(wjdcSurveyResponseService.updateWjdcSurveyResponse(wjdcSurveyResponse));
    }

    /**
     * 删除用户答卷
     */
    @PreAuthorize("@ss.hasPermi('wjdc:response:remove')")
    @Log(title = "用户答卷", businessType = BusinessType.DELETE)
	@DeleteMapping("/{responseIds}")
    public AjaxResult remove(@PathVariable Long[] responseIds)
    {
        return toAjax(wjdcSurveyResponseService.deleteWjdcSurveyResponseByResponseIds(responseIds));
    }
} 