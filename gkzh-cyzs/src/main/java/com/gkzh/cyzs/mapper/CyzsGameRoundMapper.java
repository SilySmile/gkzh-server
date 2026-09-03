package com.gkzh.cyzs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.cyzs.domain.CyzsGameRound;

import java.util.List;

public interface CyzsGameRoundMapper extends BaseMapper<CyzsGameRound>{
    /**
     * 查询答题回合列表
     *
     * @param cyzsGameRound 答题回合
     * @return 答题回合集合
     */
    public List<CyzsGameRound> selectCyzsGameRoundList(CyzsGameRound cyzsGameRound);
}
