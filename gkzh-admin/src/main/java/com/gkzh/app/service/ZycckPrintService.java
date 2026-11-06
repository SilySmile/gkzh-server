package com.gkzh.app.service;

import cn.hutool.core.img.ImgUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.mapper.week.GkzhActivityGameMapper;
import com.gkzh.activity.mapper.week.GkzhActivityWeekInstanceMapper;
import com.gkzh.common.config.GkzhConfig;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.common.utils.sign.Base64;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.zycck.domain.ZycckPrintTask;
import com.gkzh.zycck.domain.ZycckPrinter;
import com.gkzh.zycck.dto.ZycckBeaconEvidence;
import com.gkzh.zycck.dto.ZycckBeaconRuleView;
import com.gkzh.zycck.dto.ZycckBeaconRule;
import com.gkzh.zycck.dto.ZycckBeaconSettingsView;
import com.gkzh.zycck.dto.ZycckPrintEligibilityView;
import com.gkzh.zycck.mapper.ZycckPrintTaskMapper;
import com.gkzh.zycck.service.HprtCloudService;
import com.gkzh.zycck.service.ZycckPrinterService;
import com.gkzh.zycck.util.ZycckBeaconUuid;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.hprt.domain.HPRTResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * zycck 报告打印流程：报告 PDF -> 76mm x 130mm 位图 -> TSPL -> 汉印云任务。
 *
 * <p>报告 PDF 当前是按 76mm x 130mm 分页生成的，因此多页报告会在同一个云任务中
 * 依次发送多张同尺寸标签。</p>
 */
@Service
public class ZycckPrintService {
    /**
     * 本地打印任务状态，字符串需与 Web 端状态标签保持一致。
     */
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_SUBMITTED = "submitted";
    private static final String STATUS_PRINTING = "printing";
    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_FAILED = "failed";
    private static final String STATUS_CANCELLED = "cancelled";

    /**
     * 标签纸 76mm x 130mm，按汉印常见 203dpi 生成位图。
     */
    private static final int DPI = 203;
    private static final int IMAGE_WIDTH = Math.round(76f / 25.4f * DPI);
    private static final int IMAGE_HEIGHT = Math.round(130f / 25.4f * DPI);
    private static final int IMAGE_WIDTH_BYTES = (IMAGE_WIDTH + 7) / 8;
    private static final int BLACK_THRESHOLD = 180;

    private final ZycckPrintTaskMapper taskMapper;
    private final ZycckRecordMapper recordMapper;
    private final ZycckPrinterService printerService;
    private final HprtCloudService hprtCloudService;
    private final ZycckReportPdfService reportPdfService;
    private final GkzhStudentMapper studentMapper;
    private final GkzhSchoolMapper schoolMapper;
    private final GkzhActivityWeekInstanceMapper instanceMapper;
    private final GkzhActivityGameMapper gameMapper;
    private final ZycckBeaconConfigService beaconConfigService;

    public ZycckPrintService(ZycckPrintTaskMapper taskMapper,
                             ZycckRecordMapper recordMapper,
                             ZycckPrinterService printerService,
                             HprtCloudService hprtCloudService,
                             ZycckReportPdfService reportPdfService,
                             GkzhStudentMapper studentMapper,
                             GkzhSchoolMapper schoolMapper,
                             GkzhActivityWeekInstanceMapper instanceMapper,
                             GkzhActivityGameMapper gameMapper,
                             ZycckBeaconConfigService beaconConfigService) {
        this.taskMapper = taskMapper;
        this.recordMapper = recordMapper;
        this.printerService = printerService;
        this.hprtCloudService = hprtCloudService;
        this.reportPdfService = reportPdfService;
        this.studentMapper = studentMapper;
        this.schoolMapper = schoolMapper;
        this.instanceMapper = instanceMapper;
        this.gameMapper = gameMapper;
        this.beaconConfigService = beaconConfigService;
    }

    /**
     * 学生选择打印机后提交打印。
     */
    @Transactional(noRollbackFor = ServiceException.class)
    public ZycckPrintTask print(Long recordId, Long userId, Long printerId,
                                ZycckBeaconEvidence beacon) throws IOException {
        if (recordId == null) throw new ServiceException("缺少报告记录编号");
        if (userId == null) throw new ServiceException("登录信息已失效");
        ZycckRecord record = ownedRecord(recordId, userId);
        lockUser(userId);
        validateBeacon(beacon);
        ZycckPrinter printer = printablePrinter(printerId);
        prepareForNewTask(userId);
        byte[] pdf = reportPdfService.create(recordId, userId);
        return submit(record, printer, pdf, 0, beacon);
    }

    /**
     * 工作人员重打历史任务；使用报告原所属用户生成报告，避免绕过报告数据权限。
     */
    @Transactional(noRollbackFor = ServiceException.class)
    public ZycckPrintTask reprint(Long taskId) throws IOException {
        ZycckPrintTask oldTask = findTask(taskId);
        if (oldTask.getRecordId() == null) throw new ServiceException("历史任务缺少报告记录");
        ZycckRecord record = recordMapper.selectById(oldTask.getRecordId());
        if (record == null || record.getUserId() == null) throw new ServiceException("报告记录不存在");
        lockUser(record.getUserId());
        ZycckPrinter printer = printablePrinter(oldTask.getPrinterId());
        prepareForNewTask(record.getUserId());
        byte[] pdf = reportPdfService.create(record.getRecordId(), record.getUserId());
        int retry = oldTask.getRetryCount() == null ? 1 : oldTask.getRetryCount() + 1;
        return submit(record, printer, pdf, retry, null);
    }

    /**
     * 打印面板打开前返回一次打印与蓝牙信标规则。
     */
    public ZycckPrintEligibilityView eligibility(Long recordId, Long userId) {
        ownedRecord(recordId, userId);
        boolean printed = taskMapper.selectCount(userTaskQuery(userId).eq("status", STATUS_SUCCESS)) > 0;
        String message = printed ? "每位用户只能成功打印一次" : null;
        ZycckBeaconSettingsView settings = beaconConfigService.get();
        List<ZycckBeaconRuleView> beaconRules = beaconRuleViews(settings);
        ZycckBeaconRuleView firstBeacon = beaconRules.isEmpty() ? null : beaconRules.get(0);
        return ZycckPrintEligibilityView.builder()
                .allowed(!printed)
                .printed(printed)
                .beaconEnabled(settings.isBeaconEnabled())
                .beacons(beaconRules)
                .beaconUuid(firstBeacon == null ? null : firstBeacon.getUuid())
                .beaconMajor(firstBeacon == null ? null : firstBeacon.getMajor())
                .beaconMinor(firstBeacon == null ? null : firstBeacon.getMinor())
                .beaconMaxDistance(firstBeacon == null
                        ? settings.getDefaultMaxDistance() : firstBeacon.getMaxDistance())
                .beaconMinRssi(firstBeacon == null
                        ? settings.getDefaultMinRssi() : firstBeacon.getMinRssi())
                .beaconScanTimeoutSeconds(settings.getScanTimeoutSeconds())
                .beaconMaxAgeSeconds(settings.getMaxAgeSeconds())
                .message(message)
                .build();
    }

    /**
     * 接收汉印云打印任务状态推送并同步本地任务。
     * 汉印当前推送状态：1 已打印，-2 任务取消。
     */
    @Transactional
    public ZycckPrintTask updateTaskFromCallback(String printId, Integer cloudStatus, Long printTimeSeconds) {
        if (!StringUtils.hasText(printId) || cloudStatus == null) {
            return null;
        }
        ZycckPrintTask task = taskMapper.selectOne(new QueryWrapper<ZycckPrintTask>()
                .eq("print_id", printId.trim())
                .orderByDesc("task_id")
                .last("LIMIT 1"));
        if (task == null) {
            return null;
        }
        String nextStatus;
        if (cloudStatus == 1) {
            nextStatus = STATUS_SUCCESS;
        } else if (cloudStatus == -2) {
            nextStatus = STATUS_CANCELLED;
        } else if (cloudStatus == -1) {
            nextStatus = STATUS_FAILED;
        } else {
            nextStatus = STATUS_SUBMITTED;
        }
        // 回调可能重复到达，成功任务不允许被旧的待打印状态覆盖。
        if (STATUS_SUCCESS.equals(task.getStatus()) && !STATUS_SUCCESS.equals(nextStatus)) {
            return task;
        }
        task.setStatus(nextStatus);
        if ((STATUS_FAILED.equals(nextStatus) || STATUS_CANCELLED.equals(nextStatus))
                && !StringUtils.hasText(task.getErrorMessage())) {
            task.setErrorMessage("汉印云端取消打印任务");
        }
        if (STATUS_SUCCESS.equals(nextStatus)) {
            task.setErrorMessage(null);
            task.setPrintTime(printTimeSeconds == null
                    ? DateUtils.getNowDate()
                    : new Date(printTimeSeconds * 1000L));
        } else if (STATUS_CANCELLED.equals(nextStatus)) {
            task.setCancelTime(DateUtils.getNowDate());
        }
        task.setUpdateTime(DateUtils.getNowDate());
        taskMapper.updateById(task);
        return task;
    }

    /**
     * 查询汉印云端任务状态并同步本地状态。
     */
    @Transactional
    public ZycckPrintTask refreshTask(Long taskId) {
        ZycckPrintTask task = findTask(taskId);
        if (!StringUtils.hasText(task.getPrintId())) {
            if (STATUS_FAILED.equals(task.getStatus()) || STATUS_CANCELLED.equals(task.getStatus())) return task;
            throw new ServiceException("任务尚未提交到汉印云端");
        }
        HPRTResult<Integer> result = hprtCloudService.queryPrinterTask(task.getEquipmentSn(), task.getPrintId());
        ensureSuccess(result, "查询打印任务状态失败");
        task.setStatus(mapCloudStatus(result.getData()));
        if (STATUS_SUCCESS.equals(task.getStatus())) {
            task.setPrintTime(DateUtils.getNowDate());
            task.setErrorMessage(null);
        } else if (STATUS_CANCELLED.equals(task.getStatus())) {
            task.setCancelTime(DateUtils.getNowDate());
        }
        task.setUpdateTime(DateUtils.getNowDate());
        taskMapper.updateById(task);
        return task;
    }

    /**
     * 管理端取消任务；取消前必须先以云端状态为准。
     */
    @Transactional(noRollbackFor = ServiceException.class)
    public ZycckPrintTask cancelTask(Long taskId) {
        ZycckPrintTask task = findTask(taskId);
        if (STATUS_SUCCESS.equals(task.getStatus())) throw new ServiceException("任务已成功打印，无法取消");
        if (STATUS_CANCELLED.equals(task.getStatus())) return task;
        if (!StringUtils.hasText(task.getPrintId())) {
            markCancelled(task, "本地任务已取消");
            return task;
        }
        Integer cloudStatus = queryCloudStatus(task);
        if (Integer.valueOf(1).equals(cloudStatus)) {
            markSuccess(task);
            throw new ServiceException("任务已成功打印，无法取消");
        }
        if (Integer.valueOf(0).equals(cloudStatus)) cancelCloudTask(task);
        markCancelled(task, "已取消打印任务");
        return task;
    }

    /**
     * 删除本地打印任务记录，供管理员清理测试数据。
     * 进行中的任务必须先取消，避免本地记录消失后云端仍继续打印。
     */
    @Transactional
    public void deleteTask(Long taskId) {
        ZycckPrintTask task = findTask(taskId);
        if (STATUS_PENDING.equals(task.getStatus())
                || STATUS_SUBMITTED.equals(task.getStatus())
                || STATUS_PRINTING.equals(task.getStatus())) {
            throw new ServiceException("打印任务仍在进行中，请先取消打印后再删除记录");
        }
        if (taskMapper.deleteById(taskId) != 1) {
            throw new ServiceException("打印任务记录删除失败，请刷新后重试");
        }
    }

    /**
     * 管理端分页列表的数据查询，分页由 Controller 中的 PageHelper 统一处理。
     */
    public List<ZycckPrintTask> listTasks(Long printerId, String status, Long recordId,
                                          String schoolName, String studentNo, String studentName,
                                          String activityName, String gameName) {
        QueryWrapper<ZycckPrintTask> query = new QueryWrapper<>();
        if (printerId != null) query.eq("printer_id", printerId);
        if (StringUtils.hasText(status)) query.eq("status", status.trim());
        if (recordId != null) query.eq("record_id", recordId);
        if (StringUtils.hasText(schoolName)) query.like("school_name", schoolName.trim());
        if (StringUtils.hasText(studentNo)) query.like("student_no", studentNo.trim());
        if (StringUtils.hasText(studentName)) query.like("student_name", studentName.trim());
        if (StringUtils.hasText(activityName)) query.like("activity_name", activityName.trim());
        if (StringUtils.hasText(gameName)) query.like("game_name", gameName.trim());
        query.orderByDesc("create_time").orderByDesc("task_id");
        return taskMapper.selectList(query);
    }

    /**
     * 保留给旧的工作人员端接口使用，不附加快照字段筛选。
     */
    public List<ZycckPrintTask> listTasks(Long printerId, String status, Long recordId) {
        return listTasks(printerId, status, recordId, null, null, null, null, null);
    }

    /**
     * 校验学生只能查看自己的打印任务。
     */
    public void verifyTaskOwner(Long taskId, Long userId) {
        ZycckPrintTask task = findTask(taskId);
        ZycckRecord record = task.getRecordId() == null ? null : recordMapper.selectById(task.getRecordId());
        if (record == null || userId == null || !userId.equals(record.getUserId())) {
            throw new ServiceException("打印任务不存在");
        }
    }

    /**
     * 按主键查找打印任务，不存在时统一抛出中文业务提示。
     */
    public ZycckPrintTask findTask(Long taskId) {
        if (taskId == null) throw new ServiceException("缺少打印任务编号");
        ZycckPrintTask task = taskMapper.selectById(taskId);
        if (task == null) throw new ServiceException("打印任务不存在");
        return task;
    }

    /**
     * 查找报告并防止前端篡改 recordId 打印其他学生的报告。
     */
    private ZycckRecord ownedRecord(Long recordId, Long userId) {
        ZycckRecord record = recordMapper.selectById(recordId);
        if (record == null || userId == null || !userId.equals(record.getUserId())) {
            throw new ServiceException("报告记录不存在");
        }
        return record;
    }

    /**
     * 使同一用户的查状态、取消和新建任务串行化。
     */
    private GkzhStudent lockUser(Long userId) {
        GkzhStudent student = studentMapper.selectOne(new QueryWrapper<GkzhStudent>()
                .eq("user_id", userId)
                .eq("del_flag", "0")
                .orderByAsc("student_id")
                .last("LIMIT 1 FOR UPDATE"));
        if (student == null) throw new ServiceException("学生信息不存在，无法打印");
        return student;
    }

    /**
     * 兼容旧任务：新数据直接按 user_id 查，旧数据通过报告记录反查用户。
     */
    private QueryWrapper<ZycckPrintTask> userTaskQuery(Long userId) {
        return new QueryWrapper<ZycckPrintTask>().and(q -> q.eq("user_id", userId)
                .or().apply("EXISTS (SELECT 1 FROM gkzh_zycck_record r "
                        + "WHERE r.record_id = gkzh_zycck_print_task.record_id AND r.user_id = {0})", userId));
    }

    /**
     * 创建新任务前以云端状态为准：已打印则禁止，待打印则先取消。
     * 本地标记失败的最新任务也会再查一次，覆盖“云端已成功、本地未收到回调”的情况。
     */
    private void prepareForNewTask(Long userId) {
        if (taskMapper.selectCount(userTaskQuery(userId).eq("status", STATUS_SUCCESS)) > 0) {
            throw new ServiceException("每位用户只能成功打印一次");
        }
        List<ZycckPrintTask> active = taskMapper.selectList(userTaskQuery(userId)
                .in("status", STATUS_PENDING, STATUS_SUBMITTED, STATUS_PRINTING)
                .orderByDesc("task_id"));
        if (active.isEmpty()) {
            ZycckPrintTask failed = taskMapper.selectOne(userTaskQuery(userId)
                    .eq("status", STATUS_FAILED).orderByDesc("task_id").last("LIMIT 1"));
            if (failed != null) active.add(failed);
        }
        for (ZycckPrintTask task : active) reconcileBeforeRetry(task);
    }

    /**
     * 对一条历史任务执行“查状态→已成功则拦截→待打印则取消”。
     */
    private void reconcileBeforeRetry(ZycckPrintTask task) {
        if (!StringUtils.hasText(task.getPrintId())) {
            if (!STATUS_FAILED.equals(task.getStatus())) {
                task.setStatus(STATUS_FAILED);
                task.setErrorMessage("任务未成功提交到云端");
                task.setUpdateTime(DateUtils.getNowDate());
                taskMapper.updateById(task);
            }
            return;
        }
        Integer cloudStatus = queryCloudStatus(task);
        if (Integer.valueOf(1).equals(cloudStatus)) {
            markSuccess(task);
            throw new ServiceException("该用户已成功打印，本地状态已同步");
        }
        if (Integer.valueOf(0).equals(cloudStatus)) {
            cancelCloudTask(task);
            markCancelled(task, "重试前已取消原打印任务");
        } else if (Integer.valueOf(-2).equals(cloudStatus)) {
            markCancelled(task, "原打印任务已取消");
        } else {
            task.setStatus(STATUS_FAILED);
            task.setErrorMessage("原打印任务未成功");
            task.setUpdateTime(DateUtils.getNowDate());
            taskMapper.updateById(task);
        }
    }

    /**
     * 查询汉印云端的真实任务状态，未返回明确状态时禁止冒险重打。
     */
    private Integer queryCloudStatus(ZycckPrintTask task) {
        HPRTResult<Integer> result = hprtCloudService.queryPrinterTask(task.getEquipmentSn(), task.getPrintId());
        ensureSuccess(result, "查询原打印任务状态失败，请稍后重试");
        if (result.getData() == null) throw new ServiceException("云端未返回明确的打印状态，请稍后重试");
        return result.getData();
    }

    /**
     * 调用汉印云取消指定任务，返回失败时不会创建新任务。
     */
    private void cancelCloudTask(ZycckPrintTask task) {
        HPRTResult<Void> result = hprtCloudService.cancelPrinterTask(task.getEquipmentSn(), task.getPrintId());
        ensureSuccess(result, "取消原打印任务失败，暂不能重试");
    }

    /**
     * 云端已成功时同步本地状态和打印时间。
     */
    private void markSuccess(ZycckPrintTask task) {
        task.setStatus(STATUS_SUCCESS);
        task.setErrorMessage(null);
        if (task.getPrintTime() == null) task.setPrintTime(DateUtils.getNowDate());
        task.setUpdateTime(DateUtils.getNowDate());
        taskMapper.updateById(task);
    }

    /**
     * 保存本地取消状态、原因和取消时间。
     */
    private void markCancelled(ZycckPrintTask task, String message) {
        task.setStatus(STATUS_CANCELLED);
        task.setErrorMessage(message);
        task.setCancelTime(DateUtils.getNowDate());
        task.setUpdateTime(DateUtils.getNowDate());
        taskMapper.updateById(task);
    }

    /**
     * 服务端再次验证手机上报的信标。
     * 不只依赖小程序页面判断，防止绕过页面直接调用打印接口。
     */
    private void validateBeacon(ZycckBeaconEvidence beacon) {
        ZycckBeaconSettingsView settings = beaconConfigService.get();
        if (!settings.isBeaconEnabled()) return;
        List<ZycckBeaconRule> rules = settings.getBeacons();
        if (rules == null) rules = new ArrayList<>();
        if (rules.isEmpty()) throw new ServiceException("打印点蓝牙信标未配置");
        if (beacon == null || !StringUtils.hasText(beacon.getUuid())) {
            throw new ServiceException("请开启蓝牙并靠近打印点");
        }
        String evidenceUuid = ZycckBeaconUuid.normalize(beacon.getUuid());
        if (evidenceUuid == null) throw new ServiceException("蓝牙信标UUID格式不正确");
        boolean ble = "ble".equalsIgnoreCase(beacon.getSource());
        if (StringUtils.hasText(beacon.getSource()) && !ble) {
            throw new ServiceException("不支持的蓝牙扫描来源");
        }
        // 普通 BLE 无距离值；先从原始广播重新读出三个身份字段，防止客户端字段与扫描结果不一致。
        if (ble && !matchesBleAdvertisement(beacon, evidenceUuid)) {
            throw new ServiceException("蓝牙广播数据与打印点信息不一致，请重新扫描");
        }
        if (beacon.getRssi() == null || beacon.getRssi() >= 0 || beacon.getRssi() < -127) {
            throw new ServiceException("蓝牙信号强度无效，请重新扫描");
        }
        long observedAt = beacon.getObservedAt() == null ? 0L : beacon.getObservedAt();
        if (observedAt > 0 && observedAt < 10_000_000_000L) observedAt *= 1000L;
        long maxAge = Math.max(1, settings.getMaxAgeSeconds()) * 1000L;
        if (observedAt <= 0 || Math.abs(System.currentTimeMillis() - observedAt) > maxAge) {
            throw new ServiceException("蓝牙信标检测结果已过期，请重新检测");
        }
        boolean identityMatched = false;
        for (ZycckBeaconRule rule : rules) {
            if (rule != null && evidenceUuid.equals(ZycckBeaconUuid.normalize(rule.getUuid()))
                    && matches(rule.getMajor(), beacon.getMajor())
                    && matches(rule.getMinor(), beacon.getMinor())) {
                identityMatched = true;
                int minRssi = beaconMinRssi(rule, settings);
                double maxDistance = beaconMaxDistance(rule, settings);
                if (ble) {
                    // A 使用 1 米 RSSI 的正数绝对值；普通 BLE 不接受手机自报的距离，服务端重新计算。
                    double estimated = estimateBeaconDistance(beacon.getRssi(), rule);
                    if (estimated <= maxDistance) {
                        beacon.setAccuracy(estimated);
                        return;
                    }
                } else if (beacon.getRssi() >= minRssi && beacon.getAccuracy() != null
                        && Double.isFinite(beacon.getAccuracy()) && beacon.getAccuracy() >= 0
                        && beacon.getAccuracy() <= maxDistance) return;
            }
        }
        if (!identityMatched) {
            throw new ServiceException("未检测到指定打印点的蓝牙信标");
        }
        if (ble) throw new ServiceException("估算距离超出打印点范围，请靠近后重试");
        if (beacon.getRssi() == null || rules.stream().filter(rule -> rule != null
                        && evidenceUuid.equals(ZycckBeaconUuid.normalize(rule.getUuid()))
                        && matches(rule.getMajor(), beacon.getMajor()) && matches(rule.getMinor(), beacon.getMinor()))
                .noneMatch(rule -> beacon.getRssi() >= beaconMinRssi(rule, settings))) {
            throw new ServiceException("距离打印点较远，请靠近后重试");
        }
        throw new ServiceException("请进入打印点蓝牙范围后重试");
    }

    /** 按 d=10^((|RSSI|-A)/(10n)) 估算距离；A 是 1 米处 RSSI 的正数绝对值。 */
    private double estimateBeaconDistance(int rssi, ZycckBeaconRule rule) {
        double reference = rule.getReferenceRssi() == null ? 60.0D : rule.getReferenceRssi();
        double exponent = rule.getPathLossExponent() == null ? 2.0D : rule.getPathLossExponent();
        return Math.pow(10.0D, (Math.abs(rssi) - reference) / (10.0D * exponent));
    }

    /**
     * 核对普通 BLE 厂商广播的 iBeacon 段：4C000215 + 16 字节 UUID + 2 字节 Major + 2 字节 Minor + 1 字节功率。
     * 现场设备在标准 25 字节后还附带厂商私有字节，所以允许后缀，但不接受截断或非十六进制数据。
     */
    private boolean matchesBleAdvertisement(ZycckBeaconEvidence beacon, String expectedUuid) {
        String raw = beacon.getAdvertisData();
        if (!StringUtils.hasText(raw) || raw.length() > 512 || (raw.length() & 1) != 0
                || !raw.matches("(?i)[0-9a-f]+")) return false;
        String hex = raw.toUpperCase(java.util.Locale.ROOT);
        int start = hex.indexOf("4C000215");
        if (start < 0 || (start & 1) != 0 || hex.length() < start + 50) return false;
        String uuid = ZycckBeaconUuid.normalize(hex.substring(start + 8, start + 40));
        int major = Integer.parseInt(hex.substring(start + 40, start + 44), 16);
        int minor = Integer.parseInt(hex.substring(start + 44, start + 48), 16);
        return expectedUuid.equals(uuid) && beacon.getMajor() != null && beacon.getMinor() != null
                && beacon.getMajor() == major && beacon.getMinor() == minor;
    }

    /**
     * 将有效信标转为小程序规则，UUID 统一为标准格式再下发。
     */
    private List<ZycckBeaconRuleView> beaconRuleViews(ZycckBeaconSettingsView settings) {
        List<ZycckBeaconRuleView> result = new ArrayList<>();
        if (settings.getBeacons() == null) return result;
        for (ZycckBeaconRule rule : settings.getBeacons()) {
            String uuid = rule == null ? null : ZycckBeaconUuid.normalize(rule.getUuid());
            if (uuid == null) continue;
            result.add(ZycckBeaconRuleView.builder()
                    .name(trim(rule.getName()))
                    .uuid(uuid)
                    .major(rule.getMajor())
                    .minor(rule.getMinor())
                    .maxDistance(beaconMaxDistance(rule, settings))
                    .referenceRssi(rule.getReferenceRssi() == null ? 60.0D : rule.getReferenceRssi())
                    .pathLossExponent(rule.getPathLossExponent() == null ? 2.0D : rule.getPathLossExponent())
                    .minRssi(beaconMinRssi(rule, settings))
                    .build());
        }
        return result;
    }

    /**
     * 单个信标未配置距离时使用全局默认距离。
     */
    private double beaconMaxDistance(ZycckBeaconRule rule, ZycckBeaconSettingsView settings) {
        return rule.getMaxDistance() == null
                ? settings.getDefaultMaxDistance() : rule.getMaxDistance();
    }

    /**
     * 单个信标未配置 RSSI 时使用全局默认值。
     */
    private int beaconMinRssi(ZycckBeaconRule rule, ZycckBeaconSettingsView settings) {
        return rule.getMinRssi() == null
                ? settings.getDefaultMinRssi() : rule.getMinRssi();
    }

    /**
     * Major/Minor 未填时表示不限制该项。
     */
    private boolean matches(Integer expected, Integer actual) {
        return expected == null || expected.equals(actual);
    }

    /**
     * 把学校、学生、活动和游戏资料固化到打印任务中。
     */
    private void fillTaskSnapshot(ZycckPrintTask task, ZycckRecord record) {
        // 学生、学校实体未声明 MyBatis-Plus 主键，selectById 不会生成映射；使用已有 XML 查询。
        GkzhStudent student = record.getStudentId() == null ? null
                : studentMapper.selectGkzhStudentByStudentId(record.getStudentId());
        if (student == null && record.getUserId() != null) {
            student = studentMapper.selectGkzhStudentByUserId(record.getUserId());
        }
        if (student != null) {
            task.setStudentId(student.getStudentId());
            task.setStudentNo(student.getStudentNo());
            task.setStudentName(student.getStudentName());
            if (task.getSchoolId() == null) task.setSchoolId(student.getSchoolId());
        }
        GkzhSchool school = task.getSchoolId() == null ? null
                : schoolMapper.selectGkzhSchoolBySchoolId(task.getSchoolId());
        if (school != null) task.setSchoolName(school.getTitle());
        GkzhActivityWeekInstance instance = record.getInstanceId() == null ? null : instanceMapper.selectById(record.getInstanceId());
        if (instance != null) task.setActivityName(instance.getTitle());
        GkzhActivityGame game = record.getGameId() == null ? null : gameMapper.selectById(record.getGameId());
        if (game != null) task.setGameName(game.getTitle());
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 生成打印文件、创建本地任务，再将 TSPL 内容提交给汉印云。
     */
    private ZycckPrintTask submit(ZycckRecord record, ZycckPrinter printer, byte[] pdf, int retryCount,
                                  ZycckBeaconEvidence beacon) throws IOException {
        String token = UUID.randomUUID().toString().replace("-", "");
        Path directory = Path.of(GkzhConfig.getProfile(), "zycck-print", token);
        Files.createDirectories(directory);
        Path pdfPath = directory.resolve("report.pdf");
        Files.write(pdfPath, pdf);

        List<BufferedImage> pages = renderPages(pdf);
        List<String> imageUrls = new ArrayList<>();
        StringBuilder tspl = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) {
            BufferedImage image = pages.get(i);
            Path imagePath = directory.resolve("page-" + (i + 1) + ".png");
            ImageIO.write(image, "png", imagePath.toFile());
            imageUrls.add("/profile/zycck-print/" + token + "/page-" + (i + 1) + ".png");
            appendTsplLabel(tspl, image);
        }
        if (pages.isEmpty()) throw new ServiceException("报告没有可打印页面");

        Date now = DateUtils.getNowDate();
        ZycckPrintTask task = new ZycckPrintTask();
        task.setRecordId(record.getRecordId());
        task.setUserId(record.getUserId());
        task.setSchoolId(record.getSchoolId());
        task.setStudentId(record.getStudentId());
        task.setInstanceId(record.getInstanceId());
        task.setGameId(record.getGameId());
        fillTaskSnapshot(task, record);
        task.setPrinterId(printer.getPrinterId());
        task.setEquipmentSn(printer.getEquipmentSn());
        task.setOrderNo("ZYCCK-" + token);
        task.setPrintType("TSPL");
        task.setSourcePdfUrl("/profile/zycck-print/" + token + "/report.pdf");
        task.setSourceImageUrl(imageUrls.get(0));
        task.setStatus(STATUS_PENDING);
        task.setRetryCount(retryCount);
        if (beacon != null) {
            task.setBeaconUuid(ZycckBeaconUuid.normalize(beacon.getUuid()));
            task.setBeaconMajor(beacon.getMajor());
            task.setBeaconMinor(beacon.getMinor());
            task.setBeaconRssi(beacon.getRssi());
            task.setBeaconDistance(beacon.getAccuracy());
        }
        task.setCreateTime(now);
        task.setUpdateTime(now);
        taskMapper.insert(task);

        try {
            HPRTResult<String> result = hprtCloudService.sendPrinterTask(printer.getEquipmentSn(), tspl.toString(), task.getOrderNo());
            ensureSuccess(result, "提交汉印打印任务失败");
            if (!StringUtils.hasText(result.getData())) throw new ServiceException("汉印未返回打印任务编号");
            task.setPrintId(result.getData());
            task.setStatus(STATUS_SUBMITTED);
            task.setUpdateTime(DateUtils.getNowDate());
            taskMapper.updateById(task);
            return task;
        } catch (RuntimeException ex) {
            task.setStatus(STATUS_FAILED);
            task.setErrorMessage(limitMessage(ex.getMessage()));
            task.setUpdateTime(DateUtils.getNowDate());
            taskMapper.updateById(task);
            return task;
        }
    }

    /**
     * 只允许选择已启用、已绑定且在线的打印机。
     */
    private ZycckPrinter printablePrinter(Long printerId) {
        ZycckPrinter printer = printerService.findById(printerId);
        if (!ZycckPrinterService.ENABLED.equals(printer.getEnabled())) throw new ServiceException("打印机已禁用");
        if (!ZycckPrinterService.BOUND.equals(printer.getBoundStatus())) throw new ServiceException("打印机未绑定");
        if (!Integer.valueOf(1).equals(printer.getStatus())) throw new ServiceException("打印机当前不在线");
        return printer;
    }

    /**
     * 将 PDF 的每一页缩放成 76mm × 130mm、203dpi 的打印位图。
     */
    private List<BufferedImage> renderPages(byte[] pdf) throws IOException {
        List<BufferedImage> pages = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdf)) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage rendered = renderer.renderImageWithDPI(i, DPI);
                BufferedImage target = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = target.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.drawImage(rendered, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
                graphics.dispose();
                pages.add(target);
            }
        }
        return pages;
    }

    /**
     * 按当前汉印设备支持的 BITMAP Base64 格式组装 TSPL 标签。
     */
    private void appendTsplLabel(StringBuilder content, BufferedImage image) {
        content.append("SIZE 76 mm,130 mm\r\n")
                .append("GAP 2 mm,0\r\n")
                .append("CLS\r\n")
                .append("<BITMAP x=0 y=0 m=1>")
                .append(toBase64(image, "png"))
                .append("</BITMAP>\r\n")
//                .append("BITMAP 0,0,1,")
//                .append(IMAGE_WIDTH_BYTES).append(',').append(IMAGE_HEIGHT).append(",0,")
//                .append(toBase64(image,"png")).append("\r\n")
                .append("PRINT 1\r\n");
//        content.append("<SIZE>76,128</SIZE>\r\n")
//                .append("<GAP>2,0</GAP>\r\n")
//                .append("<CLS>\r\n")
//                .append("<BITMAP x=0 y=0 m=1>")
//                .append(toBase64(image,"png"))
//                .append("</BITMAP>\r\n")
//                .append("<PRINT x=1>\r\n");
    }

    /**
     * TSPL BITMAP 的最后一段是逐字节点阵数据，不能转换为可见的十六进制文本。
     */
    private String toBitmapData(BufferedImage image) {
        StringBuilder bytes = new StringBuilder(IMAGE_WIDTH_BYTES * IMAGE_HEIGHT);
        for (int y = 0; y < IMAGE_HEIGHT; y++) {
            for (int byteX = 0; byteX < IMAGE_WIDTH_BYTES; byteX++) {
                int value = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int x = byteX * 8 + bit;
                    if (x < IMAGE_WIDTH && (image.getRaster().getSample(x, y, 0) & 0xff) < BLACK_THRESHOLD) {
                        value |= 0x80 >> bit;
                    }
                }
                // 汉印 printTask 的 content 是字符串；用 ISO-8859-1 等价字符保留 0x00-0xff 原始字节。
                bytes.append((char) (value & 0xff));
            }
        }
        return bytes.toString();
    }

    /**
     * 将渲染后的图片编码为汉印 BITMAP 节点使用的 Base64。
     */
    public static String toBase64(BufferedImage image, String format) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImgUtil.write(image, format, out);   // 或 ImgUtil.writeJpg(image, out)
        return Base64.encode(out.toByteArray());
    }

    /**
     * 把汉印状态码转换为本系统的中文界面状态值。
     */
    private String mapCloudStatus(Integer status) {
        if (status == null) return STATUS_SUBMITTED;
        // 汉印任务状态：-2 超时取消，-1 固件自动取消，0 待打印，1 已打印。
        if (status == 0) return STATUS_SUBMITTED;
        if (status == 1) return STATUS_SUCCESS;
        if (status == -2) return STATUS_CANCELLED;
        return STATUS_FAILED;
    }

    private void ensureSuccess(HPRTResult<?> result, String defaultMessage) {
        if (result == null || !Boolean.TRUE.equals(result.getStatus())) {
            String message = result == null ? null : result.getMsg();
            throw new ServiceException(StringUtils.hasText(message) ? message : defaultMessage);
        }
    }

    private String limitMessage(String message) {
        if (!StringUtils.hasText(message)) return "打印任务提交失败";
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
