package com.gkzh.cyzs.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.cyzs.domain.CyzsQuestion;
import com.gkzh.cyzs.vo.UserAnswerRecordVO;

/**
 * 职场危机Mapper接口
 * 
 * @author gkzh
 * @date 2025-10-13
 */
public interface CyzsQuestionMapper extends BaseMapper<CyzsQuestion>
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
     * 删除职场危机
     * 
     * @param id 职场危机主键
     * @return 结果
     */
    public int deleteCyzsQuestionById(Long id);

    /**
     * 批量删除职场危机
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCyzsQuestionByIds(Long[] ids);

    /**
     * 查询用户答题记录用于导出
     * @return 用户答题记录列表
     */
    List<UserAnswerRecordVO> selectUserAnswerRecordsForExport();
}
