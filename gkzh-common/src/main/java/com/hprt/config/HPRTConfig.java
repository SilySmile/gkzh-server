package com.hprt.config;

import com.hprt.utils.SecretUtils;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
public class HPRTConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String user_key;
    private String user_secret;
    private String timestamp;
    private String signature;

    /**
     * HPRT 云打印配置
     *
     * @param userKey    用户Key
     * @param userSecret 用户密钥
     */
    public HPRTConfig(String userKey, String userSecret) {
        this.user_key = userKey;
        this.user_secret = userSecret;
    }

    public Map<String, Object> build() {
        this.setTimestamp(String.valueOf(Instant.now().getEpochSecond()));
        this.setSignature(SecretUtils.hmacSHA256(this.getUser_key() + this.getTimestamp(), this.getUser_secret()));
        Map<String, Object> result = new HashMap<>();
        result.put("user_key", this.getUser_key());
        result.put("timestamp", this.getTimestamp());
        result.put("signature", this.getSignature());
        return result;
    }

}
