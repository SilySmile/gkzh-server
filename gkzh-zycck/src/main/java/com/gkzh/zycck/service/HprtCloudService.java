package com.gkzh.zycck.service;

import com.gkzh.zycck.config.HprtCloudProperties;
import com.hprt.client.HPRTPrinterClient;
import com.hprt.domain.HPRTPrinter;
import com.hprt.domain.HPRTResult;
import org.springframework.stereotype.Service;

import java.util.List;

/** 未来职业模块使用的汉印云打印服务适配层。 */
@Service
public class HprtCloudService {
    private final HPRTPrinterClient printerClient;
    private final HprtCloudProperties properties;

    public HprtCloudService(HPRTPrinterClient printerClient, HprtCloudProperties properties) {
        this.printerClient = printerClient;
        this.properties = properties;
    }

    public HPRTResult<List<HPRTPrinter>> pagePrinter(int page, int size) {
        return printerClient.pagePrinter(page, size);
    }

    public HPRTResult<Void> addPrinter(List<HPRTPrinter> printers) {
        return printerClient.addPrinter(printers);
    }

    public HPRTResult<List<HPRTPrinter>> queryPrinterStatus(List<String> equipmentSnList) {
        return printerClient.queryPrinterStatus(equipmentSnList);
    }

    public HPRTResult<String> sendPrinterTask(String equipmentSn, String content, String orderNo) {
        return printerClient.sendPrinterTask(equipmentSn, content, properties.getPrintType(), orderNo);
    }

    /** 根据设备 SN 和云端任务编号查询实际打印状态。 */
    public HPRTResult<Integer> queryPrinterTask(String equipmentSn, String printId) {
        return printerClient.queryPrinterTask(equipmentSn, printId);
    }

    /** 取消尚未打印的汉印云任务，重试打印前会先调用此方法。 */
    public HPRTResult<Void> cancelPrinterTask(String equipmentSn, String printId) {
        return printerClient.cancelPrinterTask(equipmentSn, printId);
    }

    public HPRTResult<Void> reprintTask(String equipmentSn, String printId) {
        return printerClient.reprintTask(equipmentSn, printId);
    }

    public HPRTResult<Void> unbindPrinter(List<String> equipmentSnList) {
        return printerClient.unbindPrinter(equipmentSnList);
    }
}
