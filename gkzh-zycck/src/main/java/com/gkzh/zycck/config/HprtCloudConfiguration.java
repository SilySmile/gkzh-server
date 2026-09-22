package com.gkzh.zycck.config;

import com.hprt.client.HPRTPrinterClient;
import com.hprt.config.HPRTConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HprtCloudProperties.class)
public class HprtCloudConfiguration {

    @Bean
    public HPRTPrinterClient hprtPrinterClient(HprtCloudProperties properties) {
        return new HPRTPrinterClient(new HPRTConfig(properties.getUserKey(), properties.getUserSecret()));
    }
}
