package com.gkzh.jlcz.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.jlcz.domain.JlczSetting;
import com.gkzh.jlcz.domain.JlczWeight;
import com.gkzh.jlcz.mapper.JlczSettingMapper;
import com.gkzh.jlcz.mapper.JlczWeightMapper;
import com.gkzh.jlcz.support.JlczScoring;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 简历称重配置服务：20 个砝码与天平规则参数。
 * 学生端与后台使用同一份配置；停用砝码不出现在学生端，也不参与计分。
 */
@Service
public class JlczConfigService {

    private final JlczWeightMapper weightMapper;
    private final JlczSettingMapper settingMapper;

    public JlczConfigService(JlczWeightMapper weightMapper, JlczSettingMapper settingMapper) {
        this.weightMapper = weightMapper;
        this.settingMapper = settingMapper;
    }

    /** 查询全部砝码（含停用），供后台配置页使用。 */
    public List<JlczWeight> listAll() {
        return weightMapper.selectList(new QueryWrapper<JlczWeight>()
                .orderByAsc("weight_no"));
    }

    /** 查询启用砝码，供学生端展示与计分使用。 */
    public List<JlczWeight> listActive() {
        return weightMapper.selectList(new QueryWrapper<JlczWeight>()
                .eq("status", "0").orderByAsc("weight_no"));
    }

    /** 查询规则参数；配置行缺失时返回默认值，避免历史库升级后学生端无法进入。 */
    public JlczSetting getSetting() {
        JlczSetting setting = settingMapper.selectById(1);
        if (setting != null) {
            return setting;
        }
        JlczSetting fallback = new JlczSetting();
        fallback.setSettingId(1);
        fallback.setLeftWeightG(80);
        fallback.setPassWeightG(80);
        fallback.setTimeLimitSeconds(0);
        fallback.setAllowRetry("1");
        fallback.setEnabled("1");
        return fallback;
    }

    /**
     * 保存单个砝码配置。挂钩号固定为 1-20，不允许通过接口新增或删除。
     *
     * @param weight 页面提交的砝码数据，必须携带 weightNo
     * @return 保存后的砝码
     */
    public JlczWeight saveWeight(JlczWeight weight) {
        if (weight == null || weight.getWeightNo() == null) {
            throw new ServiceException("砝码编号不能为空");
        }
        if (weight.getWeightNo() < 1 || weight.getWeightNo() > 20) {
            throw new ServiceException("砝码编号必须为 1 到 20");
        }
        if (isBlank(weight.getLabel())) {
            throw new ServiceException("砝码标签不能为空");
        }
        if (weight.getWeightG() == null || weight.getWeightG() <= 0 || weight.getWeightG() > 100) {
            throw new ServiceException("砝码重量必须为 1 到 100 克");
        }
        String category = trim(weight.getCategoryCode());
        if (category == null || JlczScoring.categoryIndex(category) >= JlczScoring.CATEGORY_ORDER.length) {
            throw new ServiceException("砝码大类必须为硬通货、软实力、基本功或加分项");
        }
        JlczWeight existing = weightMapper.selectOne(new QueryWrapper<JlczWeight>()
                .eq("weight_no", weight.getWeightNo()));
        if (existing == null) {
            throw new ServiceException("砝码配置不存在，请先执行数据库迁移脚本");
        }
        existing.setCategoryCode(category);
        existing.setCategoryName(isBlank(weight.getCategoryName())
                ? JlczScoring.categoryName(category) : weight.getCategoryName().trim());
        existing.setLabel(weight.getLabel().trim());
        existing.setIcon(isBlank(weight.getIcon()) ? null : weight.getIcon().trim());
        existing.setImageUrl(isBlank(weight.getImageUrl()) ? null : weight.getImageUrl().trim());
        existing.setWeightG(weight.getWeightG());
        existing.setConditionText(isBlank(weight.getConditionText()) ? null : weight.getConditionText().trim());
        existing.setSortOrder(weight.getSortOrder() == null ? weight.getWeightNo() : weight.getSortOrder());
        existing.setStatus("1".equals(weight.getStatus()) ? "1" : "0");
        existing.setUpdateTime(DateUtils.getNowDate());
        weightMapper.updateById(existing);
        return existing;
    }

    /**
     * 保存规则参数。左侧标准砝码与通关阈值分开维护，便于现场按活动调整。
     *
     * @param setting 页面提交的规则参数
     * @return 保存后的规则参数
     */
    public JlczSetting saveSetting(JlczSetting setting) {
        if (setting == null) {
            throw new ServiceException("规则参数不能为空");
        }
        if (setting.getLeftWeightG() == null || setting.getLeftWeightG() <= 0 || setting.getLeftWeightG() > 1000) {
            throw new ServiceException("左侧标准砝码重量必须为 1 到 1000 克");
        }
        if (setting.getPassWeightG() == null || setting.getPassWeightG() <= 0 || setting.getPassWeightG() > 1000) {
            throw new ServiceException("通关阈值必须为 1 到 1000 克");
        }
        if (setting.getTimeLimitSeconds() == null || setting.getTimeLimitSeconds() < 0
                || setting.getTimeLimitSeconds() > 3600) {
            throw new ServiceException("称重限时必须在 0 到 3600 秒之间");
        }
        JlczSetting existing = settingMapper.selectById(1);
        boolean creating = existing == null;
        if (creating) {
            existing = new JlczSetting();
            existing.setSettingId(1);
            existing.setCreateTime(DateUtils.getNowDate());
        }
        existing.setLeftWeightG(setting.getLeftWeightG());
        existing.setPassWeightG(setting.getPassWeightG());
        existing.setTimeLimitSeconds(setting.getTimeLimitSeconds());
        existing.setAllowRetry("0".equals(setting.getAllowRetry()) ? "0" : "1");
        existing.setEnabled("0".equals(setting.getEnabled()) ? "0" : "1");
        existing.setUpdateTime(DateUtils.getNowDate());
        if (creating) {
            settingMapper.insert(existing);
        } else {
            settingMapper.updateById(existing);
        }
        return existing;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trim(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
