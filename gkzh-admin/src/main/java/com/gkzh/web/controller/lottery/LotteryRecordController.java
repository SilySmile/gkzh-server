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
import com.gkzh.lottery.domain.LotteryRecord;
import com.gkzh.lottery.service.ILotteryRecordService;
import com.gkzh.activity.mapper.staff.GkzhPrizeRedemptionLogMapper;
import com.gkzh.activity.domain.staff.GkzhPrizeRedemptionLog;
import com.gkzh.activity.service.IStaffService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 抽奖记录Controller
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@RestController
@RequestMapping("/lottery/record")
public class LotteryRecordController extends BaseController
{
    @Autowired
    private ILotteryRecordService lotteryRecordService;
    @Autowired
    private GkzhPrizeRedemptionLogMapper redemptionLogMapper;
    @Autowired
    private IStaffService staffService;

    @PreAuthorize("@ss.hasPermi('lottery:record:edit')")
    @PostMapping("/{recordId}/redeem")
    public AjaxResult redeem(@PathVariable("recordId") Long recordId) {
        return success(staffService.adminRedeemPrize(recordId, getUserId(), "后台管理员核销"));
    }

    @PreAuthorize("@ss.hasPermi('lottery:record:query')")
    @GetMapping("/{recordId}/redemption-logs")
    public AjaxResult redemptionLogs(@PathVariable("recordId") Long recordId) {
        return success(redemptionLogMapper.selectByRecordId(recordId));
    }

    /**
     * 查询抽奖记录列表
     */
    @PreAuthorize("@ss.hasPermi('lottery:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(LotteryRecord lotteryRecord)
    {
        startPage();
        List<LotteryRecord> list = lotteryRecordService.selectLotteryRecordList(lotteryRecord);
        return getDataTable(list);
    }

    /**
     * 导出抽奖记录列表
     */
    @PreAuthorize("@ss.hasPermi('lottery:record:export')")
    @Log(title = "抽奖记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LotteryRecord lotteryRecord)
    {
        List<LotteryRecord> list = lotteryRecordService.selectLotteryRecordList(lotteryRecord);
        ExcelUtil<LotteryRecord> util = new ExcelUtil<LotteryRecord>(LotteryRecord.class);
        util.exportExcel(response, list, "抽奖记录数据");
    }

    /**
     * 获取抽奖记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('lottery:record:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(lotteryRecordService.selectLotteryRecordByRecordId(recordId));
    }

    /**
     * 新增抽奖记录
     */
    @PreAuthorize("@ss.hasPermi('lottery:record:add')")
    @Log(title = "抽奖记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LotteryRecord lotteryRecord)
    {
        return toAjax(lotteryRecordService.insertLotteryRecord(lotteryRecord));
    }

    /**
     * 修改抽奖记录
     */
    @PreAuthorize("@ss.hasPermi('lottery:record:edit')")
    @Log(title = "抽奖记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LotteryRecord lotteryRecord)
    {
        return toAjax(lotteryRecordService.updateLotteryRecord(lotteryRecord));
    }

    /**
     * 删除抽奖记录
     */
    @PreAuthorize("@ss.hasPermi('lottery:record:remove')")
    @Log(title = "抽奖记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(lotteryRecordService.deleteLotteryRecordByRecordIds(recordIds));
    }
}
