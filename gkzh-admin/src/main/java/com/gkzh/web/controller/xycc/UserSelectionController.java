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
import com.gkzh.xycc.domain.UserSelection;
import com.gkzh.xycc.service.IUserSelectionService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 用户记录Controller
 * 
 * @author gkzh
 * @date 2025-06-16
 */
@RestController
@RequestMapping("/xycc/selection")
public class UserSelectionController extends BaseController
{
    @Autowired
    private IUserSelectionService userSelectionService;

    /**
     * 查询用户记录列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:selection:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserSelection userSelection)
    {
        startPage();
        List<UserSelection> list = userSelectionService.selectUserSelectionList(userSelection);
        return getDataTable(list);
    }

    /**
     * 导出用户记录列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:selection:export')")
    @Log(title = "用户记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserSelection userSelection)
    {
        List<UserSelection> list = userSelectionService.selectUserSelectionList(userSelection);
        ExcelUtil<UserSelection> util = new ExcelUtil<UserSelection>(UserSelection.class);
        util.exportExcel(response, list, "用户记录数据");
    }

    /**
     * 获取用户记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('xycc:selection:query')")
    @GetMapping(value = "/{userSelectionId}")
    public AjaxResult getInfo(@PathVariable("userSelectionId") Long userSelectionId)
    {
        return success(userSelectionService.selectUserSelectionByUserSelectionId(userSelectionId));
    }



    /**
     * 删除用户记录
     */
    @PreAuthorize("@ss.hasPermi('xycc:selection:remove')")
    @Log(title = "用户记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{userSelectionIds}")
    public AjaxResult remove(@PathVariable Long[] userSelectionIds)
    {
        return toAjax(userSelectionService.deleteUserSelectionByUserSelectionIds(userSelectionIds));
    }
}
