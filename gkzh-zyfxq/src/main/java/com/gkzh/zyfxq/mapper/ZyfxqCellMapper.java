package com.gkzh.zyfxq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zyfxq.domain.ZyfxqCell;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职业飞行棋棋盘格子数据访问。
 * 19 个格子为固定结构，只做查询与类型/文案/底色维护，不新增或删除格子。
 */
@Mapper
public interface ZyfxqCellMapper extends BaseMapper<ZyfxqCell> {
}
