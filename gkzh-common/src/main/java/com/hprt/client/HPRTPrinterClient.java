package com.hprt.client;

import com.hprt.config.HPRTConfig;
import com.hprt.domain.HPRTPrinter;
import com.hprt.domain.HPRTResult;
import com.hprt.utils.HttpUtils;

import java.util.List;
import java.util.Map;

/** 汉印云打印 OpenAPI 客户端。 */
public class HPRTPrinterClient {
    private static final String BASE_URL = "https://openapi.hprtcloud.com/api/v1/";
    private final HPRTConfig hprtConfig;

    public HPRTPrinterClient(HPRTConfig hprtConfig) {
        this.hprtConfig = hprtConfig;
    }

    public HPRTResult<Void> addPrinter(List<HPRTPrinter> printers) {
        Map<String, Object> body = this.hprtConfig.build();
        body.put("items", printers);
        return HttpUtils.post(BASE_URL + "bindEquipment", body);
    }

    public HPRTResult<List<HPRTPrinter>> pagePrinter(int page, int size) {
        Map<String, Object> body = this.hprtConfig.build();
        body.put("page", page);
        body.put("size", size);
        return HttpUtils.post(BASE_URL + "equipmentList", body);
    }

    public HPRTResult<List<HPRTPrinter>> queryPrinterStatus(List<String> snList) {
        Map<String, Object> body = this.hprtConfig.build();
        body.put("items", snList);
        return HttpUtils.post(BASE_URL + "equipmentStatus", body);
    }

    public HPRTResult<String> sendPrinterTask(String equipmentSn, String content, String printType, String orderNo) {
        Map<String, Object> body = this.hprtConfig.build();
        body.put("equipment_sn", equipmentSn);
        body.put("content", content);
        body.put("print_type", printType);
        body.put("order_no", orderNo);
        return HttpUtils.post(BASE_URL + "printTask", body);
    }

    public HPRTResult<Void> clearPrinterTask(String equipmentSn) {
        Map<String, Object> body = this.hprtConfig.build();
        body.put("equipment_sn", equipmentSn);
        return HttpUtils.post(BASE_URL + "clearTask", body);
    }

    public HPRTResult<Void> cancelPrinterTask(String equipmentSn, String printId) {
        Map<String, Object> body = this.hprtConfig.build();
        body.put("equipment_sn", equipmentSn);
        body.put("print_id", printId);
        return HttpUtils.post(BASE_URL + "cancelPrinterTask", body);
    }

    public HPRTResult<Integer> queryPrinterTask(String equipmentSn, String printId) {
        Map<String, Object> body = this.hprtConfig.build();
        body.put("equipment_sn", equipmentSn);
        body.put("print_id", printId);
        return HttpUtils.post(BASE_URL + "getPrintTaskStatus", body);
    }

    public HPRTResult<Void> reprintTask(String equipmentSn, String printId) {
        Map<String, Object> body = this.hprtConfig.build();
        body.put("equipment_sn", equipmentSn);
        body.put("print_id", printId);
        return HttpUtils.post(BASE_URL + "againPrintTask", body);
    }

    public HPRTResult<Void> unbindPrinter(List<String> snList) {
        Map<String, Object> body = this.hprtConfig.build();
        body.put("items", snList);
        return HttpUtils.post(BASE_URL + "unbindEquipment", body);
    }

    public HPRTResult<Void> setTaskTimeOut(String equipmentSn, Integer timeNum) {
        Map<String, Object> body = this.hprtConfig.build();
        body.put("equipment_sn", equipmentSn);
        body.put("time_num", timeNum);
        return HttpUtils.post(BASE_URL + "setTaskTimeout", body);
    }
}
