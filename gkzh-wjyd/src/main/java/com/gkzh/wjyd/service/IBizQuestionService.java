package com.gkzh.wjyd.service;

import java.util.List;

import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.wjyd.domain.BizAnswerDetail;
import com.gkzh.wjyd.domain.BizGameRound;
import com.gkzh.wjyd.domain.BizQuestion;
import com.gkzh.wjyd.dto.GameRoundQueryDTO;
import com.gkzh.wjyd.vo.*;

/**
 * 职场危机Service接口
 * 
 * @author gkzh
 * @date 2025-10-13
 */
public interface IBizQuestionService 
{
    /**
     * 查询职场危机
     * 
     * @param id 职场危机主键
     * @return 职场危机
     */
    public BizQuestion selectBizQuestionById(Long id);

    /**
     * 查询职场危机列表
     * 
     * @param bizQuestion 职场危机
     * @return 职场危机集合
     */
    public List<BizQuestion> selectBizQuestionList(BizQuestion bizQuestion);

    /**
     * 新增职场危机
     * 
     * @param bizQuestion 职场危机
     * @return 结果
     */
    public int insertBizQuestion(BizQuestion bizQuestion);

    /**
     * 修改职场危机
     * 
     * @param bizQuestion 职场危机
     * @return 结果
     */
    public int updateBizQuestion(BizQuestion bizQuestion);

    /**
     * 批量删除职场危机
     * 
     * @param ids 需要删除的职场危机主键集合
     * @return 结果
     */
    public int deleteBizQuestionByIds(Long[] ids);

    /**
     * 删除职场危机信息
     * 
     * @param id 职场危机主键
     * @return 结果
     */
    public int deleteBizQuestionById(Long id);

    /**
     * 获取随机题目
     */
    List<QuestionVO> getRandomQuestions(int count);

    /**
     * 提交答案
     */
    AnswerResult submitAnswers(StudentCheckin studentCheckin, SubmitAnswerRequest request, Long activityId);

    /**
     * 根据回合ID查询答题详情列表
     */
    List<BizAnswerDetail> selectAnswerDetailListByRoundId(Long roundId);

    List<AnswerDetailVO> selectAnswerDetailWithQuestionByRoundId(Long roundId);

    /**
     * 查询答题回合列表
     */
    public List<BizGameRound> selectGameRoundList(BizGameRound gameRound);
    List<BizGameRound> selectGameRoundList(GameRoundQueryDTO queryDTO);
    /**
     * 查询题目统计信息
     * @return 题目统计数据列表
     */
    List<QuestionStatisticsVO> selectQuestionStatistics();

    /**
     * 查询用户答题记录用于导出
     * @return 用户答题记录列表
     */
    List<UserAnswerRecordVO> selectUserAnswerRecordsForExport();

    List<UserAnswerRecordVO> selectUserAnswerRecordsForExport(GameRoundQueryDTO queryDTO);
}
