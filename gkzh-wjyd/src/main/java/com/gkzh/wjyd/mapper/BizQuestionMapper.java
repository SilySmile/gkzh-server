package com.gkzh.wjyd.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.wjyd.domain.BizQuestion;
import com.gkzh.wjyd.dto.GameRoundQueryDTO;
import com.gkzh.wjyd.vo.QuestionVO;
import com.gkzh.wjyd.vo.UserAnswerRecordVO;
import org.apache.ibatis.annotations.Param;

/**
 * 职场危机Mapper接口
 * 
 * @author gkzh
 * @date 2025-10-13
 */
public interface BizQuestionMapper extends BaseMapper<BizQuestion>
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
     * 删除职场危机
     * 
     * @param id 职场危机主键
     * @return 结果
     */
    public int deleteBizQuestionById(Long id);

    /**
     * 批量删除职场危机
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizQuestionByIds(Long[] ids);

    /**
     * 查询用户答题记录用于导出
     * @return 用户答题记录列表
     */
    List<UserAnswerRecordVO> selectUserAnswerRecordsForExport();

    List<UserAnswerRecordVO> selectUserAnswerRecordsForExport2(GameRoundQueryDTO queryDTO);
}
