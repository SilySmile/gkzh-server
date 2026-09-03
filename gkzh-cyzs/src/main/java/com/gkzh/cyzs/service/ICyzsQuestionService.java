package com.gkzh.cyzs.service;

import java.util.List;

import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.cyzs.domain.CyzsAnswerDetail;
import com.gkzh.cyzs.domain.CyzsGameRound;
import com.gkzh.cyzs.domain.CyzsQuestion;
import com.gkzh.cyzs.vo.*;

/**
 * 职场危机Service接口
 * 
 * @author gkzh
 * @date 2025-10-13
 */
public interface ICyzsQuestionService
{
    /**
     * 查询职场危机
     * 
     * @param id 职场危机主键
     * @return 职场危机
     */
    public CyzsQuestion selectCyzsQuestionById(Long id);

    /**
     * 查询职场危机列表
     * 
     * @param cyzsQuestion 职场危机
     * @return 职场危机集合
     */
    public List<CyzsQuestion> selectCyzsQuestionList(CyzsQuestion cyzsQuestion);

    /**
     * 新增职场危机
     * 
     * @param cyzsQuestion 职场危机
     * @return 结果
     */
    public int insertCyzsQuestion(CyzsQuestion cyzsQuestion);

    /**
     * 修改职场危机
     * 
     * @param cyzsQuestion 职场危机
     * @return 结果
     */
    public int updateCyzsQuestion(CyzsQuestion cyzsQuestion);

    /**
     * 批量删除职场危机
     * 
     * @param ids 需要删除的职场危机主键集合
     * @return 结果
     */
    public int deleteCyzsQuestionByIds(Long[] ids);

    /**
     * 删除职场危机信息
     * 
     * @param id 职场危机主键
     * @return 结果
     */
    public int deleteCyzsQuestionById(Long id);

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
    List<CyzsAnswerDetail> selectAnswerDetailListByRoundId(Long roundId);

    List<AnswerDetailVO> selectAnswerDetailWithQuestionByRoundId(Long roundId);

    /**
     * 查询答题回合列表
     */
    public List<CyzsGameRound> selectGameRoundList(CyzsGameRound gameRound);

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
}
