package com.gkzh.app.service;

import com.gkzh.common.exception.ServiceException;
import com.gkzh.zcyltzj.domain.ZcyltzjRuleCell;
import com.gkzh.zcyltzj.mapper.ZcyltzjRuleCellMapper;
import com.gkzh.zcyltzj.service.ZcyltzjRuleService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 骰子局规则格配置回归：只允许第 8、9 格，标题必填，保存时新增或更新。
 */
public class ZcyltzjRuleServiceTest {

    private final ZcyltzjRuleCellMapper mapper = mock(ZcyltzjRuleCellMapper.class);
    private final ZcyltzjRuleService service = new ZcyltzjRuleService(mapper);

    @Test
    public void rejectsCellsOtherThanBottomMiddleAndBottomRight() {
        ZcyltzjRuleCell body = new ZcyltzjRuleCell();
        body.setCellNo(7);
        body.setTitle("规则");
        assertThrows(ServiceException.class, () -> service.save(body));
    }

    @Test
    public void rejectsBlankTitle() {
        ZcyltzjRuleCell body = new ZcyltzjRuleCell();
        body.setCellNo(8);
        body.setTitle("   ");
        assertThrows(ServiceException.class, () -> service.save(body));
    }

    @Test
    public void insertsWhenRuleCellNotExists() {
        when(mapper.selectOne(any())).thenReturn(null);
        ZcyltzjRuleCell body = new ZcyltzjRuleCell();
        body.setCellNo(8);
        body.setTitle("再来一次");
        body.setContent("掷出 6 点后可以再掷一次");
        when(mapper.insert(any())).thenReturn(1);

        ZcyltzjRuleCell saved = service.save(body);

        assertEquals("再来一次", saved.getTitle());
        assertEquals("0", saved.getStatus());
        verify(mapper).insert(any(ZcyltzjRuleCell.class));
    }

    @Test
    public void updatesExistingRuleCell() {
        ZcyltzjRuleCell existing = new ZcyltzjRuleCell();
        existing.setRuleId(1L);
        existing.setCellNo(9);
        existing.setTitle("旧规则");
        when(mapper.selectOne(any())).thenReturn(existing);
        ZcyltzjRuleCell body = new ZcyltzjRuleCell();
        body.setCellNo(9);
        body.setTitle("新规则");
        body.setContent("新内容");
        body.setStatus("1");

        ZcyltzjRuleCell saved = service.save(body);

        assertEquals("新规则", saved.getTitle());
        assertEquals("1", saved.getStatus());
        verify(mapper).updateById(existing);
    }
}
