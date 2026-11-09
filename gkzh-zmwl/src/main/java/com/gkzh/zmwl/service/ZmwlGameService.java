package com.gkzh.zmwl.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zmwl.domain.ZmwlDirection;
import com.gkzh.zmwl.domain.ZmwlRecord;
import com.gkzh.zmwl.domain.ZmwlSetting;
import com.gkzh.zmwl.mapper.ZmwlRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职面未来学生端业务：扫码进入、进入选择页、确认未来发展方向和选择卡查看。
 *
 * <p>扫码即创建参与记录并计入统计；确认时由服务端按当前方向配置写入名称、标签、解读和寄语快照，
 * 不信任客户端提交的文案。确认后默认只读，是否允许重新选择由规则参数控制。</p>
 */
@Service
public class ZmwlGameService {

    /** 游戏类型编码，与 gkzh_game_config.route、gkzh_game_type.game_type 保持一致。 */
    public static final String GAME_TYPE = "zmwl";

    /** 参与状态：进行中。 */
    public static final String STATUS_PARTICIPATING = "participating";

    /** 参与状态：已完成（已确认发展方向）。 */
    public static final String STATUS_FINISHED = "finished";

    /** “暂未确定”方向的编码，用于统计中单独展示。 */
    public static final String DIRECTION_UNDECIDED = "undecided";

    private final ZmwlRecordMapper recordMapper;
    private final ZmwlConfigService configService;
    private final IActivityWeekService activityWeekService;

    public ZmwlGameService(ZmwlRecordMapper recordMapper, ZmwlConfigService configService,
                            IActivityWeekService activityWeekService) {
        this.recordMapper = recordMapper;
        this.configService = configService;
        this.activityWeekService = activityWeekService;
    }

    /**
     * 扫码进入：校验学校、活动实例和游戏归属后幂等创建参与记录。
     * 扫码即计入参与统计，与业务记录在同一事务写入活动游戏参与表。
     */
    @Transactional
    public ZmwlRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId, Long studentId,
                             String studentNo, String studentName, Long departmentId, String major, String gender) {
        if (schoolId == null || instanceId == null || gameId == null || userId == null) {
            throw new ServiceException("缺少活动或学校信息，请重新扫码进入");
        }
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null || !instanceId.equals(game.getInstanceId()) || !GAME_TYPE.equals(game.getGameType())) {
            throw new ServiceException("该游戏不属于当前活动");
        }
        // 活动区域的学校归属必须与学生当前学校一致，避免扫他校二维码进入。
        Long activitySchoolId = activityWeekService.getGameSchoolId(game);
        if (activitySchoolId != null && !activitySchoolId.equals(schoolId)) {
            throw new ServiceException("该活动不属于当前学校，无法进入");
        }
        ZmwlRecord existing = recordMapper.selectOne(new QueryWrapper<ZmwlRecord>()
                .eq("school_id", schoolId).eq("instance_id", instanceId)
                .eq("game_id", gameId).eq("user_id", userId));
        if (existing != null) {
            // 再次扫码只补空的学生快照，不覆盖已经确认的方向与选择卡文案。
            UpdateWrapper<ZmwlRecord> patch = new UpdateWrapper<ZmwlRecord>().eq("record_id", existing.getRecordId());
            boolean changed = false;
            if (existing.getStudentId() == null && studentId != null) {
                existing.setStudentId(studentId);
                patch.set("student_id", studentId);
                changed = true;
            }
            if (isBlank(existing.getStudentNo()) && !isBlank(studentNo)) {
                existing.setStudentNo(studentNo);
                patch.set("student_no", studentNo);
                changed = true;
            }
            if (isBlank(existing.getStudentName()) && !isBlank(studentName)) {
                existing.setStudentName(studentName);
                patch.set("student_name", studentName);
                changed = true;
            }
            if (existing.getDepartmentId() == null && departmentId != null) {
                existing.setDepartmentId(departmentId);
                patch.set("department_id", departmentId);
                changed = true;
            }
            if (isBlank(existing.getMajor()) && !isBlank(major)) {
                existing.setMajor(major);
                patch.set("major", major);
                changed = true;
            }
            if (isBlank(existing.getGender()) && !isBlank(gender)) {
                existing.setGender(gender);
                patch.set("gender", gender);
                changed = true;
            }
            if (changed) {
                existing.setUpdateTime(DateUtils.getNowDate());
                patch.set("update_time", existing.getUpdateTime());
                recordMapper.update(null, patch);
            }
            return existing;
        }

        Date now = DateUtils.getNowDate();
        ZmwlRecord record = new ZmwlRecord();
        record.setSchoolId(schoolId);
        record.setInstanceId(instanceId);
        record.setGameId(gameId);
        record.setUserId(userId);
        record.setStudentId(studentId);
        record.setStudentNo(studentNo);
        record.setStudentName(studentName);
        record.setDepartmentId(departmentId);
        record.setMajor(major);
        record.setGender(gender);
        record.setGameType(GAME_TYPE);
        record.setStatus(STATUS_PARTICIPATING);
        record.setScanTime(now);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        recordMapper.insert(record);

        GkzhGameParticipation participation = new GkzhGameParticipation();
        participation.setInstanceId(instanceId);
        participation.setGameId(gameId);
        participation.setAreaId(game.getAreaId());
        participation.setSchoolId(schoolId);
        participation.setStudentId(studentId);
        participation.setUserId(userId);
        participation.setScanTime(now);
        participation.setStatus("0");
        activityWeekService.recordGameEnter(participation);
        return record;
    }

    /** 查询本人参与记录；不能读取其他学生的记录。 */
    public ZmwlRecord get(Long recordId, Long userId) {
        ZmwlRecord record = recordMapper.selectById(recordId);
        if (record == null || userId == null || !userId.equals(record.getUserId())
                || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    /** 进入选择页：只记录开始时间，重复调用保持首次时间，用于计算选择用时。 */
    @Transactional
    public Map<String, Object> start(Long recordId, Long userId) {
        ZmwlRecord record = lockRecord(recordId, userId);
        requireEnabled();
        if (!STATUS_FINISHED.equals(record.getStatus()) && record.getStartTime() == null) {
            Date now = DateUtils.getNowDate();
            recordMapper.update(null, new UpdateWrapper<ZmwlRecord>().eq("record_id", recordId)
                    .set("start_time", now).set("update_time", now));
            record.setStartTime(now);
            record.setUpdateTime(now);
        }
        return view(record);
    }

    /**
     * 确认未来发展方向：写入方向与文案快照，并在同一事务完成活动游戏参与记录。
     * 已确认时：允许重新选择则覆盖，否则直接返回原结果，不重复完成。
     *
     * @param directionCode 学生选择的启用方向编码
     */
    @Transactional
    public Map<String, Object> choose(Long recordId, Long userId, String directionCode) {
        ZmwlRecord record = lockRecord(recordId, userId);
        ZmwlSetting setting = requireEnabled();
        if (isBlank(directionCode)) {
            throw new ServiceException("请选择一个未来发展方向");
        }
        ZmwlDirection direction = configService.findActive(directionCode);
        if (direction == null) {
            throw new ServiceException("该发展方向暂不可选，请刷新后重新选择");
        }
        boolean finished = STATUS_FINISHED.equals(record.getStatus());
        if (finished) {
            // 同一方向重复提交按幂等处理，避免网络重试被误判为修改。
            if (direction.getDirectionCode().equals(record.getDirectionCode())) {
                return view(record);
            }
            if (!"1".equals(setting.getAllowReselect())) {
                throw new ServiceException("未来发展方向已确认，不可修改");
            }
        }
        Date now = DateUtils.getNowDate();
        Date startedAt = record.getStartTime() == null ? record.getScanTime() : record.getStartTime();
        long duration = startedAt == null ? 0L : Math.max(0L, now.getTime() - startedAt.getTime());
        recordMapper.update(null, new UpdateWrapper<ZmwlRecord>().eq("record_id", recordId)
                .set("status", STATUS_FINISHED)
                .set("direction_code", direction.getDirectionCode())
                .set("direction_name", direction.getName())
                .set("tag_name", direction.getTagName())
                .set("interpretation", direction.getInterpretation())
                .set("message", direction.getMessage())
                .set("icon_url", direction.getIconUrl())
                .set("start_time", startedAt)
                .set("finish_time", now)
                .set("duration_ms", duration)
                .set("update_time", now));
        record.setStatus(STATUS_FINISHED);
        record.setDirectionCode(direction.getDirectionCode());
        record.setDirectionName(direction.getName());
        record.setTagName(direction.getTagName());
        record.setInterpretation(direction.getInterpretation());
        record.setMessage(direction.getMessage());
        record.setIconUrl(direction.getIconUrl());
        record.setStartTime(startedAt);
        record.setFinishTime(now);
        record.setDurationMs(duration);
        record.setUpdateTime(now);
        completeParticipation(record);
        return view(record);
    }

    /** 完成活动游戏参与记录；记录缺失时不阻断学生查看选择卡。 */
    private void completeParticipation(ZmwlRecord record) {
        GkzhGameParticipation participation = activityWeekService.getLatestParticipation(record.getGameId(), record.getUserId());
        if (participation == null) {
            return;
        }
        JSONObject result = new JSONObject();
        result.put("directionCode", record.getDirectionCode());
        result.put("directionName", record.getDirectionName());
        result.put("tagName", record.getTagName());
        participation.setStatus("1");
        participation.setFinishTime(DateUtils.getNowDate());
        participation.setResultJson(result.toJSONString());
        activityWeekService.updateGameParticipation(participation);
    }

    /** 学生端展示数据：记录状态、启用方向列表、规则参数和已生成的选择卡。 */
    public Map<String, Object> view(ZmwlRecord record) {
        ZmwlSetting setting = configService.getSetting();
        List<ZmwlDirection> directions = configService.listActive();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recordId", record.getRecordId());
        data.put("instanceId", record.getInstanceId());
        // 结果页据此返回对应的生涯活动或就业活动页面。
        GkzhActivityWeekInstance instance = record.getInstanceId() == null ? null
                : activityWeekService.getInstance(record.getInstanceId());
        if (instance != null) {
            data.put("bizType", instance.getBizType());
        }
        data.put("gameId", record.getGameId());
        data.put("status", record.getStatus());
        data.put("scanTime", record.getScanTime());
        data.put("startTime", record.getStartTime());
        data.put("finishTime", record.getFinishTime());
        data.put("durationMs", record.getDurationMs());

        Map<String, Object> settingView = new LinkedHashMap<>();
        settingView.put("allowReselect", setting.getAllowReselect());
        settingView.put("enabled", setting.getEnabled());
        data.put("setting", settingView);

        List<Map<String, Object>> catalog = new ArrayList<>();
        for (ZmwlDirection direction : directions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("directionId", direction.getDirectionId());
            item.put("directionCode", direction.getDirectionCode());
            item.put("name", direction.getName());
            item.put("shortIntro", direction.getShortIntro());
            item.put("tagName", direction.getTagName());
            item.put("iconUrl", direction.getIconUrl());
            item.put("selected", direction.getDirectionCode().equals(record.getDirectionCode()));
            catalog.add(item);
        }
        data.put("directions", catalog);

        if (STATUS_FINISHED.equals(record.getStatus())) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("directionCode", record.getDirectionCode());
            result.put("directionName", record.getDirectionName());
            result.put("tagName", record.getTagName());
            result.put("interpretation", record.getInterpretation());
            result.put("message", record.getMessage());
            result.put("iconUrl", record.getIconUrl());
            result.put("durationMs", record.getDurationMs());
            result.put("allowReselect", "1".equals(setting.getAllowReselect()));
            data.put("result", result);
        }
        return data;
    }

    /** 校验游戏是否启用并返回规则参数；停用时给出可理解的业务提示。 */
    private ZmwlSetting requireEnabled() {
        ZmwlSetting setting = configService.getSetting();
        if (!"1".equals(setting.getEnabled())) {
            throw new ServiceException("职面未来暂未开放，请稍后再试");
        }
        return setting;
    }

    /** 确认或重新选择前锁定记录行，避免并发请求产生两套方向。 */
    private ZmwlRecord lockRecord(Long recordId, Long userId) {
        ZmwlRecord record = recordMapper.selectOne(new QueryWrapper<ZmwlRecord>()
                .eq("record_id", recordId).last("FOR UPDATE"));
        if (record == null || userId == null || !userId.equals(record.getUserId())
                || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
