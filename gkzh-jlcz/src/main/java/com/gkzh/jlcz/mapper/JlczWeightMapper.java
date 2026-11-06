package com.gkzh.jlcz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.jlcz.domain.JlczWeight;
import org.apache.ibatis.annotations.Mapper;

/**
 * 简历称重砝码配置数据访问。
 * 配置量固定为 20 条，直接使用 MyBatis-Plus 基础查询，不额外编写动态 SQL。
 */
@Mapper
public interface JlczWeightMapper extends BaseMapper<JlczWeight> {
}
