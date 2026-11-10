package com.gkzh.zdyp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zdyp.domain.ZdypSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职得一拼规则参数 Mapper（单行配置，主键固定为 1）。
 */
@Mapper
public interface ZdypSettingMapper extends BaseMapper<ZdypSetting> {
}
