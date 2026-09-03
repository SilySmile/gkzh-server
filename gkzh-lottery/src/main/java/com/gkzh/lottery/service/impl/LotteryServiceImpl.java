package com.gkzh.lottery.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.lottery.mapper.LotteryMapper;
import com.gkzh.lottery.domain.Lottery;
import com.gkzh.lottery.service.ILotteryService;

/**
 * 抽奖环节Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-16
 */
@Service
public class LotteryServiceImpl implements ILotteryService
{
    @Autowired
    private LotteryMapper lotteryMapper;

    /**
     * 查询抽奖环节
     * 
     * @param lotteryId 抽奖环节主键
     * @return 抽奖环节
     */
    @Override
    public Lottery selectLotteryByLotteryId(Long lotteryId)
    {
        return lotteryMapper.selectLotteryByLotteryId(lotteryId);
    }

    /**
     * 查询抽奖环节列表
     * 
     * @param lotteryLottery 抽奖环节
     * @return 抽奖环节
     */
    @Override
    public List<Lottery> selectLotteryList(Lottery lotteryLottery)
    {
        return lotteryMapper.selectLotteryList(lotteryLottery);
    }

    /**
     * 新增抽奖环节
     * 
     * @param lotteryLottery 抽奖环节
     * @return 结果
     */
    @Override
    public int insertLottery(Lottery lotteryLottery)
    {
        return lotteryMapper.insertLottery(lotteryLottery);
    }

    /**
     * 修改抽奖环节
     * 
     * @param lottery 抽奖环节
     * @return 结果
     */
    @Override
    public int updateLottery(Lottery lottery)
    {
        return lotteryMapper.updateLottery(lottery);
    }

    /**
     * 批量删除抽奖环节
     * 
     * @param lotteryIds 需要删除的抽奖环节主键
     * @return 结果
     */
    @Override
    public int deleteLotteryByLotteryIds(Long[] lotteryIds)
    {
        return lotteryMapper.deleteLotteryByLotteryIds(lotteryIds);
    }

    /**
     * 删除抽奖环节信息
     * 
     * @param lotteryId 抽奖环节主键
     * @return 结果
     */
    @Override
    public int deleteLotteryByLotteryId(Long lotteryId)
    {
        return lotteryMapper.deleteLotteryByLotteryId(lotteryId);
    }

    /**
     * 修改活动状态
     *
     * @param lotteryLottery 抽奖环节
     * @return 结果
     */
    @Override
    public int updateLotteryStatus(Lottery lotteryLottery) {
        return lotteryMapper.updateLottery(lotteryLottery);
    }
}
