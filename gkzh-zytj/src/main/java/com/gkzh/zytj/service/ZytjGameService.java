package com.gkzh.zytj.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zytj.domain.ZytjItem;
import com.gkzh.zytj.domain.ZytjRecord;
import com.gkzh.zytj.domain.ZytjSetting;
import com.gkzh.zytj.domain.ZytjType;
import com.gkzh.zytj.mapper.ZytjRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职愿探究学生端业务：扫码进入、进入选择页、提交选择并生成职业探索卡。
 *
 * <p>规则来自《线上开发规则——职愿探究.xlsx》：30 秒内从四列橱窗各选 1 件，
 * 按列序生成四位人格类型代码。扫码即创建参与记录并计入统计；提交时由服务端按当前
 * 配置写入人格类型与三段文案快照，不信任客户端提交的字母或文案。</p>
 *
 * <p>超时口径（负责人 2026-11-10 确认）：到点未选满时由服务端按该列第一个启用物品
 * 补全后照常提交，并标记 is_timeout=1，保证每名学生都能拿到职业探索卡。</p>
 */
@Service
public class ZytjGameService {

    /** 游戏类型编码，与 gkzh_game_config.route、gkzh_game_type.game_type 保持一致。 */
    public static final String GAME_TYPE = "zytj";

    /** 参与状态：进行中。 */
    public static final String STATUS_PARTICIPATING = "participating";

    /** 参与状态：已提交。 */
    public static final String STATUS_FINISHED = "finished";

    /** 橱窗列数：四列各对应一个 MBTI 维度。 */
    public static final int COLUMN_COUNT = 4;

    private final ZytjRecordMapper recordMapper;
    private final ZytjConfigService configService;
    private final IActivityWeekService activityWeekService;

    public ZytjGameService(ZytjRecordMapper recordMapper, ZytjConfigService configService,
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
    public ZytjRecord enter(Long schoolId, Long instanceId, Long gameId, Long userId, Long studentId,
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
        ZytjRecord existing = recordMapper.selectOne(new QueryWrapper<ZytjRecord>()
                .eq("school_id", schoolId).eq("instance_id", instanceId)
                .eq("game_id", gameId).eq("user_id", userId));
        if (existing != null) {
            // 再次扫码只补空的学生快照，不覆盖已经生成的人格类型与文案快照。
            UpdateWrapper<ZytjRecord> patch = new UpdateWrapper<ZytjRecord>().eq("record_id", existing.getRecordId());
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
        ZytjRecord record = new ZytjRecord();
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
        record.setIsTimeout("0");
        record.setAttemptNo(1);
        record.setTimeLimit(configService.getSetting().getTimeLimit());
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
    public ZytjRecord get(Long recordId, Long userId) {
        ZytjRecord record = recordMapper.selectById(recordId);
        if (record == null || userId == null || !userId.equals(record.getUserId())
                || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    /**
     * 进入选择页：记录开始计时时间，用于服务端计算用时与超时。
     * 首次调用写入开始时间；开启“允许重新探究”后重新进入会重置计时。
     */
    @Transactional
    public Map<String, Object> start(Long recordId, Long userId) {
        ZytjRecord record = lockRecord(recordId, userId);
        ZytjSetting setting = requireEnabled();
        boolean finished = STATUS_FINISHED.equals(record.getStatus());
        boolean reselect = "1".equals(setting.getAllowReselect());
        if (!finished || reselect) {
            Date now = DateUtils.getNowDate();
            recordMapper.update(null, new UpdateWrapper<ZytjRecord>().eq("record_id", recordId)
                    .set("start_time", now).set("time_limit", setting.getTimeLimit()).set("update_time", now));
            record.setStartTime(now);
            record.setTimeLimit(setting.getTimeLimit());
            record.setUpdateTime(now);
        }
        return view(record);
    }

    /**
     * 提交选择并生成职业探索卡。
     *
     * @param recordId    参与记录编号
     * @param userId      当前登录用户编号
     * @param itemCodes   按列序选择的物品编码，长度 4；未选中的列允许为空，由服务端补全
     * @param clientTimeout 客户端是否已到点自动提交
     */
    @Transactional
    public Map<String, Object> submit(Long recordId, Long userId, List<String> itemCodes, boolean clientTimeout) {
        ZytjRecord record = lockRecord(recordId, userId);
        ZytjSetting setting = requireEnabled();
        if (itemCodes == null || itemCodes.isEmpty()) {
            throw new ServiceException("请先在每列选择一个心仪物品");
        }

        List<String> slots = new ArrayList<>(itemCodes);
        while (slots.size() < COLUMN_COUNT) {
            slots.add(null);
        }

        // 逐列解析：合法选择直接采用；未选、已停用或不属于该列时按该列首个启用物品补全。
        StringBuilder codes = new StringBuilder();
        StringBuilder ids = new StringBuilder();
        boolean autoFilled = false;
        for (int index = 0; index < COLUMN_COUNT; index++) {
            String code = trimToNull(slots.get(index));
            ZytjItem item = code == null ? null : configService.findActiveItem(code);
            if (item == null || !Integer.valueOf(index + 1).equals(item.getColumnIndex())) {
                item = firstActiveItemOfColumn(index + 1);
                if (item == null) {
                    throw new ServiceException("橱窗物品尚未配置完整，请稍后重试");
                }
                autoFilled = true;
            }
            if (index > 0) {
                codes.append(',');
                ids.append(',');
            }
            codes.append(item.getItemCode());
            ids.append(item.getItemId());
        }

        String typeCode = codes.toString().replace(",", "");
        boolean finished = STATUS_FINISHED.equals(record.getStatus());
        if (finished) {
            // 同一结果重复提交按幂等处理，避免网络重试被误判为修改。
            if (typeCode.equals(record.getTypeCode())) {
                return view(record);
            }
            if (!"1".equals(setting.getAllowReselect())) {
                throw new ServiceException("职业探索卡已生成，不可修改");
            }
        }

        ZytjType type = configService.findType(typeCode);
        if (type == null) {
            throw new ServiceException("该人格类型尚未配置，请稍后重试");
        }

        Date now = DateUtils.getNowDate();
        Date startedAt = record.getStartTime() == null ? record.getScanTime() : record.getStartTime();
        long duration = startedAt == null ? 0L : Math.max(0L, now.getTime() - startedAt.getTime());
        Integer limit = setting.getTimeLimit();
        boolean timeout = clientTimeout || autoFilled
                || (limit != null && limit > 0 && duration > limit * 1000L);
        int attemptNo = (record.getAttemptNo() == null ? 1 : record.getAttemptNo()) + (finished ? 1 : 0);

        recordMapper.update(null, new UpdateWrapper<ZytjRecord>().eq("record_id", recordId)
                .set("status", STATUS_FINISHED)
                .set("type_code", typeCode)
                .set("feature", type.getFeature())
                .set("career_field", type.getCareerField())
                .set("typical_careers", type.getTypicalCareers())
                .set("item_codes", codes.toString())
                .set("item_ids", ids.toString())
                .set("time_limit", limit)
                .set("is_timeout", timeout ? "1" : "0")
                .set("attempt_no", attemptNo)
                .set("start_time", startedAt)
                .set("finish_time", now)
                .set("duration_ms", duration)
                .set("update_time", now));

        record.setStatus(STATUS_FINISHED);
        record.setTypeCode(typeCode);
        record.setFeature(type.getFeature());
        record.setCareerField(type.getCareerField());
        record.setTypicalCareers(type.getTypicalCareers());
        record.setItemCodes(codes.toString());
        record.setItemIds(ids.toString());
        record.setTimeLimit(limit);
        record.setIsTimeout(timeout ? "1" : "0");
        record.setAttemptNo(attemptNo);
        record.setStartTime(startedAt);
        record.setFinishTime(now);
        record.setDurationMs(duration);
        record.setUpdateTime(now);
        completeParticipation(record);
        return view(record);
    }

    /** 完成活动游戏参与记录；记录缺失时不阻断学生查看职业探索卡。 */
    private void completeParticipation(ZytjRecord record) {
        GkzhGameParticipation participation = activityWeekService.getLatestParticipation(record.getGameId(), record.getUserId());
        if (participation == null) {
            return;
        }
        JSONObject result = new JSONObject();
        result.put("typeCode", record.getTypeCode());
        result.put("feature", record.getFeature());
        participation.setStatus("1");
        participation.setFinishTime(DateUtils.getNowDate());
        participation.setResultJson(result.toJSONString());
        activityWeekService.updateGameParticipation(participation);
    }

    /** 学生端展示数据：记录状态、四列橱窗物品、规则参数和已生成的职业探索卡。 */
    public Map<String, Object> view(ZytjRecord record) {
        ZytjSetting setting = configService.getSetting();
        List<ZytjItem> items = configService.listActiveItems();

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
        data.put("attemptNo", record.getAttemptNo());
        data.put("isTimeout", record.getIsTimeout());

        Map<String, Object> settingView = new LinkedHashMap<>();
        settingView.put("timeLimit", setting.getTimeLimit());
        settingView.put("allowReselect", setting.getAllowReselect());
        settingView.put("enabled", setting.getEnabled());
        data.put("setting", settingView);

        List<String> selectedCodes = splitCodes(record.getItemCodes());
        List<Map<String, Object>> columns = new ArrayList<>();
        for (int index = 0; index < COLUMN_COUNT; index++) {
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("columnIndex", index + 1);
            column.put("dimensionCode", ZytjConfigService.COLUMN_DIMENSIONS[index]);
            List<Map<String, Object>> columnItems = new ArrayList<>();
            for (ZytjItem item : items) {
                if (!Integer.valueOf(index + 1).equals(item.getColumnIndex())) {
                    continue;
                }
                Map<String, Object> node = new LinkedHashMap<>();
                node.put("itemId", item.getItemId());
                node.put("itemCode", item.getItemCode());
                node.put("itemName", item.getItemName());
                node.put("imageUrl", item.getImageUrl());
                node.put("metaphor", item.getMetaphor());
                node.put("rowIndex", item.getRowIndex());
                node.put("selected", index < selectedCodes.size()
                        && item.getItemCode().equals(selectedCodes.get(index)));
                columnItems.add(node);
            }
            column.put("items", columnItems);
            columns.add(column);
        }
        data.put("columns", columns);

        if (STATUS_FINISHED.equals(record.getStatus())) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("typeCode", record.getTypeCode());
            result.put("letters", splitLetters(record.getTypeCode()));
            result.put("feature", record.getFeature());
            result.put("careerField", record.getCareerField());
            result.put("typicalCareers", record.getTypicalCareers());
            result.put("isTimeout", "1".equals(record.getIsTimeout()));
            result.put("durationMs", record.getDurationMs());
            result.put("attemptNo", record.getAttemptNo());
            result.put("allowReselect", "1".equals(setting.getAllowReselect()));
            data.put("result", result);
        }
        return data;
    }

    /** 取某列第一个启用物品，用于超时补全。 */
    private ZytjItem firstActiveItemOfColumn(int columnIndex) {
        List<ZytjItem> list = configService.listActiveItemsByColumn(columnIndex);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 校验游戏是否启用并返回规则参数；停用时给出可理解的业务提示。 */
    private ZytjSetting requireEnabled() {
        ZytjSetting setting = configService.getSetting();
        if (!"1".equals(setting.getEnabled())) {
            throw new ServiceException("职愿探究暂未开放，请稍后再试");
        }
        return setting;
    }

    /** 提交前锁定记录行，避免并发请求生成两套人格类型。 */
    private ZytjRecord lockRecord(Long recordId, Long userId) {
        ZytjRecord record = recordMapper.selectOne(new QueryWrapper<ZytjRecord>()
                .eq("record_id", recordId).last("FOR UPDATE"));
        if (record == null || userId == null || !userId.equals(record.getUserId())
                || !GAME_TYPE.equals(record.getGameType())) {
            throw new ServiceException("参与记录不存在");
        }
        return record;
    }

    /** 拆分形如“E,N,T,P”的维度编码快照。 */
    private static List<String> splitCodes(String itemCodes) {
        List<String> list = new ArrayList<>();
        if (isBlank(itemCodes)) {
            return list;
        }
        for (String part : itemCodes.split(",")) {
            if (!isBlank(part)) {
                list.add(part.trim());
            }
        }
        return list;
    }

    /** 拆分四位人格代码为字母列表，便于学生端逐格渲染彩色字母块。 */
    private static List<String> splitLetters(String typeCode) {
        List<String> letters = new ArrayList<>();
        if (isBlank(typeCode)) {
            return letters;
        }
        for (char letter : typeCode.trim().toCharArray()) {
            letters.add(String.valueOf(letter));
        }
        return letters;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim().toUpperCase();
    }
}
