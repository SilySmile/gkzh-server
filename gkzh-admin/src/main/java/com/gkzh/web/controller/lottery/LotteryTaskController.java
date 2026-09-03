package com.gkzh.web.controller.lottery;

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
import com.gkzh.lottery.domain.LotteryTask;
import com.gkzh.lottery.service.ILotteryTaskService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 *  前置任务Controller
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@RestController
@RequestMapping("/lottery/task")
public class LotteryTaskController extends BaseController
{
    @Autowired
    private ILotteryTaskService lotteryTaskService;

    /**
     * 查询 前置任务列表
     */
    @PreAuthorize("@ss.hasPermi('lottery:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(LotteryTask lotteryTask)
    {
        startPage();
        List<LotteryTask> list = lotteryTaskService.selectLotteryTaskList(lotteryTask);
        return getDataTable(list);
    }

    /**
     * 导出 前置任务列表
     */
    @PreAuthorize("@ss.hasPermi('lottery:task:export')")
    @Log(title = " 前置任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LotteryTask lotteryTask)
    {
        List<LotteryTask> list = lotteryTaskService.selectLotteryTaskList(lotteryTask);
        ExcelUtil<LotteryTask> util = new ExcelUtil<LotteryTask>(LotteryTask.class);
        util.exportExcel(response, list, " 前置任务数据");
    }

    /**
     * 获取 前置任务详细信息
     */
    @PreAuthorize("@ss.hasPermi('lottery:task:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return success(lotteryTaskService.selectLotteryTaskByTaskId(taskId));
    }

    /**
     * 新增 前置任务
     */
    @PreAuthorize("@ss.hasPermi('lottery:task:add')")
    @Log(title = " 前置任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LotteryTask lotteryTask)
    {
        return toAjax(lotteryTaskService.insertLotteryTask(lotteryTask));
    }

    /**
     * 修改 前置任务
     */
    @PreAuthorize("@ss.hasPermi('lottery:task:edit')")
    @Log(title = " 前置任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LotteryTask lotteryTask)
    {
        return toAjax(lotteryTaskService.updateLotteryTask(lotteryTask));
    }

    /**
     * 删除 前置任务
     */
    @PreAuthorize("@ss.hasPermi('lottery:task:remove')")
    @Log(title = " 前置任务", businessType = BusinessType.DELETE)
	@DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(lotteryTaskService.deleteLotteryTaskByTaskIds(taskIds));
    }
}
