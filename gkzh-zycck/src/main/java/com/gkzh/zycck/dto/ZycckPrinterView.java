package com.gkzh.zycck.dto;

import com.gkzh.zycck.domain.ZycckPrinter;
import lombok.Data;

import java.util.Date;

/** 对前端公开的打印机信息，不包含 equipment_secret。 */
@Data
public class ZycckPrinterView {
    private Long printerId;
    private String printerName;
    private String cloudName;
    private String equipmentSn;
    private String modelName;
    private Integer status;
    private String statusText;
    private String enabled;
    private String boundStatus;
    private boolean canPrint;
    private Date lastStatusSyncTime;
    private Date lastSyncTime;

    public static ZycckPrinterView from(ZycckPrinter printer) {
        ZycckPrinterView view = new ZycckPrinterView();
        view.printerId = printer.getPrinterId();
        view.printerName = printer.getPrinterName();
        view.cloudName = printer.getCloudName();
        view.equipmentSn = printer.getEquipmentSn();
        view.modelName = printer.getModelName();
        view.status = printer.getStatus();
        view.statusText = statusText(printer.getStatus());
        view.enabled = printer.getEnabled();
        view.boundStatus = printer.getBoundStatus();
        view.canPrint = "0".equals(printer.getEnabled())
                && "0".equals(printer.getBoundStatus())
                && Integer.valueOf(1).equals(printer.getStatus());
        view.lastStatusSyncTime = printer.getLastStatusSyncTime();
        view.lastSyncTime = printer.getLastSyncTime();
        return view;
    }

    private static String statusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "离线";
            case 1: return "在线";
            case 2: return "无 USB 插入";
            case 3: return "缺纸";
            case 4: return "切刀错误";
            case 5: return "不可恢复错误";
            case 6: return "脱机";
            case 7: return "开盖";
            case 8: return "高温";
            case 9: return "耗材非法";
            case 10: return "耗材用尽";
            case 13: return "切刀错误";
            case 14: return "低压错误";
            case 22: return "打印中";
            default: return "状态" + status;
        }
    }

}
