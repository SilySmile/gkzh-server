package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.alibaba.fastjson2.JSON;
import com.gkzh.zycck.domain.ZycckCareerQuestion;
import com.gkzh.zycck.domain.ZycckCategory;
import com.gkzh.zycck.mapper.ZycckCareerQuestionMapper;
import com.gkzh.zycck.mapper.ZycckCategoryMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** zycck 职业目录缓存。目录是读多写少的数据，避免每次请求重复查询整套职业与题目。 */
@Service
public class ZycckCatalogCacheService {
    // v2 改用纯 JSON 字符串，避免对象序列化携带的 Java 类型信息导致读取失败后静默回源。
    private static final String CACHE_KEY = "zycck:catalog:v2";
    private static final String LEGACY_CACHE_KEY = "zycck:catalog:v1";
    private static final long CACHE_MINUTES = 10L;

    private final StringRedisTemplate redisTemplate;
    private final ZycckCategoryMapper categoryMapper;
    private final ZycckCareerQuestionMapper questionMapper;

    public ZycckCatalogCacheService(StringRedisTemplate redisTemplate,
                                     ZycckCategoryMapper categoryMapper,
                                     ZycckCareerQuestionMapper questionMapper) {
        this.redisTemplate = redisTemplate;
        this.categoryMapper = categoryMapper;
        this.questionMapper = questionMapper;
    }

    public Map<String, Object> getCatalog() {
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cached != null && !cached.isEmpty()) return JSON.parseObject(cached);
        } catch (RuntimeException ignored) {
            // Redis 临时不可用时回源数据库，不能影响游戏正常进入。
        }
        // 单机并发下双重检查，避免缓存刚失效时 500 个请求同时回源数据库。
        synchronized (this) {
            try {
                String cached = redisTemplate.opsForValue().get(CACHE_KEY);
                if (cached != null && !cached.isEmpty()) return JSON.parseObject(cached);
            } catch (RuntimeException ignored) {
                // Redis 不可用时继续回源数据库。
            }
            Map<String, Object> result = loadFromDatabase();
            try {
                redisTemplate.opsForValue().set(CACHE_KEY, JSON.toJSONString(result), CACHE_MINUTES, TimeUnit.MINUTES);
            } catch (RuntimeException ignored) {
                // 写缓存失败不影响本次响应。
            }
            return result;
        }
    }

    public void evict() {
        try {
            redisTemplate.delete(CACHE_KEY);
            redisTemplate.delete(LEGACY_CACHE_KEY);
        } catch (RuntimeException ignored) {
            // 删除失败时由 TTL 兜底，避免管理端保存操作失败。
        }
    }

    private Map<String, Object> loadFromDatabase() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("categories", categoryMapper.selectList(new QueryWrapper<ZycckCategory>()
                .eq("status", "0").orderByAsc("sort_order")));
        List<Map<String, Object>> careers = new ArrayList<>();
        List<Map<String, Object>> questions = new ArrayList<>();
        List<ZycckCareerQuestion> rows = questionMapper.selectList(new QueryWrapper<ZycckCareerQuestion>()
                .eq("status", "0").orderByAsc("category_id", "sort_order", "career_question_id"));
        for (ZycckCareerQuestion q : rows) {
            Map<String, Object> safe = new LinkedHashMap<>();
            safe.put("careerQuestionId", q.getCareerQuestionId());
            safe.put("careerId", q.getCareerQuestionId());
            safe.put("categoryId", q.getCategoryId());
            safe.put("careerName", q.getCareerName());
            safe.put("hasQuestion", q.getHasQuestion());
            safe.put("oneLineIntro", q.getOneLineIntro());
            safe.put("mainWork", q.getMainWork());
            safe.put("dayExample", q.getDayExample());
            safe.put("whyExists", q.getWhyExists());
            safe.put("careerImageUrl", q.getCareerImageUrl());
            safe.put("questionImageUrl", q.getQuestionImageUrl());
            safe.put("optionA", q.getOptionA());
            safe.put("optionB", q.getOptionB());
            safe.put("optionC", q.getOptionC());
            safe.put("optionD", q.getOptionD());
            safe.put("drawCandidate", q.getDrawCandidate());
            careers.add(safe);
            if ("1".equals(q.getHasQuestion())) questions.add(safe);
        }
        result.put("careers", careers);
        result.put("questions", questions);
        return result;
    }
}
