package com.gkzh.app.core.staff;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.gkzh.common.constant.CacheConstants;
import com.gkzh.common.constant.Constants;
import com.gkzh.common.core.redis.RedisCache;
import com.gkzh.common.utils.StringUtils;
import com.gkzh.common.utils.uuid.IdUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class StaffTokenService {
    private static final String STAFF_TOKEN_KEY = "staff_tokens:";
    @Value("${token.secret}") private String secret;
    @Value("${token.expireTime:30}") private int expireMinutes;
    @Autowired private RedisCache redisCache;

    public String createToken(StaffSession session) {
        String uuid = IdUtils.fastUUID();
        session.setToken(uuid); session.setLoginTime(new java.util.Date());
        session.setExpireTime(System.currentTimeMillis() + expireMinutes * 60L * 1000L);
        redisCache.setCacheObject(STAFF_TOKEN_KEY + uuid, session, expireMinutes, TimeUnit.MINUTES);
        Map<String, Object> claims = new HashMap<>(); claims.put(Constants.LOGIN_USER_KEY, uuid);
        return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public StaffSession getSession(HttpServletRequest request) {
        String value = request.getHeader("Authorization");
        if (StringUtils.isEmpty(value)) value = request.getHeader("X-Front-Token");
        if (StringUtils.isEmpty(value)) return null;
        if (value.startsWith(Constants.TOKEN_PREFIX)) value = value.replace(Constants.TOKEN_PREFIX, "");
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(value).getBody();
            String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
            return redisCache.getCacheObject(STAFF_TOKEN_KEY + uuid);
        } catch (Exception e) { return null; }
    }
}
