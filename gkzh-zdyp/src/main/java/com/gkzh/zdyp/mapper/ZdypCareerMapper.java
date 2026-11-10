package com.gkzh.zdyp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zdyp.domain.ZdypCareer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职得一拼岗位 Mapper。
 * 岗位数量固定为 10 套，查询条件简单，直接使用 MyBatis-Plus 基础方法。
 */
@Mapper
public interface ZdypCareerMapper extends BaseMapper<ZdypCareer> {
}
