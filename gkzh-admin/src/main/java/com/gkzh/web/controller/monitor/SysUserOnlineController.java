package com.gkzh.web.controller.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.gkzh.common.core.domain.model.StudentCheckin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gkzh.common.annotation.Log;
import com.gkzh.common.constant.CacheConstants;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.LoginUser;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.core.redis.RedisCache;
import com.gkzh.common.enums.BusinessType;
import com.gkzh.common.utils.StringUtils;
import com.gkzh.system.domain.SysUserOnline;
import com.gkzh.system.service.ISysUserOnlineService;

/**
 * 在线用户监控
 * 
 *
 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController
{
    @Autowired
    private ISysUserOnlineService userOnlineService;

    @Autowired
    private RedisCache redisCache;

    @PreAuthorize("@ss.hasPermi('monitor:online:list')")
    @GetMapping("/list")
    public TableDataInfo list(String ipaddr, String userName,String type )
    {
        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        if(!StringUtils.isEmpty(type) && type.equals("mobileApp")){
            Collection<String> keys = redisCache.keys(CacheConstants.STU_TOKEN_KEY + "*");
            for (String key : keys)
            {
                StudentCheckin user = redisCache.getCacheObject(key);
                if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName))
                {
                    userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
                }
                else if (StringUtils.isNotEmpty(ipaddr))
                {
                    userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
                }
                else if (StringUtils.isNotEmpty(userName))
                {
                    userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
                }
                else
                {
                    userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
                }
            }
        }else{
            Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
            for (String key : keys)
            {
                LoginUser user = redisCache.getCacheObject(key);
                if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName))
                {
                    userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
                }
                else if (StringUtils.isNotEmpty(ipaddr))
                {
                    userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
                }
                else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser()))
                {
                    userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
                }
                else
                {
                    userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
                }
            }
        }
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));
        return getDataTable(userOnlineList);
    }

    /**
     * 强退用户
     */
    @PreAuthorize("@ss.hasPermi('monitor:online:forceLogout')")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{type}/{tokenId}")
    public AjaxResult forceLogout(@PathVariable String tokenId,@PathVariable String type)
    {
        if(!StringUtils.isEmpty(type) && type.equals("mobileApp")){
            redisCache.deleteObject(CacheConstants.STU_TOKEN_KEY + tokenId);
        }else{
            redisCache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
        }
        return success();
    }
}
