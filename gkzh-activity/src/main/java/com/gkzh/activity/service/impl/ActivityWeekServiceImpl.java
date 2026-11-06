package com.gkzh.activity.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import com.alibaba.fastjson2.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return listInstances(bizType, null);
    }

    @Override
    public List<GkzhActivityWeekInstance> listInstances(String bizType, Long schoolId) {
        QueryWrapper<GkzhActivityWeekInstance> query = new QueryWrapper<>();
        if (bizType != null && !bizType.trim().isEmpty()) {
            query.eq("biz_type", bizType);
        }
        if (schoolId != null) {
            // 区域也可作为历史活动的学校归属；明确删除的关系优先，不能被区域回退重新展示。
            query.apply("(EXISTS (SELECT 1 FROM gkzh_activity_week_school ws "
                    + "WHERE ws.instance_id = gkzh_activity_week_instance.instance_id AND ws.school_id = {0}) "
                    + "OR EXISTS (SELECT 1 FROM gkzh_activity_area a "
                    + "WHERE a.instance_id = gkzh_activity_week_instance.instance_id AND a.school_id = {0})) "
                    + "AND NOT EXISTS (SELECT 1 FROM gkzh_activity_week_school deleted "
                    + "WHERE deleted.instance_id = gkzh_activity_week_instance.instance_id "
                    + "AND deleted.school_id = {0} AND deleted.status = '2')", schoolId);
        }
        query.orderByDesc("instance_id");
        List<GkzhActivityWeekInstance> list = instanceMapper.selectList(query);
        for (GkzhActivityWeekInstance item : list) {
            item.setBannerUrl(resolveUrl(item.getBannerUrl()));
            item.setSchoolId(schoolId);
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
    @Transactional
    public int saveInstance(GkzhActivityWeekInstance instance) {
        Date now = DateUtils.getNowDate();
        boolean creating = instance.getInstanceId() == null;
        GkzhActivityWeekInstance existing = null;
        if (creating && instance.getSchoolId() == null) {
            throw new ServiceException("请选择学校");
        }
        if (instance.getInstanceId() != null) {
            existing = getInstance(instance.getInstanceId());
            if (existing == null) {
                throw new ServiceException("活动不存在");
            }
            if (instance.getSchoolId() != null) {
                checkSchoolActivityAvailable(instance.getInstanceId(), instance.getSchoolId());
                if (getSchoolConfig(instance.getInstanceId(), instance.getSchoolId()) == null
                        && areaMapper.selectCount(new QueryWrapper<GkzhActivityArea>()
                        .eq("instance_id", instance.getInstanceId()).eq("school_id", instance.getSchoolId())) == 0) {
                    throw new ServiceException("该活动不属于当前学校");
                }
            }
            if (existing != null) {
                if (instance.getStartTime() == null && existing.getStartTime() != null) {
                    instance.setStartTime(new Date(existing.getStartTime().getTime()));
                }
                if (instance.getEndTime() == null && existing.getEndTime() != null) {
                    instance.setEndTime(new Date(existing.getEndTime().getTime()));
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
        if (instance.getStartTime() == null || instance.getEndTime() == null) {
            throw new ServiceException("请选择活动开始和结束时间");
        }
        if (instance.getStartTime().after(instance.getEndTime())) {
            throw new ServiceException("结束时间不能早于开始时间");
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
        if (creating) {
            checkOverlap(instance);
            if (instance.getEndTime() != null && instance.getEndTime().before(now)) {
                throw new RuntimeException("不能添加已结束的活动");
            }
            instance.setCreateTime(now);
            instance.setUpdateTime(now);
            int rows = instanceMapper.insert(instance);
            saveInstanceSchools(instance.getInstanceId(), singletonSchool(instance.getSchoolId()));
            return rows;
        }
        instance.setUpdateTime(now);
        // 标题、状态与定时状态同步不应触发旧活动时间冲突校验。
        if (!Objects.equals(instance.getStartTime(), existing.getStartTime())
                || !Objects.equals(instance.getEndTime(), existing.getEndTime())
                || !Objects.equals(instance.getBizType(), existing.getBizType())) {
            List<GkzhActivityWeekSchool> schools = listInstanceSchools(instance.getInstanceId());
            for (GkzhActivityWeekSchool school : schools) {
                checkOverlap(instance, school.getSchoolId());
            }
            if (instance.getSchoolId() != null && schools.stream()
                    .noneMatch(school -> instance.getSchoolId().equals(school.getSchoolId()))) {
                checkOverlap(instance, instance.getSchoolId());
            }
        }
        return instanceMapper.updateById(instance);
    }

    private List<GkzhActivityWeekSchool> singletonSchool(Long schoolId) {
        GkzhActivityWeekSchool relation = new GkzhActivityWeekSchool();
        relation.setSchoolId(schoolId);
        return java.util.Collections.singletonList(relation);
    }

    private void checkOverlap(GkzhActivityWeekInstance instance) {
        checkOverlap(instance, instance.getSchoolId());
    }

    private void checkOverlap(GkzhActivityWeekInstance instance, Long schoolId) {
        if (schoolId == null) {
            throw new ServiceException("请选择学校后再设置活动时间");
        }
        if (instance.getBizType() == null || instance.getStartTime() == null || instance.getEndTime() == null) {
            return;
        }
        List<GkzhActivityWeekInstance> exists = listInstances(instance.getBizType(), schoolId);
        for (GkzhActivityWeekInstance item : exists) {
            if (instance.getInstanceId() != null && instance.getInstanceId().equals(item.getInstanceId())) {
                continue;
            }
            if (item.getStartTime() == null || item.getEndTime() == null) {
                continue;
            }
            if (instance.getStartTime().before(item.getEndTime()) && instance.getEndTime().after(item.getStartTime())) {
                throw new ServiceException("所选时间范围与该学校已有的同类型活动时间重叠，请调整后再添加");
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
    @Transactional
    public int deleteInstance(Long instanceId, Long schoolId) {
        if (instanceId == null || schoolId == null) {
            throw new ServiceException("请选择要删除活动的学校");
        }
        if (instanceMapper.selectById(instanceId) == null) {
            throw new ServiceException("活动不存在");
        }
        GkzhActivityWeekSchool relation = weekSchoolMapper.selectOne(new QueryWrapper<GkzhActivityWeekSchool>()
                .eq("instance_id", instanceId).eq("school_id", schoolId));
        if (relation != null) {
            // 重复请求保持幂等；永远不删除实例、区域、游戏及历史参与/报告数据。
            return "2".equals(relation.getStatus()) ? 1 : weekSchoolMapper.markSchoolDeleted(instanceId, schoolId);
        }
        if (areaMapper.selectCount(new QueryWrapper<GkzhActivityArea>()
                .eq("instance_id", instanceId).eq("school_id", schoolId)) == 0) {
            throw new ServiceException("该学校未关联此活动");
        }
        // 无关系表的历史学校区域：增加删除标记，避免区域回退再次展示。
        relation = new GkzhActivityWeekSchool();
        relation.setInstanceId(instanceId);
        relation.setSchoolId(schoolId);
        relation.setStatus("2");
        relation.setCreateTime(DateUtils.getNowDate());
        return weekSchoolMapper.insert(relation);
    }

    @Override
    public void checkSchoolActivityAvailable(Long instanceId, Long schoolId) {
        if (instanceId != null && schoolId != null && weekSchoolMapper.selectCount(
                new QueryWrapper<GkzhActivityWeekSchool>().eq("instance_id", instanceId)
                        .eq("school_id", schoolId).eq("status", "2")) > 0) {
            throw new ServiceException("该学校的活动已删除");
        }
    }

    @Override
    public List<GkzhActivityWeekSchool> listInstanceSchools(Long instanceId) {
        QueryWrapper<GkzhActivityWeekSchool> query = new QueryWrapper<>();
        query.eq("instance_id", instanceId);
        query.and(q -> q.isNull("status").or().ne("status", "2"));
        return weekSchoolMapper.selectList(query);
    }

    @Override
    public GkzhActivityWeekSchool getSchoolConfig(Long instanceId, Long schoolId) {
        QueryWrapper<GkzhActivityWeekSchool> query = new QueryWrapper<>();
        query.eq("instance_id", instanceId).eq("school_id", schoolId);
        query.and(q -> q.isNull("status").or().ne("status", "2"));
        return weekSchoolMapper.selectOne(query);
    }

    @Override
    public int countFinishedGames(Long instanceId, Long userId) {
        QueryWrapper<GkzhGameParticipation> query = new QueryWrapper<>();
        query.eq("instance_id", instanceId).eq("user_id", userId).eq("status", "1");
        // 抽奖等完成数量也必须排除未反馈的猜猜看，避免旧完成标记提前解锁后续环节。
        query.apply("(NOT EXISTS (SELECT 1 FROM gkzh_activity_game g "
                + "WHERE g.game_id = gkzh_game_participation.game_id AND g.game_type = 'zycck') "
                + "OR EXISTS (SELECT 1 FROM gkzh_zycck_record r "
                + "JOIN gkzh_zycck_activity_feedback f ON f.record_id = r.record_id "
                + "WHERE r.instance_id = gkzh_game_participation.instance_id "
                + "AND r.game_id = gkzh_game_participation.game_id "
                + "AND r.user_id = gkzh_game_participation.user_id "
                + "AND r.game_type = 'zycck' AND r.status = 'finished'))");
        return Math.toIntExact(gameParticipationMapper.selectCount(query));
    }

    @Override
    @Transactional
    public int saveInstanceSchools(Long instanceId, List<GkzhActivityWeekSchool> schoolConfigs) {
        if (instanceId == null || instanceMapper.selectById(instanceId) == null) {
            throw new ServiceException("活动实例不存在");
        }
        if (schoolConfigs == null || schoolConfigs.size() != 1 || schoolConfigs.get(0) == null
                || schoolConfigs.get(0).getSchoolId() == null) {
            throw new ServiceException("每次只能保存当前一个学校的配置");
        }
        GkzhActivityWeekSchool config = schoolConfigs.get(0);
        checkSchoolActivityAvailable(instanceId, config.getSchoolId());
        GkzhActivityWeekSchool relation = getSchoolConfig(instanceId, config.getSchoolId());
        if (relation == null) {
            if (!listInstanceSchools(instanceId).isEmpty() && areaMapper.selectCount(
                    new QueryWrapper<GkzhActivityArea>().eq("instance_id", instanceId)
                            .eq("school_id", config.getSchoolId())) == 0) {
                throw new ServiceException("不能为其他学校的活动新增学校绑定，请在当前学校新建活动");
            }
            relation = new GkzhActivityWeekSchool();
            relation.setInstanceId(instanceId);
            relation.setSchoolId(config.getSchoolId());
            relation.setStatus("0");
            relation.setCreateTime(DateUtils.getNowDate());
        }
        relation.setMinFinishCount(config.getMinFinishCount() == null ? 0 : config.getMinFinishCount());
        relation.setLotteryId(config.getLotteryId());
        relation.setMaxDrawCount(config.getMaxDrawCount() == null ? 1 : config.getMaxDrawCount());
        if (relation.getMinFinishCount() < 0 || relation.getMaxDrawCount() < 1) {
            throw new ServiceException("完成游戏数不能小于0，最多抽奖次数不能小于1");
        }
        if (relation.getId() == null) {
            return weekSchoolMapper.insert(relation);
        }
        // 原有多学校活动只更新指定学校这一行，绝不能先清空整个活动的学校关系。
        return weekSchoolMapper.updateSchoolConfig(relation);
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
        checkSchoolActivityAvailable(area.getInstanceId(), area.getSchoolId());
        if (getSchoolConfig(area.getInstanceId(), area.getSchoolId()) == null
                && areaMapper.selectCount(new QueryWrapper<GkzhActivityArea>()
                .eq("instance_id", area.getInstanceId()).eq("school_id", area.getSchoolId())) == 0) {
            throw new ServiceException("只能在活动所属学校添加区域");
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
    public Long getGameSchoolId(GkzhActivityGame game) {
        if (game == null || game.getAreaId() == null) {
            return null;
        }
        // 区域是活动内容的学校归属来源，与 flow 接口按学校过滤区域的口径保持一致。
        GkzhActivityArea area = areaMapper.selectById(game.getAreaId());
        if (area != null) {
            checkSchoolActivityAvailable(area.getInstanceId(), area.getSchoolId());
        }
        return area == null ? null : area.getSchoolId();
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

    /** 只删除同一活动、游戏、用户的参与数据；不按最新一条删除，避免旧完成记录残留。 */
    @Override
    public int deleteGameParticipations(Long instanceId, Long gameId, Long userId) {
        if (instanceId == null || gameId == null || userId == null) {
            throw new ServiceException("缺少活动、游戏或用户信息，无法删除游戏参与记录");
        }
        return gameParticipationMapper.delete(new QueryWrapper<GkzhGameParticipation>()
                .eq("instance_id", instanceId).eq("game_id", gameId).eq("user_id", userId));
    }
}
