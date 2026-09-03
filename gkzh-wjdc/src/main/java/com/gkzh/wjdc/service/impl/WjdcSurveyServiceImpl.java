package com.gkzh.wjdc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.wjdc.mapper.WjdcSurveyMapper;
import com.gkzh.wjdc.domain.WjdcSurvey;
import com.gkzh.wjdc.domain.WjdcSurveyQuestion;
import com.gkzh.wjdc.domain.WjdcSurveyOption;
import com.gkzh.wjdc.service.IWjdcSurveyService;
import com.gkzh.wjdc.service.IWjdcSurveyQuestionService;
import com.gkzh.wjdc.service.IWjdcSurveyOptionService;
import com.gkzh.common.utils.DateUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * 问卷管理Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Service
public class WjdcSurveyServiceImpl implements IWjdcSurveyService 
{
    @Autowired
    private WjdcSurveyMapper wjdcSurveyMapper;

    @Autowired
    private IWjdcSurveyQuestionService wjdcSurveyQuestionService;

    @Autowired
    private IWjdcSurveyOptionService wjdcSurveyOptionService;

    /**
     * 查询问卷管理
     * 
     * @param id 问卷管理主键
     * @return 问卷管理
     */
    @Override
    public WjdcSurvey selectWjdcSurveyById(Long id)
    {
        return wjdcSurveyMapper.selectWjdcSurveyById(id);
    }

    /**
     * 查询问卷管理列表
     * 
     * @param wjdcSurvey 问卷管理
     * @return 问卷管理
     */
    @Override
    public List<WjdcSurvey> selectWjdcSurveyList(WjdcSurvey wjdcSurvey)
    {
        return wjdcSurveyMapper.selectWjdcSurveyList(wjdcSurvey);
    }

    /**
     * 查询问卷管理列表（包含问题和选项）
     * 
     * @param wjdcSurvey 问卷管理
     * @return 问卷管理
     */
    @Override
    public List<WjdcSurvey> selectWjdcSurveyListWithDetails(WjdcSurvey wjdcSurvey)
    {
        List<WjdcSurvey> surveyList = wjdcSurveyMapper.selectWjdcSurveyList(wjdcSurvey);
        
        // 为每个问卷加载问题和选项
        for (WjdcSurvey survey : surveyList) {
            // 加载问题列表
            List<WjdcSurveyQuestion> questions = wjdcSurveyQuestionService.selectWjdcSurveyQuestionBySurveyId(survey.getId().intValue());
            
            // 为每个问题加载选项列表
            for (WjdcSurveyQuestion question : questions) {
                if ("1".equals(question.getQuestionType()) || "2".equals(question.getQuestionType())) {
                    // 只有单选题和多选题才有选项
                    List<WjdcSurveyOption> options = wjdcSurveyOptionService.selectWjdcSurveyOptionByQuestionId(question.getId());
                    question.setOptions(options);
                }
            }
            
            survey.setQuestions(questions);
        }
        
        return surveyList;
    }

    /**
     * 新增问卷管理
     * 
     * @param wjdcSurvey 问卷管理
     * @return 结果
     */
    @Override
    public int insertWjdcSurvey(WjdcSurvey wjdcSurvey)
    {
        // 设置默认状态为启用
        if (wjdcSurvey.getStatus() == null) {
            wjdcSurvey.setStatus("1");
        }
        // 设置创建时间为当前时间
        wjdcSurvey.setCreatedAt(DateUtils.getNowDate());
        return wjdcSurveyMapper.insertWjdcSurvey(wjdcSurvey);
    }

    /**
     * 修改问卷管理
     * 
     * @param wjdcSurvey 问卷管理
     * @return 结果
     */
    @Override
    public int updateWjdcSurvey(WjdcSurvey wjdcSurvey)
    {
        return wjdcSurveyMapper.updateWjdcSurvey(wjdcSurvey);
    }

    /**
     * 批量删除问卷管理
     * 
     * @param ids 需要删除的问卷管理主键
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyByIds(Long[] ids)
    {
        return wjdcSurveyMapper.deleteWjdcSurveyByIds(ids);
    }

    /**
     * 删除问卷管理信息
     * 
     * @param id 问卷管理主键
     * @return 结果
     */
    @Override
    public int deleteWjdcSurveyById(Long id)
    {
        return wjdcSurveyMapper.deleteWjdcSurveyById(id);
    }


    @Override
    public WjdcSurvey selectWjdcSurveyWithQuestions(Long surveyId) {
        WjdcSurvey survey = wjdcSurveyMapper.selectWjdcSurveyById(surveyId);
        if (survey == null) return null;
        List<WjdcSurveyQuestion> questions = wjdcSurveyQuestionService.selectWjdcSurveyQuestionBySurveyId(surveyId.intValue());
        for (WjdcSurveyQuestion q : questions) {
            if ("1".equals(q.getQuestionType()) || "2".equals(q.getQuestionType()) || "5".equals(q.getQuestionType())) {
                List<WjdcSurveyOption> options = wjdcSurveyOptionService.selectWjdcSurveyOptionByQuestionId(q.getId());
                q.setOptions(options);
            }
        }
        survey.setQuestions(questions);
        return survey;
    }

    /**
     * 复制问卷
     *
     * @param surveyId 原始问卷ID
     * @return 新问卷ID
     */
    @Override
    @Transactional
    public Long copySurvey(Long surveyId) {
        // 查询原始问卷
        WjdcSurvey originalSurvey = wjdcSurveyMapper.selectWjdcSurveyById(surveyId);
        if (originalSurvey == null) {
            throw new RuntimeException("原始问卷不存在");
        }

        // 创建新问卷
        WjdcSurvey newSurvey = new WjdcSurvey();
        newSurvey.setTitle(originalSurvey.getTitle() + " - 副本");
        newSurvey.setDescription(originalSurvey.getDescription());
        newSurvey.setStatus("1"); // 默认设为未发布状态
        newSurvey.setStartTime(originalSurvey.getStartTime());
        newSurvey.setEndTime(originalSurvey.getEndTime());
        newSurvey.setCreatedAt(DateUtils.getNowDate());

        // 插入新问卷
        wjdcSurveyMapper.insertWjdcSurvey(newSurvey);
        Long newSurveyId = newSurvey.getId();

        // 复制问卷下的所有问题
        List<WjdcSurveyQuestion> originalQuestions = wjdcSurveyQuestionService.selectWjdcSurveyQuestionBySurveyId(surveyId.intValue());
        for (WjdcSurveyQuestion originalQuestion : originalQuestions) {
            // 创建新问题
            WjdcSurveyQuestion newQuestion = new WjdcSurveyQuestion();
            newQuestion.setSurveyId(newSurveyId.intValue());
            newQuestion.setQuestionTitle(originalQuestion.getQuestionTitle());
            newQuestion.setQuestionType(originalQuestion.getQuestionType());
            newQuestion.setRequired(originalQuestion.getRequired());
            newQuestion.setSortOrder(originalQuestion.getSortOrder());

            // 插入新问题
            wjdcSurveyQuestionService.insertWjdcSurveyQuestion(newQuestion);
            Integer newQuestionId = newQuestion.getId();

            // 如果是选择题，复制选项
            if ("1".equals(originalQuestion.getQuestionType()) || "2".equals(originalQuestion.getQuestionType())) {
                List<WjdcSurveyOption> originalOptions = wjdcSurveyOptionService.selectWjdcSurveyOptionByQuestionId(originalQuestion.getId());
                for (WjdcSurveyOption originalOption : originalOptions) {
                    // 创建新选项
                    WjdcSurveyOption newOption = new WjdcSurveyOption();
                    newOption.setQuestionId(newQuestionId);
                    newOption.setOptionText(originalOption.getOptionText());
                    newOption.setSortOrder(originalOption.getSortOrder());

                    // 插入新选项
                    wjdcSurveyOptionService.insertWjdcSurveyOption(newOption);
                }
            }
        }

        return newSurveyId;
    }
}
