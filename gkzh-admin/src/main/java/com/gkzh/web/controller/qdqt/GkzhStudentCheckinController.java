package com.gkzh.web.controller.qdqt;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.gkzh.qdqt.dto.GkzhStudentCheckinDTO;
import com.gkzh.qdqt.vo.GkzhStudentCheckinExportVO;
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
import com.gkzh.qdqt.domain.GkzhStudentCheckin;
import com.gkzh.qdqt.service.IGkzhStudentCheckinService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 签到签退Controller
 * 
 * @author gkzh
 * @date 2025-06-22
 */
@RestController
@RequestMapping("/qdqt/checkin")
public class GkzhStudentCheckinController extends BaseController
{
    @Autowired
    private IGkzhStudentCheckinService gkzhStudentCheckinService;

    /**
     * 查询签到签退列表
     */
    @PreAuthorize("@ss.hasPermi('qdqt:checkin:list')")
    @GetMapping("/list")
    public TableDataInfo list(GkzhStudentCheckinDTO gkzhStudentCheckin)
    {
        startPage();
        List<GkzhStudentCheckinExportVO> list = gkzhStudentCheckinService.selectGkzhStudentCheckinList(gkzhStudentCheckin);
        return getDataTable(list);
    }

    /**
     * 导出签到签退列表
     */
    @PreAuthorize("@ss.hasPermi('qdqt:checkin:export')")
    @Log(title = "签到签退", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GkzhStudentCheckinDTO gkzhStudentCheckin)
    {
        List<GkzhStudentCheckinExportVO> list = gkzhStudentCheckinService.selectGkzhStudentCheckinList(gkzhStudentCheckin);
        ExcelUtil<GkzhStudentCheckinExportVO> util = new ExcelUtil<GkzhStudentCheckinExportVO>(GkzhStudentCheckinExportVO.class);
        util.exportExcel(response, list, "签到签退数据");
    }

    /**
     * 获取签到签退详细信息
     */
    @PreAuthorize("@ss.hasPermi('qdqt:checkin:query')")
    @GetMapping(value = "/{checkinId}")
    public AjaxResult getInfo(@PathVariable("checkinId") Long checkinId)
    {
        return success(gkzhStudentCheckinService.selectGkzhStudentCheckinByCheckinId(checkinId));
    }

    /**
     * 新增签到签退
     */
    @PreAuthorize("@ss.hasPermi('qdqt:checkin:add')")
    @Log(title = "签到签退", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GkzhStudentCheckin gkzhStudentCheckin)
    {
        return toAjax(gkzhStudentCheckinService.insertGkzhStudentCheckin(gkzhStudentCheckin));
    }

    /**
     * 修改签到签退
     */
    @PreAuthorize("@ss.hasPermi('qdqt:checkin:edit')")
    @Log(title = "签到签退", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GkzhStudentCheckin gkzhStudentCheckin)
    {
        return toAjax(gkzhStudentCheckinService.updateGkzhStudentCheckin(gkzhStudentCheckin));
    }

    /**
     * 删除签到签退
     */
    @PreAuthorize("@ss.hasPermi('qdqt:checkin:remove')")
    @Log(title = "签到签退", businessType = BusinessType.DELETE)
	@DeleteMapping("/{checkinIds}")
    public AjaxResult remove(@PathVariable Long[] checkinIds)
    {
        return toAjax(gkzhStudentCheckinService.deleteGkzhStudentCheckinByCheckinIds(checkinIds));
    }
}
