package com.gkzh.web.controller.zytj;

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
import com.gkzh.zytj.domain.GkzhMbtiStudentChoice;
import com.gkzh.zytj.service.IGkzhMbtiStudentChoiceService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 职愿探究-学生选择记录Controller
 * 
 * @author gkzh
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/zytj/choice")
public class GkzhMbtiStudentChoiceController extends BaseController
{
    @Autowired
    private IGkzhMbtiStudentChoiceService gkzhMbtiStudentChoiceService;

    /**
     * 查询职愿探究-学生选择记录列表
     */
    @PreAuthorize("@ss.hasPermi('zytj:choice:list')")
    @GetMapping("/list")
    public TableDataInfo list(GkzhMbtiStudentChoice gkzhMbtiStudentChoice)
    {
        startPage();
        List<GkzhMbtiStudentChoice> list = gkzhMbtiStudentChoiceService.selectGkzhMbtiStudentChoiceList(gkzhMbtiStudentChoice);
        return getDataTable(list);
    }

    /**
     * 导出职愿探究-学生选择记录列表
     */
    @PreAuthorize("@ss.hasPermi('zytj:choice:export')")
    @Log(title = "职愿探究-学生选择记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GkzhMbtiStudentChoice gkzhMbtiStudentChoice)
    {
        List<GkzhMbtiStudentChoice> list = gkzhMbtiStudentChoiceService.selectGkzhMbtiStudentChoiceList(gkzhMbtiStudentChoice);
        ExcelUtil<GkzhMbtiStudentChoice> util = new ExcelUtil<GkzhMbtiStudentChoice>(GkzhMbtiStudentChoice.class);
        util.exportExcel(response, list, "职愿探究-学生选择记录数据");
    }

    /**
     * 获取职愿探究-学生选择记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('zytj:choice:query')")
    @GetMapping(value = "/{choiceId}")
    public AjaxResult getInfo(@PathVariable("choiceId") Long choiceId)
    {
        return success(gkzhMbtiStudentChoiceService.selectGkzhMbtiStudentChoiceByChoiceId(choiceId));
    }

    /**
     * 新增职愿探究-学生选择记录
     */
    @PreAuthorize("@ss.hasPermi('zytj:choice:add')")
    @Log(title = "职愿探究-学生选择记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GkzhMbtiStudentChoice gkzhMbtiStudentChoice)
    {
        return toAjax(gkzhMbtiStudentChoiceService.insertGkzhMbtiStudentChoice(gkzhMbtiStudentChoice));
    }

//    /**
//     * 修改职愿探究-学生选择记录
//     */
//    @PreAuthorize("@ss.hasPermi('lottery:choice:edit')")
//    @Log(title = "职愿探究-学生选择记录", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult edit(@RequestBody GkzhMbtiStudentChoice gkzhMbtiStudentChoice)
//    {
//        return toAjax(gkzhMbtiStudentChoiceService.updateGkzhMbtiStudentChoice(gkzhMbtiStudentChoice));
//    }
//
//    /**
//     * 删除职愿探究-学生选择记录
//     */
//    @PreAuthorize("@ss.hasPermi('lottery:choice:remove')")
//    @Log(title = "职愿探究-学生选择记录", businessType = BusinessType.DELETE)
//	@DeleteMapping("/{choiceIds}")
//    public AjaxResult remove(@PathVariable Long[] choiceIds)
//    {
//        return toAjax(gkzhMbtiStudentChoiceService.deleteGkzhMbtiStudentChoiceByChoiceIds(choiceIds));
//    }
}
