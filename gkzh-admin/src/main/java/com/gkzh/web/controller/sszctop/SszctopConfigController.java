package com.gkzh.web.controller.sszctop;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.mapper.week.GkzhActivityGameMapper;
import com.gkzh.activity.mapper.week.GkzhGameParticipationMapper;
import com.gkzh.common.annotation.Log;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.enums.BusinessType;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.sszctop.domain.*;
import com.gkzh.sszctop.mapper.SszctopCareerMapper;
import com.gkzh.sszctop.mapper.SszctopDimensionMapper;
import com.gkzh.sszctop.mapper.SszctopDimensionRankMapper;
import com.gkzh.sszctop.mapper.SszctopRoomMapper;
import com.gkzh.sszctop.mapper.SszctopRoomMemberMapper;
import com.gkzh.sszctop.mapper.SszctopRoomLogMapper;
import com.gkzh.sszctop.mapper.SszctopStudentReportMapper;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 后台人工维护职业、维度和排序文案；不与既有游戏配置共用表。
 */
@RestController
@RequestMapping("/sszctop/config")
public class SszctopConfigController extends BaseController {
    // 后台配置接口只操作 sszctop 专属 Mapper，不复用其他游戏的实体和表。
    @Autowired
    private SszctopDimensionMapper dimensionMapper;
    @Autowired
    private SszctopCareerMapper careerMapper;
    @Autowired
    private SszctopDimensionRankMapper rankMapper;
    @Autowired
    private SszctopRoomLogMapper logMapper;
    @Autowired
    private SszctopRoomMapper roomMapper;
    @Autowired
    private SszctopRoomMemberMapper roomMemberMapper;
    @Autowired
    private SszctopStudentReportMapper reportMapper;
    @Autowired
    private GkzhGameParticipationMapper participationMapper;
    @Autowired
    private GkzhActivityGameMapper activityGameMapper;

    @PreAuthorize("@ss.hasPermi('sszctop:config:list')")
    @GetMapping("/dimensions")
    public TableDataInfo dimensions(String name) {
        startPage();
        QueryWrapper<SszctopDimension> q = new QueryWrapper<SszctopDimension>().orderByAsc("sort_order");
        if (name != null && !name.trim().isEmpty()) q.like("name", name.trim());
        return getDataTable(dimensionMapper.selectList(q));
    }

    @PreAuthorize("@ss.hasPermi('sszctop:config:add')")
    @Log(title = "谁是职场TOP维度", businessType = BusinessType.INSERT)
    @PostMapping("/dimensions")
    public AjaxResult addDimension(@RequestBody SszctopDimension v) {
        v.setCreateTime(DateUtils.getNowDate());
        v.setUpdateTime(DateUtils.getNowDate());
        return toAjax(dimensionMapper.insert(v));
    }

    @PreAuthorize("@ss.hasPermi('sszctop:config:edit')")
    @Log(title = "谁是职场TOP维度", businessType = BusinessType.UPDATE)
    @PutMapping("/dimensions")
    public AjaxResult editDimension(@RequestBody SszctopDimension v) {
        v.setUpdateTime(DateUtils.getNowDate());
        return toAjax(dimensionMapper.updateById(v));
    }

    /**
     * 删除维度配置；关联排序数据由数据库约束负责保护，避免误删产生孤立规则。
     */
    @PreAuthorize("@ss.hasPermi('sszctop:config:remove')")
    @Log(title = "谁是职场TOP维度", businessType = BusinessType.DELETE)
    @DeleteMapping("/dimensions/{id}")
    public AjaxResult removeDimension(@PathVariable Long id) {
        return toAjax(dimensionMapper.deleteById(id));
    }

    @PreAuthorize("@ss.hasPermi('sszctop:config:list')")
    @GetMapping("/careers")
    public TableDataInfo careers(String name) {
        startPage();
        QueryWrapper<SszctopCareer> q = new QueryWrapper<SszctopCareer>().orderByAsc("sort_order");
        if (name != null && !name.trim().isEmpty()) q.like("name", name.trim());
        return getDataTable(careerMapper.selectList(q));
    }

    @PreAuthorize("@ss.hasPermi('sszctop:config:add')")
    @Log(title = "谁是职场TOP职业", businessType = BusinessType.INSERT)
    @PostMapping("/careers")
    public AjaxResult addCareer(@RequestBody SszctopCareer v) {
        v.setCreateTime(DateUtils.getNowDate());
        v.setUpdateTime(DateUtils.getNowDate());
        return toAjax(careerMapper.insert(v));
    }

    @PreAuthorize("@ss.hasPermi('sszctop:config:edit')")
    @Log(title = "谁是职场TOP职业", businessType = BusinessType.UPDATE)
    @PutMapping("/careers")
    public AjaxResult editCareer(@RequestBody SszctopCareer v) {
        v.setUpdateTime(DateUtils.getNowDate());
        return toAjax(careerMapper.updateById(v));
    }

    /**
     * 删除职业配置；关联排序数据由数据库约束负责保护，避免误删产生孤立规则。
     */
    @PreAuthorize("@ss.hasPermi('sszctop:config:remove')")
    @Log(title = "谁是职场TOP职业", businessType = BusinessType.DELETE)
    @DeleteMapping("/careers/{id}")
    public AjaxResult removeCareer(@PathVariable Long id) {
        return toAjax(careerMapper.deleteById(id));
    }

    @PreAuthorize("@ss.hasPermi('sszctop:config:list')")
    @GetMapping("/ranks")
    public TableDataInfo ranks(Long dimensionId) {
        startPage();
        QueryWrapper<SszctopDimensionRank> q = new QueryWrapper<SszctopDimensionRank>().orderByAsc("dimension_id").orderByAsc("rank_order");
        if (dimensionId != null) q.eq("dimension_id", dimensionId);
        java.util.List<SszctopDimensionRank> rows = rankMapper.selectList(q);
        java.util.Map<Long, String> dimensions = new java.util.HashMap<>(), careers = new java.util.HashMap<>();
        dimensionMapper.selectList(null).forEach(x -> dimensions.put(x.getDimensionId(), x.getName()));
        careerMapper.selectList(null).forEach(x -> careers.put(x.getCareerId(), x.getName()));
        rows.forEach(x -> {
            x.setDimensionName(dimensions.get(x.getDimensionId()));
            x.setCareerName(careers.get(x.getCareerId()));
        });
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPermi('sszctop:config:add')")
    @Log(title = "谁是职场TOP排序文案", businessType = BusinessType.INSERT)
    @PostMapping("/ranks")
    public AjaxResult addRank(@RequestBody SszctopDimensionRank v) {
        v.setCreateTime(DateUtils.getNowDate());
        v.setUpdateTime(DateUtils.getNowDate());
        return toAjax(rankMapper.insert(v));
    }

    @PreAuthorize("@ss.hasPermi('sszctop:config:edit')")
    @Log(title = "谁是职场TOP排序文案", businessType = BusinessType.UPDATE)
    @PutMapping("/ranks")
    public AjaxResult editRank(@RequestBody SszctopDimensionRank v) {
        v.setUpdateTime(DateUtils.getNowDate());
        return toAjax(rankMapper.updateById(v));
    }

    /**
     * 删除单条维度排序及文案。
     */
    @PreAuthorize("@ss.hasPermi('sszctop:config:remove')")
    @Log(title = "谁是职场TOP排序文案", businessType = BusinessType.DELETE)
    @DeleteMapping("/ranks/{id}")
    public AjaxResult removeRank(@PathVariable Long id) {
        return toAjax(rankMapper.deleteById(id));
    }

    /**
     * 房间游玩记录列表：按活动实例和用户汇总，支持活动名称查询，每位用户在同一活动只展示最新的一条记录。
     */
    @PreAuthorize("@ss.hasPermi('sszctop:log:list')")
    @GetMapping("/room-logs")
    public TableDataInfo roomLogs(String activityName, String roomCode, String keyword) {
        startPage();
        List<SszctopRoomLog> logs = logMapper.selectUserActivitySummaries(activityName == null ? null : activityName.trim(), roomCode == null ? null : roomCode.trim(), keyword == null ? null : keyword.trim());
        logs.forEach(log -> log.setRoomStatusName(roomStatusName(log.getRoomStatus())));
        return getDataTable(logs);
    }

    /**
     * 返回单个房间从创建到结束的完整事件记录，供后台详情弹窗使用。
     */
    @PreAuthorize("@ss.hasPermi('sszctop:log:list')")
    @GetMapping("/room-logs/{roomId}")
    public AjaxResult roomLogDetails(@PathVariable Long roomId) {
        List<SszctopRoomLog> logs = logMapper.selectRoomDetails(roomId);
        logs.forEach(log -> log.setEventName(eventName(log.getEventType())));
        return AjaxResult.success(logs);
    }

    /**
     * 从某条带用户信息的房间日志定位学生，删除该学生当前活动的职场TOP报告、参与记录和用户日志。
     * 删除条件固定为“活动实例 + 用户”，不会影响其他活动或同组成员。
     */
    @PreAuthorize("@ss.hasPermi('sszctop:log:list')")
    @Log(title = "谁是职场TOP测试记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/room-logs/{logId}/test-record")
    @Transactional
    public AjaxResult clearTestRecord(@PathVariable Long logId) {
        SszctopRoomLog log = logMapper.selectById(logId);
        if (log == null) return AjaxResult.error("日志不存在或已删除");
        if (log.getUserId() != null && log.getInstanceId() != null) {
            deleteUserActivityRecord(log.getInstanceId(), Collections.singleton(log.getUserId()));
            return AjaxResult.success("已清除该用户当前活动的职场TOP记录和日志");
        }
        logMapper.deleteById(logId);
        return AjaxResult.success("日志已删除");
    }

    /**
     * 批量清除测试记录：按活动实例分组后，以 user_id IN (...) 直接删除报告、参与记录及用户日志，
     * 避免前端逐行发请求和数据库逐条删除，适合一次清理大量测试用户。
     */
    @PreAuthorize("@ss.hasPermi('sszctop:log:list')")
    @Log(title = "谁是职场TOP批量测试记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/room-logs/test-records")
    @Transactional
    public AjaxResult clearTestRecords(@RequestBody Map<String, List<Long>> body) {
        List<Long> logIds = body.get("logIds");
        if (logIds == null || logIds.isEmpty()) return AjaxResult.error("请选择要删除的用户记录");
        List<SszctopRoomLog> logs = logMapper.selectList(new QueryWrapper<SszctopRoomLog>().in("log_id", logIds));
        Map<Long, Set<Long>> usersByInstance = new HashMap<>();
        for (SszctopRoomLog log : logs)
            if (log.getInstanceId() != null && log.getUserId() != null)
                usersByInstance.computeIfAbsent(log.getInstanceId(), key -> new HashSet<>()).add(log.getUserId());
        usersByInstance.forEach((instanceId, userIds) -> {
            deleteUserActivityRecord(instanceId, userIds);
        });
        // 系统事件没有 user_id，也允许按勾选行直接删除，保证日志列表每一条、记录均可清理。
        logMapper.delete(new QueryWrapper<SszctopRoomLog>().in("log_id", logIds));
        return AjaxResult.success("已批量删除所选日志和关联测试记录");
    }

    /**
     * 按活动实例和用户集合删除职场TOP数据。
     * 参与记录额外限定为 sszctop 游戏 ID，绝不误删同一活动内的心愿橱窗、抽奖等其他游戏记录。
     */
    private void deleteUserActivityRecord(Long instanceId, Collection<Long> userIds) {
        if (instanceId == null || userIds == null || userIds.isEmpty()) return;

        // 先记录该用户出现过的房间；成员删除后据此清除已无任何成员的废弃房间。
        List<Long> relatedRoomIds = roomMemberMapper.selectRoomIdsByInstanceAndUsers(instanceId, userIds);
        reportMapper.delete(new QueryWrapper<SszctopStudentReport>().eq("instance_id", instanceId).in("user_id", userIds));
        logMapper.delete(new QueryWrapper<SszctopRoomLog>().eq("instance_id", instanceId).in("user_id", userIds));
        // 删除“房间成员及准备确认状态表”中的对应行，用户记录不再残留准备/确认历史。
        roomMemberMapper.deleteByInstanceAndUsers(instanceId, userIds);

        // 同组还有成员时保留房间；无人时删除其系统日志与房间主表，避免作废房间持续累积。
        if (!relatedRoomIds.isEmpty()) {
            List<Long> emptyRoomIds = roomMapper.selectEmptyRoomIds(relatedRoomIds);
            if (!emptyRoomIds.isEmpty()) {
                // 空房不是有效游玩：同步删除成员状态、房间日志和房间主数据。
                roomMemberMapper.delete(new QueryWrapper<SszctopRoomMember>().in("room_id", emptyRoomIds));
                logMapper.delete(new QueryWrapper<SszctopRoomLog>().in("room_id", emptyRoomIds));
                roomMapper.deleteBatchIds(emptyRoomIds);
            }
        }

        List<GkzhActivityGame> games = activityGameMapper.selectList(new QueryWrapper<GkzhActivityGame>().eq("instance_id", instanceId).eq("game_type", "sszctop"));
        List<Long> gameIds = new ArrayList<>();
        for (GkzhActivityGame game : games) if (game.getGameId() != null) gameIds.add(game.getGameId());
        if (!gameIds.isEmpty())
            participationMapper.delete(new QueryWrapper<GkzhGameParticipation>().eq("instance_id", instanceId).in("game_id", gameIds).in("user_id", userIds));
    }

    /**
     * 将内部事件编码转换为后台用户可读的中文文案。
     */
    private String eventName(String event) {
        if (event == null) return "未知事件";
        switch (event) {
            case "room.created":
                return "创建房间";
            case "room.started":
                return "开始游戏";
            case "room.passed":
                return "挑战成功";
            case "room.failed":
                return "挑战失败";
            case "room.abandoned": // 兼容旧日志编码。
            case "room.destroyed":
                return "房间销毁";
            case "member.joined":
                return "加入房间";
            case "member.rejoined":
                return "重新加入房间";
            case "member.left":
                return "退出房间";
            case "member.ready":
                return "成员准备";
            case "member.unready":
                return "取消准备";
            case "member.disconnected":
                return "连接断开";
            case "owner.transferred":
                return "转移房主";
            case "order.updated":
                return "更新排序";
            case "confirm.submitted":
                return "确认排序";
            default:
                return event;
        }
    }

    /**
     * 将房间内部状态转换为后台用户可读的中文状态。
     */
    private String roomStatusName(String status) {
        if (status == null) return "已删除";
        switch (status) {
            case "waiting":
                return "等待开始";
            case "playing":
                return "进行中";
            case "passed":
                return "成功";
            case "failed":
                return "失败";
            case "abandoned":
            case "destroyed":
                return "已销毁";
            default:
                return status;
        }
    }
}
