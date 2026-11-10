package com.gkzh.zytj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zytj.domain.ZytjItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职愿探究橱窗物品 Mapper。
 * 查询条件由 MyBatis-Plus 条件构造器生成，无动态 SQL，故不单独编写 XML。
 */
@Mapper
public interface ZytjItemMapper extends BaseMapper<ZytjItem> {
}
