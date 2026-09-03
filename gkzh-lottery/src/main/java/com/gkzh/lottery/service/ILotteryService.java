package com.gkzh.lottery.service;

import java.util.List;

import com.gkzh.lottery.domain.Lottery;

/**
 * 抽奖环节Service接口
 * 
 * @author gkzh
 * @date 2025-06-16
 */
public interface ILotteryService 
{
    /**
     * 查询抽奖环节
     * 
     * @param activityId 抽奖环节主键
     * @return 抽奖环节
     */
    public Lottery selectLotteryByLotteryId(Long activityId);

    /**
     * 查询抽奖环节列表
     * 
     * @param lottery 抽奖环节
     * @return 抽奖环节集合
     */
    public List<Lottery> selectLotteryList(Lottery lottery);

    /**
     * 新增抽奖环节
     * 
     * @param lottery 抽奖环节
     * @return 结果
     */
    public int insertLottery(Lottery lottery);

    /**
     * 修改抽奖环节
     * 
     * @param lottery 抽奖环节
     * @return 结果
     */
    public int updateLottery(Lottery lottery);

    /**
     * 批量删除抽奖环节
     * 
     * @param activityIds 需要删除的抽奖环节主键集合
     * @return 结果
     */
    public int deleteLotteryByLotteryIds(Long[] activityIds);

    /**
     * 删除抽奖环节信息
     * 
     * @param activityId 抽奖环节主键
     * @return 结果
     */
    public int deleteLotteryByLotteryId(Long activityId);


    /**
     * 修改活动状态
     *
     * @param lottery 活动
     * @return 结果
     */
    public int updateLotteryStatus(Lottery lottery);
}
