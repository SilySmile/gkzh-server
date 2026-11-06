package com.gkzh.zyfxq.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zyfxq.domain.ZyfxqCell;
import com.gkzh.zyfxq.domain.ZyfxqOption;
import com.gkzh.zyfxq.domain.ZyfxqQuestion;
import com.gkzh.zyfxq.domain.ZyfxqSetting;
import com.gkzh.zyfxq.mapper.ZyfxqCellMapper;
import com.gkzh.zyfxq.mapper.ZyfxqOptionMapper;
import com.gkzh.zyfxq.mapper.ZyfxqQuestionMapper;
import com.gkzh.zyfxq.mapper.ZyfxqSettingMapper;
import com.gkzh.zyfxq.support.ZyfxqBoard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职业飞行棋配置服务：棋盘格子、规则参数和题库。
 *
 * <p>题库分两类（职业问答 career、情景任务卡 scenario），每题固定 4 个选项且必须有且只有一个
 * 正确答案；保存时统一校验，避免学生端出现无法判分的题目。</p>
 */
@Service
public class ZyfxqConfigService {

    /** 题目类型：职业问答。 */
    public static final String TYPE_CAREER = "career";

    /** 题目类型：情景任务卡。 */
    public static final String TYPE_SCENARIO = "scenario";

    /** 格子类型：普通格。 */
    public static final String CELL_NORMAL = "normal";

    /** 格子类型：答题格。 */
    public static final String CELL_QUESTION = "question";

    /** 格子类型：终点。 */
    public static final String CELL_END = "end";

    /** 固定选项字母，顺序即学生端展示顺序。 */
    private static final String LABELS = "ABCD";

    private final ZyfxqCellMapper cellMapper;
    private final ZyfxqQuestionMapper questionMapper;
    private final ZyfxqOptionMapper optionMapper;
    private final ZyfxqSettingMapper settingMapper;

    public ZyfxqConfigService(ZyfxqCellMapper cellMapper, ZyfxqQuestionMapper questionMapper,
                              ZyfxqOptionMapper optionMapper, ZyfxqSettingMapper settingMapper) {
        this.cellMapper = cellMapper;
        this.questionMapper = questionMapper;
        this.optionMapper = optionMapper;
        this.settingMapper = settingMapper;
    }

    /** 读取规则参数；配置行缺失时返回与初始化脚本一致的默认值。 */
    public ZyfxqSetting setting() {
        ZyfxqSetting setting = settingMapper.selectById(1);
        if (setting != null) {
            return setting;
        }
        ZyfxqSetting fallback = new ZyfxqSetting();
        fallback.setSettingId(1);
        fallback.setTotalSeconds(180);
        fallback.setQuestionSeconds(20);
        fallback.setBackSteps(2);
        fallback.setDiceFaces(6);
        fallback.setEnabled("1");
        return fallback;
    }

    /** 全部棋盘格，按格号升序。 */
    public List<ZyfxqCell> listCells() {
        return cellMapper.selectList(new QueryWrapper<ZyfxqCell>().orderByAsc("cell_no"));
    }

    /** 按格号取格子；不存在返回 null。 */
    public ZyfxqCell getCell(int cellNo) {
        return cellMapper.selectOne(new QueryWrapper<ZyfxqCell>().eq("cell_no", cellNo).last("LIMIT 1"));
    }

    /** 某类型的启用题目，按排序值和编号升序。 */
    public List<ZyfxqQuestion> listEnabledQuestions(String questionType) {
        return questionMapper.selectList(new QueryWrapper<ZyfxqQuestion>()
                .eq("question_type", questionType).eq("status", "0")
                .orderByAsc("sort_order").orderByAsc("question_id"));
    }

    /** 后台题目列表：可按类型过滤，包含停用题目，并按题目附带 4 个选项。 */
    public List<ZyfxqQuestion> listAdminQuestions(String questionType) {
        QueryWrapper<ZyfxqQuestion> wrapper = new QueryWrapper<>();
        if (isValidType(questionType)) {
            wrapper.eq("question_type", questionType);
        }
        wrapper.orderByAsc("question_type").orderByAsc("sort_order").orderByAsc("question_id");
        List<ZyfxqQuestion> questions = questionMapper.selectList(wrapper);
        if (questions.isEmpty()) {
            return questions;
        }
        List<Long> ids = new ArrayList<>();
        for (ZyfxqQuestion question : questions) {
            ids.add(question.getQuestionId());
        }
        Map<Long, List<ZyfxqOption>> optionMap = new LinkedHashMap<>();
        for (ZyfxqOption option : optionMapper.selectList(new QueryWrapper<ZyfxqOption>()
                .in("question_id", ids).orderByAsc("question_id").orderByAsc("sort_order"))) {
            optionMap.computeIfAbsent(option.getQuestionId(), key -> new ArrayList<>()).add(option);
        }
        for (ZyfxqQuestion question : questions) {
            question.setOptions(optionMap.getOrDefault(question.getQuestionId(), new ArrayList<>()));
        }
        return questions;
    }

    /** 按编号取题目及其选项；停用题目也返回，供历史记录详情展示。 */
    public ZyfxqQuestion getQuestion(Long questionId) {
        ZyfxqQuestion question = questionId == null ? null : questionMapper.selectById(questionId);
        if (question == null) {
            return null;
        }
        question.setOptions(optionMapper.selectList(new QueryWrapper<ZyfxqOption>()
                .eq("question_id", questionId).orderByAsc("sort_order")));
        return question;
    }

    /** 保存格子类型、题目类型、文案、底色与启停；19 格必须保持终点格。 */
    @Transactional
    public ZyfxqCell saveCell(ZyfxqCell cell) {
        if (cell == null || cell.getCellNo() == null) {
            throw new ServiceException("缺少格子编号");
        }
        ZyfxqCell existing = getCell(cell.getCellNo());
        if (existing == null) {
            throw new ServiceException("格子不存在，请执行数据库迁移后再配置");
        }
        String cellType = trimToNull(cell.getCellType());
        if (!CELL_NORMAL.equals(cellType) && !CELL_QUESTION.equals(cellType) && !CELL_END.equals(cellType)) {
            throw new ServiceException("格子类型只能是普通格、答题格或终点");
        }
        boolean lastCell = cell.getCellNo() == ZyfxqBoard.END_CELL;
        if (lastCell && !CELL_END.equals(cellType)) {
            throw new ServiceException("第 19 格必须是终点格");
        }
        if (!lastCell && CELL_END.equals(cellType)) {
            throw new ServiceException("只有第 19 格可以设置为终点");
        }
        String questionType = null;
        if (CELL_QUESTION.equals(cellType)) {
            questionType = trimToNull(cell.getQuestionType());
            if (!isValidType(questionType)) {
                throw new ServiceException("答题格必须选择题目类型：职业问答或情景任务卡");
            }
            if (listEnabledQuestions(questionType).isEmpty()) {
                throw new ServiceException("该类型暂无可启用题目，请先在题库中新增题目");
            }
        }
        String color = trimToNull(cell.getCellColor());
        if (color == null || !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new ServiceException("格子底色必须为 #RRGGBB 格式");
        }
        existing.setCellType(cellType);
        existing.setQuestionType(questionType);
        existing.setCellLabel(trimToNull(cell.getCellLabel()));
        existing.setCellColor(color.toUpperCase());
        existing.setStatus("1".equals(cell.getStatus()) ? "1" : "0");
        existing.setUpdateTime(DateUtils.getNowDate());
        cellMapper.updateById(existing);
        return existing;
    }

    /** 保存规则参数；数值范围做下限保护，避免出现无法完成的对局。 */
    @Transactional
    public ZyfxqSetting saveSetting(ZyfxqSetting setting) {
        if (setting == null) {
            throw new ServiceException("缺少规则参数");
        }
        int totalSeconds = intValue(setting.getTotalSeconds(), 180);
        int questionSeconds = intValue(setting.getQuestionSeconds(), 20);
        int backSteps = intValue(setting.getBackSteps(), 2);
        int diceFaces = intValue(setting.getDiceFaces(), 6);
        if (totalSeconds < 30 || totalSeconds > 1800) {
            throw new ServiceException("单局总时长需在 30 至 1800 秒之间");
        }
        if (questionSeconds < 5 || questionSeconds > 300) {
            throw new ServiceException("单题限时需在 5 至 300 秒之间");
        }
        if (questionSeconds >= totalSeconds) {
            throw new ServiceException("单题限时不能大于等于单局总时长");
        }
        if (backSteps < 0 || backSteps > 10) {
            throw new ServiceException("答错后退格数需在 0 至 10 之间");
        }
        if (diceFaces < 1 || diceFaces > 12) {
            throw new ServiceException("骰子面数需在 1 至 12 之间");
        }
        ZyfxqSetting existing = settingMapper.selectById(1);
        Date now = DateUtils.getNowDate();
        if (existing == null) {
            setting.setSettingId(1);
            setting.setTotalSeconds(totalSeconds);
            setting.setQuestionSeconds(questionSeconds);
            setting.setBackSteps(backSteps);
            setting.setDiceFaces(diceFaces);
            setting.setEnabled("0".equals(setting.getEnabled()) ? "0" : "1");
            setting.setCreateTime(now);
            setting.setUpdateTime(now);
            settingMapper.insert(setting);
            return setting;
        }
        existing.setTotalSeconds(totalSeconds);
        existing.setQuestionSeconds(questionSeconds);
        existing.setBackSteps(backSteps);
        existing.setDiceFaces(diceFaces);
        existing.setEnabled("0".equals(setting.getEnabled()) ? "0" : "1");
        existing.setUpdateTime(now);
        settingMapper.updateById(existing);
        return existing;
    }

    /** 新增或修改题目及其 4 个选项；题目 id 为空时新增。 */
    @Transactional
    public ZyfxqQuestion saveQuestion(ZyfxqQuestion question) {
        if (question == null) {
            throw new ServiceException("缺少题目内容");
        }
        String type = trimToNull(question.getQuestionType());
        if (!isValidType(type)) {
            throw new ServiceException("题目类型只能是职业问答或情景任务卡");
        }
        String title = trimToNull(question.getTitle());
        if (title == null) {
            throw new ServiceException("题干不能为空");
        }
        List<ZyfxqOption> options = question.getOptions();
        if (options == null || options.size() != LABELS.length()) {
            throw new ServiceException("每题必须填写 4 个选项");
        }
        Map<String, ZyfxqOption> byLabel = new LinkedHashMap<>();
        int correctCount = 0;
        int index = 0;
        for (ZyfxqOption option : options) {
            String content = trimToNull(option == null ? null : option.getOptionContent());
            if (content == null) {
                throw new ServiceException("选项内容不能为空");
            }
            String label = String.valueOf(LABELS.charAt(index));
            ZyfxqOption normalized = new ZyfxqOption();
            normalized.setOptionLabel(label);
            normalized.setOptionContent(content);
            normalized.setIsCorrect("1".equals(option.getIsCorrect()) ? "1" : "0");
            normalized.setSortOrder(index + 1);
            if ("1".equals(normalized.getIsCorrect())) {
                correctCount++;
            }
            byLabel.put(label, normalized);
            index++;
        }
        if (correctCount != 1) {
            throw new ServiceException("每题必须且只能设置一个正确答案");
        }
        Date now = DateUtils.getNowDate();
        ZyfxqQuestion target;
        if (question.getQuestionId() == null) {
            target = new ZyfxqQuestion();
            target.setCreateTime(now);
        } else {
            target = questionMapper.selectById(question.getQuestionId());
            if (target == null) {
                throw new ServiceException("题目不存在");
            }
        }
        target.setQuestionType(type);
        target.setTitle(title);
        target.setAnalysis(trimToNull(question.getAnalysis()));
        target.setSortOrder(question.getSortOrder() == null ? 0 : question.getSortOrder());
        target.setStatus("1".equals(question.getStatus()) ? "1" : "0");
        target.setUpdateTime(now);
        if (target.getQuestionId() == null) {
            questionMapper.insert(target);
        } else {
            questionMapper.updateById(target);
            optionMapper.delete(new QueryWrapper<ZyfxqOption>().eq("question_id", target.getQuestionId()));
        }
        for (ZyfxqOption option : byLabel.values()) {
            ZyfxqOption row = new ZyfxqOption();
            row.setQuestionId(target.getQuestionId());
            row.setOptionLabel(option.getOptionLabel());
            row.setOptionContent(option.getOptionContent());
            row.setIsCorrect(option.getIsCorrect());
            row.setSortOrder(option.getSortOrder());
            row.setCreateTime(now);
            optionMapper.insert(row);
        }
        return getQuestion(target.getQuestionId());
    }

    /** 删除题目及其选项；历史答题明细保留题干与选项快照，不受影响。 */
    @Transactional
    public void deleteQuestion(Long questionId) {
        ZyfxqQuestion question = questionId == null ? null : questionMapper.selectById(questionId);
        if (question == null) {
            throw new ServiceException("题目不存在");
        }
        optionMapper.delete(new QueryWrapper<ZyfxqOption>().eq("question_id", questionId));
        questionMapper.deleteById(questionId);
    }

    /** 题目类型中文名称。 */
    public static String questionTypeName(String questionType) {
        if (TYPE_CAREER.equals(questionType)) {
            return "职业问答";
        }
        if (TYPE_SCENARIO.equals(questionType)) {
            return "情景任务卡";
        }
        return "未分类";
    }

    /** 是否合法题目类型。 */
    public static boolean isValidType(String questionType) {
        return TYPE_CAREER.equals(questionType) || TYPE_SCENARIO.equals(questionType);
    }

    private static String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static int intValue(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
