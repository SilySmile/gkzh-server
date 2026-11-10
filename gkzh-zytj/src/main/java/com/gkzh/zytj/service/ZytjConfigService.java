package com.gkzh.zytj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zytj.domain.ZytjItem;
import com.gkzh.zytj.domain.ZytjSetting;
import com.gkzh.zytj.domain.ZytjType;
import com.gkzh.zytj.mapper.ZytjItemMapper;
import com.gkzh.zytj.mapper.ZytjSettingMapper;
import com.gkzh.zytj.mapper.ZytjTypeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 职愿探究配置服务：8 个橱窗物品、16 型人格对照与规则参数。
 * 学生端只读启用物品与规则参数；后台配置页读取全部数据，便于停用、改名或换图。
 */
@Service
public class ZytjConfigService {

    /** 维度列序与维度代码的对应关系：第 1 列 EI、第 2 列 SN、第 3 列 TF、第 4 列 JP。 */
    public static final String[] COLUMN_DIMENSIONS = {"EI", "SN", "TF", "JP"};

    private final ZytjItemMapper itemMapper;
    private final ZytjTypeMapper typeMapper;
    private final ZytjSettingMapper settingMapper;

    public ZytjConfigService(ZytjItemMapper itemMapper, ZytjTypeMapper typeMapper,
                             ZytjSettingMapper settingMapper) {
        this.itemMapper = itemMapper;
        this.typeMapper = typeMapper;
        this.settingMapper = settingMapper;
    }

    /** 查询全部物品（含停用），供后台配置页使用。 */
    public List<ZytjItem> listItems() {
        return itemMapper.selectList(new QueryWrapper<ZytjItem>()
                .orderByAsc("column_index").orderByAsc("row_index").orderByAsc("sort_order"));
    }

    /** 查询启用物品，供学生端橱窗展示与选择使用。 */
    public List<ZytjItem> listActiveItems() {
        return itemMapper.selectList(new QueryWrapper<ZytjItem>()
                .eq("status", "0")
                .orderByAsc("column_index").orderByAsc("row_index").orderByAsc("sort_order"));
    }

    /** 按物品编码查询启用物品；不存在或已停用时返回 null。 */
    public ZytjItem findActiveItem(String itemCode) {
        if (isBlank(itemCode)) {
            return null;
        }
        return itemMapper.selectOne(new QueryWrapper<ZytjItem>()
                .eq("item_code", itemCode.trim().toUpperCase()).eq("status", "0"));
    }

    /** 查询某个维度列下的启用物品，超时补全时按行序取第一个。 */
    public List<ZytjItem> listActiveItemsByColumn(int columnIndex) {
        return itemMapper.selectList(new QueryWrapper<ZytjItem>()
                .eq("status", "0").eq("column_index", columnIndex)
                .orderByAsc("row_index").orderByAsc("sort_order"));
    }

    /** 查询全部 16 型，供后台配置页使用。 */
    public List<ZytjType> listTypes() {
        return typeMapper.selectList(new QueryWrapper<ZytjType>()
                .orderByAsc("sort_order").orderByAsc("type_id"));
    }

    /** 查询全部启用型别代码，用于校验生成的人格代码是否已配置。 */
    public List<ZytjType> listActiveTypes() {
        return typeMapper.selectList(new QueryWrapper<ZytjType>()
                .eq("status", "0").orderByAsc("sort_order").orderByAsc("type_id"));
    }

    /** 按四位代码查询型别；未配置时返回 null，由调用方决定是否用默认文案兜底。 */
    public ZytjType findType(String typeCode) {
        if (isBlank(typeCode)) {
            return null;
        }
        return typeMapper.selectOne(new QueryWrapper<ZytjType>()
                .eq("type_code", typeCode.trim().toUpperCase()));
    }

    /** 查询规则参数；配置行缺失时返回默认值，避免历史库升级后学生端无法进入。 */
    public ZytjSetting getSetting() {
        ZytjSetting setting = settingMapper.selectById(1);
        if (setting != null) {
            if (setting.getTimeLimit() == null) {
                setting.setTimeLimit(30);
            }
            return setting;
        }
        ZytjSetting fallback = new ZytjSetting();
        fallback.setSettingId(1);
        fallback.setTimeLimit(30);
        fallback.setAllowReselect("0");
        fallback.setEnabled("1");
        return fallback;
    }

    /**
     * 保存单个橱窗物品。物品编码即维度字母，是记录与统计的依据，创建后不允许修改。
     *
     * @param item 页面提交的物品数据，必须携带 itemId
     * @return 保存后的物品
     */
    public ZytjItem saveItem(ZytjItem item) {
        if (item == null || item.getItemId() == null) {
            throw new ServiceException("物品编号不能为空");
        }
        if (isBlank(item.getItemName())) {
            throw new ServiceException("物品名称不能为空");
        }
        ZytjItem existing = itemMapper.selectById(item.getItemId());
        if (existing == null) {
            throw new ServiceException("物品不存在，请先执行数据库迁移脚本");
        }
        existing.setItemName(item.getItemName().trim());
        existing.setImageUrl(trimToNull(item.getImageUrl()));
        existing.setMetaphor(trimToNull(item.getMetaphor()));
        existing.setSortOrder(item.getSortOrder() == null ? existing.getSortOrder() : item.getSortOrder());
        existing.setStatus("1".equals(item.getStatus()) ? "1" : "0");
        existing.setUpdateBy("admin");
        existing.setUpdateTime(DateUtils.getNowDate());
        itemMapper.updateById(existing);
        return existing;
    }

    /**
     * 保存单个人格类型文案。类型代码是记录快照依据，创建后不允许修改。
     *
     * @param type 页面提交的型别数据，必须携带 typeId
     * @return 保存后的型别
     */
    public ZytjType saveType(ZytjType type) {
        if (type == null || type.getTypeId() == null) {
            throw new ServiceException("人格类型编号不能为空");
        }
        ZytjType existing = typeMapper.selectById(type.getTypeId());
        if (existing == null) {
            throw new ServiceException("人格类型不存在，请先执行数据库迁移脚本");
        }
        existing.setFeature(trimToNull(type.getFeature()));
        existing.setCareerField(trimToNull(type.getCareerField()));
        existing.setTypicalCareers(trimToNull(type.getTypicalCareers()));
        existing.setSortOrder(type.getSortOrder() == null ? existing.getSortOrder() : type.getSortOrder());
        existing.setStatus("1".equals(type.getStatus()) ? "1" : "0");
        existing.setUpdateTime(DateUtils.getNowDate());
        typeMapper.updateById(existing);
        return existing;
    }

    /**
     * 保存规则参数。现场维护“单局限时”“是否启用”“提交后是否允许重新探究”。
     *
     * @param setting 页面提交的规则参数
     * @return 保存后的规则参数
     */
    public ZytjSetting saveSetting(ZytjSetting setting) {
        if (setting == null) {
            throw new ServiceException("规则参数不能为空");
        }
        ZytjSetting existing = settingMapper.selectById(1);
        boolean creating = existing == null;
        if (creating) {
            existing = new ZytjSetting();
            existing.setSettingId(1);
            existing.setCreateTime(DateUtils.getNowDate());
        }
        Integer limit = setting.getTimeLimit();
        if (limit == null || limit < 0) {
            throw new ServiceException("单局限时不能小于 0 秒");
        }
        existing.setTimeLimit(limit);
        existing.setAllowReselect("1".equals(setting.getAllowReselect()) ? "1" : "0");
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

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
