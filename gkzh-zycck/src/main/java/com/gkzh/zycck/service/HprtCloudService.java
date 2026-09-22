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

    public HPRTResult<Integer> queryPrinterTask(String equipmentSn, String printId) {
        return printerClient.queryPrinterTask(equipmentSn, printId);
    }

    public HPRTResult<Void> reprintTask(String equipmentSn, String printId) {
        return printerClient.reprintTask(equipmentSn, printId);
    }

    public HPRTResult<Void> unbindPrinter(List<String> equipmentSnList) {
        return printerClient.unbindPrinter(equipmentSnList);
    }
}
