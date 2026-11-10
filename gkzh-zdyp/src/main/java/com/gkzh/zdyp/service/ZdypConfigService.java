package com.gkzh.zdyp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zdyp.domain.ZdypCareer;
import com.gkzh.zdyp.domain.ZdypCard;
import com.gkzh.zdyp.domain.ZdypSetting;
import com.gkzh.zdyp.mapper.ZdypCareerMapper;
import com.gkzh.zdyp.mapper.ZdypCardMapper;
import com.gkzh.zdyp.mapper.ZdypSettingMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职得一拼配置服务：10 套岗位、40 张维度卡片与规则参数。
 * 学生端只读取启用岗位与启用卡片；后台配置页读取全部数据，便于停用或改文案。
 */
@Service
public class ZdypConfigService {

    /** 四个卡槽（维度）编码与中文名，顺序与设计稿一致：左上、右上、左下、右下。 */
    public static final Map<String, String> SLOTS;

    static {
        Map<String, String> slots = new LinkedHashMap<>();
        slots.put("content", "工作内容");
        slots.put("skill", "核心技能");
        slots.put("weakness", "职业缺点");
        slots.put("prospect", "发展前景");
        SLOTS = java.util.Collections.unmodifiableMap(slots);
    }

    private final ZdypCareerMapper careerMapper;
    private final ZdypCardMapper cardMapper;
    private final ZdypSettingMapper settingMapper;

    public ZdypConfigService(ZdypCareerMapper careerMapper, ZdypCardMapper cardMapper,
                             ZdypSettingMapper settingMapper) {
        this.careerMapper = careerMapper;
        this.cardMapper = cardMapper;
        this.settingMapper = settingMapper;
    }

    /** 查询全部岗位（含停用），供后台配置页使用。 */
    public List<ZdypCareer> listAllCareers() {
        return careerMapper.selectList(new QueryWrapper<ZdypCareer>()
                .orderByAsc("sort_order").orderByAsc("career_id"));
    }

    /** 查询启用岗位，供学生端选择使用。 */
    public List<ZdypCareer> listActiveCareers() {
        return careerMapper.selectList(new QueryWrapper<ZdypCareer>()
                .eq("status", "0").orderByAsc("sort_order").orderByAsc("career_id"));
    }

    /** 按岗位编号查询启用岗位；不存在或已停用时返回 null。 */
    public ZdypCareer findActiveCareer(Long careerId) {
        if (careerId == null) {
            return null;
        }
        ZdypCareer career = careerMapper.selectById(careerId);
        return career != null && "0".equals(career.getStatus()) ? career : null;
    }

    /** 查询岗位下全部卡片（含停用），供后台配置页使用。 */
    public List<ZdypCard> listAllCards(Long careerId) {
        return cardMapper.selectList(new QueryWrapper<ZdypCard>()
                .eq("career_id", careerId).orderByAsc("sort_order").orderByAsc("card_id"));
    }

    /** 查询岗位下启用卡片；拼图要求恰好四张，数量不足时由调用方给出业务提示。 */
    public List<ZdypCard> listActiveCards(Long careerId) {
        return cardMapper.selectList(new QueryWrapper<ZdypCard>()
                .eq("career_id", careerId).eq("status", "0")
                .orderByAsc("sort_order").orderByAsc("card_id"));
    }

    /** 把卡片按维度索引，便于判定与生成正确答案。 */
    public Map<String, ZdypCard> cardsByDimension(Long careerId) {
        Map<String, ZdypCard> map = new LinkedHashMap<>();
        for (ZdypCard card : listActiveCards(careerId)) {
            map.put(card.getDimensionCode(), card);
        }
        return map;
    }

    /** 查询规则参数；配置行缺失时返回默认值，避免历史库升级后学生端无法进入。 */
    public ZdypSetting getSetting() {
        ZdypSetting setting = settingMapper.selectById(1);
        if (setting != null) {
            return setting;
        }
        ZdypSetting fallback = new ZdypSetting();
        fallback.setSettingId(1);
        fallback.setTimeLimit(45);
        fallback.setEnabled("1");
        return fallback;
    }

    /**
     * 保存单个岗位。岗位编码是历史统计与记录快照的依据，创建后不允许修改。
     *
     * @param career 页面提交的岗位数据，必须携带 careerId
     * @return 保存后的岗位
     */
    public ZdypCareer saveCareer(ZdypCareer career) {
        if (career == null || career.getCareerId() == null) {
            throw new ServiceException("岗位编号不能为空");
        }
        if (isBlank(career.getCareerName())) {
            throw new ServiceException("岗位名称不能为空");
        }
        ZdypCareer existing = careerMapper.selectById(career.getCareerId());
        if (existing == null) {
            throw new ServiceException("岗位不存在，请先执行数据库迁移脚本");
        }
        existing.setCareerName(career.getCareerName().trim());
        existing.setIntro(trimToNull(career.getIntro()));
        existing.setIndustryValue(trimToNull(career.getIndustryValue()));
        existing.setNewcomerAdvice(trimToNull(career.getNewcomerAdvice()));
        existing.setSortOrder(career.getSortOrder() == null ? existing.getSortOrder() : career.getSortOrder());
        existing.setStatus("1".equals(career.getStatus()) ? "1" : "0");
        existing.setUpdateTime(DateUtils.getNowDate());
        careerMapper.updateById(existing);
        return existing;
    }

    /**
     * 保存单张卡片。卡片维度是判定与统计依据，只允许修改正文、排序与启停用。
     *
     * @param card 页面提交的卡片数据，必须携带 cardId
     * @return 保存后的卡片
     */
    public ZdypCard saveCard(ZdypCard card) {
        if (card == null || card.getCardId() == null) {
            throw new ServiceException("卡片编号不能为空");
        }
        if (isBlank(card.getCardText())) {
            throw new ServiceException("卡片内容不能为空");
        }
        ZdypCard existing = cardMapper.selectById(card.getCardId());
        if (existing == null) {
            throw new ServiceException("卡片不存在，请先执行数据库迁移脚本");
        }
        existing.setCardText(card.getCardText().trim());
        existing.setSortOrder(card.getSortOrder() == null ? existing.getSortOrder() : card.getSortOrder());
        existing.setStatus("1".equals(card.getStatus()) ? "1" : "0");
        existing.setUpdateTime(DateUtils.getNowDate());
        cardMapper.updateById(existing);
        return existing;
    }

    /**
     * 保存规则参数。限时按秒维护，0 表示不限时；停用后学生端一律拒绝进入。
     *
     * @param setting 页面提交的规则参数
     * @return 保存后的规则参数
     */
    public ZdypSetting saveSetting(ZdypSetting setting) {
        if (setting == null) {
            throw new ServiceException("规则参数不能为空");
        }
        int limit = setting.getTimeLimit() == null ? 45 : setting.getTimeLimit();
        if (limit < 0) {
            throw new ServiceException("限时不能为负数");
        }
        ZdypSetting existing = settingMapper.selectById(1);
        boolean creating = existing == null;
        if (creating) {
            existing = new ZdypSetting();
            existing.setSettingId(1);
            existing.setCreateTime(DateUtils.getNowDate());
        }
        existing.setTimeLimit(limit);
        existing.setEnabled("0".equals(setting.getEnabled()) ? "0" : "1");
        existing.setUpdateTime(DateUtils.getNowDate());
        if (creating) {
            settingMapper.insert(existing);
        } else {
            settingMapper.updateById(existing);
        }
        return existing;
    }

    /** 后台配置页需要的“岗位 + 卡片”整体结构。 */
    public List<Map<String, Object>> careerConfigs() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ZdypCareer career : listAllCareers()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("career", career);
            row.put("cards", listAllCards(career.getCareerId()));
            rows.add(row);
        }
        return rows;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
