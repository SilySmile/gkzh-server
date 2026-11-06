package com.hprt.domain;

import cn.hutool.core.lang.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HPRTResultTest {

    @Test
    void parsesPrinterListReturnedByCloudApi() {
        String json = "{\"code\":200,\"status\":true,\"msg\":\"请求成功\",\"data\":{\"list\":[{\"equipment_sn\":\"SN001\",\"name\":\"一号机\",\"status\":1}]}}";

        HPRTResult<List<HPRTPrinter>> result = new HPRTResult<>(json, new TypeReference<List<HPRTPrinter>>() { });

        assertTrue(result.getStatus());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().get(0) instanceof HPRTPrinter);
        assertEquals("SN001", result.getData().get(0).getEquipment_sn());
    }

    @Test
    void parsesPrintIdAndTaskStatusReturnedByCloudApi() {
        HPRTResult<String> submitted = new HPRTResult<>(
                "{\"code\":200,\"status\":true,\"data\":{\"print_id\":\"task-1\"}}",
                new TypeReference<String>() { });
        HPRTResult<Integer> completed = new HPRTResult<>(
                "{\"code\":200,\"status\":true,\"data\":{\"status\":1}}",
                new TypeReference<Integer>() { });

        assertEquals("task-1", submitted.getData());
        assertEquals(1, completed.getData());
    }
}
