package com.gkzh.xycc.service.impl;

import java.util.List;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.xycc.domain.HollandCode;
import com.gkzh.xycc.mapper.HollandCodeMapper;
import com.gkzh.xycc.service.IHollandCodeService;
import com.gkzh.common.utils.DateUtils;

@Service
public class HollandCodeServiceImpl implements IHollandCodeService {

    @Autowired
    private HollandCodeMapper hollandCodeMapper;

    @Override
    public List<HollandCode> listCodes() {
        QueryWrapper<HollandCode> query = new QueryWrapper<>();
        query.eq("status", "0").orderByAsc("sort_order");
        return hollandCodeMapper.selectList(query);
    }

    @Override
    public HollandCode getCode(String code) {
        return hollandCodeMapper.selectById(code);
    }

    @Override
    public int saveCode(HollandCode code) {
        Date now = DateUtils.getNowDate();
        if (hollandCodeMapper.selectById(code.getCode()) == null) {
            code.setCreateTime(now);
            code.setUpdateTime(now);
            if (code.getStatus() == null) {
                code.setStatus("0");
            }
            return hollandCodeMapper.insert(code);
        }
        code.setUpdateTime(now);
        return hollandCodeMapper.updateById(code);
    }

    @Override
    public int deleteCode(String code) {
        return hollandCodeMapper.deleteById(code);
    }
}
