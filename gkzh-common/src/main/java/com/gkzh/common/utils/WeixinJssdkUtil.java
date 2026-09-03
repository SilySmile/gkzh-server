package com.gkzh.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gkzh.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信JS-SDK工具类
 * 
 * @author gkzh
 */
@Component
public class WeixinJssdkUtil {
    
    private static final Logger log = LoggerFactory.getLogger(WeixinJssdkUtil.class);
    
    // 微信公众号配置（需要替换为实际配置）
    @Value("${app.wx_appId}")
    private String APP_ID;
    @Value("${app.wx_appSecret}")
    private String APP_SECRET;
    
    // 缓存access_token和jsapi_ticket
    private static String accessToken = null;
    private static String jsapiTicket = null;
    private static long accessTokenExpireTime = 0;
    private static long jsapiTicketExpireTime = 0;
    
    /**
     * 获取微信JS-SDK配置
     * 
     * @param url 当前页面URL
     * @return 配置参数
     */
    public Map<String, Object> getJssdkConfig(String url) {
        try {
            String jsapiTicket = getJsapiTicket();
            String nonceStr = generateNonceStr();
            long timestamp = System.currentTimeMillis() / 1000;
            
            // 生成签名
            String signature = generateSignature(jsapiTicket, nonceStr, timestamp, url);
            
            Map<String, Object> config = new HashMap<>();
            config.put("appId", APP_ID);
            config.put("timestamp", timestamp);
            config.put("nonceStr", nonceStr);
            config.put("signature", signature);
            config.put("url", url);
            
            return config;
        } catch (Exception e) {
            log.error("获取微信JS-SDK配置失败", e);
            throw new RuntimeException("获取微信JS-SDK配置失败", e);
        }
    }
    
    /**
     * 获取access_token
     */
    private String getAccessToken() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        if (accessToken != null && currentTime < accessTokenExpireTime) {
            return accessToken;
        }
        
        try {
            String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                APP_ID, APP_SECRET
            );
            
            String response = HttpUtils.sendGet(url);
            JSONObject json = JSON.parseObject(response);
            
            if (json.containsKey("access_token")) {
                accessToken = json.getString("access_token");
                // access_token有效期为7200秒，提前5分钟过期
                accessTokenExpireTime = currentTime + (7200 - 300) * 1000;
                log.info("获取access_token成功: {}", accessToken);
                return accessToken;
            } else {
                log.error("获取access_token失败: {}", response);
                throw new RuntimeException("获取access_token失败: " + json.getString("errmsg"));
            }
        } catch (Exception e) {
            log.error("获取access_token异常", e);
            throw new RuntimeException("获取access_token异常", e);
        }
    }
    
    /**
     * 获取jsapi_ticket
     */
    private String getJsapiTicket() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        if (jsapiTicket != null && currentTime < jsapiTicketExpireTime) {
            return jsapiTicket;
        }
        
        try {
            String accessToken = getAccessToken();
            String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi",
                accessToken
            );
            
            String response = HttpUtils.sendGet(url);
            JSONObject json = JSON.parseObject(response);
            
            if (json.getInteger("errcode") == 0) {
                jsapiTicket = json.getString("ticket");
                // jsapi_ticket有效期为7200秒，提前5分钟过期
                jsapiTicketExpireTime = currentTime + (7200 - 300) * 1000;
                log.info("获取jsapi_ticket成功: {}", jsapiTicket);
                return jsapiTicket;
            } else {
                log.error("获取jsapi_ticket失败: {}", response);
                throw new RuntimeException("获取jsapi_ticket失败: " + json.getString("errmsg"));
            }
        } catch (Exception e) {
            log.error("获取jsapi_ticket异常", e);
            throw new RuntimeException("获取jsapi_ticket异常", e);
        }
    }
    
    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        return "gkzh_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 生成签名
     * 
     * @param jsapiTicket jsapi_ticket
     * @param nonceStr 随机字符串
     * @param timestamp 时间戳
     * @param url 当前页面URL
     * @return 签名
     */
    private String generateSignature(String jsapiTicket, String nonceStr, long timestamp, String url) {
        try {
            // 1. 对所有待签名参数按照字段名的ASCII 码从小到大排序（字典序）
            String[] params = {
                "jsapi_ticket=" + jsapiTicket,
                "noncestr=" + nonceStr,
                "timestamp=" + timestamp,
                "url=" + url
            };
            Arrays.sort(params);
            
            // 2. 使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串string1
            String string1 = String.join("&", params);
            
            // 3. 对string1进行sha1签名，得到signature
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(string1.getBytes("UTF-8"));
            
            StringBuilder signature = new StringBuilder();
            for (byte b : digest) {
                signature.append(String.format("%02x", b));
            }
            
            log.debug("签名参数: {}", string1);
            log.debug("生成签名: {}", signature.toString());
            
            return signature.toString();
        } catch (Exception e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("生成签名失败", e);
        }
    }
} 