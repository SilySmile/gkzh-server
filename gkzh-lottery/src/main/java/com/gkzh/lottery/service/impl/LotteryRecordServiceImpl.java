package com.gkzh.lottery.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.lottery.mapper.LotteryRecordMapper;
import com.gkzh.lottery.domain.LotteryRecord;
import com.gkzh.lottery.service.ILotteryRecordService;

/**
 * 抽奖记录Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Service
public class LotteryRecordServiceImpl implements ILotteryRecordService 
{
    @Autowired
    private LotteryRecordMapper lotteryRecordMapper;

    /**
     * 查询抽奖记录
     * 
     * @param recordId 抽奖记录主键
     * @return 抽奖记录
     */
    @Override
    public LotteryRecord selectLotteryRecordByRecordId(Long recordId)
    {
        return lotteryRecordMapper.selectLotteryRecordByRecordId(recordId);
    }

    /**
     * 查询抽奖记录列表
     * 
     * @param lotteryRecord 抽奖记录
     * @return 抽奖记录
     */
    @Override
    public List<LotteryRecord> selectLotteryRecordList(LotteryRecord lotteryRecord)
    {
        return lotteryRecordMapper.selectLotteryRecordList(lotteryRecord);
    }

    /**
     * 新增抽奖记录
     * 
     * @param lotteryRecord 抽奖记录
     * @return 结果
     */
    @Override
    public int insertLotteryRecord(LotteryRecord lotteryRecord)
    {
        return lotteryRecordMapper.insertLotteryRecord(lotteryRecord);
    }

    /**
     * 修改抽奖记录
     * 
     * @param lotteryRecord 抽奖记录
     * @return 结果
     */
    @Override
    public int updateLotteryRecord(LotteryRecord lotteryRecord)
    {
        return lotteryRecordMapper.updateLotteryRecord(lotteryRecord);
    }

    /**
     * 批量删除抽奖记录
     * 
     * @param recordIds 需要删除的抽奖记录主键
     * @return 结果
     */
    @Override
    public int deleteLotteryRecordByRecordIds(Long[] recordIds)
    {
        return lotteryRecordMapper.deleteLotteryRecordByRecordIds(recordIds);
    }

    /**
     * 删除抽奖记录信息
     * 
     * @param recordId 抽奖记录主键
     * @return 结果
     */
    @Override
    public int deleteLotteryRecordByRecordId(Long recordId)
    {
        return lotteryRecordMapper.deleteLotteryRecordByRecordId(recordId);
    }
}
