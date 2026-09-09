package com.gkzh.app.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zycck.domain.ZycckCareerQuestion;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckCareerQuestionMapper;
import com.gkzh.zycck.mapper.ZycckCategoryMapper;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.gkzh.zycck.service.ZycckRecordService;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;

/** 单独下载和活动报告包共用同一份职业报告。 */
@Service
public class ZycckReportPdfService {
    private final ZycckRecordService records;
    private final ZycckRecordMapper recordMapper;
    private final ZycckCareerQuestionMapper careers;
    private final ZycckCategoryMapper categoryMapper;

    public ZycckReportPdfService(ZycckRecordService records, ZycckRecordMapper recordMapper,
                                 ZycckCareerQuestionMapper careers, ZycckCategoryMapper categoryMapper) {
        this.records = records;
        this.recordMapper = recordMapper;
        this.careers = careers;
        this.categoryMapper = categoryMapper;
    }

    public byte[] create(Long recordId, Long userId) throws IOException {
        ZycckRecord record = records.get(recordId, userId);
        String idsJson = record.getViewedCareerIds() != null ? record.getViewedCareerIds() : record.getExplorationCareerIds();
        List<Long> ids = idsJson == null ? Collections.emptyList() : JSON.parseArray(idsJson, Long.class);
        List<ZycckCareerQuestion> selected = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            Map<Long, ZycckCareerQuestion> byId = new HashMap<>();
            for (ZycckCareerQuestion career : careers.selectBatchIds(ids)) byId.put(career.getCareerQuestionId(), career);
            for (Long id : ids) {
                if (!byId.containsKey(id)) throw new ServiceException("部分职业信息已不存在，请联系管理员恢复后下载");
                selected.add(byId.get(id));
            }
        }
        Map<Long, String> categoryNames = new HashMap<>();
        for (com.gkzh.zycck.domain.ZycckCategory c : categoryMapper.selectList(new QueryWrapper<com.gkzh.zycck.domain.ZycckCategory>().eq("status", "0"))) categoryNames.put(c.getCategoryId(), c.getName());
        return ZycckReportPdfRenderer.render(record, selected, categoryNames);
    }

    public byte[] createForActivity(Long schoolId, Long instanceId, Long gameId, Long userId) throws IOException {
        ZycckRecord record = recordMapper.selectOne(new QueryWrapper<ZycckRecord>()
                .eq("school_id", schoolId).eq("instance_id", instanceId).eq("game_id", gameId)
                .eq("user_id", userId).eq("status", "finished").orderByDesc("record_id").last("limit 1"));
        if (record == null) throw new ServiceException("未找到本次活动的职业探索报告");
        return create(record.getRecordId(), userId);
    }
}
