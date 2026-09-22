package com.gkzh.app.controller.zycck;

import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zycck.domain.ZycckPrinter;
import com.gkzh.zycck.dto.ZycckPrinterView;
import com.gkzh.zycck.service.ZycckPrinterService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** zycck 用户端和工作人员端打印机接口。 */
@RestController
public class ZycckPrinterController {
    private final ZycckPrinterService printerService;

    public ZycckPrinterController(ZycckPrinterService printerService) {
        this.printerService = printerService;
    }

    /** 用户生成报告后获取可选择的打印机。 */
    @GetMapping("/api/zycck/printers")
    public AjaxResult availablePrinters() {
        return AjaxResult.success(printerService.listEnabledPrinters().stream()
                .map(ZycckPrinterView::from).collect(Collectors.toList()));
    }

    /** 工作人员端查看本地打印机池。 */
    @GetMapping("/api/staff/zycck/printers")
    public AjaxResult listPrinters() {
        return AjaxResult.success(toViews(printerService.listAllPrinters()));
    }

    /** 从汉印云端同步已绑定设备。 */
    @PostMapping("/api/staff/zycck/printers/sync")
    public AjaxResult syncPrinters() {
        return AjaxResult.success(toViews(printerService.syncCloudPrinters()));
    }

    /** 将设备绑定到汉印云端并保存本地业务名称。 */
    @PostMapping("/api/staff/zycck/printers/bind")
    public AjaxResult bindPrinter(@RequestBody Map<String, Object> body) {
        String equipmentSn = text(body.get("equipmentSn"));
        String equipmentSecret = text(body.get("equipmentSecret"));
        String printerName = text(body.get("printerName"));
        return AjaxResult.success(ZycckPrinterView.from(
                printerService.bindPrinter(equipmentSn, equipmentSecret, printerName)));
    }

    /** 刷新单台打印机的在线状态。 */
    @PostMapping("/api/staff/zycck/printers/{id}/status")
    public AjaxResult refreshStatus(@PathVariable Long id) {
        return AjaxResult.success(ZycckPrinterView.from(printerService.refreshPrinterStatus(id)));
    }

    /** 解绑云端设备并禁用本地记录，保留历史打印任务。 */
    @DeleteMapping("/api/staff/zycck/printers/{id}")
    public AjaxResult unbindPrinter(@PathVariable Long id) {
        return AjaxResult.success(ZycckPrinterView.from(printerService.unbindPrinter(id)));
    }

    private List<ZycckPrinterView> toViews(List<ZycckPrinter> printers) {
        return printers.stream().map(ZycckPrinterView::from).collect(Collectors.toList());
    }

    private String text(Object value) {
        if (value == null) return null;
        String result = String.valueOf(value).trim();
        return result.isEmpty() ? null : result;
    }
}
