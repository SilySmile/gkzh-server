package com.hprt.utils;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.hprt.domain.HPRTResult;

import java.util.Map;

public class HttpUtils {

    public static <T> HPRTResult<T> post(String url, Map<String, Object> body) {
        return new HPRTResult<>(HttpUtil.post(url, JSONUtil.toJsonStr(body)), new TypeReference<>() {
        });
    }
}
