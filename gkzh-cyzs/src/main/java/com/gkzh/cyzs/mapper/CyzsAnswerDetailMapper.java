package com.gkzh.cyzs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.cyzs.domain.CyzsAnswerDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CyzsAnswerDetailMapper extends BaseMapper<CyzsAnswerDetail> {
    @Insert("<script>" +
            "INSERT INTO cyzs_answer_detail (round_id, question_id, user_answer, is_correct) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.roundId}, #{item.questionId}, #{item.userAnswer}, #{item.isCorrect})" +
            "</foreach>" +
            "</script>")
    int insertBatchSomeColumn(@Param("list") List<CyzsAnswerDetail> list);
}
