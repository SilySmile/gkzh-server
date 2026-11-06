package com.gkzh.activity.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.gkzh.activity.domain.week.*;
import com.gkzh.activity.mapper.week.*;
import com.gkzh.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ActivityWeekSchoolScopeTest {
    private ActivityWeekServiceImpl service;
    private GkzhActivityWeekInstanceMapper instances;
    private GkzhActivityWeekSchoolMapper schools;
    private GkzhActivityAreaMapper areas;
    private GkzhActivityGameMapper games;
    private GkzhGameParticipationMapper participation;

    @BeforeEach
    void setup() {
        service = spy(new ActivityWeekServiceImpl());
        instances = mock(GkzhActivityWeekInstanceMapper.class);
        schools = mock(GkzhActivityWeekSchoolMapper.class);
        areas = mock(GkzhActivityAreaMapper.class);
        games = mock(GkzhActivityGameMapper.class);
        participation = mock(GkzhGameParticipationMapper.class);
        ReflectionTestUtils.setField(service, "instanceMapper", instances);
        ReflectionTestUtils.setField(service, "weekSchoolMapper", schools);
        ReflectionTestUtils.setField(service, "areaMapper", areas);
        ReflectionTestUtils.setField(service, "gameMapper", games);
        ReflectionTestUtils.setField(service, "gameParticipationMapper", participation);
        // 测试不连接数据库。任何误用全校列表的冲突检查都会立即失败。
        doThrow(new AssertionError("must filter overlap by school"))
                .when(service).listInstances(anyString());
    }

    @Test
    void createRequiresSchoolBeforeAnyWrite() {
        GkzhActivityWeekInstance instance = activity(null);
        instance.setSchoolId(null);
        assertThrows(ServiceException.class, () -> service.saveInstance(instance));
        verifyNoInteractions(instances, schools, areas, games, participation);
    }

    @Test
    void createUsesSelectedSchoolAndBindsItWithInstance() {
        GkzhActivityWeekInstance instance = activity(null);
        doReturn(Collections.emptyList()).when(service).listInstances("career_week", 5L);
        when(instances.insert(instance)).thenAnswer(call -> {
            instance.setInstanceId(101L);
            return 1;
        });
        when(instances.selectById(101L)).thenReturn(instance);
        when(schools.insert(any())).thenReturn(1);
        assertEquals(1, service.saveInstance(instance));
        verify(service).listInstances("career_week", 5L);
        ArgumentCaptor<GkzhActivityWeekSchool> binding = ArgumentCaptor.forClass(GkzhActivityWeekSchool.class);
        verify(schools).insert(binding.capture());
        assertEquals(101L, binding.getValue().getInstanceId());
        assertEquals(5L, binding.getValue().getSchoolId());
        assertEquals("0", binding.getValue().getStatus());
    }

    @Test
    void sameSchoolOverlapIsRejectedBeforeInsert() {
        doReturn(Collections.singletonList(activity(21L))).when(service).listInstances("career_week", 5L);
        assertThrows(ServiceException.class, () -> service.saveInstance(activity(null)));
        verify(instances, never()).insert(any());
        verifyNoInteractions(schools);
    }

    @Test
    void editingTitleOrStatusDoesNotRecheckHistoricalOverlap() {
        GkzhActivityWeekInstance existing = activity(21L);
        existing.setStartTime(atStart("2020-01-01"));
        existing.setEndTime(new Date(atStart("2020-01-03").getTime() - 1000));
        when(instances.selectById(21L)).thenReturn(existing);
        when(instances.updateById(any())).thenReturn(1);
        GkzhActivityWeekInstance update = new GkzhActivityWeekInstance();
        update.setInstanceId(21L);
        update.setStatus("3");
        assertEquals(1, service.saveInstance(update));
        verify(service, never()).listInstances(anyString(), anyLong());
    }

    @Test
    void deletionMarksOnlySchoolEvenForEndedActivityAndPreservesHistory() {
        GkzhActivityWeekInstance instance = activity(21L);
        instance.setStatus("2");
        when(instances.selectById(21L)).thenReturn(instance);
        when(schools.selectOne(any())).thenReturn(binding(70L, 5L));
        when(schools.markSchoolDeleted(21L, 5L)).thenReturn(1);
        assertEquals(1, service.deleteInstance(21L, 5L));
        verify(instances).selectById(21L);
        verifyNoMoreInteractions(instances);
        verify(schools).selectOne(any());
        verify(schools).markSchoolDeleted(21L, 5L);
        verifyNoMoreInteractions(schools);
        verifyNoInteractions(areas, games, participation);
    }

    @Test
    void deletionRequiresSchoolAndCannotFallBackToGlobalDelete() {
        assertThrows(ServiceException.class, () -> service.deleteInstance(21L, null));
        verifyNoInteractions(instances, schools, areas, games, participation);
    }

    @Test
    void repeatedDeletionDoesNotDeleteAnyData() {
        when(instances.selectById(21L)).thenReturn(activity(21L));
        GkzhActivityWeekSchool deleted = binding(70L, 5L);
        deleted.setStatus("2");
        when(schools.selectOne(any())).thenReturn(deleted);
        assertEquals(1, service.deleteInstance(21L, 5L));
        verify(schools, never()).markSchoolDeleted(anyLong(), anyLong());
        verifyNoInteractions(areas, games, participation);
    }

    @Test
    void deletionOfUnrelatedSchoolIsRejected() {
        when(instances.selectById(21L)).thenReturn(activity(21L));
        when(areas.selectCount(any())).thenReturn(0L);
        assertThrows(ServiceException.class, () -> service.deleteInstance(21L, 99L));
        verify(schools, never()).markSchoolDeleted(anyLong(), anyLong());
        verify(schools, never()).insert(any());
        verifyNoInteractions(games, participation);
    }

    @Test
    void legacyAreaOnlySchoolGetsDeletionMarkerWithoutDeletingAreas() {
        when(instances.selectById(21L)).thenReturn(activity(21L));
        when(areas.selectCount(any())).thenReturn(1L);
        when(schools.insert(any())).thenReturn(1);
        assertEquals(1, service.deleteInstance(21L, 5L));
        ArgumentCaptor<GkzhActivityWeekSchool> marker = ArgumentCaptor.forClass(GkzhActivityWeekSchool.class);
        verify(schools).insert(marker.capture());
        assertEquals(21L, marker.getValue().getInstanceId());
        assertEquals(5L, marker.getValue().getSchoolId());
        assertEquals("2", marker.getValue().getStatus());
        verify(areas).selectCount(any());
        verifyNoMoreInteractions(areas);
        verifyNoInteractions(games, participation);
    }

    @Test
    void savingSchoolLotteryNeverReplacesOtherSchoolsAndCanClearLottery() {
        when(instances.selectById(21L)).thenReturn(activity(21L));
        GkzhActivityWeekSchool existing = binding(70L, 5L);
        existing.setLotteryId(9L);
        when(schools.selectOne(any())).thenReturn(existing);
        when(schools.updateSchoolConfig(any())).thenReturn(1);
        GkzhActivityWeekSchool form = binding(null, 5L);
        form.setLotteryId(null);
        form.setMinFinishCount(3);
        form.setMaxDrawCount(2);
        assertEquals(1, service.saveInstanceSchools(21L, Collections.singletonList(form)));
        ArgumentCaptor<GkzhActivityWeekSchool> update = ArgumentCaptor.forClass(GkzhActivityWeekSchool.class);
        verify(schools).updateSchoolConfig(update.capture());
        assertEquals(70L, update.getValue().getId());
        assertEquals(5L, update.getValue().getSchoolId());
        assertEquals(3, update.getValue().getMinFinishCount());
        assertNull(update.getValue().getLotteryId());
        verify(schools, never()).delete(any());
        verify(schools, never()).insert(any());
        verify(schools, never()).updateById(any());
    }

    @Test
    void staleConfigCannotResurrectDeletedSchoolActivity() {
        when(instances.selectById(21L)).thenReturn(activity(21L));
        when(schools.selectCount(any())).thenReturn(1L);
        assertThrows(ServiceException.class, () -> service.saveInstanceSchools(21L,
                Collections.singletonList(binding(null, 5L))));
        verify(schools, never()).insert(any());
        verify(schools, never()).updateSchoolConfig(any());
    }

    @Test
    void schoolListIncludesLegacyAreasButExcludesDeletionMarker() {
        when(instances.selectList(any())).thenReturn(Collections.emptyList());
        service.listInstances("career_week", 5L);
        ArgumentCaptor<Wrapper<GkzhActivityWeekInstance>> query = ArgumentCaptor.forClass(Wrapper.class);
        verify(instances).selectList(query.capture());
        String sql = query.getValue().getSqlSegment();
        assertTrue(sql.contains("gkzh_activity_area"), sql);
        assertTrue(sql.contains("NOT EXISTS"), sql);
        assertTrue(sql.contains("deleted.status = '2'"), sql);
        assertTrue(sql.contains("deleted.school_id ="), sql);
    }

    @Test
    void scanEntryRejectsDeletedSchoolWithoutTouchingGameOrRecords() {
        GkzhActivityGame game = new GkzhActivityGame();
        game.setAreaId(31L);
        GkzhActivityArea area = new GkzhActivityArea();
        area.setInstanceId(21L);
        area.setSchoolId(5L);
        when(areas.selectById(31L)).thenReturn(area);
        when(schools.selectCount(any())).thenReturn(1L);
        assertThrows(ServiceException.class, () -> service.getGameSchoolId(game));
        verifyNoInteractions(games, participation);
    }

    @Test
    void deletionAndConfigSqlAreScopedByBothInstanceAndSchool() {
        MybatisConfiguration config = new MybatisConfiguration();
        config.addMapper(GkzhActivityWeekSchoolMapper.class);
        Map<String, Object> params = new HashMap<>();
        params.put("instanceId", 21L);
        params.put("schoolId", 5L);
        String namespace = GkzhActivityWeekSchoolMapper.class.getName();
        String deletion = config.getMappedStatement(namespace + ".markSchoolDeleted").getBoundSql(params).getSql();
        assertTrue(deletion.contains("SET status = '2'"), deletion);
        assertTrue(deletion.contains("instance_id = ? AND school_id = ?"), deletion);
        assertFalse(deletion.contains("DELETE"), deletion);
        params.put("config", binding(70L, 5L));
        String update = config.getMappedStatement(namespace + ".updateSchoolConfig").getBoundSql(params).getSql();
        assertTrue(update.contains("lottery_id = ?"), update);
        assertTrue(update.contains("instance_id = ? AND school_id = ?"), update);
        assertTrue(update.contains("status != '2'"), update);
    }

    private GkzhActivityWeekInstance activity(Long id) {
        GkzhActivityWeekInstance instance = new GkzhActivityWeekInstance();
        instance.setInstanceId(id);
        instance.setDefinitionId(1L);
        instance.setBizType("career_week");
        instance.setTitle("学校活动");
        instance.setSchoolId(5L);
        instance.setStartTime(atStart("2090-01-01"));
        instance.setEndTime(new Date(atStart("2090-01-04").getTime() - 1000));
        return instance;
    }

    private GkzhActivityWeekSchool binding(Long id, Long schoolId) {
        GkzhActivityWeekSchool school = new GkzhActivityWeekSchool();
        school.setId(id);
        school.setInstanceId(21L);
        school.setSchoolId(schoolId);
        school.setStatus("0");
        return school;
    }

    private Date atStart(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
