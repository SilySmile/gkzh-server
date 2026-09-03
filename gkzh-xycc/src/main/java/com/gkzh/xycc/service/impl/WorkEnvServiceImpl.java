package com.gkzh.xycc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.xycc.mapper.WorkEnvMapper;
import com.gkzh.xycc.domain.WorkEnv;
import com.gkzh.xycc.service.IWorkEnvService;

/**
 * 工作环境偏好Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-15
 */
@Service
public class WorkEnvServiceImpl implements IWorkEnvService 
{
    @Autowired
    private WorkEnvMapper workEnvMapper;

    /**
     * 查询工作环境偏好
     * 
     * @param workEnvId 工作环境偏好主键
     * @return 工作环境偏好
     */
    @Override
    public WorkEnv selectWorkEnvByWorkEnvId(Long workEnvId)
    {
        return workEnvMapper.selectWorkEnvByWorkEnvId(workEnvId);
    }

    /**
     * 查询工作环境偏好列表
     * 
     * @param workEnv 工作环境偏好
     * @return 工作环境偏好
     */
    @Override
    public List<WorkEnv> selectWorkEnvList(WorkEnv workEnv)
    {
        return workEnvMapper.selectWorkEnvList(workEnv);
    }

    /**
     * 新增工作环境偏好
     * 
     * @param workEnv 工作环境偏好
     * @return 结果
     */
    @Override
    public int insertWorkEnv(WorkEnv workEnv)
    {
        return workEnvMapper.insertWorkEnv(workEnv);
    }

    /**
     * 修改工作环境偏好
     * 
     * @param workEnv 工作环境偏好
     * @return 结果
     */
    @Override
    public int updateWorkEnv(WorkEnv workEnv)
    {
        return workEnvMapper.updateWorkEnv(workEnv);
    }

    /**
     * 批量删除工作环境偏好
     * 
     * @param workEnvIds 需要删除的工作环境偏好主键
     * @return 结果
     */
    @Override
    public int deleteWorkEnvByWorkEnvIds(Long[] workEnvIds)
    {
        return workEnvMapper.deleteWorkEnvByWorkEnvIds(workEnvIds);
    }

    /**
     * 删除工作环境偏好信息
     * 
     * @param workEnvId 工作环境偏好主键
     * @return 结果
     */
    @Override
    public int deleteWorkEnvByWorkEnvId(Long workEnvId)
    {
        return workEnvMapper.deleteWorkEnvByWorkEnvId(workEnvId);
    }
}
