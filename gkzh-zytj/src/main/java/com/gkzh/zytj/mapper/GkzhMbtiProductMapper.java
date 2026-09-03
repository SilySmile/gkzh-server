package com.gkzh.zytj.mapper;

import java.util.List;
import com.gkzh.zytj.domain.GkzhMbtiProduct;

/**
 * 职愿探究-MBTI商品Mapper接口
 * 
 * @author gkzh
 * @date 2026-06-02
 */
public interface GkzhMbtiProductMapper 
{
    /**
     * 查询职愿探究-MBTI商品
     * 
     * @param productId 职愿探究-MBTI商品主键
     * @return 职愿探究-MBTI商品
     */
    public GkzhMbtiProduct selectGkzhMbtiProductByProductId(Long productId);

    /**
     * 查询职愿探究-MBTI商品列表
     * 
     * @param gkzhMbtiProduct 职愿探究-MBTI商品
     * @return 职愿探究-MBTI商品集合
     */
    public List<GkzhMbtiProduct> selectGkzhMbtiProductList(GkzhMbtiProduct gkzhMbtiProduct);

    /**
     * 新增职愿探究-MBTI商品
     * 
     * @param gkzhMbtiProduct 职愿探究-MBTI商品
     * @return 结果
     */
    public int insertGkzhMbtiProduct(GkzhMbtiProduct gkzhMbtiProduct);

    /**
     * 修改职愿探究-MBTI商品
     * 
     * @param gkzhMbtiProduct 职愿探究-MBTI商品
     * @return 结果
     */
    public int updateGkzhMbtiProduct(GkzhMbtiProduct gkzhMbtiProduct);

    /**
     * 删除职愿探究-MBTI商品
     * 
     * @param productId 职愿探究-MBTI商品主键
     * @return 结果
     */
    public int deleteGkzhMbtiProductByProductId(Long productId);

    /**
     * 批量删除职愿探究-MBTI商品
     * 
     * @param productIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGkzhMbtiProductByProductIds(Long[] productIds);
}
