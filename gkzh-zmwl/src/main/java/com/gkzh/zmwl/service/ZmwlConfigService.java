package com.gkzh.zmwl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zmwl.domain.ZmwlDirection;
import com.gkzh.zmwl.domain.ZmwlSetting;
import com.gkzh.zmwl.mapper.ZmwlDirectionMapper;
import com.gkzh.zmwl.mapper.ZmwlSettingMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 职面未来配置服务：8 个未来发展方向与规则参数。
 * 学生端只读取启用方向；后台配置页读取全部方向，便于停用或改名。
 */
@Service
public class ZmwlConfigService {

    private final ZmwlDirectionMapper directionMapper;
    private final ZmwlSettingMapper settingMapper;

    public ZmwlConfigService(ZmwlDirectionMapper directionMapper, ZmwlSettingMapper settingMapper) {
        this.directionMapper = directionMapper;
        this.settingMapper = settingMapper;
    }

    /** 查询全部发展方向（含停用），供后台配置页使用。 */
    public List<ZmwlDirection> listAll() {
        return directionMapper.selectList(new QueryWrapper<ZmwlDirection>()
                .orderByAsc("sort_order").orderByAsc("direction_id"));
    }

    /** 查询启用方向，供学生端展示与选择使用。 */
    public List<ZmwlDirection> listActive() {
        return directionMapper.selectList(new QueryWrapper<ZmwlDirection>()
                .eq("status", "0").orderByAsc("sort_order").orderByAsc("direction_id"));
    }

    /** 按编码查询启用方向；不存在或已停用时返回 null。 */
    public ZmwlDirection findActive(String directionCode) {
        if (directionCode == null || directionCode.trim().isEmpty()) {
            return null;
        }
        return directionMapper.selectOne(new QueryWrapper<ZmwlDirection>()
                .eq("direction_code", directionCode.trim()).eq("status", "0"));
    }

    /** 查询规则参数；配置行缺失时返回默认值，避免历史库升级后学生端无法进入。 */
    public ZmwlSetting getSetting() {
        ZmwlSetting setting = settingMapper.selectById(1);
        if (setting != null) {
            return setting;
        }
        ZmwlSetting fallback = new ZmwlSetting();
        fallback.setSettingId(1);
        fallback.setAllowReselect("0");
        fallback.setEnabled("1");
        return fallback;
    }

    /**
     * 保存单个发展方向。方向编码是历史统计与记录快照的依据，创建后不允许修改。
     *
     * @param direction 页面提交的方向数据，必须携带 directionId
     * @return 保存后的方向
     */
    public ZmwlDirection saveDirection(ZmwlDirection direction) {
        if (direction == null || direction.getDirectionId() == null) {
            throw new ServiceException("发展方向编号不能为空");
        }
        if (isBlank(direction.getName())) {
            throw new ServiceException("发展方向名称不能为空");
        }
        ZmwlDirection existing = directionMapper.selectById(direction.getDirectionId());
        if (existing == null) {
            throw new ServiceException("发展方向不存在，请先执行数据库迁移脚本");
        }
        existing.setName(direction.getName().trim());
        existing.setShortIntro(trimToNull(direction.getShortIntro()));
        existing.setTagName(trimToNull(direction.getTagName()));
        existing.setInterpretation(trimToNull(direction.getInterpretation()));
        existing.setMessage(trimToNull(direction.getMessage()));
        existing.setIconUrl(trimToNull(direction.getIconUrl()));
        existing.setSortOrder(direction.getSortOrder() == null ? existing.getSortOrder() : direction.getSortOrder());
        existing.setStatus("1".equals(direction.getStatus()) ? "1" : "0");
        existing.setUpdateTime(DateUtils.getNowDate());
        directionMapper.updateById(existing);
        return existing;
    }

    /**
     * 保存规则参数。现场只维护“是否启用”和“确认后是否允许重新选择”两个开关。
     *
     * @param setting 页面提交的规则参数
     * @return 保存后的规则参数
     */
    public ZmwlSetting saveSetting(ZmwlSetting setting) {
        if (setting == null) {
            throw new ServiceException("规则参数不能为空");
        }
        ZmwlSetting existing = settingMapper.selectById(1);
        boolean creating = existing == null;
        if (creating) {
            existing = new ZmwlSetting();
            existing.setSettingId(1);
            existing.setCreateTime(DateUtils.getNowDate());
        }
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
