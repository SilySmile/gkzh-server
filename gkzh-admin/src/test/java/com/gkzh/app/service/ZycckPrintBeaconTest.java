package com.gkzh.app.service;

import com.gkzh.common.exception.ServiceException;
import com.gkzh.zycck.domain.ZycckBeaconConfig;
import com.gkzh.zycck.dto.ZycckBeaconEvidence;
import com.gkzh.zycck.dto.ZycckBeaconRule;
import com.gkzh.zycck.dto.ZycckBeaconSettingsView;
import com.gkzh.zycck.mapper.ZycckBeaconConfigMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Web 数据库配置驱动的多蓝牙信标打印范围校验。 */
class ZycckPrintBeaconTest {

    /** 没有保存过规则时默认关闭信标限制，管理页获得可编辑的默认值。 */
    @Test
    void missingConfigurationDefaultsToDisabled() {
        ZycckBeaconConfigService config = config(mock(ZycckBeaconConfigMapper.class));
        ZycckBeaconSettingsView defaults = config.get();
        assertFalse(defaults.isBeaconEnabled());
        assertTrue(defaults.getBeacons().isEmpty());
        assertEquals(3.0D, defaults.getDefaultMaxDistance());
        assertDoesNotThrow(() -> validate(service(config), null));
    }

    /** Web 保存后立即使用数据库规则；第二个信标及其独立阈值也能通过。 */
    @Test
    void savedConfigurationAppliesToAnyBeacon() {
        AtomicReference<ZycckBeaconConfig> stored = new AtomicReference<>();
        ZycckBeaconConfigMapper mapper = mock(ZycckBeaconConfigMapper.class);
        when(mapper.selectById(1L)).thenAnswer(invocation -> stored.get());
        when(mapper.insert(any())).thenAnswer(invocation -> {
            stored.set(invocation.getArgument(0));
            return 1;
        });
        ZycckBeaconConfigService config = config(mapper);
        ZycckBeaconSettingsView input = config.get();
        input.setBeaconEnabled(true);
        input.setBeacons(Arrays.asList(
                beacon("AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA", 1, 1, 1.0D, -60),
                beacon("BBBBBBBB-BBBB-BBBB-BBBB-BBBBBBBBBBBB", 2, 2, 5.0D, -95)));
        assertEquals(2, config.save(input).getBeacons().size());

        ZycckPrintService print = service(config);
        assertDoesNotThrow(() -> validate(print,
                evidence("BBBBBBBB-BBBB-BBBB-BBBB-BBBBBBBBBBBB", 2, 2, -80, 3.0D)));
        assertThrows(ServiceException.class, () -> validate(print,
                evidence("BBBBBBBB-BBBB-BBBB-BBBB-BBBBBBBBBBBB", 2, 9, -80, 3.0D)));
        assertThrows(ServiceException.class, () -> validate(print,
                evidence("BBBBBBBB-BBBB-BBBB-BBBB-BBBBBBBBBBBB", 2, 2, -96, 3.0D)));
    }

    /** 连续 32 位 UUID 保存为标准格式，手机上报原格式仍能通过校验。 */
    @Test
    void acceptsCompactBeaconUuid() {
        String compact = "000102030405060708090A0B0C0D0E0F";
        String canonical = "00010203-0405-0607-0809-0A0B0C0D0E0F";
        AtomicReference<ZycckBeaconConfig> stored = new AtomicReference<>();
        ZycckBeaconConfigMapper mapper = mock(ZycckBeaconConfigMapper.class);
        when(mapper.selectById(1L)).thenAnswer(invocation -> stored.get());
        when(mapper.insert(any())).thenAnswer(invocation -> {
            stored.set(invocation.getArgument(0));
            return 1;
        });
        ZycckBeaconConfigService config = config(mapper);
        ZycckBeaconSettingsView input = config.get();
        input.setBeaconEnabled(true);
        input.setBeacons(Arrays.asList(beacon(compact, 1, 2, 3.0D, -90)));
        assertEquals(canonical, config.save(input).getBeacons().get(0).getUuid());
        assertDoesNotThrow(() -> validate(service(config), evidence(compact, 1, 2, -80, 1.0D)));
    }

    /** 现场 BLE 广播可直接作为打印依据：原始字节须对应配置，距离由服务端重新估算。 */
    @Test
    void validatesObservedBleAdvertisementAndRssi() {
        String uuid = "00010203-0405-0607-0809-0A0B0C0D0E0F";
        String raw = "4C000215000102030405060708090A0B0C0D0E0F3412CDAB0016184587CAED700F1234ABCD0603E862";
        ZycckBeaconConfig row = new ZycckBeaconConfig();
        row.setConfigId(1L);
        row.setBeaconEnabled("1");
        row.setBeaconsJson("[{\"uuid\":\"" + uuid + "\",\"major\":13330,\"minor\":52651,\"minRssi\":-70}]");
        row.setDefaultMaxDistance(3.0D);
        row.setDefaultMinRssi(-90);
        row.setMaxAgeSeconds(15);
        row.setScanTimeoutSeconds(10);
        ZycckBeaconConfigMapper mapper = mock(ZycckBeaconConfigMapper.class);
        when(mapper.selectById(1L)).thenReturn(row);
        ZycckPrintService print = service(config(mapper));
        ZycckBeaconEvidence found = evidence(uuid, 13330, 52651, -35, 0D);
        found.setSource("ble");
        found.setAdvertisData(raw);
        found.setAccuracy(99D);
        assertDoesNotThrow(() -> validate(print, found));
        assertEquals(Math.pow(10D, (35D - 60D) / 20D), found.getAccuracy(), 0.0001D);

        found.setRssi(-80);
        assertThrows(ServiceException.class, () -> validate(print, found));
        found.setRssi(-35);
        found.setMinor(1);
        assertThrows(ServiceException.class, () -> validate(print, found));
        found.setMinor(52651);
        found.setAdvertisData("4C000215");
        assertThrows(ServiceException.class, () -> validate(print, found));
        found.setAdvertisData(raw);
        found.setObservedAt(System.currentTimeMillis() - 60000);
        assertThrows(ServiceException.class, () -> validate(print, found));
    }

    /** 每个信标可分别设置 1 米参考值 A 和环境因子 n，距离超过该信标上限时拒绝。 */
    @Test
    void bleDistanceUsesPerBeaconCalibration() {
        String uuid = "00010203-0405-0607-0809-0A0B0C0D0E0F";
        ZycckBeaconConfig row = new ZycckBeaconConfig();
        row.setConfigId(1L);
        row.setBeaconEnabled("1");
        row.setBeaconsJson("[{\"uuid\":\"" + uuid + "\",\"major\":1,\"minor\":2,"
                + "\"maxDistance\":2,\"referenceRssi\":60,\"pathLossExponent\":2}]");
        row.setDefaultMaxDistance(3D);
        row.setDefaultMinRssi(-90);
        row.setMaxAgeSeconds(15);
        row.setScanTimeoutSeconds(10);
        ZycckBeaconConfigMapper mapper = mock(ZycckBeaconConfigMapper.class);
        when(mapper.selectById(1L)).thenReturn(row);
        ZycckPrintService print = service(config(mapper));
        ZycckBeaconEvidence found = evidence(uuid, 1, 2, -70, 0D);
        found.setSource("ble");
        found.setAdvertisData("4C000215000102030405060708090A0B0C0D0E0F00010002C5");
        assertThrows(ServiceException.class, () -> validate(print, found));
        row.setBeaconsJson("[{\"uuid\":\"" + uuid + "\",\"major\":1,\"minor\":2,"
                + "\"maxDistance\":2,\"referenceRssi\":60,\"pathLossExponent\":4}]");
        ZycckPrintService adjusted = service(config(mapper));
        assertDoesNotThrow(() -> validate(adjusted, found));
        assertEquals(Math.pow(10D, 0.25D), found.getAccuracy(), 0.0001D);
    }

    /** 已启用时拒绝空列表和格式错误的 UUID，避免配置后所有学生无法打印。 */
    @Test
    void invalidWebConfigurationIsRejected() {
        ZycckBeaconConfigService config = config(mock(ZycckBeaconConfigMapper.class));
        ZycckBeaconSettingsView input = config.get();
        input.setBeaconEnabled(true);
        assertThrows(ServiceException.class, () -> config.save(input));
        input.setBeacons(Arrays.asList(beacon("不是UUID", 1, 1, null, null)));
        assertThrows(ServiceException.class, () -> config.save(input));
        ZycckBeaconRule badCalibration = beacon("00010203-0405-0607-0809-0A0B0C0D0E0F", 1, 2, null, null);
        badCalibration.setReferenceRssi(-60D);
        input.setBeacons(Arrays.asList(badCalibration));
        assertThrows(ServiceException.class, () -> config.save(input));
        badCalibration.setReferenceRssi(60D);
        badCalibration.setPathLossExponent(0D);
        assertThrows(ServiceException.class, () -> config.save(input));
    }

    /** 重复读取命中 Redis，Web 保存后失效缓存并读取新的数据库配置。 */
    @Test
    void cachesReadsAndEvictsAfterSave() {
        ZycckBeaconConfigMapper mapper = mock(ZycckBeaconConfigMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        AtomicReference<String> cached = new AtomicReference<>();
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenAnswer(invocation -> cached.get());
        doAnswer(invocation -> {
            cached.set(invocation.getArgument(1));
            return null;
        }).when(values).set(anyString(), anyString(), anyLong(), eq(TimeUnit.SECONDS));
        when(redis.delete(anyString())).thenAnswer(invocation -> {
            cached.set(null);
            return true;
        });
        ZycckBeaconConfig row = new ZycckBeaconConfig();
        row.setConfigId(1L);
        row.setBeaconEnabled("0");
        row.setBeaconsJson("[]");
        row.setDefaultMaxDistance(3.0D);
        row.setDefaultMinRssi(-90);
        row.setMaxAgeSeconds(15);
        row.setScanTimeoutSeconds(10);
        when(mapper.selectById(1L)).thenReturn(row);
        ZycckBeaconConfigService config = new ZycckBeaconConfigService(mapper, redis);
        assertEquals(-90, config.get().getDefaultMinRssi());
        assertEquals(-90, config.get().getDefaultMinRssi());
        verify(mapper, times(1)).selectById(1L);

        ZycckBeaconSettingsView changed = config.get();
        changed.setDefaultMinRssi(-80);
        config.save(changed);
        assertEquals(-80, config.get().getDefaultMinRssi());
        verify(mapper, times(3)).selectById(1L);
    }

    /** Redis 故障期间的连续请求使用短时本地快照，不逐次访问数据库。 */
    @Test
    void redisOutageUsesShortLocalFallback() {
        ZycckBeaconConfigMapper mapper = mock(ZycckBeaconConfigMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.opsForValue()).thenThrow(new IllegalStateException("Redis unavailable"));
        ZycckBeaconConfigService config = new ZycckBeaconConfigService(mapper, redis);
        assertFalse(config.get().isBeaconEnabled());
        assertFalse(config.get().isBeaconEnabled());
        verify(mapper, times(1)).selectById(1L);
    }

    /** 测试默认使用不可用的 Redis，业务应回退数据库或短时本地快照。 */
    private ZycckBeaconConfigService config(ZycckBeaconConfigMapper mapper) {
        return new ZycckBeaconConfigService(mapper, mock(StringRedisTemplate.class));
    }

    /** 创建单个信标规则。 */
    private ZycckBeaconRule beacon(String uuid, int major, int minor,
                                   Double maxDistance, Integer minRssi) {
        ZycckBeaconRule rule = new ZycckBeaconRule();
        rule.setUuid(uuid);
        rule.setMajor(major);
        rule.setMinor(minor);
        rule.setMaxDistance(maxDistance);
        rule.setMinRssi(minRssi);
        return rule;
    }

    /** 创建手机最近一次扫描到的信标证据。 */
    private ZycckBeaconEvidence evidence(String uuid, int major, int minor,
                                         int rssi, double accuracy) {
        ZycckBeaconEvidence evidence = new ZycckBeaconEvidence();
        evidence.setUuid(uuid);
        evidence.setMajor(major);
        evidence.setMinor(minor);
        evidence.setRssi(rssi);
        evidence.setAccuracy(accuracy);
        evidence.setObservedAt(System.currentTimeMillis());
        return evidence;
    }

    /** 本测试只验证信标规则，其余打印依赖无需实例化。 */
    private ZycckPrintService service(ZycckBeaconConfigService config) {
        return new ZycckPrintService(null, null, null, null, null,
                null, null, null, null, config);
    }

    /** 调用服务端内部校验并还原真实业务异常。 */
    private void validate(ZycckPrintService service, ZycckBeaconEvidence evidence) {
        try {
            Method method = ZycckPrintService.class.getDeclaredMethod("validateBeacon", ZycckBeaconEvidence.class);
            method.setAccessible(true);
            method.invoke(service, evidence);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof RuntimeException) {
                throw (RuntimeException) exception.getCause();
            }
            throw new AssertionError(exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
