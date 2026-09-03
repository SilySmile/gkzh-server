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
import com.gkzh.zytj.domain.GkzhMbtiProduct;
import com.gkzh.zytj.service.IGkzhMbtiProductService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 职愿探究-MBTI商品Controller
 * 
 * @author gkzh
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/zytj/product")
public class GkzhMbtiProductController extends BaseController
{
    @Autowired
    private IGkzhMbtiProductService gkzhMbtiProductService;

    /**
     * 查询职愿探究-MBTI商品列表
     */
    @PreAuthorize("@ss.hasPermi('zytj:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(GkzhMbtiProduct gkzhMbtiProduct)
    {
        startPage();
        List<GkzhMbtiProduct> list = gkzhMbtiProductService.selectGkzhMbtiProductList(gkzhMbtiProduct);
        return getDataTable(list);
    }

    /**
     * 导出职愿探究-MBTI商品列表
     */
    @PreAuthorize("@ss.hasPermi('zytj:product:export')")
    @Log(title = "职愿探究-MBTI商品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GkzhMbtiProduct gkzhMbtiProduct)
    {
        List<GkzhMbtiProduct> list = gkzhMbtiProductService.selectGkzhMbtiProductList(gkzhMbtiProduct);
        ExcelUtil<GkzhMbtiProduct> util = new ExcelUtil<GkzhMbtiProduct>(GkzhMbtiProduct.class);
        util.exportExcel(response, list, "职愿探究-MBTI商品数据");
    }

    /**
     * 获取职愿探究-MBTI商品详细信息
     */
    @PreAuthorize("@ss.hasPermi('zytj:product:query')")
    @GetMapping(value = "/{productId}")
    public AjaxResult getInfo(@PathVariable("productId") Long productId)
    {
        return success(gkzhMbtiProductService.selectGkzhMbtiProductByProductId(productId));
    }

    /**
     * 新增职愿探究-MBTI商品
     */
    @PreAuthorize("@ss.hasPermi('zytj:product:add')")
    @Log(title = "职愿探究-MBTI商品", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GkzhMbtiProduct gkzhMbtiProduct)
    {
        return toAjax(gkzhMbtiProductService.insertGkzhMbtiProduct(gkzhMbtiProduct));
    }

    /**
     * 修改职愿探究-MBTI商品
     */
    @PreAuthorize("@ss.hasPermi('zytj:product:edit')")
    @Log(title = "职愿探究-MBTI商品", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GkzhMbtiProduct gkzhMbtiProduct)
    {
        return toAjax(gkzhMbtiProductService.updateGkzhMbtiProduct(gkzhMbtiProduct));
    }

    /**
     * 删除职愿探究-MBTI商品
     */
    @PreAuthorize("@ss.hasPermi('zytj:product:remove')")
    @Log(title = "职愿探究-MBTI商品", businessType = BusinessType.DELETE)
	@DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds)
    {
        return toAjax(gkzhMbtiProductService.deleteGkzhMbtiProductByProductIds(productIds));
    }
}
