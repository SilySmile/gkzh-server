package com.gkzh.xycc.service;

import java.util.List;
import com.gkzh.xycc.domain.Career;

/**
 * 职业方向Service接口
 * 
 * @author gkzh
 * @date 2025-06-15
 */
public interface ICareerService 
{
    /**
     * 查询职业方向
     * 
     * @param careerId 职业方向主键
     * @return 职业方向
     */
    public Career selectCareerByCareerId(Long careerId);

    /**
     * 查询职业方向列表
     * 
     * @param career 职业方向
     * @return 职业方向集合
     */
    public List<Career> selectCareerList(Career career);

    /**
     * 新增职业方向
     * 
     * @param career 职业方向
     * @return 结果
     */
    public int insertCareer(Career career);

    /**
     * 修改职业方向
     * 
     * @param career 职业方向
     * @return 结果
     */
    public int updateCareer(Career career);

    /**
     * 批量删除职业方向
     * 
     * @param careerIds 需要删除的职业方向主键集合
     * @return 结果
     */
    public int deleteCareerByCareerIds(Long[] careerIds);

    /**
     * 删除职业方向信息
     * 
     * @param careerId 职业方向主键
     * @return 结果
     */
    public int deleteCareerByCareerId(Long careerId);
}
