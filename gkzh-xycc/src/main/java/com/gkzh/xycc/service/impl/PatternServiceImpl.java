package com.gkzh.xycc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.gkzh.xycc.mapper.PatternMapper;
import com.gkzh.xycc.domain.Pattern;
import com.gkzh.xycc.service.IPatternService;

/**
 * 心愿橱窗Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-12
 */
@Service
public class PatternServiceImpl implements IPatternService 
{
    @Autowired
    private PatternMapper patternMapper;

    @Value("${gkzh.material-domain:}")
    private String materialDomain;

    /**
     * 查询心愿橱窗
     * 
     * @param patternId 心愿橱窗主键
     * @return 心愿橱窗
     */
    @Override
    public Pattern selectPatternByPatternId(Long patternId)
    {
        Pattern pattern = patternMapper.selectPatternByPatternId(patternId);
        if (pattern != null) {
            pattern.setImgUrl(resolveUrl(pattern.getImgUrl()));
            pattern.setMaterialUrl(resolveUrl(pattern.getMaterialUrl()));
        }
        return pattern;
    }

    /**
     * 查询心愿橱窗列表
     * 
     * @param pattern 心愿橱窗
     * @return 心愿橱窗
     */
    @Override
    public List<Pattern> selectPatternList(Pattern pattern)
    {
        List<Pattern> list = patternMapper.selectPatternList(pattern);
        for (Pattern item : list) {
            item.setImgUrl(resolveUrl(item.getImgUrl()));
            item.setMaterialUrl(resolveUrl(item.getMaterialUrl()));
        }
        return list;
    }

    private String resolveUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }
        String domain = materialDomain == null ? "" : materialDomain.replaceAll("/+$", "");
        if (url.startsWith("/")) {
            return domain + url;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            int schemeIndex = url.indexOf("://");
            int pathIndex = url.indexOf("/", schemeIndex + 3);
            String path = pathIndex >= 0 ? url.substring(pathIndex) : "/";
            return domain + path;
        }
        return domain + "/" + url;
    }

    /**
     * 新增心愿橱窗
     * 
     * @param pattern 心愿橱窗
     * @return 结果
     */
    @Override
    public int insertPattern(Pattern pattern)
    {
        return patternMapper.insertPattern(pattern);
    }

    /**
     * 修改心愿橱窗
     * 
     * @param pattern 心愿橱窗
     * @return 结果
     */
    @Override
    public int updatePattern(Pattern pattern)
    {
        return patternMapper.updatePattern(pattern);
    }

    /**
     * 批量删除心愿橱窗
     * 
     * @param patternIds 需要删除的心愿橱窗主键
     * @return 结果
     */
    @Override
    public int deletePatternByPatternIds(Long[] patternIds)
    {
        return patternMapper.deletePatternByPatternIds(patternIds);
    }

    /**
     * 删除心愿橱窗信息
     * 
     * @param patternId 心愿橱窗主键
     * @return 结果
     */
    @Override
    public int deletePatternByPatternId(Long patternId)
    {
        return patternMapper.deletePatternByPatternId(patternId);
    }
}
