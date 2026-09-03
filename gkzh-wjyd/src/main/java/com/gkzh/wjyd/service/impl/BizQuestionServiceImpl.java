package com.gkzh.wjyd.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.common.utils.SecurityUtils;
import com.gkzh.wjyd.domain.BizAnswerDetail;
import com.gkzh.wjyd.domain.BizGameRound;
import com.gkzh.wjyd.dto.GameRoundQueryDTO;
import com.gkzh.wjyd.mapper.BizAnswerDetailMapper;
import com.gkzh.wjyd.mapper.BizGameRoundMapper;
import com.gkzh.wjyd.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.wjyd.mapper.BizQuestionMapper;
import com.gkzh.wjyd.domain.BizQuestion;
import com.gkzh.wjyd.service.IBizQuestionService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 职场危机Service业务层处理
 * 
 * @author gkzh
 * @date 2025-10-13
 */
@Service
public class BizQuestionServiceImpl implements IBizQuestionService 
{
    @Autowired
    private BizQuestionMapper bizQuestionMapper;

    @Autowired
    private BizGameRoundMapper gameRoundMapper;

    @Autowired
    private BizAnswerDetailMapper answerDetailMapper;
    @Autowired
    private IGkzhActivityParticipationRecordService gkzhActivityParticipationRecordService;
    /**
     * 查询职场危机
     * 
     * @param id 职场危机主键
     * @return 职场危机
     */
    @Override
    public BizQuestion selectBizQuestionById(Long id)
    {
        return bizQuestionMapper.selectBizQuestionById(id);
    }

    /**
     * 查询职场危机列表
     * 
     * @param bizQuestion 职场危机
     * @return 职场危机
     */
    @Override
    public List<BizQuestion> selectBizQuestionList(BizQuestion bizQuestion)
    {
        return bizQuestionMapper.selectBizQuestionList(bizQuestion);
    }

    /**
     * 新增职场危机
     * 
     * @param bizQuestion 职场危机
     * @return 结果
     */
    @Override
    public int insertBizQuestion(BizQuestion bizQuestion)
    {
        bizQuestion.setCreateBy(SecurityUtils.getUsername());
        bizQuestion.setCreateTime(DateUtils.getNowDate());
        return bizQuestionMapper.insertBizQuestion(bizQuestion);
    }

    /**
     * 修改职场危机
     * 
     * @param bizQuestion 职场危机
     * @return 结果
     */
    @Override
    public int updateBizQuestion(BizQuestion bizQuestion)
    {
        bizQuestion.setUpdateBy(SecurityUtils.getUsername());
        bizQuestion.setUpdateTime(DateUtils.getNowDate());
        return bizQuestionMapper.updateBizQuestion(bizQuestion);
    }

    /**
     * 批量删除职场危机
     * 
     * @param ids 需要删除的职场危机主键
     * @return 结果
     */
    @Override
    public int deleteBizQuestionByIds(Long[] ids)
    {
        return bizQuestionMapper.deleteBizQuestionByIds(ids);
    }

    /**
     * 删除职场危机信息
     * 
     * @param id 职场危机主键
     * @return 结果
     */
    @Override
    public int deleteBizQuestionById(Long id)
    {
        return bizQuestionMapper.deleteBizQuestionById(id);
    }



    @Override
    public List<QuestionVO> getRandomQuestions(int count) {
        // 获取随机题目，排除停用的题目
        QueryWrapper<BizQuestion> query = Wrappers.query();
        query.eq("status", 0);
        query.last("ORDER BY RAND() LIMIT " + count);
        List<BizQuestion> bizQuestions = bizQuestionMapper.selectList(query);
        if (bizQuestions.size() < count) {
            throw new RuntimeException("题目数量不足");
        }
        List<QuestionVO> questions = bizQuestions.stream().map(question -> {
            QuestionVO questionVO = new QuestionVO();
            questionVO.setId(question.getId());
            questionVO.setQuestionText(question.getQuestionText());
            questionVO.setQuestionImage(question.getQuestionImage());
            questionVO.setOptionA(question.getOptionA());
            questionVO.setOptionB(question.getOptionB());
            questionVO.setOptionC(question.getOptionC());
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
        if(gkzhActivityParticipationRecordService.isParticipated(studentCheckin.getUserId(),activityId,6)){
            throw new RuntimeException("您已参加过本关卡");
        }
        // 验证必须回答3道题
        if (request.getAnswers() == null || request.getAnswers().size() != 3) {
            throw new RuntimeException("必须回答3道题");
        }

        Long userId = studentCheckin.getUserId();
        // 创建答题回合记录
        BizGameRound gameRound = new BizGameRound();
        gameRound.setUserId(userId);
        gameRound.setStartTime(DateUtils.getNowDate());
        gameRound.setCreateTime(DateUtils.getNowDate());
        gameRoundMapper.insert(gameRound);

        int correctCount = 0;
        List<BizAnswerDetail> answerDetails = new ArrayList<>();

        // 处理每道题的答案
        for (AnswerRequest answerReq : request.getAnswers()) {
            BizQuestion question = bizQuestionMapper.selectById(answerReq.getQuestionId());
            if (question == null) {
                throw new RuntimeException("题目不存在");
            }

            boolean isCorrect = question.getCorrectOptionKey().equalsIgnoreCase(answerReq.getUserAnswer());
            if (isCorrect) {
                correctCount++;
            }

            BizAnswerDetail detail = new BizAnswerDetail();
            detail.setRoundId(gameRound.getId());
            detail.setQuestionId(answerReq.getQuestionId());
            detail.setUserAnswer(answerReq.getUserAnswer().toUpperCase());
            detail.setIsCorrect(isCorrect ? 1 : 0);
            answerDetails.add(detail);
        }

        // 批量保存答题详情
        answerDetailMapper.insertBatchSomeColumn(answerDetails);

        // 更新回合结果
        boolean isSuccess = correctCount >= 2;
        gameRound.setIsSuccess(isSuccess ? 1 : 0);
        gameRound.setEndTime(new Date());
        gameRoundMapper.updateById(gameRound);

        // 构建返回结果
        AnswerResult result = new AnswerResult();
        result.setSuccess(isSuccess);
        result.setCorrectCount(correctCount);
        result.setTotalCount(3);
        result.setRoundId(gameRound.getId());
        Integer status = 0;
        // 根据答对题数设置提示信息
        if (correctCount == 3) {
            result.setMessage("全部答对，通关完成");
            status = 1;
        } else if (correctCount >= 2) {
            status = 1;
            result.setMessage("答对2题，通关完成！");
        } else {
            status = 2;
            result.setMessage("答对" + correctCount + "道，答错" + (3 - correctCount) + "道，很遗憾未通过");
        }
        //添加结果到gkzh_activity_participation_record
        GkzhActivityParticipationRecord record = new GkzhActivityParticipationRecord();
        record.setActivityId(activityId);
        record.setModuleId(gameRound.getId());
        record.setUserId(userId);
        record.setUserCode(studentCheckin.getStuNo());
        record.setUserName(studentCheckin.getStuName());
        record.setParticipationType(6);
        record.setParticipationTime(DateUtils.getNowDate());
        record.setResult("职场危机应对完成");
        record.setRemark(result.getMessage());
        record.setStatus(status);
        record.setCreateTime(DateUtils.getNowDate());
        gkzhActivityParticipationRecordService.insertGkzhActivityParticipationRecord(record);

        return result;
    }

    @Override
    public List<BizAnswerDetail> selectAnswerDetailListByRoundId(Long roundId) {
        QueryWrapper<BizAnswerDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("round_id", roundId);
        return answerDetailMapper.selectList(queryWrapper);
    }

    @Override
    public List<AnswerDetailVO> selectAnswerDetailWithQuestionByRoundId(Long roundId) {
        // 查询答题详情
        QueryWrapper<BizAnswerDetail> detailQuery = new QueryWrapper<>();
        detailQuery.eq("round_id", roundId);
        List<BizAnswerDetail> details = answerDetailMapper.selectList(detailQuery);

        // 获取题目ID列表
        List<Long> questionIds = details.stream()
                .map(BizAnswerDetail::getQuestionId)
                .collect(Collectors.toList());

        // 批量查询题目信息
        List<BizQuestion> questions = bizQuestionMapper.selectBatchIds(questionIds);
        Map<Long, BizQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(BizQuestion::getId, q -> q));

        // 组装VO对象
        return details.stream().map(detail -> {
            AnswerDetailVO vo = new AnswerDetailVO();
            vo.setQuestionId(detail.getQuestionId());
            BizQuestion question = questionMap.get(detail.getQuestionId());
            if (question != null) {
                vo.setQuestionText(question.getQuestionText());
            }
            vo.setUserAnswer(detail.getUserAnswer());
            vo.setIsCorrect(detail.getIsCorrect());
            return vo;
        }).collect(Collectors.toList());
    }
    @Override
    public List<BizGameRound> selectGameRoundList(BizGameRound gameRound) {
        return gameRoundMapper.selectBizGameRoundList(gameRound);
    }

    @Override
    public List<BizGameRound> selectGameRoundList(GameRoundQueryDTO queryDTO) {
        return gameRoundMapper.selectBizGameRoundList2(queryDTO);
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
        return bizQuestionMapper.selectUserAnswerRecordsForExport();
    }

    @Override
    public List<UserAnswerRecordVO> selectUserAnswerRecordsForExport(GameRoundQueryDTO queryDTO) {
        List<UserAnswerRecordVO> userAnswerRecordVOS = bizQuestionMapper.selectUserAnswerRecordsForExport2(queryDTO);
        Set<Long> questionIds = userAnswerRecordVOS.stream()
                .map(UserAnswerRecordVO::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, BizQuestion> questionMap = new HashMap<>();
        if (!questionIds.isEmpty()) {
            QueryWrapper<BizQuestion> questionQuery = new QueryWrapper<>();
            questionQuery.in("id", questionIds);
            List<BizQuestion> questions = bizQuestionMapper.selectList(questionQuery);
            questionMap = questions.stream()
                    .collect(Collectors.toMap(BizQuestion::getId, q -> q));
        }
        // 为每个UserAnswerRecordVO设置题目信息
        for (UserAnswerRecordVO record : userAnswerRecordVOS) {
            BizQuestion question = questionMap.get(record.getQuestionId());
            if (question != null) {
                record.setQuestionText(question.getQuestionText());
                record.setOptionA(question.getOptionA());
                record.setOptionB(question.getOptionB());
                record.setOptionC(question.getOptionC());
            }
        }
        return userAnswerRecordVOS;
    }
}
