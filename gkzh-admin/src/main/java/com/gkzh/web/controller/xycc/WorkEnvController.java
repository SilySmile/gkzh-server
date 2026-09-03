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
import com.gkzh.xycc.domain.WorkEnv;
import com.gkzh.xycc.service.IWorkEnvService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 工作环境偏好Controller
 * 
 * @author gkzh
 * @date 2025-06-15
 */
@RestController
@RequestMapping("/xycc/env")
public class WorkEnvController extends BaseController
{
    @Autowired
    private IWorkEnvService workEnvService;

    /**
     * 查询工作环境偏好列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:env:list')")
    @GetMapping("/list")
    public TableDataInfo list(WorkEnv workEnv)
    {
        startPage();
        startOrderBy();
        List<WorkEnv> list = workEnvService.selectWorkEnvList(workEnv);
        return getDataTable(list);
    }

    /**
     * 导出工作环境偏好列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:env:export')")
    @Log(title = "工作环境偏好", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WorkEnv workEnv)
    {
        List<WorkEnv> list = workEnvService.selectWorkEnvList(workEnv);
        ExcelUtil<WorkEnv> util = new ExcelUtil<WorkEnv>(WorkEnv.class);
        util.exportExcel(response, list, "工作环境偏好数据");
    }

    /**
     * 获取工作环境偏好详细信息
     */
    @PreAuthorize("@ss.hasPermi('xycc:env:query')")
    @GetMapping(value = "/{workEnvId}")
    public AjaxResult getInfo(@PathVariable("workEnvId") Long workEnvId)
    {
        return success(workEnvService.selectWorkEnvByWorkEnvId(workEnvId));
    }

    /**
     * 新增工作环境偏好
     */
    @PreAuthorize("@ss.hasPermi('xycc:env:add')")
    @Log(title = "工作环境偏好", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WorkEnv workEnv)
    {
        return toAjax(workEnvService.insertWorkEnv(workEnv));
    }

    /**
     * 修改工作环境偏好
     */
    @PreAuthorize("@ss.hasPermi('xycc:env:edit')")
    @Log(title = "工作环境偏好", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WorkEnv workEnv)
    {
        return toAjax(workEnvService.updateWorkEnv(workEnv));
    }

    /**
     * 删除工作环境偏好
     */
    @PreAuthorize("@ss.hasPermi('xycc:env:remove')")
    @Log(title = "工作环境偏好", businessType = BusinessType.DELETE)
	@DeleteMapping("/{workEnvIds}")
    public AjaxResult remove(@PathVariable Long[] workEnvIds)
    {
        return toAjax(workEnvService.deleteWorkEnvByWorkEnvIds(workEnvIds));
    }
}
