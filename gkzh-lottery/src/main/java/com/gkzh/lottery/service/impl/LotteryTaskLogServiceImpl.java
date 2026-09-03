package com.gkzh.lottery.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.lottery.mapper.LotteryTaskLogMapper;
import com.gkzh.lottery.domain.LotteryTaskLog;
import com.gkzh.lottery.service.ILotteryTaskLogService;

/**
 * 完成记录Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Service
public class LotteryTaskLogServiceImpl implements ILotteryTaskLogService 
{
    @Autowired
    private LotteryTaskLogMapper lotteryTaskLogMapper;

    /**
     * 查询完成记录
     * 
     * @param logId 完成记录主键
     * @return 完成记录
     */
    @Override
    public LotteryTaskLog selectLotteryTaskLogByLogId(Long logId)
    {
        return lotteryTaskLogMapper.selectLotteryTaskLogByLogId(logId);
    }

    /**
     * 查询完成记录列表
     * 
     * @param lotteryTaskLog 完成记录
     * @return 完成记录
     */
    @Override
    public List<LotteryTaskLog> selectLotteryTaskLogList(LotteryTaskLog lotteryTaskLog)
    {
        return lotteryTaskLogMapper.selectLotteryTaskLogList(lotteryTaskLog);
    }

    /**
     * 新增完成记录
     * 
     * @param lotteryTaskLog 完成记录
     * @return 结果
     */
    @Override
    public int insertLotteryTaskLog(LotteryTaskLog lotteryTaskLog)
    {
        return lotteryTaskLogMapper.insertLotteryTaskLog(lotteryTaskLog);
    }

    /**
     * 修改完成记录
     * 
     * @param lotteryTaskLog 完成记录
     * @return 结果
     */
    @Override
    public int updateLotteryTaskLog(LotteryTaskLog lotteryTaskLog)
    {
        return lotteryTaskLogMapper.updateLotteryTaskLog(lotteryTaskLog);
    }

    /**
     * 批量删除完成记录
     * 
     * @param logIds 需要删除的完成记录主键
     * @return 结果
     */
    @Override
    public int deleteLotteryTaskLogByLogIds(Long[] logIds)
    {
        return lotteryTaskLogMapper.deleteLotteryTaskLogByLogIds(logIds);
    }

    /**
     * 删除完成记录信息
     * 
     * @param logId 完成记录主键
     * @return 结果
     */
    @Override
    public int deleteLotteryTaskLogByLogId(Long logId)
    {
        return lotteryTaskLogMapper.deleteLotteryTaskLogByLogId(logId);
    }
}
