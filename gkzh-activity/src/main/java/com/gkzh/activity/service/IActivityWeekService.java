package com.gkzh.activity.service;

import java.util.List;

import com.gkzh.activity.domain.week.GkzhActivityArea;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekDefinition;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhActivityWeekSchool;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.domain.week.GkzhGameType;
import com.gkzh.activity.domain.week.GkzhGameConfig;

/**
 * 活动/区域/游戏服务
 */
public interface IActivityWeekService {

    List<GkzhGameType> listGameTypes();

    GkzhGameType getGameType(String gameType);

    List<GkzhGameConfig> listGameConfigs(String gameType);

    List<GkzhGameConfig> listGameConfigs(String gameType, String status);

    int saveGameConfig(GkzhGameConfig config);

    int deleteGameConfig(Long configId);

    List<GkzhActivityWeekDefinition> listDefinitions();

    int saveDefinition(GkzhActivityWeekDefinition definition);

    List<GkzhActivityWeekInstance> listInstances(String bizType);

    GkzhActivityWeekInstance getInstance(Long instanceId);

    int saveInstance(GkzhActivityWeekInstance instance);

    int deleteInstance(Long instanceId);

    List<GkzhActivityWeekSchool> listInstanceSchools(Long instanceId);

    GkzhActivityWeekSchool getSchoolConfig(Long instanceId, Long schoolId);

    int countFinishedGames(Long instanceId, Long userId);

    int saveInstanceSchools(Long instanceId, List<GkzhActivityWeekSchool> schoolConfigs);

    List<GkzhActivityArea> listAreas(Long instanceId, Long schoolId);

    int saveArea(GkzhActivityArea area);

    int deleteArea(Long areaId);

    List<GkzhActivityGame> listGames(Long areaId);

    GkzhActivityGame getGame(Long gameId);

    int saveGame(GkzhActivityGame game);

    int deleteGame(Long gameId);

    int recordGameEnter(GkzhGameParticipation participation);

    GkzhGameParticipation getLatestParticipation(Long gameId, Long userId);

    int updateGameParticipation(GkzhGameParticipation participation);
}
