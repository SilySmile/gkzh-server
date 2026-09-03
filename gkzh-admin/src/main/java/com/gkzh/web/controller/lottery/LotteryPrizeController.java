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
import com.gkzh.lottery.domain.LotteryPrize;
import com.gkzh.lottery.service.ILotteryPrizeService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 抽奖奖品Controller
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@RestController
@RequestMapping("/lottery/prize")
public class LotteryPrizeController extends BaseController
{
    @Autowired
    private ILotteryPrizeService lotteryPrizeService;

    /**
     * 查询抽奖奖品列表
     */
    @PreAuthorize("@ss.hasPermi('lottery:prize:list')")
    @GetMapping("/list")
    public TableDataInfo list(LotteryPrize lotteryPrize)
    {
        startPage();
        List<LotteryPrize> list = lotteryPrizeService.selectLotteryPrizeList(lotteryPrize);
        return getDataTable(list);
    }

    /**
     * 导出抽奖奖品列表
     */
    @PreAuthorize("@ss.hasPermi('lottery:prize:export')")
    @Log(title = "抽奖奖品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LotteryPrize lotteryPrize)
    {
        List<LotteryPrize> list = lotteryPrizeService.selectLotteryPrizeList(lotteryPrize);
        ExcelUtil<LotteryPrize> util = new ExcelUtil<LotteryPrize>(LotteryPrize.class);
        util.exportExcel(response, list, "抽奖奖品数据");
    }

    /**
     * 获取抽奖奖品详细信息
     */
    @PreAuthorize("@ss.hasPermi('lottery:prize:query')")
    @GetMapping(value = "/{prizeId}")
    public AjaxResult getInfo(@PathVariable("prizeId") Long prizeId)
    {
        return success(lotteryPrizeService.selectLotteryPrizeByPrizeId(prizeId));
    }

    /**
     * 新增抽奖奖品
     */
    @PreAuthorize("@ss.hasPermi('lottery:prize:add')")
    @Log(title = "抽奖奖品", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LotteryPrize lotteryPrize)
    {
        return toAjax(lotteryPrizeService.insertLotteryPrize(lotteryPrize));
    }

    /**
     * 修改抽奖奖品
     */
    @PreAuthorize("@ss.hasPermi('lottery:prize:edit')")
    @Log(title = "抽奖奖品", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LotteryPrize lotteryPrize)
    {
        return toAjax(lotteryPrizeService.updateLotteryPrize(lotteryPrize));
    }

    /**
     * 删除抽奖奖品
     */
    @PreAuthorize("@ss.hasPermi('lottery:prize:remove')")
    @Log(title = "抽奖奖品", businessType = BusinessType.DELETE)
	@DeleteMapping("/{prizeIds}")
    public AjaxResult remove(@PathVariable Long[] prizeIds)
    {
        return toAjax(lotteryPrizeService.deleteLotteryPrizeByPrizeIds(prizeIds));
    }
}
