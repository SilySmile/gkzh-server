package com.gkzh.app.controller.zycck;

import com.gkzh.app.service.ZycckPrintService;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zycck.domain.ZycckPrintTask;
import com.gkzh.zycck.dto.ZycckPrintTaskView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/** zycck 报告打印接口。 */
@RestController
public class ZycckPrintController extends FrontBaseController {
    private final ZycckPrintService printService;

    public ZycckPrintController(ZycckPrintService printService) {
        this.printService = printService;
    }

    /** 学生选择打印机后打印当前职业探索报告。 */
    @PostMapping("/api/zycck/records/{recordId}/print")
    public AjaxResult print(@PathVariable Long recordId, @RequestBody Map<String, Object> body) throws IOException {
        Long printerId = id(body.get("printerId"));
        return AjaxResult.success(toView(printService.print(recordId, getCurrentStudent().getUserId(), printerId)));
    }

    /** 学生查看自己提交的打印任务状态。 */
    @GetMapping("/api/zycck/print-tasks/{taskId}")
    public AjaxResult task(@PathVariable Long taskId) {
        ZycckPrintTask task = printService.findTask(taskId);
        if (!ownsTask(task)) throw new ServiceException("打印任务不存在");
        return AjaxResult.success(toView(printService.refreshTask(taskId)));
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
        // 通过报告服务的数据权限校验，避免在控制器重复引入记录 mapper。
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

    private static Long id(Object value) {
        if (value == null) throw new ServiceException("缺少打印机编号");
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new ServiceException("打印机编号格式错误");
        }
    }
}
