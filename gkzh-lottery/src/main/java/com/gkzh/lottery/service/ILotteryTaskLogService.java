package com.gkzh.lottery.service;

import java.util.List;
import com.gkzh.lottery.domain.LotteryTaskLog;

/**
 * 完成记录Service接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface ILotteryTaskLogService 
{
    /**
     * 查询完成记录
     * 
     * @param logId 完成记录主键
     * @return 完成记录
     */
    public LotteryTaskLog selectLotteryTaskLogByLogId(Long logId);

    /**
     * 查询完成记录列表
     * 
     * @param lotteryTaskLog 完成记录
     * @return 完成记录集合
     */
    public List<LotteryTaskLog> selectLotteryTaskLogList(LotteryTaskLog lotteryTaskLog);

    /**
     * 新增完成记录
     * 
     * @param lotteryTaskLog 完成记录
     * @return 结果
     */
    public int insertLotteryTaskLog(LotteryTaskLog lotteryTaskLog);

    /**
     * 修改完成记录
     * 
     * @param lotteryTaskLog 完成记录
     * @return 结果
     */
    public int updateLotteryTaskLog(LotteryTaskLog lotteryTaskLog);

    /**
     * 批量删除完成记录
     * 
     * @param logIds 需要删除的完成记录主键集合
     * @return 结果
     */
    public int deleteLotteryTaskLogByLogIds(Long[] logIds);

    /**
     * 删除完成记录信息
     * 
     * @param logId 完成记录主键
     * @return 结果
     */
    public int deleteLotteryTaskLogByLogId(Long logId);
}
