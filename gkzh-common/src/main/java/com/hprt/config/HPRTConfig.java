package com.hprt.config;

import com.hprt.utils.SecretUtils;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** 汉印云打印请求认证配置。 */
@Getter
public class HPRTConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String userKey;
    private final String userSecret;

    /**
     * HPRT 云打印配置。
     *
     * @param userKey    用户 Key
     * @param userSecret 用户密钥
     */
    public HPRTConfig(String userKey, String userSecret) {
        this.userKey = userKey;
        this.userSecret = userSecret;
    }

    /**
     * 每次请求生成独立的时间戳和签名，避免 Spring 单例被并发请求共享可变认证状态。
     */
    public Map<String, Object> build() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String signature = SecretUtils.hmacSHA256(userKey + timestamp, userSecret);
        Map<String, Object> result = new HashMap<>();
        result.put("user_key", userKey);
        result.put("timestamp", timestamp);
        result.put("signature", signature);
        return result;
    }
}
