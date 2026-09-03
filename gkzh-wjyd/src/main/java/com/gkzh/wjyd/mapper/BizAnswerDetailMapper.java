package com.gkzh.wjyd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.wjyd.domain.BizAnswerDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizAnswerDetailMapper extends BaseMapper<BizAnswerDetail> {
    @Insert("<script>" +
            "INSERT INTO biz_answer_detail (round_id, question_id, user_answer, is_correct) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.roundId}, #{item.questionId}, #{item.userAnswer}, #{item.isCorrect})" +
            "</foreach>" +
            "</script>")
    int insertBatchSomeColumn(@Param("list") List<BizAnswerDetail> list);
}
