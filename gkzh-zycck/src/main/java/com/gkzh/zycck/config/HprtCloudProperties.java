package com.gkzh.zycck.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 汉印云打印配置。 */
@Data
@ConfigurationProperties(prefix = "hprt.cloud")
public class HprtCloudProperties {
    /** 汉印开放平台 user_key。建议通过环境变量注入。 */
    private String userKey;

    /** 汉印开放平台 user_secret。建议通过环境变量注入。 */
    private String userSecret;

    /** 打印类型，例如 ESC、TSPL 或 CPCL。 */
    private String printType = "TSPL";
}
