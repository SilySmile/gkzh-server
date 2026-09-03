package com.gkzh.wjyd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.wjyd.domain.BizGameRound;
import com.gkzh.wjyd.dto.GameRoundQueryDTO;

import java.util.List;

public interface BizGameRoundMapper extends BaseMapper<BizGameRound>{
    /**
     * 查询答题回合列表
     *
     * @param bizGameRound 答题回合
     * @return 答题回合集合
     */
    public List<BizGameRound> selectBizGameRoundList(BizGameRound bizGameRound);

    public List<BizGameRound> selectBizGameRoundList2(GameRoundQueryDTO queryDTO);
}
