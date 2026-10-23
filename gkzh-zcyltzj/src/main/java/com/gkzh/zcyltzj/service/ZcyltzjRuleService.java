package com.gkzh.zcyltzj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zcyltzj.domain.ZcyltzjRuleCell;
import com.gkzh.zcyltzj.mapper.ZcyltzjRuleCellMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 骰子局九宫格规则格配置：只允许维护下中（第 8 格）和下右（第 9 格）两格。
 */
@Service
public class ZcyltzjRuleService {

    private final ZcyltzjRuleCellMapper ruleCellMapper;

    public ZcyltzjRuleService(ZcyltzjRuleCellMapper ruleCellMapper) {
        this.ruleCellMapper = ruleCellMapper;
    }

    /** 后台查看两格规则（含停用）。 */
    public List<ZcyltzjRuleCell> listAll() {
        return ruleCellMapper.selectList(new QueryWrapper<ZcyltzjRuleCell>().orderByAsc("cell_no"));
    }

    /** 学生端只返回启用的规则格。 */
    public List<ZcyltzjRuleCell> listActive() {
        return ruleCellMapper.selectList(new QueryWrapper<ZcyltzjRuleCell>()
                .eq("status", "0").orderByAsc("cell_no"));
    }

    /** 保存规则格标题、内容和状态。 */
    @Transactional
    public ZcyltzjRuleCell save(ZcyltzjRuleCell body) {
        if (body == null || body.getCellNo() == null || (body.getCellNo() != 8 && body.getCellNo() != 9)) {
            throw new ServiceException("规则格只允许第 8 格（下中）和第 9 格（下右）");
        }
        String title = body.getTitle() == null ? "" : body.getTitle().trim();
        if (title.isEmpty()) {
            throw new ServiceException("请填写规则格标题");
        }
        if (title.length() > 50) {
            throw new ServiceException("规则格标题不能超过 50 个字符");
        }
        String content = body.getContent() == null ? "" : body.getContent().trim();
        if (content.length() > 500) {
            throw new ServiceException("规则内容不能超过 500 个字符");
        }
        ZcyltzjRuleCell cell = ruleCellMapper.selectOne(new QueryWrapper<ZcyltzjRuleCell>()
                .eq("cell_no", body.getCellNo()));
        boolean insert = cell == null;
        if (insert) {
            cell = new ZcyltzjRuleCell();
            cell.setCellNo(body.getCellNo());
            cell.setCreateTime(DateUtils.getNowDate());
        }
        cell.setTitle(title);
        cell.setContent(content);
        cell.setStatus(body.getStatus() == null ? "0" : ("1".equals(body.getStatus()) ? "1" : "0"));
        cell.setUpdateTime(DateUtils.getNowDate());
        if (insert) {
            ruleCellMapper.insert(cell);
        } else {
            ruleCellMapper.updateById(cell);
        }
        return cell;
    }
}
