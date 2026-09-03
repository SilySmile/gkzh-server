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
import com.gkzh.xycc.domain.Career;
import com.gkzh.xycc.service.ICareerService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 职业方向Controller
 * 
 * @author gkzh
 * @date 2025-06-15
 */
@RestController
@RequestMapping("/xycc/career")
public class CareerController extends BaseController
{
    @Autowired
    private ICareerService careerService;

    /**
     * 查询职业方向列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:career:list')")
    @GetMapping("/list")
    public TableDataInfo list(Career career)
    {
        startPage();
        startOrderBy();
        List<Career> list = careerService.selectCareerList(career);
        return getDataTable(list);
    }

    /**
     * 导出职业方向列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:career:export')")
    @Log(title = "职业方向", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Career career)
    {
        List<Career> list = careerService.selectCareerList(career);
        ExcelUtil<Career> util = new ExcelUtil<Career>(Career.class);
        util.exportExcel(response, list, "职业方向数据");
    }

    /**
     * 获取职业方向详细信息
     */
    @PreAuthorize("@ss.hasPermi('xycc:career:query')")
    @GetMapping(value = "/{careerId}")
    public AjaxResult getInfo(@PathVariable("careerId") Long careerId)
    {
        return success(careerService.selectCareerByCareerId(careerId));
    }

    /**
     * 新增职业方向
     */
    @PreAuthorize("@ss.hasPermi('xycc:career:add')")
    @Log(title = "职业方向", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Career career)
    {
        return toAjax(careerService.insertCareer(career));
    }

    /**
     * 修改职业方向
     */
    @PreAuthorize("@ss.hasPermi('xycc:career:edit')")
    @Log(title = "职业方向", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Career career)
    {
        return toAjax(careerService.updateCareer(career));
    }

    /**
     * 删除职业方向
     */
    @PreAuthorize("@ss.hasPermi('xycc:career:remove')")
    @Log(title = "职业方向", businessType = BusinessType.DELETE)
	@DeleteMapping("/{careerIds}")
    public AjaxResult remove(@PathVariable Long[] careerIds)
    {
        return toAjax(careerService.deleteCareerByCareerIds(careerIds));
    }
}
