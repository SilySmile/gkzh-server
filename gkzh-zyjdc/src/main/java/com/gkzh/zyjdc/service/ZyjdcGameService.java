package com.gkzh.zyjdc.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zyjdc.domain.ZyjdcRecord;
import com.gkzh.zyjdc.domain.ZyjdcRole;
import com.gkzh.zyjdc.mapper.ZyjdcRecordMapper;
import com.gkzh.zyjdc.mapper.ZyjdcRoleMapper;
import com.gkzh.zyjdc.support.ZyjdcAi;
import com.gkzh.zyjdc.support.ZyjdcBoard;
import com.gkzh.zyjdc.support.ZyjdcGameState;
import com.gkzh.zyjdc.support.ZyjdcMove;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 职业角斗场学生端业务：扫码进入、选择角色、开局、落子、电脑走子、结算和重试。
 *
 * <p>棋盘和计时全部以服务端为准；客户端只提交起点与终点。
 * 只有获胜才把参与记录置为已完成，失败或平局保留参与状态并允许重试。</p>
 */
@Service
public class ZyjdcGameService {

    /** 游戏类型编码，与 gkzh_game_config.route、gkzh_game_type.game_type 保持一致。 */
    public static final String GAME_TYPE = "zyjdc";

    /** 对局结果：学生胜利。 */
    public static final String RESULT_WIN = "win";

    /** 对局结果：学生失败。 */
    public static final String RESULT_LOSE = "lose";

    /** 对局结果：平局。 */
    public static final String RESULT_DRAW = "draw";

    /** 结束原因：吃光对方。 */
    public static final String REASON_CAPTURE = "capture_all";

    /** 结束原因：3 分钟时间到。 */
    public static final String REASON_TIMEOUT = "timeout";

    /** 结束原因：无子可动。 */
    public static final String REASON_NO_MOVE = "no_move";

    private final ZyjdcRecordMapper recordMapper;
    private final ZyjdcRoleMapper roleMapper;
    private final IActivityWeekService activityWeekService;
    private final Random random = new Random();

    public ZyjdcGameService(ZyjdcRecordMapper recordMapper, ZyjdcRoleMapper roleMapper,
                            IActivityWeekService activityWeekService) {
        this.recordMapper = recordMapper;
        this.roleMapper = roleMapper;
        this.activityWeekService = activityWeekService;
    }

    /**
     * 扫码进入：校验学校、活动实例、游戏归属后幂等创建参与记录。
     * 扫码即计入参与统计，并与业务记录在同一事务写入活动游戏参与表。
     */
    @Transactional
    public ZyjdcRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId, Long studentId,
                             String studentNo, String studentName, Long departmentId, String major, String gender) {
        if (schoolId == null || instanceId == null || gameId == null || userId == null) {
            throw new ServiceException("缺少活动或学校信息，请重新扫码进入");
        }
        GkzhActivityGame game = activityWeekService.getGame(gameId);
        if (game == null || !instanceId.equals(game.getInstanceId()) || !GAME_TYPE.equals(game.getGameType())) {
            throw new ServiceException("该游戏不属于当前活动");
        }
        Long activitySchoolId = activityWeekService.getGameSchoolId(game);
        if (activitySchoolId != null && !activitySchoolId.equals(schoolId)) {
            throw new ServiceException("该活动不属于当前学校，无法进入");
        }

        ZyjdcRecord existing = recordMapper.selectOne(new QueryWrapper<ZyjdcRecord>()
                .eq("school_id", schoolId).eq("instance_id", instanceId)
                .eq("game_id", gameId).eq("user_id", userId));
        if (existing != null) {
            // 再次扫码只补空的学生快照，不覆盖已经选择的角色、棋盘和结果。
            UpdateWrapper<ZyjdcRecord> patch = new UpdateWrapper<ZyjdcRecord>().eq("record_id", existing.getRecordId());
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
        ZyjdcRecord record = new ZyjdcRecord();
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
        record.setStatus("participating");
        record.setStage("entered");
        record.setAttemptNo(1);
        record.setMoveCount(0);
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
    public ZyjdcRecord get(Long recordId, Long userId) {
        ZyjdcRecord record = recordMapper.selectById(recordId);
        if (record == null || userId == null || !userId.equals(record.getUserId())) {
            throw new ServiceException("参与记录不存在");
        }
        if (!GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    /** 读取本人记录并返回展示数据；对局超时会在读取时按服务端时间结算。 */
    @Transactional
    public Map<String, Object> viewRecord(Long recordId, Long userId) {
        ZyjdcRecord record = get(recordId, userId);
        return view(record);
    }

    /** 选择角色。对局开始后不允许更换；已通关记录只读。 */
    @Transactional
    public Map<String, Object> selectRole(Long recordId, Long userId, Long roleId) {
        ZyjdcRecord record = lockRecord(recordId, userId);
        if ("finished".equals(record.getStatus())) {
            throw new ServiceException("本局已通关，结果仅可查看");
        }
        if ("playing".equals(record.getStage())) {
            throw new ServiceException("对局进行中，不能更换角色");
        }
        ZyjdcRole role = roleId == null ? null : roleMapper.selectById(roleId);
        if (role == null || "1".equals(role.getStatus())) {
            throw new ServiceException("该角色暂不可选，请重新选择");
        }
        Date now = DateUtils.getNowDate();
        recordMapper.update(null, new UpdateWrapper<ZyjdcRecord>().eq("record_id", recordId)
                .set("role_id", role.getRoleId())
                .set("role_code", role.getRoleCode())
                .set("role_name", role.getRoleName())
                .set("job_title", role.getJobTitle())
                .set("stage", "role_selected")
                .set("update_time", now));
        record.setRoleId(role.getRoleId());
        record.setRoleCode(role.getRoleCode());
        record.setRoleName(role.getRoleName());
        record.setJobTitle(role.getJobTitle());
        record.setStage("role_selected");
        record.setUpdateTime(now);
        return view(record);
    }

    /** 开始对局；重复调用时返回当前进行中的棋盘。 */
    @Transactional
    public Map<String, Object> start(Long recordId, Long userId) {
        ZyjdcRecord record = lockRecord(recordId, userId);
        if ("finished".equals(record.getStatus())) {
            return view(record);
        }
        if (record.getRoleId() == null) {
            throw new ServiceException("请先选择角色");
        }
        if ("playing".equals(record.getStage())) {
            ZyjdcGameState state = parseState(record.getBoardJson());
            if (state != null && ZyjdcBoard.remainingSeconds(state, System.currentTimeMillis()) <= 0) {
                settleTimeout(record, state);
                return view(record);
            }
            if (state != null) {
                return view(record);
            }
        }
        ZyjdcRole role = roleMapper.selectById(record.getRoleId());
        if (role == null) {
            throw new ServiceException("所选角色已不存在，请重新选择");
        }
        ZyjdcGameState state = ZyjdcBoard.newState(role.getRoleCode(), random);
        Date now = DateUtils.getNowDate();
        // 从上一局失败/平局结果直接重新开始时，同样累计尝试次数。
        int attempt = record.getAttemptNo() == null ? 1 : record.getAttemptNo();
        if ("finished".equals(record.getStage())) {
            attempt++;
        }
        recordMapper.update(null, new UpdateWrapper<ZyjdcRecord>().eq("record_id", recordId)
                .set("board_json", JSON.toJSONString(state))
                .set("stage", "playing")
                .set("status", "participating")
                .set("attempt_no", attempt)
                .set("result", null)
                .set("result_reason", null)
                .set("remaining_seconds", null)
                .set("move_count", 0)
                .set("start_time", now)
                .set("finish_time", null)
                .set("update_time", now));
        record.setBoardJson(JSON.toJSONString(state));
        record.setStage("playing");
        record.setStatus("participating");
        record.setAttemptNo(attempt);
        record.setResult(null);
        record.setResultReason(null);
        record.setRemainingSeconds(null);
        record.setMoveCount(0);
        record.setStartTime(now);
        record.setFinishTime(null);
        record.setUpdateTime(now);
        return view(record);
    }

    /**
     * 学生落子：服务端校验合法性后落子，并立即执行电脑走子。
     * 接口返回学生走子、电脑走子、最新棋盘、剩余时间和结算结果。
     */
    @Transactional
    public Map<String, Object> move(Long recordId, Long userId,
                                    int fromRow, int fromCol, int toRow, int toCol) {
        ZyjdcRecord record = lockRecord(recordId, userId);
        if ("finished".equals(record.getStatus())) {
            throw new ServiceException("本局已通关，结果仅可查看");
        }
        if (!"playing".equals(record.getStage())) {
            throw new ServiceException("请先开始对局");
        }
        ZyjdcGameState state = parseState(record.getBoardJson());
        if (state == null) {
            throw new ServiceException("对局数据已失效，请重新开始");
        }
        if (ZyjdcBoard.remainingSeconds(state, System.currentTimeMillis()) <= 0) {
            settleTimeout(record, state);
            return view(record);
        }
        if (!ZyjdcGameState.PLAYER.equals(state.getTurn())) {
            throw new ServiceException("当前不是你的回合");
        }
        if (!ZyjdcBoard.isLegal(state.getBoard(), ZyjdcGameState.PLAYER, fromRow, fromCol, toRow, toCol)) {
            throw new ServiceException("该走法不合法，请重新选择");
        }

        ZyjdcBoard.apply(state, fromRow, fromCol, toRow, toCol);
        Map<String, Object> playerMove = lastMove(state);
        if (ZyjdcBoard.wipedOut(state.getBoard(), ZyjdcGameState.AI)) {
            return finishAndView(record, state, RESULT_WIN, REASON_CAPTURE, playerMove, null);
        }

        // 电脑走子；电脑无合法走法则学生胜。
        int[] aiChoice = ZyjdcAi.choose(state.getBoard(), random);
        if (aiChoice == null) {
            return finishAndView(record, state, RESULT_WIN, REASON_NO_MOVE, playerMove, null);
        }
        ZyjdcBoard.apply(state, aiChoice[0], aiChoice[1], aiChoice[2], aiChoice[3]);
        Map<String, Object> aiMove = lastMove(state);
        if (ZyjdcBoard.wipedOut(state.getBoard(), ZyjdcGameState.PLAYER)) {
            return finishAndView(record, state, RESULT_LOSE, REASON_CAPTURE, playerMove, aiMove);
        }
        if (ZyjdcBoard.legalMoves(state.getBoard(), ZyjdcGameState.PLAYER).isEmpty()) {
            return finishAndView(record, state, RESULT_LOSE, REASON_NO_MOVE, playerMove, aiMove);
        }

        Date now = DateUtils.getNowDate();
        String boardJson = JSON.toJSONString(state);
        recordMapper.update(null, new UpdateWrapper<ZyjdcRecord>().eq("record_id", recordId)
                .set("board_json", boardJson)
                .set("move_count", state.getMoveCount())
                .set("update_time", now));
        record.setBoardJson(boardJson);
        record.setMoveCount(state.getMoveCount());
        record.setUpdateTime(now);

        Map<String, Object> data = view(record);
        data.put("playerMove", playerMove);
        data.put("aiMove", aiMove);
        return data;
    }

    /** 失败或平局后重开一局：角色不变，棋盘与 3 分钟计时重置。 */
    @Transactional
    public Map<String, Object> retry(Long recordId, Long userId) {
        ZyjdcRecord record = lockRecord(recordId, userId);
        if ("finished".equals(record.getStatus())) {
            throw new ServiceException("已通关，不能重新挑战");
        }
        if (record.getRoleId() == null) {
            throw new ServiceException("请先选择角色");
        }
        // 对局进行中不能通过“重试”重置棋局逃避失败；到时未结算的，先按超时结算。
        if ("playing".equals(record.getStage())) {
            ZyjdcGameState current = parseState(record.getBoardJson());
            if (current == null) {
                throw new ServiceException("对局数据已失效，请重新进入");
            }
            if (ZyjdcBoard.remainingSeconds(current, System.currentTimeMillis()) > 0) {
                throw new ServiceException("对局进行中，不能重新开始");
            }
            settleTimeout(record, current);
        }
        if (!"finished".equals(record.getStage())) {
            throw new ServiceException("本局尚未结束，不能重新挑战");
        }
        // 超时结算刚好获胜时也不允许再重开，保持“通关后只读”。
        if ("finished".equals(record.getStatus())) {
            throw new ServiceException("已通关，不能重新挑战");
        }
        ZyjdcRole role = roleMapper.selectById(record.getRoleId());
        if (role == null) {
            throw new ServiceException("所选角色已不存在，请重新选择");
        }
        ZyjdcGameState state = ZyjdcBoard.newState(role.getRoleCode(), random);
        int attempt = record.getAttemptNo() == null ? 1 : record.getAttemptNo() + 1;
        Date now = DateUtils.getNowDate();
        recordMapper.update(null, new UpdateWrapper<ZyjdcRecord>().eq("record_id", recordId)
                .set("board_json", JSON.toJSONString(state))
                .set("stage", "playing")
                .set("status", "participating")
                .set("attempt_no", attempt)
                .set("result", null)
                .set("result_reason", null)
                .set("remaining_seconds", null)
                .set("move_count", 0)
                .set("start_time", now)
                .set("finish_time", null)
                .set("update_time", now));
        record.setBoardJson(JSON.toJSONString(state));
        record.setStage("playing");
        record.setStatus("participating");
        record.setAttemptNo(attempt);
        record.setResult(null);
        record.setResultReason(null);
        record.setRemainingSeconds(null);
        record.setMoveCount(0);
        record.setStartTime(now);
        record.setFinishTime(null);
        record.setUpdateTime(now);
        return view(record);
    }

    /** 后台删除参与记录：同步清理该活动游戏的全部活动参与行。 */
    @Transactional
    public void deleteRecord(Long recordId) {
        ZyjdcRecord record = recordId == null ? null : recordMapper.selectById(recordId);
        if (record == null || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        if (record.getInstanceId() == null || record.getGameId() == null || record.getUserId() == null) {
            throw new ServiceException("参与记录缺少活动上下文，无法删除");
        }
        activityWeekService.deleteGameParticipations(record.getInstanceId(), record.getGameId(), record.getUserId());
        recordMapper.deleteById(recordId);
    }

    /** 组装学生端展示数据：记录状态、角色内容、棋盘、剩余时间、合法走法和结果。 */
    public Map<String, Object> view(ZyjdcRecord record) {
        ZyjdcGameState state = parseState(record.getBoardJson());
        if (state != null && "playing".equals(record.getStage())
                && ZyjdcBoard.remainingSeconds(state, System.currentTimeMillis()) <= 0) {
            settleTimeout(record, state);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recordId", record.getRecordId());
        data.put("instanceId", record.getInstanceId());
        data.put("gameId", record.getGameId());
        GkzhActivityWeekInstance instance = record.getInstanceId() == null ? null
                : activityWeekService.getInstance(record.getInstanceId());
        if (instance != null) {
            // 结果页据此返回对应的生涯活动或就业活动页面。
            data.put("bizType", instance.getBizType());
        }
        data.put("status", record.getStatus());
        data.put("stage", record.getStage());
        data.put("attemptNo", record.getAttemptNo());
        data.put("scanTime", record.getScanTime());
        data.put("startTime", record.getStartTime());
        data.put("finishTime", record.getFinishTime());
        data.put("result", record.getResult());
        data.put("resultReason", record.getResultReason());
        data.put("remainingSeconds", record.getRemainingSeconds());
        data.put("moveCount", record.getMoveCount());

        ZyjdcRole role = record.getRoleId() == null ? null : roleMapper.selectById(record.getRoleId());
        data.put("roleId", record.getRoleId());
        data.put("roleCode", record.getRoleCode());
        data.put("roleName", record.getRoleName() == null && role != null ? role.getRoleName() : record.getRoleName());
        data.put("jobTitle", record.getJobTitle() == null && role != null ? role.getJobTitle() : record.getJobTitle());
        if (role != null) {
            data.put("role", role);
        }
        data.put("pieceMeta", pieceMeta());

        if (state != null) {
            data.put("board", state.getBoard());
            data.put("turn", state.getTurn());
            data.put("playerRoles", state.getPlayerRoles());
            data.put("aiRoles", state.getAiRoles());
            data.put("moves", state.getMoves());
            data.put("remainingSeconds", ZyjdcBoard.remainingSeconds(state, System.currentTimeMillis()));
            if ("playing".equals(record.getStage()) && ZyjdcGameState.PLAYER.equals(state.getTurn())) {
                data.put("legalMoves", legalMoves(state));
            } else {
                data.put("legalMoves", new ArrayList<>());
            }
        }
        data.put("completed", RESULT_WIN.equals(record.getResult()));
        return data;
    }

    /** 解析棋盘快照；损坏时返回 null，由调用方提示重新开始。 */
    public ZyjdcGameState parseState(String boardJson) {
        if (isBlank(boardJson)) {
            return null;
        }
        try {
            return JSON.parseObject(boardJson, ZyjdcGameState.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** 电脑棋子与等级信息，供小程序绘制棋子头像。 */
    private Map<String, Object> pieceMeta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        for (ZyjdcRole item : roleMapper.selectList(new QueryWrapper<ZyjdcRole>().orderByAsc("sort_order"))) {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("roleName", item.getRoleName());
            value.put("imageUrl", item.getImageUrl());
            value.put("rankLevel", item.getRankLevel());
            value.put("jobTitle", item.getJobTitle());
            meta.put(item.getRoleCode(), value);
        }
        return meta;
    }

    /** 学生当前全部合法走法，客户端直接用于高亮提示。 */
    private List<Map<String, Integer>> legalMoves(ZyjdcGameState state) {
        List<Map<String, Integer>> rows = new ArrayList<>();
        for (int[] move : ZyjdcBoard.legalMoves(state.getBoard(), ZyjdcGameState.PLAYER)) {
            Map<String, Integer> item = new LinkedHashMap<>();
            item.put("fromRow", move[0]);
            item.put("fromCol", move[1]);
            item.put("toRow", move[2]);
            item.put("toCol", move[3]);
            rows.add(item);
        }
        return rows;
    }

    /** 最近一步落子，供客户端播放落子动画。 */
    private Map<String, Object> lastMove(ZyjdcGameState state) {
        if (state.getMoves() == null || state.getMoves().isEmpty()) {
            return null;
        }
        ZyjdcMove move = state.getMoves().get(state.getMoves().size() - 1);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("side", move.getSide());
        data.put("code", move.getCode());
        data.put("fromRow", move.getFromRow());
        data.put("fromCol", move.getFromCol());
        data.put("toRow", move.getToRow());
        data.put("toCol", move.getToCol());
        data.put("captured", move.getCaptured());
        return data;
    }

    /** 结算并返回展示数据，同时保存棋盘和结果。 */
    private Map<String, Object> finishAndView(ZyjdcRecord record, ZyjdcGameState state, String result,
                                              String reason, Map<String, Object> playerMove,
                                              Map<String, Object> aiMove) {
        Date now = DateUtils.getNowDate();
        int remaining = ZyjdcBoard.remainingSeconds(state, System.currentTimeMillis());
        boolean win = RESULT_WIN.equals(result);
        recordMapper.update(null, new UpdateWrapper<ZyjdcRecord>().eq("record_id", record.getRecordId())
                .set("board_json", JSON.toJSONString(state))
                .set("stage", "finished")
                .set("status", win ? "finished" : "participating")
                .set("result", result)
                .set("result_reason", reason)
                .set("remaining_seconds", remaining)
                .set("move_count", state.getMoveCount())
                .set("finish_time", now)
                .set("update_time", now));
        record.setBoardJson(JSON.toJSONString(state));
        record.setStage("finished");
        record.setStatus(win ? "finished" : "participating");
        record.setResult(result);
        record.setResultReason(reason);
        record.setRemainingSeconds(remaining);
        record.setMoveCount(state.getMoveCount());
        record.setFinishTime(now);
        record.setUpdateTime(now);
        if (win) {
            completeParticipation(record, reason);
        }
        Map<String, Object> data = view(record);
        data.put("playerMove", playerMove);
        data.put("aiMove", aiMove);
        return data;
    }

    /** 3 分钟到时按超时规则结算。 */
    private void settleTimeout(ZyjdcRecord record, ZyjdcGameState state) {
        String result = ZyjdcBoard.timeoutResult(state.getBoard());
        finishAndView(record, state, result, REASON_TIMEOUT, null, null);
    }

    /** 胜利时同步完成活动游戏参与记录；参与记录缺失不影响学生查看结果。 */
    private void completeParticipation(ZyjdcRecord record, String reason) {
        GkzhGameParticipation participation = activityWeekService.getLatestParticipation(record.getGameId(), record.getUserId());
        if (participation == null) {
            return;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("roleCode", record.getRoleCode());
        result.put("roleName", record.getRoleName());
        result.put("jobTitle", record.getJobTitle());
        result.put("result", RESULT_WIN);
        result.put("reason", reason);
        participation.setStatus("1");
        participation.setFinishTime(DateUtils.getNowDate());
        participation.setResultJson(JSON.toJSONString(result));
        activityWeekService.updateGameParticipation(participation);
    }

    /** 落子、开局、重试前锁定记录行，避免并发请求产生两套棋盘或两次完成。 */
    private ZyjdcRecord lockRecord(Long recordId, Long userId) {
        ZyjdcRecord record = recordMapper.selectOne(new QueryWrapper<ZyjdcRecord>()
                .eq("record_id", recordId).last("FOR UPDATE"));
        if (record == null || userId == null || !userId.equals(record.getUserId())) {
            throw new ServiceException("参与记录不存在");
        }
        if (!GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
