package com.gkzh.nlld.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.nlld.domain.NlldDimension;
import org.apache.ibatis.annotations.Mapper;

/**
 * 能量雷达维度配置数据访问。
 * 15 个维度固定存在，只允许更新文案、分组、排序和状态。
 */
@Mapper
public interface NlldDimensionMapper extends BaseMapper<NlldDimension> {
}
