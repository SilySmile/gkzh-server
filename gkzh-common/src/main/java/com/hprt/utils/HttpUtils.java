package com.hprt.utils;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.hprt.domain.HPRTResult;
import com.hprt.exception.HPRTSdkException;

import java.util.Map;

/** 汉印云打印 HTTP 调用封装。 */
public final class HttpUtils {
    private static final int TIMEOUT_MILLIS = 15_000;

    private HttpUtils() {
    }

    public static <T> HPRTResult<T> post(String url, Map<String, Object> body) {
        try (HttpResponse response = HttpRequest.post(url)
                .timeout(TIMEOUT_MILLIS)
                .contentType("application/json")
                .body(JSONUtil.toJsonStr(body))
                .execute()) {
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                throw new HPRTSdkException("汉印云接口 HTTP 状态异常: " + response.getStatus());
            }
            return new HPRTResult<>(response.body(), new TypeReference<>() {
            });
        } catch (HPRTSdkException e) {
            throw e;
        } catch (Exception e) {
            throw new HPRTSdkException("调用汉印云接口失败", e);
        }
    }
}
