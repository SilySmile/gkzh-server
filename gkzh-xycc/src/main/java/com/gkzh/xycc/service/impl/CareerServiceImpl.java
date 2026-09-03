package com.gkzh.xycc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.xycc.mapper.CareerMapper;
import com.gkzh.xycc.domain.Career;
import com.gkzh.xycc.service.ICareerService;

/**
 * 职业方向Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-15
 */
@Service
public class CareerServiceImpl implements ICareerService 
{
    @Autowired
    private CareerMapper careerMapper;

    /**
     * 查询职业方向
     * 
     * @param careerId 职业方向主键
     * @return 职业方向
     */
    @Override
    public Career selectCareerByCareerId(Long careerId)
    {
        return careerMapper.selectCareerByCareerId(careerId);
    }

    /**
     * 查询职业方向列表
     * 
     * @param career 职业方向
     * @return 职业方向
     */
    @Override
    public List<Career> selectCareerList(Career career)
    {
        return careerMapper.selectCareerList(career);
    }

    /**
     * 新增职业方向
     * 
     * @param career 职业方向
     * @return 结果
     */
    @Override
    public int insertCareer(Career career)
    {
        return careerMapper.insertCareer(career);
    }

    /**
     * 修改职业方向
     * 
     * @param career 职业方向
     * @return 结果
     */
    @Override
    public int updateCareer(Career career)
    {
        return careerMapper.updateCareer(career);
    }

    /**
     * 批量删除职业方向
     * 
     * @param careerIds 需要删除的职业方向主键
     * @return 结果
     */
    @Override
    public int deleteCareerByCareerIds(Long[] careerIds)
    {
        return careerMapper.deleteCareerByCareerIds(careerIds);
    }

    /**
     * 删除职业方向信息
     * 
     * @param careerId 职业方向主键
     * @return 结果
     */
    @Override
    public int deleteCareerByCareerId(Long careerId)
    {
        return careerMapper.deleteCareerByCareerId(careerId);
    }
}
