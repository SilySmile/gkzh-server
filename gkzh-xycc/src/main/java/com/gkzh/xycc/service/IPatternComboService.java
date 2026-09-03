package com.gkzh.xycc.service;

import java.util.List;
import java.util.Map;

import com.gkzh.xycc.domain.Career;
import com.gkzh.xycc.domain.PatternCombo;
import com.gkzh.xycc.domain.WorkEnv;

/**
 * 编码组合Service接口
 * 
 * @author gkzh
 * @date 2025-06-15
 */
public interface IPatternComboService 
{
    /**
     * 查询编码组合
     * 
     * @param patternComboId 编码组合主键
     * @return 编码组合
     */
    public PatternCombo selectPatternComboByPatternComboId(Long patternComboId);

    /**
     * 查询编码组合列表
     * 
     * @param patternCombo 编码组合
     * @return 编码组合集合
     */
    public List<PatternCombo> selectPatternComboList(PatternCombo patternCombo);

    /**
     * 新增编码组合
     * 
     * @param patternCombo 编码组合
     * @return 结果
     */
    public int insertPatternCombo(PatternCombo patternCombo);

    /**
     * 修改编码组合
     * 
     * @param patternCombo 编码组合
     * @return 结果
     */
    public int updatePatternCombo(PatternCombo patternCombo);

    /**
     * 批量删除编码组合
     * 
     * @param patternComboIds 需要删除的编码组合主键集合
     * @return 结果
     */
    public int deletePatternComboByPatternComboIds(Long[] patternComboIds);

    /**
     * 删除编码组合信息
     * 
     * @param patternComboId 编码组合主键
     * @return 结果
     */
    public int deletePatternComboByPatternComboId(Long patternComboId);

    /**
     * 根据编码组合查询对应的职业偏好和职业方向
     * 
     * @param codeGroup 编码组合
     * @return 编码组合
     */
    public List<Career> selectCareerByCodeGroup(String codeGroup);

    /**
     * 根据编码组合查询对应的职业偏好和职业方向
     * 
     * @param codeGroup 编码组合
     * @return 编码组合
     */
    public List<WorkEnv> selectWorkEnvByCodeGroup(String codeGroup);

    public Map getXyccResult(Long activityId, Long userId);
}
