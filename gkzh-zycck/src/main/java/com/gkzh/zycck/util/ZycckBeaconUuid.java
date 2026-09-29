package com.gkzh.zycck.util;

import java.util.Locale;
import java.util.regex.Pattern;

/** 把连续 32 位或标准带连字符的 iBeacon UUID 统一为标准格式。 */
public final class ZycckBeaconUuid {
    private static final Pattern HEX_32 = Pattern.compile("[0-9a-fA-F]{32}");
    private static final Pattern CANONICAL = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private ZycckBeaconUuid() {
    }

    /** 无效格式返回 null；有效格式返回大写的 8-4-4-4-12 UUID。 */
    public static String normalize(String value) {
        if (value == null) return null;
        String raw = value.trim();
        if (CANONICAL.matcher(raw).matches()) return raw.toUpperCase(Locale.ROOT);
        if (!HEX_32.matcher(raw).matches()) return null;
        return (raw.substring(0, 8) + "-" + raw.substring(8, 12) + "-"
                + raw.substring(12, 16) + "-" + raw.substring(16, 20) + "-"
                + raw.substring(20)).toUpperCase(Locale.ROOT);
    }
}
