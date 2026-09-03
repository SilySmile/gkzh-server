package com.gkzh.lottery.service;

import java.util.List;
import com.gkzh.lottery.domain.LotteryRecord;

/**
 * 抽奖记录Service接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface ILotteryRecordService 
{
    /**
     * 查询抽奖记录
     * 
     * @param recordId 抽奖记录主键
     * @return 抽奖记录
     */
    public LotteryRecord selectLotteryRecordByRecordId(Long recordId);

    /**
     * 查询抽奖记录列表
     * 
     * @param lotteryRecord 抽奖记录
     * @return 抽奖记录集合
     */
    public List<LotteryRecord> selectLotteryRecordList(LotteryRecord lotteryRecord);

    /**
     * 新增抽奖记录
     * 
     * @param lotteryRecord 抽奖记录
     * @return 结果
     */
    public int insertLotteryRecord(LotteryRecord lotteryRecord);

    /**
     * 修改抽奖记录
     * 
     * @param lotteryRecord 抽奖记录
     * @return 结果
     */
    public int updateLotteryRecord(LotteryRecord lotteryRecord);

    /**
     * 批量删除抽奖记录
     * 
     * @param recordIds 需要删除的抽奖记录主键集合
     * @return 结果
     */
    public int deleteLotteryRecordByRecordIds(Long[] recordIds);

    /**
     * 删除抽奖记录信息
     * 
     * @param recordId 抽奖记录主键
     * @return 结果
     */
    public int deleteLotteryRecordByRecordId(Long recordId);
}
