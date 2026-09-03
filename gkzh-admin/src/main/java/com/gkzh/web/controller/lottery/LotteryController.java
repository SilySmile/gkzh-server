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
import com.gkzh.lottery.domain.Lottery;
import com.gkzh.lottery.service.ILotteryService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 活动管理Controller
 * 
 * @author gkzh
 * @date 2025-06-16
 */
@RestController
@RequestMapping("/lottery/activity")
public class LotteryController extends BaseController
{
    @Autowired
    private ILotteryService lotteryService;

    /**
     * 查询活动管理列表
     */
    @PreAuthorize("@ss.hasPermi('lottery:activity:list')")
    @GetMapping("/list")
    public TableDataInfo list(Lottery lottery)
    {
        startPage();
        List<Lottery> list = lotteryService.selectLotteryList(lottery);
        return getDataTable(list);
    }

    /**
     * 导出活动管理列表
     */
    @PreAuthorize("@ss.hasPermi('lottery:activity:export')")
    @Log(title = "活动管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Lottery lottery)
    {
        List<Lottery> list = lotteryService.selectLotteryList(lottery);
        ExcelUtil<Lottery> util = new ExcelUtil<Lottery>(Lottery.class);
        util.exportExcel(response, list, "活动管理数据");
    }

    /**
     * 获取活动管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('lottery:activity:query')")
    @GetMapping(value = "/{activityId}")
    public AjaxResult getInfo(@PathVariable("activityId") Long activityId)
    {
        return success(lotteryService.selectLotteryByLotteryId(activityId));
    }

    /**
     * 新增活动管理
     */
    @PreAuthorize("@ss.hasPermi('lottery:activity:add')")
    @Log(title = "活动管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Lottery lottery)
    {
        return toAjax(lotteryService.insertLottery(lottery));
    }

    /**
     * 修改活动管理
     */
    @PreAuthorize("@ss.hasPermi('lottery:activity:edit')")
    @Log(title = "活动管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Lottery lottery)
    {
        return toAjax(lotteryService.updateLottery(lottery));
    }
    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('lottery:activity:edit')")
    @Log(title = "活动管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody Lottery lottery)
    {
        return toAjax(lotteryService.updateLotteryStatus(lottery));
    }
    /**
     * 删除活动管理
     */
    @PreAuthorize("@ss.hasPermi('lottery:activity:remove')")
    @Log(title = "活动管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{activityIds}")
    public AjaxResult remove(@PathVariable Long[] activityIds)
    {
        return toAjax(lotteryService.deleteLotteryByLotteryIds(activityIds));
    }
}
