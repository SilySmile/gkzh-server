package com.gkzh.lottery.mapper;

import java.util.List;
import com.gkzh.lottery.domain.LotteryPrize;

/**
 * 抽奖奖品Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface LotteryPrizeMapper 
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
     * 删除抽奖奖品
     * 
     * @param prizeId 抽奖奖品主键
     * @return 结果
     */
    public int deleteLotteryPrizeByPrizeId(Long prizeId);

    /**
     * 批量删除抽奖奖品
     * 
     * @param prizeIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLotteryPrizeByPrizeIds(Long[] prizeIds);
}
