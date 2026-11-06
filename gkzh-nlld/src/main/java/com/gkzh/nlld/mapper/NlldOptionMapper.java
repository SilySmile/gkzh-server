package com.gkzh.nlld.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.nlld.domain.NlldOption;
import org.apache.ibatis.annotations.Mapper;

/**
 * 能量雷达关卡物品选项数据访问。
 * 每关 5 件物品固定存在，只允许更新名称、描述、所属维度、排序和状态。
 */
@Mapper
public interface NlldOptionMapper extends BaseMapper<NlldOption> {
}
