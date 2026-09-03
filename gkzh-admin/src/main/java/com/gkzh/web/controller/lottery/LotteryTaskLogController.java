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
import com.gkzh.lottery.domain.LotteryTaskLog;
import com.gkzh.lottery.service.ILotteryTaskLogService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 完成记录Controller
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@RestController
@RequestMapping("/lottery/taskLog")
public class LotteryTaskLogController extends BaseController
{
    @Autowired
    private ILotteryTaskLogService lotteryTaskLogService;

    /**
     * 查询完成记录列表
     */
    @PreAuthorize("@ss.hasPermi('lottery:taskLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(LotteryTaskLog lotteryTaskLog)
    {
        startPage();
        List<LotteryTaskLog> list = lotteryTaskLogService.selectLotteryTaskLogList(lotteryTaskLog);
        return getDataTable(list);
    }

    /**
     * 导出完成记录列表
     */
    @PreAuthorize("@ss.hasPermi('lottery:taskLog:export')")
    @Log(title = "完成记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LotteryTaskLog lotteryTaskLog)
    {
        List<LotteryTaskLog> list = lotteryTaskLogService.selectLotteryTaskLogList(lotteryTaskLog);
        ExcelUtil<LotteryTaskLog> util = new ExcelUtil<LotteryTaskLog>(LotteryTaskLog.class);
        util.exportExcel(response, list, "完成记录数据");
    }

    /**
     * 获取完成记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('lottery:taskLog:query')")
    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId)
    {
        return success(lotteryTaskLogService.selectLotteryTaskLogByLogId(logId));
    }

    /**
     * 新增完成记录
     */
    @PreAuthorize("@ss.hasPermi('lottery:taskLog:add')")
    @Log(title = "完成记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LotteryTaskLog lotteryTaskLog)
    {
        return toAjax(lotteryTaskLogService.insertLotteryTaskLog(lotteryTaskLog));
    }

    /**
     * 修改完成记录
     */
    @PreAuthorize("@ss.hasPermi('lottery:taskLog:edit')")
    @Log(title = "完成记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LotteryTaskLog lotteryTaskLog)
    {
        return toAjax(lotteryTaskLogService.updateLotteryTaskLog(lotteryTaskLog));
    }

    /**
     * 删除完成记录
     */
    @PreAuthorize("@ss.hasPermi('lottery:taskLog:remove')")
    @Log(title = "完成记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{logIds}")
    public AjaxResult remove(@PathVariable Long[] logIds)
    {
        return toAjax(lotteryTaskLogService.deleteLotteryTaskLogByLogIds(logIds));
    }
}
