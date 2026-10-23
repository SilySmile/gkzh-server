package com.gkzh.zcyltzj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zcyltzj.domain.ZcyltzjPoint;
import com.gkzh.zcyltzj.domain.ZcyltzjQuestion;
import com.gkzh.zcyltzj.mapper.ZcyltzjPointMapper;
import com.gkzh.zcyltzj.mapper.ZcyltzjQuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 骰子局后台配置：维护六个点数的名称、主题、职场投射以及每个点数的试题。
 * 点数固定为 1-6，不允许新增其他点数。
 */
@Service
public class ZcyltzjConfigService {

    private final ZcyltzjPointMapper pointMapper;
    private final ZcyltzjQuestionMapper questionMapper;

    public ZcyltzjConfigService(ZcyltzjPointMapper pointMapper, ZcyltzjQuestionMapper questionMapper) {
        this.pointMapper = pointMapper;
        this.questionMapper = questionMapper;
    }

    /** 查询六个点数，并附带每个点数已启用的题目数量。 */
    public List<ZcyltzjPoint> listPoints() {
        List<ZcyltzjPoint> points = pointMapper.selectList(new QueryWrapper<ZcyltzjPoint>()
                .orderByAsc("point_no"));
        for (ZcyltzjPoint point : points) {
            point.setQuestionCount(questionMapper.selectCount(new QueryWrapper<ZcyltzjQuestion>()
                    .eq("point_no", point.getPointNo()).eq("status", "0")));
        }
        return points;
    }

    /** 学生端九宫格展示使用：只返回启用点数，按点数排序。 */
    public List<ZcyltzjPoint> listActivePoints() {
        return pointMapper.selectList(new QueryWrapper<ZcyltzjPoint>()
                .eq("status", "0").orderByAsc("point_no"));
    }

    /** 保存点数名称、主题和职场投射；点数为固定值，不可修改。 */
    @Transactional
    public ZcyltzjPoint savePoint(ZcyltzjPoint body) {
        if (body == null || body.getPointNo() == null || body.getPointNo() < 1 || body.getPointNo() > 6) {
            throw new ServiceException("骰子点数必须为 1 到 6");
        }
        String name = trim(body.getPointName());
        if (name.isEmpty()) {
            throw new ServiceException("请填写点数名称");
        }
        if (name.length() > 50) {
            throw new ServiceException("点数名称不能超过 50 个字符");
        }
        ZcyltzjPoint point = pointMapper.selectOne(new QueryWrapper<ZcyltzjPoint>().eq("point_no", body.getPointNo()));
        boolean insert = point == null;
        if (insert) {
            point = new ZcyltzjPoint();
            point.setPointNo(body.getPointNo());
            point.setSortOrder(body.getPointNo());
            point.setStatus("0");
            point.setCreateTime(DateUtils.getNowDate());
        }
        point.setPointName(name);
        point.setTheme(limit(trim(body.getTheme()), 100, "压力主题"));
        point.setProjection(limit(trim(body.getProjection()), 255, "职场投射"));
        if (body.getStatus() != null) {
            point.setStatus("1".equals(body.getStatus()) ? "1" : "0");
        }
        point.setUpdateTime(DateUtils.getNowDate());
        if (insert) {
            pointMapper.insert(point);
        } else {
            pointMapper.updateById(point);
        }
        return point;
    }

    /** 查询指定点数下的全部试题（含停用），按题号排序。 */
    public List<ZcyltzjQuestion> listQuestions(Integer pointNo) {
        QueryWrapper<ZcyltzjQuestion> query = new QueryWrapper<ZcyltzjQuestion>().orderByAsc("point_no", "question_no");
        if (pointNo != null) {
            query.eq("point_no", pointNo);
        }
        return questionMapper.selectList(query);
    }

    /** 新增或修改试题；同一点数内题号不能重复。 */
    @Transactional
    public ZcyltzjQuestion saveQuestion(ZcyltzjQuestion body) {
        if (body == null) {
            throw new ServiceException("缺少试题内容");
        }
        if (body.getPointNo() == null || body.getPointNo() < 1 || body.getPointNo() > 6) {
            throw new ServiceException("所属点数必须为 1 到 6");
        }
        if (body.getQuestionNo() == null || body.getQuestionNo() < 1) {
            throw new ServiceException("题号必须为大于 0 的整数");
        }
        String content = trim(body.getContent());
        String optionA = trim(body.getOptionA());
        String optionB = trim(body.getOptionB());
        String optionC = trim(body.getOptionC());
        String optionD = trim(body.getOptionD());
        if (content.isEmpty()) {
            throw new ServiceException("请填写题干");
        }
        if (optionA.isEmpty() || optionB.isEmpty() || optionC.isEmpty() || optionD.isEmpty()) {
            throw new ServiceException("请填写完整的 A、B、C、D 四个选项");
        }
        ZcyltzjQuestion duplicate = questionMapper.selectOne(new QueryWrapper<ZcyltzjQuestion>()
                .eq("point_no", body.getPointNo()).eq("question_no", body.getQuestionNo())
                .ne(body.getQuestionId() != null, "question_id", body.getQuestionId())
                .last("LIMIT 1"));
        if (duplicate != null) {
            throw new ServiceException("该点数下已存在相同题号的试题");
        }

        ZcyltzjQuestion question = body.getQuestionId() == null ? null : questionMapper.selectById(body.getQuestionId());
        boolean insert = question == null;
        if (insert) {
            question = new ZcyltzjQuestion();
            question.setCreateTime(DateUtils.getNowDate());
        }
        question.setPointNo(body.getPointNo());
        question.setQuestionNo(body.getQuestionNo());
        question.setContent(limit(content, 500, "题干"));
        question.setOptionA(limit(optionA, 300, "A 选项"));
        question.setOptionB(limit(optionB, 300, "B 选项"));
        question.setOptionC(limit(optionC, 300, "C 选项"));
        question.setOptionD(limit(optionD, 300, "D 选项"));
        question.setSortOrder(body.getSortOrder() == null ? body.getQuestionNo() : body.getSortOrder());
        question.setStatus(body.getStatus() == null ? "0" : ("1".equals(body.getStatus()) ? "1" : "0"));
        question.setUpdateTime(DateUtils.getNowDate());
        if (insert) {
            questionMapper.insert(question);
        } else {
            questionMapper.updateById(question);
        }
        return question;
    }

    /** 删除试题；不影响其他点数或参与记录中的历史快照。 */
    @Transactional
    public void deleteQuestion(Long questionId) {
        if (questionId == null || questionMapper.selectById(questionId) == null) {
            throw new ServiceException("试题不存在或已删除");
        }
        questionMapper.deleteById(questionId);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String limit(String value, int max, String label) {
        if (value.length() > max) {
            throw new ServiceException(label + "不能超过 " + max + " 个字符");
        }
        return value;
    }
}
