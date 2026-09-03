package com.gkzh.lottery.mapper;

import java.util.List;
import com.gkzh.lottery.domain.LotteryTask;

/**
 *  前置任务Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public interface LotteryTaskMapper 
{
    /**
     * 查询 前置任务
     * 
     * @param taskId  前置任务主键
     * @return  前置任务
     */
    public LotteryTask selectLotteryTaskByTaskId(Long taskId);

    /**
     * 查询 前置任务列表
     * 
     * @param lotteryTask  前置任务
     * @return  前置任务集合
     */
    public List<LotteryTask> selectLotteryTaskList(LotteryTask lotteryTask);

    /**
     * 新增 前置任务
     * 
     * @param lotteryTask  前置任务
     * @return 结果
     */
    public int insertLotteryTask(LotteryTask lotteryTask);

    /**
     * 修改 前置任务
     * 
     * @param lotteryTask  前置任务
     * @return 结果
     */
    public int updateLotteryTask(LotteryTask lotteryTask);

    /**
     * 删除 前置任务
     * 
     * @param taskId  前置任务主键
     * @return 结果
     */
    public int deleteLotteryTaskByTaskId(Long taskId);

    /**
     * 批量删除 前置任务
     * 
     * @param taskIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLotteryTaskByTaskIds(Long[] taskIds);
}
