package com.gkzh.xycc.mapper;

import java.util.List;
import com.gkzh.xycc.domain.Pattern;

/**
 * 心愿橱窗Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-12
 */
public interface PatternMapper 
{
    /**
     * 查询心愿橱窗
     * 
     * @param patternId 心愿橱窗主键
     * @return 心愿橱窗
     */
    public Pattern selectPatternByPatternId(Long patternId);

    /**
     * 查询心愿橱窗列表
     * 
     * @param pattern 心愿橱窗
     * @return 心愿橱窗集合
     */
    public List<Pattern> selectPatternList(Pattern pattern);

    /**
     * 新增心愿橱窗
     * 
     * @param pattern 心愿橱窗
     * @return 结果
     */
    public int insertPattern(Pattern pattern);

    /**
     * 修改心愿橱窗
     * 
     * @param pattern 心愿橱窗
     * @return 结果
     */
    public int updatePattern(Pattern pattern);

    /**
     * 删除心愿橱窗
     * 
     * @param patternId 心愿橱窗主键
     * @return 结果
     */
    public int deletePatternByPatternId(Long patternId);

    /**
     * 批量删除心愿橱窗
     * 
     * @param patternIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePatternByPatternIds(Long[] patternIds);
}
