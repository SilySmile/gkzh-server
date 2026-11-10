package com.gkzh.zdyp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zdyp.domain.ZdypAttempt;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职得一拼闯关尝试 Mapper。
 * 按记录查询尝试列表，重闯统计在 XML 中汇总，避免逐条累加。
 */
@Mapper
public interface ZdypAttemptMapper extends BaseMapper<ZdypAttempt> {
}
