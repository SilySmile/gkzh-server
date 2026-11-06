package com.gkzh.nlld.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.nlld.domain.NlldDimension;
import com.gkzh.nlld.domain.NlldOption;
import com.gkzh.nlld.domain.NlldRound;
import com.gkzh.nlld.mapper.NlldDimensionMapper;
import com.gkzh.nlld.mapper.NlldOptionMapper;
import com.gkzh.nlld.mapper.NlldRoundMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 能量雷达配置服务。
 * 15 个维度、6 个关卡和 30 件物品固定存在，后台只能维护文案、所属维度、排序和启停。
 */
@Service
public class NlldConfigService {

    /** 允许的维度分组。 */
    private static final List<String> GROUPS = java.util.Arrays.asList("内在价值", "外在与保障", "人际影响与生活方式");

    private final NlldDimensionMapper dimensionMapper;
    private final NlldRoundMapper roundMapper;
    private final NlldOptionMapper optionMapper;

    public NlldConfigService(NlldDimensionMapper dimensionMapper, NlldRoundMapper roundMapper,
                             NlldOptionMapper optionMapper) {
        this.dimensionMapper = dimensionMapper;
        this.roundMapper = roundMapper;
        this.optionMapper = optionMapper;
    }

    /** 全部维度，按配置顺序排列。 */
    public List<NlldDimension> listDimensions() {
        return dimensionMapper.selectList(new QueryWrapper<NlldDimension>()
                .orderByAsc("sort_order").orderByAsc("dimension_id"));
    }

    /** 学生端可用维度：只返回启用中的维度。 */
    public List<NlldDimension> listActiveDimensions() {
        return dimensionMapper.selectList(new QueryWrapper<NlldDimension>()
                .eq("status", "0")
                .orderByAsc("sort_order").orderByAsc("dimension_id"));
    }

    /** 学生端关卡配置：只包含启用关卡和启用物品，不暴露维度绑定。 */
    public List<Map<String, Object>> listStudentRounds() {
        List<NlldRound> rounds = roundMapper.selectList(new QueryWrapper<NlldRound>()
                .eq("status", "0").orderByAsc("round_no"));
        List<NlldOption> options = optionMapper.selectList(new QueryWrapper<NlldOption>()
                .eq("status", "0").orderByAsc("round_id").orderByAsc("sort_order"));
        Map<Long, List<NlldOption>> optionMap = new LinkedHashMap<>();
        for (NlldOption option : options) {
            optionMap.computeIfAbsent(option.getRoundId(), key -> new ArrayList<>()).add(option);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (NlldRound round : rounds) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("roundId", round.getRoundId());
            row.put("roundNo", round.getRoundNo());
            row.put("title", round.getTitle());
            row.put("scene", round.getScene());
            List<Map<String, Object>> items = new ArrayList<>();
            for (NlldOption option : optionMap.getOrDefault(round.getRoundId(), new ArrayList<>())) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("optionId", option.getOptionId());
                item.put("label", option.getOptionLabel());
                item.put("name", option.getOptionName());
                item.put("desc", option.getOptionDesc());
                items.add(item);
            }
            row.put("options", items);
            result.add(row);
        }
        return result;
    }

    /** 后台关卡列表：包含停用关卡和物品，并带出所属维度名称，便于维护。 */
    public List<Map<String, Object>> listAdminRounds() {
        List<NlldRound> rounds = roundMapper.selectList(new QueryWrapper<NlldRound>()
                .orderByAsc("round_no"));
        List<NlldOption> options = optionMapper.selectList(new QueryWrapper<NlldOption>()
                .orderByAsc("round_id").orderByAsc("sort_order"));
        Map<Long, NlldDimension> dimensionMap = new LinkedHashMap<>();
        for (NlldDimension dimension : listDimensions()) {
            dimensionMap.put(dimension.getDimensionId(), dimension);
        }
        Map<Long, List<NlldOption>> optionMap = new LinkedHashMap<>();
        for (NlldOption option : options) {
            optionMap.computeIfAbsent(option.getRoundId(), key -> new ArrayList<>()).add(option);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (NlldRound round : rounds) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("roundId", round.getRoundId());
            row.put("roundNo", round.getRoundNo());
            row.put("title", round.getTitle());
            row.put("scene", round.getScene());
            row.put("sortOrder", round.getSortOrder());
            row.put("status", round.getStatus());
            List<Map<String, Object>> items = new ArrayList<>();
            for (NlldOption option : optionMap.getOrDefault(round.getRoundId(), new ArrayList<>())) {
                NlldDimension dimension = dimensionMap.get(option.getDimensionId());
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("optionId", option.getOptionId());
                item.put("optionLabel", option.getOptionLabel());
                item.put("optionName", option.getOptionName());
                item.put("optionDesc", option.getOptionDesc());
                item.put("dimensionId", option.getDimensionId());
                item.put("dimensionName", dimension == null ? null : dimension.getDimensionName());
                item.put("sortOrder", option.getSortOrder());
                item.put("status", option.getStatus());
                items.add(item);
            }
            row.put("options", items);
            result.add(row);
        }
        return result;
    }

    /** 按编号查询关卡。 */
    public NlldRound getRound(Long roundId) {
        return roundId == null ? null : roundMapper.selectById(roundId);
    }

    /** 按关号查询启用关卡；不存在返回 null。 */
    public NlldRound getActiveRound(Integer roundNo) {
        if (roundNo == null) {
            return null;
        }
        return roundMapper.selectOne(new QueryWrapper<NlldRound>()
                .eq("round_no", roundNo).eq("status", "0").last("LIMIT 1"));
    }

    /** 按编号查询维度。 */
    public NlldDimension getDimension(Long dimensionId) {
        return dimensionId == null ? null : dimensionMapper.selectById(dimensionId);
    }

    /** 校验并保存维度文案；编码不允许修改。 */
    public int saveDimension(NlldDimension input) {
        if (input == null || input.getDimensionId() == null) {
            throw new ServiceException("维度编号不能为空");
        }
        NlldDimension existing = dimensionMapper.selectById(input.getDimensionId());
        if (existing == null) {
            throw new ServiceException("维度不存在");
        }
        if (isBlank(input.getDimensionName())) {
            throw new ServiceException("维度名称不能为空");
        }
        if (input.getDimensionName().trim().length() > 20) {
            throw new ServiceException("维度名称不能超过 20 个字");
        }
        String group = isBlank(input.getDimensionGroup()) ? existing.getDimensionGroup() : input.getDimensionGroup().trim();
        if (!GROUPS.contains(group)) {
            throw new ServiceException("维度分组只能为内在价值、外在与保障或人际影响与生活方式");
        }
        String status = normalizeStatus(input.getStatus(), existing.getStatus());
        existing.setDimensionName(input.getDimensionName().trim());
        existing.setDimensionGroup(group);
        existing.setSummary(input.getSummary());
        existing.setCareerReference(input.getCareerReference());
        existing.setSortOrder(input.getSortOrder() == null ? existing.getSortOrder() : input.getSortOrder());
        existing.setStatus(status);
        existing.setUpdateTime(DateUtils.getNowDate());
        return dimensionMapper.updateById(existing);
    }

    /** 校验并保存关卡名称与情景；关号不允许修改。 */
    public int saveRound(NlldRound input) {
        if (input == null || input.getRoundId() == null) {
            throw new ServiceException("关卡编号不能为空");
        }
        NlldRound existing = roundMapper.selectById(input.getRoundId());
        if (existing == null) {
            throw new ServiceException("关卡不存在");
        }
        if (isBlank(input.getTitle())) {
            throw new ServiceException("关卡名称不能为空");
        }
        if (input.getTitle().trim().length() > 50) {
            throw new ServiceException("关卡名称不能超过 50 个字");
        }
        if (input.getScene() != null && input.getScene().length() > 2000) {
            throw new ServiceException("任务情景不能超过 2000 个字");
        }
        String status = normalizeStatus(input.getStatus(), existing.getStatus());
        existing.setTitle(input.getTitle().trim());
        existing.setScene(input.getScene());
        existing.setSortOrder(input.getSortOrder() == null ? existing.getSortOrder() : input.getSortOrder());
        existing.setStatus(status);
        existing.setUpdateTime(DateUtils.getNowDate());
        return roundMapper.updateById(existing);
    }

    /** 校验并保存物品名称、描述、所属维度、排序和启停；关号和选项字母不允许修改。 */
    public int saveOption(NlldOption input) {
        if (input == null || input.getOptionId() == null) {
            throw new ServiceException("物品编号不能为空");
        }
        NlldOption existing = optionMapper.selectById(input.getOptionId());
        if (existing == null) {
            throw new ServiceException("物品不存在");
        }
        if (isBlank(input.getOptionName())) {
            throw new ServiceException("物品名称不能为空");
        }
        if (input.getOptionName().trim().length() > 100) {
            throw new ServiceException("物品名称不能超过 100 个字");
        }
        if (input.getOptionDesc() != null && input.getOptionDesc().length() > 1000) {
            throw new ServiceException("物品说明不能超过 1000 个字");
        }
        Long dimensionId = input.getDimensionId() == null ? existing.getDimensionId() : input.getDimensionId();
        NlldDimension dimension = dimensionMapper.selectById(dimensionId);
        if (dimension == null) {
            throw new ServiceException("所选维度不存在");
        }
        if (!dimensionId.equals(existing.getDimensionId())) {
            // 计分规则要求 15 个维度各对应 2 件物品；调整绑定后必须仍然满足，否则拒绝保存。
            List<NlldOption> all = optionMapper.selectList(new QueryWrapper<NlldOption>());
            java.util.Map<Long, Integer> counts = new java.util.LinkedHashMap<>();
            for (NlldOption item : all) {
                Long key = item.getOptionId().equals(existing.getOptionId())
                        ? dimensionId : item.getDimensionId();
                counts.put(key, counts.getOrDefault(key, 0) + 1);
            }
            for (NlldDimension item : dimensionMapper.selectList(new QueryWrapper<NlldDimension>())) {
                if (counts.getOrDefault(item.getDimensionId(), 0) != 2) {
                    throw new ServiceException("调整维度后每个维度必须仍对应 2 件物品，请与另一件物品交换维度后再保存");
                }
            }
        }
        String status = normalizeStatus(input.getStatus(), existing.getStatus());
        existing.setOptionName(input.getOptionName().trim());
        existing.setOptionDesc(input.getOptionDesc());
        existing.setDimensionId(dimensionId);
        existing.setSortOrder(input.getSortOrder() == null ? existing.getSortOrder() : input.getSortOrder());
        existing.setStatus(status);
        existing.setUpdateTime(DateUtils.getNowDate());
        return optionMapper.updateById(existing);
    }

    private static String normalizeStatus(String input, String fallback) {
        String status = isBlank(input) ? fallback : input.trim();
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ServiceException("状态只能为启用或停用");
        }
        return status;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
