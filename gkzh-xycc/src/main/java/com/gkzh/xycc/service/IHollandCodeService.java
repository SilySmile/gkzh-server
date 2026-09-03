package com.gkzh.xycc.service;

import java.util.List;

import com.gkzh.xycc.domain.HollandCode;

public interface IHollandCodeService {
    List<HollandCode> listCodes();

    HollandCode getCode(String code);

    int saveCode(HollandCode code);

    int deleteCode(String code);
}
