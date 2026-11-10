package com.gkzh.zdyp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zdyp.domain.ZdypAnswer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职得一拼作答明细 Mapper。
 * 明细用于维度错误率、单块耗时与操作轨迹，统计查询统一写在记录 Mapper 的 XML 中。
 */
@Mapper
public interface ZdypAnswerMapper extends BaseMapper<ZdypAnswer> {
}
