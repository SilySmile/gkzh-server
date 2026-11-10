package com.gkzh.zdyp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zdyp.domain.ZdypCard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职得一拼岗位卡片 Mapper。
 * 卡片按岗位查询，数量固定为每岗位四张，直接使用 MyBatis-Plus 基础方法。
 */
@Mapper
public interface ZdypCardMapper extends BaseMapper<ZdypCard> {
}
