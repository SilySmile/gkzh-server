package com.gkzh.app.controller.zycck;

import com.gkzh.app.service.ZycckPrintService;
import com.gkzh.common.annotation.Anonymous;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zycck.domain.ZycckPrintTask;
import com.gkzh.zycck.dto.ZycckPrintRequest;
import com.gkzh.zycck.dto.ZycckPrintTaskView;
import com.gkzh.zycck.service.ZycckPrinterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** zycck 报告打印接口。 */
@RestController
public class ZycckPrintController extends FrontBaseController {
    private static final Logger log = LoggerFactory.getLogger(ZycckPrintController.class);
    private final ZycckPrintService printService;
    private final ZycckPrinterService printerService;

    public ZycckPrintController(ZycckPrintService printService, ZycckPrinterService printerService) {
        this.printService = printService;
        this.printerService = printerService;
    }

    /** 学生选择打印机后打印当前职业探索报告。 */
    @PostMapping("/api/zycck/records/{recordId}/print")
    public AjaxResult print(@PathVariable Long recordId, @RequestBody ZycckPrintRequest request) throws IOException {
        if (request == null || request.getPrinterId() == null) throw new ServiceException("缺少打印机编号");
        return AjaxResult.success(toView(printService.print(
                recordId, getCurrentStudent().getUserId(), request.getPrinterId(), request.getBeacon())));
    }

    /** 打开打印面板前获取一次打印与蓝牙信标要求。 */
    @GetMapping("/api/zycck/records/{recordId}/print-eligibility")
    public AjaxResult printEligibility(@PathVariable Long recordId) {
        return AjaxResult.success(printService.eligibility(recordId, getCurrentStudent().getUserId()));
    }

    /** 学生查看自己提交的打印任务状态。 */
    @GetMapping("/api/zycck/print-tasks/{taskId}")
    public AjaxResult task(@PathVariable Long taskId) {
        ZycckPrintTask task = printService.findTask(taskId);
        if (!ownsTask(task)) throw new ServiceException("打印任务不存在");
        return AjaxResult.success(toView(printService.refreshTask(taskId)));
    }

    /**
     * 汉印云消息推送回调。
     * 汉印以 application/x-www-form-urlencoded 推送，收到后必须在 3 秒内返回固定确认内容；
     * 同时放开 GET，兼容开放平台保存回调地址时的连通性校验。
     */
    @Anonymous
    @RequestMapping(value = "/api/zycck/hprt/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, String> hprtCallback(@RequestParam Map<String, String> form) {
        try {
            Integer messageType = integer(form.get("message_type"));
            if (messageType != null && messageType == 1) {
                printService.updateTaskFromCallback(
                        form.get("print_id"),
                        integer(form.get("status")),
                        longValue(form.get("print_time")));
            } else if (messageType != null && messageType == 2) {
                printerService.updateStatusFromCallback(form.get("equipment_sn"), integer(form.get("status")));
            }
        } catch (RuntimeException ex) {
            // 始终按协议应答，日志用于排查推送数据或数据库同步异常。
            log.warn("处理汉印云回调失败，messageType={}，printId={}，equipmentSn={}",
                    form.get("message_type"), form.get("print_id"), form.get("equipment_sn"), ex);
        }
        Map<String, String> response = new LinkedHashMap<>();
        response.put("msg", "ok");
        response.put("sign", "hprtcloud");
        return response;
    }

    /** 工作人员查看打印历史，可按打印机、记录或状态筛选。 */
    @GetMapping("/api/staff/zycck/print-tasks")
    public AjaxResult list(@RequestParam(required = false) Long printerId,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) Long recordId) {
        return AjaxResult.success(printService.listTasks(printerId, status, recordId).stream()
                .map(ZycckPrintTaskView::from).collect(Collectors.toList()));
    }

    /** 工作人员重新打印历史任务。 */
    @PostMapping("/api/staff/zycck/print-tasks/{taskId}/reprint")
    public AjaxResult reprint(@PathVariable Long taskId) throws IOException {
        return AjaxResult.success(toView(printService.reprint(taskId)));
    }

    /** 工作人员主动同步单个云端任务状态。 */
    @PostMapping("/api/staff/zycck/print-tasks/{taskId}/status")
    public AjaxResult refreshStatus(@PathVariable Long taskId) {
        return AjaxResult.success(toView(printService.refreshTask(taskId)));
    }

    private boolean ownsTask(ZycckPrintTask task) {
        if (task.getRecordId() == null || getCurrentStudent() == null) return false;
        try {
            printService.verifyTaskOwner(task.getTaskId(), getCurrentStudent().getUserId());
            return true;
        } catch (ServiceException e) {
            return false;
        }
    }

    private ZycckPrintTaskView toView(ZycckPrintTask task) {
        return ZycckPrintTaskView.from(task);
    }

    private static Integer integer(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long longValue(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
