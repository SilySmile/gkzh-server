package com.gkzh.zytj.service.impl;

import java.util.List;
import com.gkzh.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.zytj.mapper.GkzhMbtiProductMapper;
import com.gkzh.zytj.domain.GkzhMbtiProduct;
import com.gkzh.zytj.service.IGkzhMbtiProductService;

/**
 * 职愿探究-MBTI商品Service业务层处理
 * 
 * @author gkzh
 * @date 2026-06-02
 */
@Service
public class GkzhMbtiProductServiceImpl implements IGkzhMbtiProductService 
{
    @Autowired
    private GkzhMbtiProductMapper gkzhMbtiProductMapper;

    /**
     * 查询职愿探究-MBTI商品
     * 
     * @param productId 职愿探究-MBTI商品主键
     * @return 职愿探究-MBTI商品
     */
    @Override
    public GkzhMbtiProduct selectGkzhMbtiProductByProductId(Long productId)
    {
        return gkzhMbtiProductMapper.selectGkzhMbtiProductByProductId(productId);
    }

    /**
     * 查询职愿探究-MBTI商品列表
     * 
     * @param gkzhMbtiProduct 职愿探究-MBTI商品
     * @return 职愿探究-MBTI商品
     */
    @Override
    public List<GkzhMbtiProduct> selectGkzhMbtiProductList(GkzhMbtiProduct gkzhMbtiProduct)
    {
        return gkzhMbtiProductMapper.selectGkzhMbtiProductList(gkzhMbtiProduct);
    }

    /**
     * 新增职愿探究-MBTI商品
     * 
     * @param gkzhMbtiProduct 职愿探究-MBTI商品
     * @return 结果
     */
    @Override
    public int insertGkzhMbtiProduct(GkzhMbtiProduct gkzhMbtiProduct)
    {
        gkzhMbtiProduct.setCreateTime(DateUtils.getNowDate());
        return gkzhMbtiProductMapper.insertGkzhMbtiProduct(gkzhMbtiProduct);
    }

    /**
     * 修改职愿探究-MBTI商品
     * 
     * @param gkzhMbtiProduct 职愿探究-MBTI商品
     * @return 结果
     */
    @Override
    public int updateGkzhMbtiProduct(GkzhMbtiProduct gkzhMbtiProduct)
    {
        gkzhMbtiProduct.setUpdateTime(DateUtils.getNowDate());
        return gkzhMbtiProductMapper.updateGkzhMbtiProduct(gkzhMbtiProduct);
    }

    /**
     * 批量删除职愿探究-MBTI商品
     * 
     * @param productIds 需要删除的职愿探究-MBTI商品主键
     * @return 结果
     */
    @Override
    public int deleteGkzhMbtiProductByProductIds(Long[] productIds)
    {
        return gkzhMbtiProductMapper.deleteGkzhMbtiProductByProductIds(productIds);
    }

    /**
     * 删除职愿探究-MBTI商品信息
     * 
     * @param productId 职愿探究-MBTI商品主键
     * @return 结果
     */
    @Override
    public int deleteGkzhMbtiProductByProductId(Long productId)
    {
        return gkzhMbtiProductMapper.deleteGkzhMbtiProductByProductId(productId);
    }
}
