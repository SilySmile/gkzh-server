package com.gkzh.web.controller.zycck;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ZycckAdminPaginationTest {
    private final ZycckRecordMapper mapper = mock(ZycckRecordMapper.class);
    private final IActivityWeekService activities = mock(IActivityWeekService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ZycckAdminController(
            null, null, mapper, null, null, null, activities, null)).build();

    @AfterEach void clearPage() { PageHelper.clearPage(); }

    private void page(int number, int size, long total, long... ids) {
        when(mapper.selectList(any())).thenAnswer(invocation -> {
            Page<?> request = PageHelper.getLocalPage();
            assertNotNull(request, "记录查询必须启用数据库分页");
            assertEquals(number, request.getPageNum());
            assertEquals(size, request.getPageSize());
            QueryWrapper<?> query = invocation.getArgument(0);
            assertTrue(query.getSqlSegment().contains("record_id DESC"));
            Page<ZycckRecord> result = new Page<>(number, size);
            result.setTotal(total);
            for (long id : ids) {
                ZycckRecord row = new ZycckRecord();
                row.setRecordId(id);
                result.add(row);
            }
            // 模拟 PageHelper 拦截器在数据库查询结束后清理线程状态。
            PageHelper.clearPage();
            return result;
        });
    }

    @Test void recordsRetainTotalAfterEnrichmentOnLastPage() throws Exception {
        page(3, 10, 21, 1);
        mvc.perform(get("/zycck/admin/records").param("pageNum", "3").param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(21))
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].recordId").value(1));
    }

    @Test void statisticsKeepFilteredTotalsIndependentOfCurrentPage() throws Exception {
        when(mapper.selectCount(any())).thenAnswer(invocation -> {
            assertNull(PageHelper.getLocalPage(), "汇总不能被分页截断");
            QueryWrapper<?> query = invocation.getArgument(0);
            String sql = query.getSqlSegment();
            for (String column : new String[] {"game_type", "instance_id", "school_id", "department_id", "major", "gender"}) {
                assertTrue(sql.contains(column), "汇总必须保留筛选条件: " + column);
            }
            return sql.contains("status") ? 12L : 21L;
        });
        page(2, 20, 21, 1);
        mvc.perform(get("/zycck/admin/statistics").param("pageNum", "2").param("pageSize", "20")
                        .param("instanceId", "1").param("schoolId", "2").param("departmentId", "3")
                        .param("major", "计算机").param("gender", "男"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(21))
                .andExpect(jsonPath("$.data.enteredCount").value(21))
                .andExpect(jsonPath("$.data.participating").value(21))
                .andExpect(jsonPath("$.data.finishedCount").value(12))
                .andExpect(jsonPath("$.data.finished").value(12))
                .andExpect(jsonPath("$.data.records.length()").value(1));
        verify(mapper, times(1)).selectList(any());
        verify(mapper, times(2)).selectCount(any());
    }

    @Test void emptyStatisticsUseDefaultPaginationAndSkipEnrichment() throws Exception {
        when(mapper.selectCount(any())).thenReturn(0L);
        page(1, 10, 0);
        mvc.perform(get("/zycck/admin/statistics"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records.length()").value(0));
        verifyNoInteractions(activities);
    }
}
