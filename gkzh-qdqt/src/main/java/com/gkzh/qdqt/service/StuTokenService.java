package com.gkzh.qdqt.service;

import com.gkzh.common.constant.CacheConstants;
import com.gkzh.common.constant.Constants;
import com.gkzh.common.core.domain.model.LoginUser;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.core.redis.RedisCache;
import com.gkzh.common.utils.StringUtils;
import com.gkzh.common.utils.uuid.IdUtils;
import com.gkzh.qdqt.domain.GkzhStudentCheckin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class StuTokenService {
    private static final Logger log = LoggerFactory.getLogger(StuTokenService.class);
    // 令牌自定义标识
    // 令牌秘钥
    @Value("${token.secret}")
    private String secret;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;
    public boolean validateToken(StudentCheckin checkin) {
        long expireTime = checkin.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TWENTY)
        {
            refreshToken(checkin);
        }
        return true;
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims)
    {
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        return token;
    }

    /**
     * 创建令牌
     *
     * @param checkin 用户信息
     * @return 令牌
     */
    public String createToken(StudentCheckin checkin)
    {
        String token = IdUtils.fastUUID();
        checkin.setToken(token);
        refreshToken(checkin);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);
        claims.put(Constants.JWT_USERNAME, checkin.getStuName());
        return createToken(claims);
    }
    /**
     * 删除用户身份信息
     */
    public void delLoginUser(String uuid)
    {
        if (StringUtils.isNotEmpty(uuid))
        {
            String userKey = getTokenKey(uuid);
            redisCache.deleteObject(userKey);
        }
    }
    /**
     * 刷新令牌有效期
     *
     * @param checkin 签到信息
     */
    public void refreshToken(StudentCheckin checkin)
    {
        checkin.setLoginTime(System.currentTimeMillis());
        checkin.setExpireTime(checkin.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据uuid将loginUser缓存
        String userKey = getTokenKey(checkin.getToken());
        redisCache.setCacheObject(userKey, checkin, expireTime, TimeUnit.MINUTES);
    }

    private String getTokenKey(String uuid)
    {
        return CacheConstants.STU_TOKEN_KEY + uuid;
    }

    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    private String getToken(HttpServletRequest request)
    {
        String token = request.getHeader("X-Front-Token");
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public StudentCheckin getStudentCheckin(HttpServletRequest request)
    {
        // 获取请求携带的令牌
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String userKey = getTokenKey(uuid);
                StudentCheckin studentCheckin = redisCache.getCacheObject(userKey);
                return studentCheckin;
            }
            catch (Exception e)
            {
                log.error("获取用户信息异常'{}'", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token)
    {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

}
