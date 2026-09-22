package com.hprt.utils;

import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HPRTContentSerializationTest {

    @Test
    void preservesTsplBitmapCharactersInJsonContent() {
        StringBuilder bitmap = new StringBuilder(256);
        for (int value = 0; value <= 255; value++) bitmap.append((char) value);
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("content", "BITMAP 0,0,32,8,0," + bitmap + "\r\nPRINT 1\r\n");

        String json = JSONUtil.toJsonStr(request);
        String restored = JSONUtil.parseObj(json).getStr("content");

        assertEquals(request.get("content"), restored);
    }
}
