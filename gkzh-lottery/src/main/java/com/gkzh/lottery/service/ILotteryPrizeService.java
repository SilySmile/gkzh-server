package com.gkzh.lottery.service;

import java.util.List;
import java.math.BigDecimal;

import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.lottery.domain.LotteryPrize;

/**
 * 抽奖奖品Service接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface ILotteryPrizeService 
{
    /**
     * 查询抽奖奖品
     * 
     * @param prizeId 抽奖奖品主键
     * @return 抽奖奖品
     */
    public LotteryPrize selectLotteryPrizeByPrizeId(Long prizeId);

    /**
     * 查询抽奖奖品列表
     * 
     * @param lotteryPrize 抽奖奖品
     * @return 抽奖奖品集合
     */
    public List<LotteryPrize> selectLotteryPrizeList(LotteryPrize lotteryPrize);

    /**
     * 查询活动奖品列表（包含实际概率计算）
     * 
     * @param activityId 活动ID
     * @return 抽奖奖品集合
     */
    public List<LotteryPrize> selectActivityPrizesWithProbability(Long activityId);

    /**
     * 根据权重随机选择奖品
     * 
     * @param activityId 活动ID
     * @return 中奖奖品
     */
    public LotteryPrize selectRandomPrizeByWeight(Long activityId);

    /**
     * 计算奖品实际概率
     * 
     * @param prizes 奖品列表
     * @return 包含实际概率的奖品列表
     */
    public List<LotteryPrize> calculateActualProbability(List<LotteryPrize> prizes);

    /**
     * 新增抽奖奖品
     * 
     * @param lotteryPrize 抽奖奖品
     * @return 结果
     */
    public int insertLotteryPrize(LotteryPrize lotteryPrize);

    /**
     * 修改抽奖奖品
     * 
     * @param lotteryPrize 抽奖奖品
     * @return 结果
     */
    public int updateLotteryPrize(LotteryPrize lotteryPrize);

    /**
     * 批量删除抽奖奖品
     * 
     * @param prizeIds 需要删除的抽奖奖品主键集合
     * @return 结果
     */
    public int deleteLotteryPrizeByPrizeIds(Long[] prizeIds);

    /**
     * 删除抽奖奖品信息
     * 
     * @param prizeId 抽奖奖品主键
     * @return 结果
     */
    public int deleteLotteryPrizeByPrizeId(Long prizeId);

    public LotteryPrize drawPrize(Long activityId, StudentCheckin studentCheckin);

    public List<LotteryPrize> selectPrizesByLotteryId(Long lotteryId);

    public LotteryPrize drawPrizeByLottery(Long lotteryId, StudentCheckin studentCheckin, String bizType, Long activityId);
}
