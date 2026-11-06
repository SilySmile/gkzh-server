package com.gkzh.nlld.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.nlld.domain.NlldAnswer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 能量雷达学生选择明细数据访问。
 * 计分以该表为事实来源，报告和统计也以这里的 12 条选择为准。
 */
@Mapper
public interface NlldAnswerMapper extends BaseMapper<NlldAnswer> {
}
