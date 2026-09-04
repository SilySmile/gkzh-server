package com.gkzh.activity.service.impl;

import java.util.Date;
import java.util.List;
import com.alibaba.fastjson2.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.domain.week.GkzhActivityArea;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekDefinition;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhActivityWeekSchool;
import com.gkzh.activity.domain.week.GkzhGameType;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.domain.week.GkzhGameConfig;
import com.gkzh.activity.mapper.week.GkzhActivityAreaMapper;
import com.gkzh.activity.mapper.week.GkzhActivityGameMapper;
import com.gkzh.activity.mapper.week.GkzhActivityWeekDefinitionMapper;
import com.gkzh.activity.mapper.week.GkzhActivityWeekInstanceMapper;
import com.gkzh.activity.mapper.week.GkzhActivityWeekSchoolMapper;
import com.gkzh.activity.mapper.week.GkzhGameTypeMapper;
import com.gkzh.activity.mapper.week.GkzhGameParticipationMapper;
import com.gkzh.activity.mapper.week.GkzhGameConfigMapper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.common.exception.ServiceException;

/**
 * 活动/区域/游戏服务实现
 */
@Service
public class ActivityWeekServiceImpl implements IActivityWeekService {

    @Value("${gkzh.material-domain:}")
    private String materialDomain;

    @Autowired
    private GkzhGameTypeMapper gameTypeMapper;

    @Autowired
    private GkzhActivityWeekDefinitionMapper definitionMapper;

    @Autowired
    private GkzhActivityWeekInstanceMapper instanceMapper;

    @Autowired
    private GkzhActivityWeekSchoolMapper weekSchoolMapper;

    @Autowired
    private GkzhActivityAreaMapper areaMapper;

    @Autowired
    private GkzhActivityGameMapper gameMapper;

    @Autowired
    private GkzhGameParticipationMapper gameParticipationMapper;

    @Autowired
    private GkzhGameConfigMapper gameConfigMapper;

    @Override
    public List<GkzhGameType> listGameTypes() {
        QueryWrapper<GkzhGameType> query = new QueryWrapper<>();
        query.eq("status", "0").orderByAsc("game_type");
        return gameTypeMapper.selectList(query);
    }

    @Override
    public GkzhGameType getGameType(String gameType) {
        return gameTypeMapper.selectById(gameType);
    }

    @Override
    public List<GkzhGameConfig> listGameConfigs(String gameType) {
        return listGameConfigs(gameType, null);
    }

    @Override
    public List<GkzhGameConfig> listGameConfigs(String gameType, String status) {
        QueryWrapper<GkzhGameConfig> query = new QueryWrapper<>();
        if (status != null && !status.trim().isEmpty()) {
            query.eq("status", status.trim());
        }
        if (gameType != null && !gameType.trim().isEmpty()) {
            if ("choice".equals(gameType) || "answer".equals(gameType) || "cooperation".equals(gameType)) {
                query.eq("category", gameType);
            } else {
                query.eq("game_type", gameType);
            }
        }
        query.orderByAsc("config_id");
        return gameConfigMapper.selectList(query);
    }

    @Override
    public int saveGameConfig(GkzhGameConfig config) {
        Date now = DateUtils.getNowDate();
        if (config.getConfigId() == null) {
            config.setCreateTime(now);
            config.setUpdateTime(now);
            if (config.getViewType() == null || config.getViewType().trim().isEmpty()) {
                config.setViewType("generic");
            }
            if (config.getStatus() == null) {
                config.setStatus("0");
            }
            return gameConfigMapper.insert(config);
        }
        config.setUpdateTime(now);
        return gameConfigMapper.updateById(config);
    }

    @Override
    public int deleteGameConfig(Long configId) {
        return gameConfigMapper.deleteById(configId);
    }

    @Override
    public List<GkzhActivityWeekDefinition> listDefinitions() {
        QueryWrapper<GkzhActivityWeekDefinition> query = new QueryWrapper<>();
        query.orderByAsc("definition_id");
        return definitionMapper.selectList(query);
    }

    @Override
    public int saveDefinition(GkzhActivityWeekDefinition definition) {
        Date now = DateUtils.getNowDate();
        if (definition.getDefinitionId() == null) {
            definition.setCreateTime(now);
            definition.setUpdateTime(now);
            if (definition.getStatus() == null) {
                definition.setStatus("0");
            }
            return definitionMapper.insert(definition);
        }
        definition.setUpdateTime(now);
        return definitionMapper.updateById(definition);
    }

    @Override
    public List<GkzhActivityWeekInstance> listInstances(String bizType) {
        QueryWrapper<GkzhActivityWeekInstance> query = new QueryWrapper<>();
        if (bizType != null && !bizType.trim().isEmpty()) {
            query.eq("biz_type", bizType);
        }
        query.orderByDesc("instance_id");
        List<GkzhActivityWeekInstance> list = instanceMapper.selectList(query);
        for (GkzhActivityWeekInstance item : list) {
            item.setBannerUrl(resolveUrl(item.getBannerUrl()));
        }
        return list;
    }

    @Override
    public GkzhActivityWeekInstance getInstance(Long instanceId) {
        GkzhActivityWeekInstance instance = instanceMapper.selectById(instanceId);
        if (instance != null) {
            instance.setBannerUrl(resolveUrl(instance.getBannerUrl()));
        }
        return instance;
    }

    private String resolveUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }
        String domain = materialDomain == null ? "" : materialDomain.replaceAll("/+$", "");
        if (url.startsWith("/")) {
            return domain + url;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            int schemeIndex = url.indexOf("://");
            int pathIndex = url.indexOf("/", schemeIndex + 3);
            String path = pathIndex >= 0 ? url.substring(pathIndex) : "/";
            return domain + path;
        }
        return domain + "/" + url;
    }

    @Override
    public int saveInstance(GkzhActivityWeekInstance instance) {
        Date now = DateUtils.getNowDate();
        if (instance.getInstanceId() != null) {
            GkzhActivityWeekInstance existing = getInstance(instance.getInstanceId());
            if (existing != null) {
                if (instance.getStartTime() == null) {
                    instance.setStartTime(existing.getStartTime());
                }
                if (instance.getEndTime() == null) {
                    instance.setEndTime(existing.getEndTime());
                }
                if (instance.getBizType() == null) {
                    instance.setBizType(existing.getBizType());
                }
                if (instance.getDefinitionId() == null) {
                    instance.setDefinitionId(existing.getDefinitionId());
                }
            }
        }
        if (instance.getStartTime() != null) {
            instance.getStartTime().setHours(0);
            instance.getStartTime().setMinutes(0);
            instance.getStartTime().setSeconds(0);
        }
        if (instance.getEndTime() != null) {
            instance.getEndTime().setHours(23);
            instance.getEndTime().setMinutes(59);
            instance.getEndTime().setSeconds(59);
        }
        if (!"3".equals(instance.getStatus())) {
            instance.setStatus(resolveTimeStatus(instance));
        }
        if (instance.getInstanceId() == null && instance.getDefinitionId() == null
                && (instance.getBizType() == null || instance.getBizType().trim().isEmpty())) {
            throw new ServiceException("请选择活动类型");
        }
        if (instance.getDefinitionId() == null && instance.getBizType() != null) {
            QueryWrapper<GkzhActivityWeekDefinition> definitionQuery = new QueryWrapper<>();
            definitionQuery.eq("biz_type", instance.getBizType());
            GkzhActivityWeekDefinition definition = definitionMapper.selectOne(definitionQuery);
            if (definition != null) {
                instance.setDefinitionId(definition.getDefinitionId());
            }
        }
        if (instance.getInstanceId() == null) {
            checkOverlap(instance);
            if (instance.getEndTime() != null && instance.getEndTime().before(now)) {
                throw new RuntimeException("不能添加已结束的活动");
            }
            instance.setCreateTime(now);
            instance.setUpdateTime(now);
            return instanceMapper.insert(instance);
        }
        instance.setUpdateTime(now);
        checkOverlap(instance);
        return instanceMapper.updateById(instance);
    }

    private void checkOverlap(GkzhActivityWeekInstance instance) {
        if (instance.getBizType() == null || instance.getStartTime() == null || instance.getEndTime() == null) {
            return;
        }
        List<GkzhActivityWeekInstance> exists = listInstances(instance.getBizType());
        for (GkzhActivityWeekInstance item : exists) {
            if (instance.getInstanceId() != null && instance.getInstanceId().equals(item.getInstanceId())) {
                continue;
            }
            if (item.getStartTime() == null || item.getEndTime() == null) {
                continue;
            }
            if (instance.getStartTime().before(item.getEndTime()) && instance.getEndTime().after(item.getStartTime())) {
                throw new RuntimeException("所选时间范围与已有活动时间重叠，请调整后再添加");
            }
        }
    }

    private String resolveTimeStatus(GkzhActivityWeekInstance instance) {
        Date now = DateUtils.getNowDate();
        if (instance.getStartTime() != null && now.before(instance.getStartTime())) {
            return "0";
        }
        if (instance.getEndTime() != null && !now.before(instance.getEndTime())) {
            return "2";
        }
        return "1";
    }

    @Override
    public int deleteInstance(Long instanceId) {
        GkzhActivityWeekInstance instance = getInstance(instanceId);
        if (instance == null) {
            return 0;
        }
        if (!"0".equals(instance.getStatus())) {
            throw new RuntimeException("只有未开始的活动可以删除");
        }
        QueryWrapper<GkzhActivityArea> areaQuery = new QueryWrapper<>();
        areaQuery.eq("instance_id", instanceId);
        List<GkzhActivityArea> areas = areaMapper.selectList(areaQuery);
        for (GkzhActivityArea area : areas) {
            deleteArea(area.getAreaId());
        }

        QueryWrapper<GkzhActivityWeekSchool> schoolQuery = new QueryWrapper<>();
        schoolQuery.eq("instance_id", instanceId);
        weekSchoolMapper.delete(schoolQuery);

        return instanceMapper.deleteById(instanceId);
    }

    @Override
    public List<GkzhActivityWeekSchool> listInstanceSchools(Long instanceId) {
        QueryWrapper<GkzhActivityWeekSchool> query = new QueryWrapper<>();
        query.eq("instance_id", instanceId);
        return weekSchoolMapper.selectList(query);
    }

    @Override
    public GkzhActivityWeekSchool getSchoolConfig(Long instanceId, Long schoolId) {
        QueryWrapper<GkzhActivityWeekSchool> query = new QueryWrapper<>();
        query.eq("instance_id", instanceId).eq("school_id", schoolId);
        return weekSchoolMapper.selectOne(query);
    }

    @Override
    public int countFinishedGames(Long instanceId, Long userId) {
        QueryWrapper<GkzhGameParticipation> query = new QueryWrapper<>();
        query.eq("instance_id", instanceId).eq("user_id", userId).eq("status", "1");
        return Math.toIntExact(gameParticipationMapper.selectCount(query));
    }

    @Override
    public int saveInstanceSchools(Long instanceId, List<GkzhActivityWeekSchool> schoolConfigs) {
        QueryWrapper<GkzhActivityWeekSchool> delete = new QueryWrapper<>();
        delete.eq("instance_id", instanceId);
        weekSchoolMapper.delete(delete);

        int rows = 0;
        if (schoolConfigs != null) {
            for (GkzhActivityWeekSchool config : schoolConfigs) {
                GkzhActivityWeekSchool relation = new GkzhActivityWeekSchool();
                relation.setInstanceId(instanceId);
                relation.setSchoolId(config.getSchoolId());
                relation.setMinFinishCount(config.getMinFinishCount() == null ? 0 : config.getMinFinishCount());
                relation.setLotteryId(config.getLotteryId());
                relation.setMaxDrawCount(config.getMaxDrawCount() == null ? 1 : config.getMaxDrawCount());
                relation.setStatus("0");
                relation.setCreateTime(DateUtils.getNowDate());
                rows += weekSchoolMapper.insert(relation);
            }
        }
        return rows;
    }

    @Override
    public List<GkzhActivityArea> listAreas(Long instanceId, Long schoolId) {
        QueryWrapper<GkzhActivityArea> query = new QueryWrapper<>();
        query.eq("instance_id", instanceId);
        if (schoolId != null) {
            query.eq("school_id", schoolId);
        }
        query.orderByAsc("sort_order", "area_id");
        return areaMapper.selectList(query);
    }

    @Override
    public int saveArea(GkzhActivityArea area) {
        if (area.getSchoolId() == null) {
            throw new ServiceException("学校不能为空");
        }
        Date now = DateUtils.getNowDate();
        if (area.getAreaId() == null) {
            area.setCreateTime(now);
            area.setUpdateTime(now);
            if (area.getStatus() == null) {
                area.setStatus("0");
            }
            if (area.getSortOrder() == null) {
                area.setSortOrder(0);
            }
            return areaMapper.insert(area);
        }
        area.setUpdateTime(now);
        return areaMapper.updateById(area);
    }

    @Override
    public int deleteArea(Long areaId) {
        QueryWrapper<GkzhActivityGame> gameQuery = new QueryWrapper<>();
        gameQuery.eq("area_id", areaId);
        gameMapper.delete(gameQuery);
        return areaMapper.deleteById(areaId);
    }

    @Override
    public List<GkzhActivityGame> listGames(Long areaId) {
        QueryWrapper<GkzhActivityGame> query = new QueryWrapper<>();
        query.eq("area_id", areaId).orderByAsc("sort_order", "game_id");
        return gameMapper.selectList(query);
    }

    @Override
    public GkzhActivityGame getGame(Long gameId) {
        return gameMapper.selectById(gameId);
    }

    @Override
    public int saveGame(GkzhActivityGame game) {
        Date now = DateUtils.getNowDate();
        if (game.getGameId() == null) {
            if (game.getAreaId() == null || game.getGameType() == null || game.getGameType().trim().isEmpty()) {
                throw new ServiceException("区域和游戏类型不能为空");
            }
            GkzhActivityArea area = areaMapper.selectById(game.getAreaId());
            if (area == null || area.getInstanceId() == null) {
                throw new ServiceException("所属活动区域不存在");
            }
            // 以区域所属活动为准，防止前端传入错误实例导致游戏分散到不同活动。
            game.setInstanceId(area.getInstanceId());
            Long configId = configId(game.getConfig());
            List<GkzhActivityGame> existingGames = gameMapper.selectList(new QueryWrapper<GkzhActivityGame>().eq("instance_id", area.getInstanceId()));
            boolean duplicated = existingGames.stream().anyMatch(existing -> sameGame(existing, game, configId));
            if (duplicated) {
                throw new ServiceException("该活动已添加此游戏，不能重复新增");
            }
            game.setCreateTime(now);
            game.setUpdateTime(now);
            if (game.getStatus() == null) {
                game.setStatus("0");
            }
            if (game.getRequiredFlag() == null) {
                game.setRequiredFlag("0");
            }
            if (game.getSortOrder() == null) {
                game.setSortOrder(0);
            }
            return gameMapper.insert(game);
        }
        game.setUpdateTime(now);
        return gameMapper.updateById(game);
    }

    @Override
    public int deleteGame(Long gameId) {
        return gameMapper.deleteById(gameId);
    }

    /** 相同 configId 才是同一个具体游戏；没有配置 ID 的历史记录再按路由比较。 */
    private boolean sameGame(GkzhActivityGame existing, GkzhActivityGame incoming, Long incomingConfigId) {
        Long existingConfigId = configId(existing.getConfig());
        if (incomingConfigId != null && existingConfigId != null) return incomingConfigId.equals(existingConfigId);
        return incomingConfigId == null && existingConfigId == null
                && incoming.getGameType() != null && incoming.getGameType().equals(existing.getGameType());
    }

    private Long configId(String config) {
        if (config == null || config.trim().isEmpty()) return null;
        try { return JSON.parseObject(config).getLong("configId"); } catch (Exception ignored) { return null; }
    }

    @Override
    public int recordGameEnter(GkzhGameParticipation participation) {
        participation.setCreateTime(DateUtils.getNowDate());
        participation.setUpdateTime(DateUtils.getNowDate());
        if (participation.getStatus() == null) {
            participation.setStatus("0");
        }
        return gameParticipationMapper.insert(participation);
    }

    @Override
    public GkzhGameParticipation getLatestParticipation(Long gameId, Long userId) {
        QueryWrapper<GkzhGameParticipation> query = new QueryWrapper<>();
        query.eq("game_id", gameId)
                .eq("user_id", userId)
                .last("ORDER BY CASE status WHEN '1' THEN 0 WHEN '2' THEN 1 ELSE 2 END, create_time DESC LIMIT 1");
        return gameParticipationMapper.selectOne(query);
    }

    @Override
    public int updateGameParticipation(GkzhGameParticipation participation) {
        participation.setUpdateTime(DateUtils.getNowDate());
        return gameParticipationMapper.updateById(participation);
    }
}
