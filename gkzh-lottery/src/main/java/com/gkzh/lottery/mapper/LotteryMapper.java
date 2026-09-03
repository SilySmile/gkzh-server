package com.gkzh.lottery.mapper;

import java.util.List;
import com.gkzh.lottery.domain.Lottery;

/**
 * 抽奖环节Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-16
 */
public interface LotteryMapper 
{
    /**
     * 查询抽奖环节
     * 
     * @param lotteryId 抽奖环节主键
     * @return 抽奖环节
     */
    public Lottery selectLotteryByLotteryId(Long lotteryId);

    /**
     * 查询抽奖环节列表
     * 
     * @param Lottery 抽奖环节
     * @return 抽奖环节集合
     */
    public List<Lottery> selectLotteryList(Lottery Lottery);

    /**
     * 新增抽奖环节
     * 
     * @param Lottery 抽奖环节
     * @return 结果
     */
    public int insertLottery(Lottery Lottery);

    /**
     * 修改抽奖环节
     * 
     * @param Lottery 抽奖环节
     * @return 结果
     */
    public int updateLottery(Lottery Lottery);

    /**
     * 删除抽奖环节
     * 
     * @param lotteryId 抽奖环节主键
     * @return 结果
     */
    public int deleteLotteryByLotteryId(Long lotteryId);

    /**
     * 批量删除抽奖环节
     * 
     * @param lotteryIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLotteryByLotteryIds(Long[] lotteryIds);
}
