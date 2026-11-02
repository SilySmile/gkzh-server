package com.gkzh.zyjdc.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zyjdc.domain.ZyjdcRole;
import com.gkzh.zyjdc.mapper.ZyjdcRoleMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职业角斗场角色配置服务。
 * 8 个角色固定存在，后台只能维护文案、头像、排序和启停，不能新增或删除角色。
 */
@Service
public class ZyjdcConfigService {

    /** 棋子元数据缓存有效期：配置保存后会立即清除，未保存时最多缓存 60 秒。 */
    private static final long META_CACHE_MILLIS = 60_000L;

    private final ZyjdcRoleMapper roleMapper;

    /** 棋子头像、名称和等级缓存，避免每一步落子都查询角色表。 */
    private volatile Map<String, Object> pieceMetaCache;

    /** 棋子元数据缓存加载时间。 */
    private volatile long pieceMetaLoadedAt;

    public ZyjdcConfigService(ZyjdcRoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    /** 学生端可选角色：只返回启用中的角色，按配置顺序排列。 */
    public List<ZyjdcRole> listActive() {
        return roleMapper.selectList(new QueryWrapper<ZyjdcRole>()
                .eq("status", "0")
                .orderByAsc("sort_order")
                .orderByAsc("role_id"));
    }

    /** 后台角色列表：包含停用角色，便于重新启用。 */
    public List<ZyjdcRole> listAll() {
        return roleMapper.selectList(new QueryWrapper<ZyjdcRole>()
                .orderByAsc("sort_order")
                .orderByAsc("role_id"));
    }

    /** 按编号查询角色；不存在返回 null。 */
    public ZyjdcRole get(Long roleId) {
        return roleId == null ? null : roleMapper.selectById(roleId);
    }

    /**
     * 棋子头像、名称、等级和职场定位的轻量元数据。
     * 每一步落子都需要返回给小程序，使用短期缓存减少角色表查询。
     */
    public Map<String, Object> pieceMeta() {
        long now = System.currentTimeMillis();
        Map<String, Object> cache = pieceMetaCache;
        if (cache != null && now - pieceMetaLoadedAt < META_CACHE_MILLIS) {
            return cache;
        }
        synchronized (this) {
            if (pieceMetaCache != null && System.currentTimeMillis() - pieceMetaLoadedAt < META_CACHE_MILLIS) {
                return pieceMetaCache;
            }
            Map<String, Object> meta = new LinkedHashMap<>();
            for (ZyjdcRole item : listAll()) {
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("roleName", item.getRoleName());
                value.put("imageUrl", item.getImageUrl());
                value.put("rankLevel", item.getRankLevel());
                value.put("jobTitle", item.getJobTitle());
                meta.put(item.getRoleCode(), value);
            }
            pieceMetaCache = meta;
            pieceMetaLoadedAt = System.currentTimeMillis();
            return meta;
        }
    }

    /**
     * 保存角色展示内容。
     * 编码和等级决定对弈规则，不允许后台修改；其余文案、头像、排序和状态可以维护。
     */
    public int save(ZyjdcRole input) {
        if (input == null || input.getRoleId() == null) {
            throw new ServiceException("角色编号不能为空");
        }
        ZyjdcRole existing = roleMapper.selectById(input.getRoleId());
        if (existing == null) {
            throw new ServiceException("角色不存在");
        }
        if (isBlank(input.getRoleName())) {
            throw new ServiceException("动物名称不能为空");
        }
        if (isBlank(input.getJobTitle())) {
            throw new ServiceException("职场定位不能为空");
        }
        String status = isBlank(input.getStatus()) ? existing.getStatus() : input.getStatus().trim();
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ServiceException("角色状态只能为启用或停用");
        }
        if (input.getImageUrl() != null && input.getImageUrl().length() > 500) {
            throw new ServiceException("头像地址过长，请重新上传");
        }

        existing.setRoleName(input.getRoleName().trim());
        existing.setJobTitle(input.getJobTitle().trim());
        existing.setImageUrl(input.getImageUrl());
        existing.setSurvivalRule(input.getSurvivalRule());
        existing.setCareerAbility(input.getCareerAbility());
        existing.setRoleInterpretation(input.getRoleInterpretation());
        existing.setPositioning(input.getPositioning());
        existing.setAdvantage(input.getAdvantage());
        existing.setSortOrder(input.getSortOrder() == null ? existing.getSortOrder() : input.getSortOrder());
        existing.setStatus(status);
        existing.setUpdateTime(DateUtils.getNowDate());
        int rows = roleMapper.updateById(existing);
        // 后台保存头像/名称后立即清空缓存，下一步落子即可看到新素材。
        pieceMetaCache = null;
        return rows;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
