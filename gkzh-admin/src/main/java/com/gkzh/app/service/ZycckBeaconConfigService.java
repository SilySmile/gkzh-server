package com.gkzh.app.service;

import com.alibaba.fastjson2.JSON;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zycck.domain.ZycckBeaconConfig;
import com.gkzh.zycck.dto.ZycckBeaconRule;
import com.gkzh.zycck.dto.ZycckBeaconSettingsView;
import com.gkzh.zycck.mapper.ZycckBeaconConfigMapper;
import com.gkzh.zycck.util.ZycckBeaconUuid;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Web 信标管理中的配置，保存后立即成为打印资格与服务端校验的唯一依据。 */
@Service
public class ZycckBeaconConfigService {
    private static final long CONFIG_ID = 1L;
    /** 配置读多写少；短 TTL 为跨服务实例的缓存删除失败提供最终兜底。 */
    private static final String CACHE_KEY = "zycck:beacon:settings:v1";
    private static final long CACHE_SECONDS = 60L;
    /** Redis 临时不可用时，进程内短时缓存避免每个学生请求都打到数据库。 */
    private static final long LOCAL_FALLBACK_MILLIS = 5000L;

    private final ZycckBeaconConfigMapper mapper;
    private final StringRedisTemplate redisTemplate;
    private volatile String localSnapshot;
    private volatile long localExpiresAt;

    public ZycckBeaconConfigService(ZycckBeaconConfigMapper mapper, StringRedisTemplate redisTemplate) {
        this.mapper = mapper;
        this.redisTemplate = redisTemplate;
    }

    /** 优先读 Redis；缓存未命中时双重检查，降低高并发下的数据库回源量。 */
    public ZycckBeaconSettingsView get() {
        ZycckBeaconSettingsView cached = fromCache();
        if (cached != null) return cached;
        synchronized (this) {
            cached = fromCache();
            if (cached != null) return cached;
            ZycckBeaconSettingsView view = loadFromDatabase();
            String json = JSON.toJSONString(view);
            localSnapshot = json;
            localExpiresAt = System.currentTimeMillis() + LOCAL_FALLBACK_MILLIS;
            try {
                redisTemplate.opsForValue().set(CACHE_KEY, json, CACHE_SECONDS, TimeUnit.SECONDS);
            } catch (RuntimeException ignored) {
                // Redis 故障时仍可打印；短时本地快照避免瞬时请求压垮数据库。
            }
            return view;
        }
    }

    /** Redis 异常时只允许使用数秒内的本地快照，正常缓存未命中必须回源数据库。 */
    private ZycckBeaconSettingsView fromCache() {
        try {
            String json = redisTemplate.opsForValue().get(CACHE_KEY);
            return StringUtils.hasText(json) ? JSON.parseObject(json, ZycckBeaconSettingsView.class) : null;
        } catch (RuntimeException ignored) {
            String fallback = localSnapshot;
            return fallback != null && System.currentTimeMillis() < localExpiresAt
                    ? JSON.parseObject(fallback, ZycckBeaconSettingsView.class) : null;
        }
    }

    /** Redis 与本地缓存都未命中时读取数据库的唯一配置行。 */
    private ZycckBeaconSettingsView loadFromDatabase() {
        ZycckBeaconConfig saved = mapper.selectById(CONFIG_ID);
        if (saved == null) return defaultSettings();
        ZycckBeaconSettingsView view = new ZycckBeaconSettingsView();
        view.setBeaconEnabled("1".equals(saved.getBeaconEnabled()));
        List<ZycckBeaconRule> rules = JSON.parseArray(saved.getBeaconsJson(), ZycckBeaconRule.class);
        view.setBeacons(rules == null ? new ArrayList<>() : rules);
        view.setDefaultMaxDistance(saved.getDefaultMaxDistance());
        view.setDefaultMinRssi(saved.getDefaultMinRssi());
        view.setMaxAgeSeconds(saved.getMaxAgeSeconds());
        view.setScanTimeoutSeconds(saved.getScanTimeoutSeconds());
        return view;
    }

    /** 校验完整配置后一次保存，拒绝启用没有有效信标的配置。 */
    @Transactional
    public ZycckBeaconSettingsView save(ZycckBeaconSettingsView input) {
        if (input == null) throw new ServiceException("缺少蓝牙信标配置");
        if (!Double.isFinite(input.getDefaultMaxDistance())
                || input.getDefaultMaxDistance() <= 0 || input.getDefaultMaxDistance() > 100) {
            throw new ServiceException("默认最大距离需在0至100米之间");
        }
        if (input.getDefaultMinRssi() < -127 || input.getDefaultMinRssi() > 0) {
            throw new ServiceException("默认最低RSSI需在-127至0之间");
        }
        if (input.getMaxAgeSeconds() < 1 || input.getMaxAgeSeconds() > 300) {
            throw new ServiceException("检测结果有效期需在1至300秒之间");
        }
        if (input.getScanTimeoutSeconds() < 3 || input.getScanTimeoutSeconds() > 60) {
            throw new ServiceException("扫描超时需在3至60秒之间");
        }
        List<ZycckBeaconRule> rules = normalize(input.getBeacons());
        if (input.isBeaconEnabled() && rules.isEmpty()) {
            throw new ServiceException("启用蓝牙限制前请至少配置一个信标");
        }
        ZycckBeaconConfig saved = mapper.selectById(CONFIG_ID);
        boolean inserting = saved == null;
        if (inserting) saved = new ZycckBeaconConfig();
        saved.setConfigId(CONFIG_ID);
        saved.setBeaconEnabled(input.isBeaconEnabled() ? "1" : "0");
        saved.setBeaconsJson(JSON.toJSONString(rules));
        saved.setDefaultMaxDistance(input.getDefaultMaxDistance());
        saved.setDefaultMinRssi(input.getDefaultMinRssi());
        saved.setMaxAgeSeconds(input.getMaxAgeSeconds());
        saved.setScanTimeoutSeconds(input.getScanTimeoutSeconds());
        saved.setUpdateTime(DateUtils.getNowDate());
        if (inserting) mapper.insert(saved);
        else mapper.updateById(saved);
        // 事务真正提交后再失效缓存，避免其他实例在提交前回源并重新缓存旧配置。
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictCache();
                }
            });
        } else {
            evictCache();
        }
        ZycckBeaconSettingsView result = new ZycckBeaconSettingsView();
        result.setBeaconEnabled(input.isBeaconEnabled());
        result.setBeacons(rules);
        result.setDefaultMaxDistance(input.getDefaultMaxDistance());
        result.setDefaultMinRssi(input.getDefaultMinRssi());
        result.setMaxAgeSeconds(input.getMaxAgeSeconds());
        result.setScanTimeoutSeconds(input.getScanTimeoutSeconds());
        return result;
    }

    /** Web 保存后清理 Redis 与进程内备用快照，后续请求立即读取新配置。 */
    private synchronized void evictCache() {
        localSnapshot = null;
        localExpiresAt = 0L;
        try {
            redisTemplate.delete(CACHE_KEY);
        } catch (RuntimeException ignored) {
            // Redis 不可用时由 60 秒 TTL 兜底；不能回滚已保存的数据库配置。
        }
    }

    /** 表内尚无配置时给管理页面显示安全的默认值，蓝牙限制保持关闭。 */
    private ZycckBeaconSettingsView defaultSettings() {
        ZycckBeaconSettingsView view = new ZycckBeaconSettingsView();
        view.setBeaconEnabled(false);
        view.setDefaultMaxDistance(3.0D);
        view.setDefaultMinRssi(-90);
        view.setMaxAgeSeconds(15);
        view.setScanTimeoutSeconds(10);
        return view;
    }

    /** 将页面输入统一格式化，并阻止同一 UUID/Major/Minor 重复配置。 */
    private List<ZycckBeaconRule> normalize(List<ZycckBeaconRule> input) {
        List<ZycckBeaconRule> rules = new ArrayList<>();
        Set<String> identities = new HashSet<>();
        if (input == null) return rules;
        if (input.size() > 50) throw new ServiceException("最多配置50个信标");
        for (ZycckBeaconRule raw : input) {
            String uuid = raw == null ? null : ZycckBeaconUuid.normalize(raw.getUuid());
            if (uuid == null) {
                throw new ServiceException("信标UUID格式不正确");
            }
            ZycckBeaconRule rule = copy(raw);
            rule.setUuid(uuid);
            if (rule.getName() != null && rule.getName().length() > 100) {
                throw new ServiceException("信标名称不能超过100个字符");
            }
            checkPart(rule.getMajor(), "Major");
            checkPart(rule.getMinor(), "Minor");
            if (rule.getMaxDistance() != null && (!Double.isFinite(rule.getMaxDistance())
                    || rule.getMaxDistance() <= 0 || rule.getMaxDistance() > 100)) {
                throw new ServiceException("信标最大距离需在0至100米之间");
            }
            if (rule.getReferenceRssi() != null && (!Double.isFinite(rule.getReferenceRssi())
                    || rule.getReferenceRssi() < 20 || rule.getReferenceRssi() > 100)) {
                throw new ServiceException("1米参考RSSI绝对值A需在20至100之间");
            }
            if (rule.getPathLossExponent() != null && (!Double.isFinite(rule.getPathLossExponent())
                    || rule.getPathLossExponent() < 1 || rule.getPathLossExponent() > 6)) {
                throw new ServiceException("环境衰减因子n需在1至6之间");
            }
            if (rule.getMinRssi() != null && (rule.getMinRssi() < -127 || rule.getMinRssi() > 0)) {
                throw new ServiceException("信标最低RSSI需在-127至0之间");
            }
            String identity = rule.getUuid() + ":" + rule.getMajor() + ":" + rule.getMinor();
            if (!identities.add(identity)) throw new ServiceException("同一UUID、Major和Minor的信标不能重复");
            rules.add(rule);
        }
        return rules;
    }

    /** Major 和 Minor 都必须是 iBeacon 规定的 0 至 65535。 */
    private void checkPart(Integer value, String label) {
        if (value != null && (value < 0 || value > 65535)) {
            throw new ServiceException(label + "需在0至65535之间，或留空表示不限制");
        }
    }

    /** 复制规则以隔离接口输入对象和后续归一化处理。 */
    private ZycckBeaconRule copy(ZycckBeaconRule raw) {
        ZycckBeaconRule item = new ZycckBeaconRule();
        item.setName(raw.getName() == null ? null : raw.getName().trim());
        item.setUuid(raw.getUuid() == null ? null : raw.getUuid().trim());
        item.setMajor(raw.getMajor());
        item.setMinor(raw.getMinor());
        item.setMaxDistance(raw.getMaxDistance());
        item.setReferenceRssi(raw.getReferenceRssi());
        item.setPathLossExponent(raw.getPathLossExponent());
        item.setMinRssi(raw.getMinRssi());
        return item;
    }
}
