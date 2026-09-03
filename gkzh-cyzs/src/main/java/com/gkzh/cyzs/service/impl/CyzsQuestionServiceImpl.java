package com.gkzh.cyzs.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.common.utils.SecurityUtils;
import com.gkzh.cyzs.domain.CyzsAnswerDetail;
import com.gkzh.cyzs.domain.CyzsGameRound;
import com.gkzh.cyzs.mapper.CyzsAnswerDetailMapper;
import com.gkzh.cyzs.mapper.CyzsGameRoundMapper;
import com.gkzh.cyzs.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.cyzs.mapper.CyzsQuestionMapper;
import com.gkzh.cyzs.domain.CyzsQuestion;
import com.gkzh.cyzs.service.ICyzsQuestionService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 职场危机Service业务层处理
 * 
 * @author gkzh
 * @date 2025-10-13
 */
@Service
public class CyzsQuestionServiceImpl implements ICyzsQuestionService
{
    @Autowired
    private CyzsQuestionMapper cyzsQuestionMapper;

    @Autowired
    private CyzsGameRoundMapper gameRoundMapper;

    @Autowired
    private CyzsAnswerDetailMapper answerDetailMapper;
    @Autowired
    private IGkzhActivityParticipationRecordService gkzhActivityParticipationRecordService;
    /**
     * 查询职场危机
     * 
     * @param id 职场危机主键
     * @return 职场危机
     */
    @Override
    public CyzsQuestion selectCyzsQuestionById(Long id)
    {
        return cyzsQuestionMapper.selectById(id);
    }

    /**
     * 查询职场危机列表
     * 
     * @param cyzsQuestion 职场危机
     * @return 职场危机
     */
    @Override
    public List<CyzsQuestion> selectCyzsQuestionList(CyzsQuestion cyzsQuestion)
    {

        QueryWrapper<CyzsQuestion> query = Wrappers.query();
        if(cyzsQuestion != null){
            if(cyzsQuestion.getStatus() != null){
                query.eq("status", cyzsQuestion.getStatus());
            }
        }
        return cyzsQuestionMapper.selectList(query);
    }

    /**
     * 新增职场危机
     * 
     * @param cyzsQuestion 职场危机
     * @return 结果
     */
    @Override
    public int insertCyzsQuestion(CyzsQuestion cyzsQuestion)
    {
        cyzsQuestion.setCreateBy(SecurityUtils.getUsername());
        cyzsQuestion.setCreateTime(DateUtils.getNowDate());

        return cyzsQuestionMapper.insert(cyzsQuestion);
    }

    /**
     * 修改职场危机
     * 
     * @param cyzsQuestion 职场危机
     * @return 结果
     */
    @Override
    public int updateCyzsQuestion(CyzsQuestion cyzsQuestion)
    {
        cyzsQuestion.setUpdateBy(SecurityUtils.getUsername());
        cyzsQuestion.setUpdateTime(DateUtils.getNowDate());

        return cyzsQuestionMapper.updateById(cyzsQuestion);
    }

    /**
     * 批量删除职场危机
     * 
     * @param ids 需要删除的职场危机主键
     * @return 结果
     */
    @Override
    public int deleteCyzsQuestionByIds(Long[] ids)
    {
        return cyzsQuestionMapper.deleteCyzsQuestionByIds(ids);
    }

    /**
     * 删除职场危机信息
     * 
     * @param id 职场危机主键
     * @return 结果
     */
    @Override
    public int deleteCyzsQuestionById(Long id)
    {
        return cyzsQuestionMapper.deleteCyzsQuestionById(id);
    }



    @Override
    public List<QuestionVO> getRandomQuestions(int count) {
        // 获取随机题目，排除停用的题目
        QueryWrapper<CyzsQuestion> query = Wrappers.query();
        query.eq("status", 0);
        query.last("ORDER BY RAND() LIMIT " + count);
        List<CyzsQuestion> cyzsQuestions = cyzsQuestionMapper.selectList(query);
        if (cyzsQuestions.size() < count) {
            throw new RuntimeException("题目数量不足");
        }
        List<QuestionVO> questions = cyzsQuestions.stream().map(question -> {
            QuestionVO questionVO = new QuestionVO();
            questionVO.setId(question.getId());
            questionVO.setQuestionText(question.getQuestionText());
            questionVO.setOptionA(question.getOptionA());
            questionVO.setOptionB(question.getOptionB());
            questionVO.setOptionC(question.getOptionC());
            questionVO.setOptionD(question.getOptionD());
            questionVO.setType(question.getType());
            return questionVO;
        }).collect(Collectors.toList());
        return questions;
    }

    @Override
    @Transactional
    public AnswerResult submitAnswers(StudentCheckin studentCheckin, SubmitAnswerRequest request,Long activityId) {
        if(activityId == null){
            throw new RuntimeException("活动ID不能为空");
        }
        //判断是否参加过本次活动
        if(gkzhActivityParticipationRecordService.isParticipated(studentCheckin.getUserId(),activityId,7)){
            throw new RuntimeException("您已参加过本关卡");
        }
        // 验证必须回答3道题
        if (request.getAnswers() == null || request.getAnswers().size() != 5) {
            throw new RuntimeException("必须回答5道题");
        }

        Long userId = studentCheckin.getUserId();
        // 创建答题回合记录
        CyzsGameRound gameRound = new CyzsGameRound();
        gameRound.setUserId(userId);
        gameRound.setStartTime(DateUtils.getNowDate());
        gameRound.setCreateTime(DateUtils.getNowDate());
        gameRoundMapper.insert(gameRound);

        int correctCount = 0;
        List<CyzsAnswerDetail> answerDetails = new ArrayList<>();

        // 处理每道题的答案
        for (AnswerRequest answerReq : request.getAnswers()) {
            CyzsQuestion question = cyzsQuestionMapper.selectById(answerReq.getQuestionId());
            if (question == null) {
                throw new RuntimeException("题目不存在");
            }

            boolean isCorrect = question.getCorrectOptionKey().equalsIgnoreCase(answerReq.getUserAnswer());
            if (isCorrect) {
                correctCount++;
            }

            CyzsAnswerDetail detail = new CyzsAnswerDetail();
            detail.setRoundId(gameRound.getId());
            detail.setQuestionId(answerReq.getQuestionId());
            detail.setUserAnswer(answerReq.getUserAnswer().toUpperCase());
            detail.setIsCorrect(isCorrect ? 1 : 0);
            answerDetails.add(detail);
        }

        // 批量保存答题详情
        answerDetailMapper.insertBatchSomeColumn(answerDetails);

        // 更新回合结果
        boolean isSuccess = correctCount >= 3;
        gameRound.setIsSuccess(isSuccess ? 1 : 0);
        gameRound.setEndTime(new Date());
        gameRoundMapper.updateById(gameRound);

        // 构建返回结果
        AnswerResult result = new AnswerResult();
        result.setSuccess(isSuccess);
        result.setCorrectCount(correctCount);
        result.setTotalCount(5);
        result.setRoundId(gameRound.getId());
        Integer status = 0;
        // 根据答对题数设置提示信息
        if (correctCount == 5) {
            result.setMessage("全部答对，通关完成");
            status = 1;
        } else if (correctCount >= 3) {
            status = 1;
            result.setMessage("答对"+ correctCount +"题，通关完成！");
        } else {
            status = 2;
            result.setMessage("答对" + correctCount + "道，答错" + (5 - correctCount) + "道，很遗憾未通过");
        }
        //添加结果到gkzh_activity_participation_record
        GkzhActivityParticipationRecord record = new GkzhActivityParticipationRecord();
        record.setActivityId(activityId);
        record.setModuleId(gameRound.getId());
        record.setUserId(userId);
        record.setUserCode(studentCheckin.getStuNo());
        record.setUserName(studentCheckin.getStuName());
        record.setParticipationType(7);
        record.setParticipationTime(DateUtils.getNowDate());
        record.setResult("创业知识答答答完成");
        record.setRemark(result.getMessage());
        record.setStatus(status);
        record.setCreateTime(DateUtils.getNowDate());
        gkzhActivityParticipationRecordService.insertGkzhActivityParticipationRecord(record);

        return result;
    }

    @Override
    public List<CyzsAnswerDetail> selectAnswerDetailListByRoundId(Long roundId) {
        QueryWrapper<CyzsAnswerDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("round_id", roundId);
        return answerDetailMapper.selectList(queryWrapper);
    }

    @Override
    public List<AnswerDetailVO> selectAnswerDetailWithQuestionByRoundId(Long roundId) {
        // 查询答题详情
        QueryWrapper<CyzsAnswerDetail> detailQuery = new QueryWrapper<>();
        detailQuery.eq("round_id", roundId);
        List<CyzsAnswerDetail> details = answerDetailMapper.selectList(detailQuery);

        // 获取题目ID列表
        List<Long> questionIds = details.stream()
                .map(CyzsAnswerDetail::getQuestionId)
                .collect(Collectors.toList());

        // 批量查询题目信息
        List<CyzsQuestion> questions = cyzsQuestionMapper.selectBatchIds(questionIds);
        Map<Long, CyzsQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(CyzsQuestion::getId, q -> q));

        // 组装VO对象
        return details.stream().map(detail -> {
            AnswerDetailVO vo = new AnswerDetailVO();
            vo.setQuestionId(detail.getQuestionId());
            CyzsQuestion question = questionMap.get(detail.getQuestionId());
            if (question != null) {
                vo.setQuestionText(question.getQuestionText());
            }
            vo.setUserAnswer(detail.getUserAnswer());
            vo.setIsCorrect(detail.getIsCorrect());
            return vo;
        }).collect(Collectors.toList());
    }
    @Override
    public List<CyzsGameRound> selectGameRoundList(CyzsGameRound gameRound) {
        return gameRoundMapper.selectCyzsGameRoundList(gameRound);
    }

    @Override
    public List<QuestionStatisticsVO> selectQuestionStatistics() {
        // 实现统计数据查询逻辑
        // 1. 查询特定题目的所有答题记录
        // 2. 按选项分组统计数量
        // 3. 计算百分比
        // 4. 返回结果列表
        return null;
    }

    @Override
    public List<UserAnswerRecordVO> selectUserAnswerRecordsForExport() {
        return cyzsQuestionMapper.selectUserAnswerRecordsForExport();
    }
}
