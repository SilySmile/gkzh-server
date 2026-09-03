package com.gkzh.lottery.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.lottery.mapper.LotteryTaskMapper;
import com.gkzh.lottery.domain.LotteryTask;
import com.gkzh.lottery.service.ILotteryTaskService;

/**
 *  前置任务Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Service
public class LotteryTaskServiceImpl implements ILotteryTaskService 
{
    @Autowired
    private LotteryTaskMapper lotteryTaskMapper;

    /**
     * 查询 前置任务
     * 
     * @param taskId  前置任务主键
     * @return  前置任务
     */
    @Override
    public LotteryTask selectLotteryTaskByTaskId(Long taskId)
    {
        return lotteryTaskMapper.selectLotteryTaskByTaskId(taskId);
    }

    /**
     * 查询 前置任务列表
     * 
     * @param lotteryTask  前置任务
     * @return  前置任务
     */
    @Override
    public List<LotteryTask> selectLotteryTaskList(LotteryTask lotteryTask)
    {
        return lotteryTaskMapper.selectLotteryTaskList(lotteryTask);
    }

    /**
     * 新增 前置任务
     * 
     * @param lotteryTask  前置任务
     * @return 结果
     */
    @Override
    public int insertLotteryTask(LotteryTask lotteryTask)
    {
        return lotteryTaskMapper.insertLotteryTask(lotteryTask);
    }

    /**
     * 修改 前置任务
     * 
     * @param lotteryTask  前置任务
     * @return 结果
     */
    @Override
    public int updateLotteryTask(LotteryTask lotteryTask)
    {
        return lotteryTaskMapper.updateLotteryTask(lotteryTask);
    }

    /**
     * 批量删除 前置任务
     * 
     * @param taskIds 需要删除的 前置任务主键
     * @return 结果
     */
    @Override
    public int deleteLotteryTaskByTaskIds(Long[] taskIds)
    {
        return lotteryTaskMapper.deleteLotteryTaskByTaskIds(taskIds);
    }

    /**
     * 删除 前置任务信息
     * 
     * @param taskId  前置任务主键
     * @return 结果
     */
    @Override
    public int deleteLotteryTaskByTaskId(Long taskId)
    {
        return lotteryTaskMapper.deleteLotteryTaskByTaskId(taskId);
    }
}
