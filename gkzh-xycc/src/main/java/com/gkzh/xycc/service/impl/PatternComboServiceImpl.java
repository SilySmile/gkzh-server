package com.gkzh.xycc.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gkzh.common.utils.StringUtils;
import com.gkzh.xycc.domain.*;
import com.gkzh.xycc.mapper.UserSelectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.xycc.mapper.PatternComboMapper;
import com.gkzh.xycc.service.IPatternComboService;

/**
 * 编码组合Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-15
 */
@Service
public class PatternComboServiceImpl implements IPatternComboService 
{
    @Autowired
    private PatternComboMapper patternComboMapper;

    @Autowired
    private UserSelectionMapper userSelectionMapper;

    /**
     * 查询编码组合
     * 
     * @param patternComboId 编码组合主键
     * @return 编码组合
     */
    @Override
    public PatternCombo selectPatternComboByPatternComboId(Long patternComboId)
    {
        PatternCombo patternCombo = patternComboMapper.selectPatternComboByPatternComboId(patternComboId);
        if (patternCombo != null) {
            patternCombo.setCareerIds(patternComboMapper.selectCareerIdsByComboId(patternComboId));
            patternCombo.setEnvIds(patternComboMapper.selectEnvIdsByComboId(patternComboId));
        }
        return patternCombo;
    }

    /**
     * 查询编码组合列表
     * 
     * @param patternCombo 编码组合
     * @return 编码组合
     */
    @Override
    public List<PatternCombo> selectPatternComboList(PatternCombo patternCombo)
    {
        return patternComboMapper.selectPatternComboList(patternCombo);
    }

    /**
     * 新增编码组合
     * 
     * @param patternCombo 编码组合
     * @return 结果
     */
    @Override
    public int insertPatternCombo(PatternCombo patternCombo)
    {
        try {
            int rows = patternComboMapper.insertPatternCombo(patternCombo);
            insertPatternComboCareer(patternCombo);
            insertPatternComboEnv(patternCombo);
            return rows;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 修改编码组合
     * 
     * @param patternCombo 编码组合
     * @return 结果
     */
    @Override
    public int updatePatternCombo(PatternCombo patternCombo)
    {
        // 删除原有关联
        patternComboMapper.deletePatternComboCareerByComboId(patternCombo.getPatternComboId());
        patternComboMapper.deletePatternComboEnvByComboId(patternCombo.getPatternComboId());
        // 添加新的关联
        insertPatternComboCareer(patternCombo);
        insertPatternComboEnv(patternCombo);
        return patternComboMapper.updatePatternCombo(patternCombo);
    }

    /**
     * 批量删除编码组合
     * 
     * @param patternComboIds 需要删除的编码组合主键
     * @return 结果
     */
    @Override
    public int deletePatternComboByPatternComboIds(Long[] patternComboIds)
    {
        patternComboMapper.deletePatternComboCareerByComboIds(patternComboIds);
        patternComboMapper.deletePatternComboEnvByComboIds(patternComboIds);
        return patternComboMapper.deletePatternComboByPatternComboIds(patternComboIds);
    }

    /**
     * 删除编码组合信息
     * 
     * @param patternComboId 编码组合主键
     * @return 结果
     */
    @Override
    public int deletePatternComboByPatternComboId(Long patternComboId)
    {
        patternComboMapper.deletePatternComboCareerByComboId(patternComboId);
        patternComboMapper.deletePatternComboEnvByComboId(patternComboId);
        return patternComboMapper.deletePatternComboByPatternComboId(patternComboId);
    }

    @Override
    public List<Career> selectCareerByCodeGroup(String codeGroup) {
        return patternComboMapper.selectCareersByCodeGroup(codeGroup);
    }

    @Override
    public List<WorkEnv> selectWorkEnvByCodeGroup(String codeGroup) {
        return patternComboMapper.selectWorkEnvsByCodeGroup(codeGroup);
    }

    @Override
    public Map getXyccResult(Long activityId, Long userId) {
        UserSelection userSelection= userSelectionMapper.getUserSelectionByActivityIdAndUserId(activityId,userId);
        String code = userSelection.getPatternComboCode();
        List<Career> careers = selectCareerByCodeGroup(code);
        List<WorkEnv> workEnvs = selectWorkEnvByCodeGroup(code);
        HashMap<Object, Object> ret = new HashMap<>();
        ret.put("code",code);
        ret.put("careers",careers);
        ret.put("workEnvs",workEnvs);
        return ret;
    }

    /**
     * 新增编码组合 - 职业方向 关联信息
     *
     * @param patternCombo 编码组合对象
     */
    public void insertPatternComboCareer(PatternCombo patternCombo)
    {
        List<Long> careerIds = patternCombo.getCareerIds();
        Long patternComboId = patternCombo.getPatternComboId();
        if (StringUtils.isNotEmpty(careerIds))
        {
            List<PatternComboCareer> list = new ArrayList<PatternComboCareer>();
            for (Long careerId : careerIds)
            {
                PatternComboCareer pcc = new PatternComboCareer();
                pcc.setCareerId(careerId);
                pcc.setComboId(patternComboId);
                list.add(pcc);
            }
            if (list.size() > 0)
            {
                patternComboMapper.batchPatternComboCareer(list);
            }
        }
    }
    /**
     * 新增编码组合 - 工作环境偏好 关联信息
     *
     * @param patternCombo 编码组合对象
     */
    public void insertPatternComboEnv(PatternCombo patternCombo)
    {
        List<Long> envIds = patternCombo.getEnvIds();
        Long patternComboId = patternCombo.getPatternComboId();
        if (StringUtils.isNotEmpty(envIds))
        {
            List<PatternComboEnv> list = new ArrayList<PatternComboEnv>();
            for (Long envId : envIds)
            {
                PatternComboEnv pce = new PatternComboEnv();
                pce.setEnvId(envId);
                pce.setComboId(patternComboId);
                list.add(pce);
            }
            if (list.size() > 0)
            {
                patternComboMapper.batchPatternComboEnv(list);
            }
        }
    }
}
