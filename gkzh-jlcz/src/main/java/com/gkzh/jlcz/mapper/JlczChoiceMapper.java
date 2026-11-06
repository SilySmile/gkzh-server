package com.gkzh.jlcz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.jlcz.domain.JlczChoice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 简历称重砝码选择明细数据访问。
 * 按记录与称重次数查询，用于结果页、后台详情和活动查看。
 */
@Mapper
public interface JlczChoiceMapper extends BaseMapper<JlczChoice> {
}
