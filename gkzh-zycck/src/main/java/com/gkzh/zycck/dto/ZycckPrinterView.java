package com.gkzh.zycck.dto;

import com.gkzh.zycck.domain.ZycckPrinter;
import lombok.Data;

import java.util.Date;

/** 对前端公开的打印机信息，不包含 equipment_secret。 */
@Data
public class ZycckPrinterView {
    /** 本地打印机编号。 */
    private Long printerId;
    /** 管理员设置的业务名称。 */
    private String printerName;
    /** 汉印云开放平台中的设备名称。 */
    private String cloudName;
    /** 汉印云打印设备序列号 SN。 */
    private String equipmentSn;
    /** 打印机型号名称。 */
    private String modelName;
    /** 汉印云设备状态码。 */
    private Integer status;
    /** 由设备状态码转换后的中文状态名称。 */
    private String statusText;
    /** 本系统启用状态：0 启用，1 禁用。 */
    private String enabled;
    /** 汉印云绑定状态：0 已绑定，1 未绑定。 */
    private String boundStatus;
    /** 当前设备是否满足已启用、已绑定且在线三个打印条件。 */
    private boolean canPrint;
    /** 最近一次查询设备状态的时间。 */
    private Date lastStatusSyncTime;
    /** 最近一次同步汉印云设备列表的时间。 */
    private Date lastSyncTime;

    /** 将打印机实体转换为不包含设备密钥的前端返回对象。 */
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

    /** 将汉印云设备状态码转换为 Web 和小程序可直接展示的中文名称。 */
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
