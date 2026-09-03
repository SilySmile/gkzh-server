package com.gkzh.xycc.mapper;

import java.util.List;

import com.gkzh.xycc.domain.*;

/**
 * 编码组合Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-15
 */
public interface PatternComboMapper 
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
     * 删除编码组合
     * 
     * @param patternComboId 编码组合主键
     * @return 结果
     */
    public int deletePatternComboByPatternComboId(Long patternComboId);

    /**
     * 批量删除编码组合
     * 
     * @param patternComboIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePatternComboByPatternComboIds(Long[] patternComboIds);

    /**
     * 批量删除编码组合 - 职业方向 关联
     * 
     * @param patternComboIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePatternComboCareerByComboIds(Long[] patternComboIds);
    
    /**
     * 批量新增编码组合 - 职业方向 关联
     * 
     * @param patternComboCareerList 编码组合 - 职业方向 关联列表
     * @return 结果
     */
    public int batchPatternComboCareer(List<PatternComboCareer> patternComboCareerList);

    /**
     * 批量新增编码组合 - 工作环境偏好 关联
     *
     * @param patternComboEnvList 编码组合 - 职业方向 关联列表
     * @return 结果
     */
    public int batchPatternComboEnv(List<PatternComboEnv> patternComboEnvList);

    /**
     * 通过编码组合主键删除编码组合 - 职业方向 关联信息
     * 
     * @param patternComboId 编码组合ID
     * @return 结果
     */
    public int deletePatternComboCareerByComboId(Long patternComboId);

    /**
     * 通过编码组合主键删除编码组合 - 工作环境偏好 关联信息
     * 
     * @param comboId 编码组合ID
     * @return 结果
     */
    public int deletePatternComboEnvByComboId(Long comboId);

    /**
     * 批量删除编码组合 - 工作环境偏好 关联
     * 
     * @param comboIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePatternComboEnvByComboIds(Long[] comboIds);

    /**
     * 查询编码组合的职业方向ID列表
     *
     * @param comboId 编码组合ID
     * @return 职业方向ID列表
     */
    public List<Long> selectCareerIdsByComboId(Long comboId);

    /**
     * 查询编码组合的工作环境偏好ID列表
     *
     * @param comboId 编码组合ID
     * @return 工作环境偏好ID列表
     */
    public List<Long> selectEnvIdsByComboId(Long comboId);


    public List<Career> selectCareersByCodeGroup(String codeGroup);

    public List<WorkEnv> selectWorkEnvsByCodeGroup(String codeGroup);
}
