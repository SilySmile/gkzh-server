package com.gkzh.jlcz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.jlcz.domain.JlczSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 简历称重规则参数数据访问（单行配置，主键固定为 1）。
 */
@Mapper
public interface JlczSettingMapper extends BaseMapper<JlczSetting> {
}
