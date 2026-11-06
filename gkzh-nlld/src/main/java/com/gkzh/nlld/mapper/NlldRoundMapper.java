package com.gkzh.nlld.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.nlld.domain.NlldRound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 能量雷达关卡配置数据访问。
 * 6 个关卡固定存在，只允许更新名称、情景、排序和状态。
 */
@Mapper
public interface NlldRoundMapper extends BaseMapper<NlldRound> {
}
