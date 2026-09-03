package com.gkzh.xycc.service;

import java.util.List;
import com.gkzh.xycc.domain.WorkEnv;

/**
 * 工作环境偏好Service接口
 * 
 * @author gkzh
 * @date 2025-06-15
 */
public interface IWorkEnvService 
{
    /**
     * 查询工作环境偏好
     * 
     * @param workEnvId 工作环境偏好主键
     * @return 工作环境偏好
     */
    public WorkEnv selectWorkEnvByWorkEnvId(Long workEnvId);

    /**
     * 查询工作环境偏好列表
     * 
     * @param workEnv 工作环境偏好
     * @return 工作环境偏好集合
     */
    public List<WorkEnv> selectWorkEnvList(WorkEnv workEnv);

    /**
     * 新增工作环境偏好
     * 
     * @param workEnv 工作环境偏好
     * @return 结果
     */
    public int insertWorkEnv(WorkEnv workEnv);

    /**
     * 修改工作环境偏好
     * 
     * @param workEnv 工作环境偏好
     * @return 结果
     */
    public int updateWorkEnv(WorkEnv workEnv);

    /**
     * 批量删除工作环境偏好
     * 
     * @param workEnvIds 需要删除的工作环境偏好主键集合
     * @return 结果
     */
    public int deleteWorkEnvByWorkEnvIds(Long[] workEnvIds);

    /**
     * 删除工作环境偏好信息
     * 
     * @param workEnvId 工作环境偏好主键
     * @return 结果
     */
    public int deleteWorkEnvByWorkEnvId(Long workEnvId);
}
