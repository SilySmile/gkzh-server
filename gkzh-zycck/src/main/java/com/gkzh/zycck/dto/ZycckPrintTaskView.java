package com.gkzh.zycck.dto;

import com.gkzh.zycck.domain.ZycckPrintTask;
import lombok.Data;

import java.util.Date;

/**
 * 返回给 Web 管理端和小程序的打印任务视图。
 * 字段含义与 {@link ZycckPrintTask} 一致，通过独立视图避免接口直接暴露持久化对象。
 */
@Data
public class ZycckPrintTaskView {
    /** 本地打印任务编号。 */
    private Long taskId;
    /** 职业探索报告记录编号。 */
    private Long recordId;
    /** 提交打印的用户编号。 */
    private Long userId;
    /** 打印时的学校编号。 */
    private Long schoolId;
    /** 打印时的学校名称。 */
    private String schoolName;
    /** 打印时的学生编号。 */
    private Long studentId;
    /** 打印时的学生学号。 */
    private String studentNo;
    /** 打印时的学生姓名。 */
    private String studentName;
    /** 打印时的活动实例编号。 */
    private Long instanceId;
    /** 打印时的活动名称。 */
    private String activityName;
    /** 打印时的游戏编号。 */
    private Long gameId;
    /** 打印时的游戏名称。 */
    private String gameName;
    /** 本地打印机编号。 */
    private Long printerId;
    /** 汉印云打印设备序列号 SN。 */
    private String equipmentSn;
    /** 汉印云端打印任务编号。 */
    private String printId;
    /** 本系统提交给汉印云的唯一订单号。 */
    private String orderNo;
    /** 打印指令类型，当前为 TSPL。 */
    private String printType;
    /** 报告 PDF 的相对访问地址。 */
    private String sourcePdfUrl;
    /** 报告首页图片的相对访问地址。 */
    private String sourceImageUrl;
    /** 任务状态：pending、submitted、printing、success、failed 或 cancelled。 */
    private String status;
    /** 失败或取消原因；正常任务为空。 */
    private String errorMessage;
    /** 历史失败任务的重试次数。 */
    private Integer retryCount;
    /** 提交打印时检测到的 iBeacon UUID。 */
    private String beaconUuid;
    /** 提交打印时检测到的 iBeacon Major。 */
    private Integer beaconMajor;
    /** 提交打印时检测到的 iBeacon Minor。 */
    private Integer beaconMinor;
    /** 提交打印时检测到的 iBeacon 信号强度。 */
    private Integer beaconRssi;
    /** 手机估算的 iBeacon 距离，单位米。 */
    private Double beaconDistance;
    /** 任务取消时间。 */
    private Date cancelTime;
    /** 任务创建时间。 */
    private Date createTime;
    /** 云端确认成功打印的时间。 */
    private Date printTime;
    /** 任务最后更新时间。 */
    private Date updateTime;

    /** 将数据库打印任务完整复制成接口返回对象。 */
    public static ZycckPrintTaskView from(ZycckPrintTask task) {
        ZycckPrintTaskView view = new ZycckPrintTaskView();
        view.taskId = task.getTaskId();
        view.recordId = task.getRecordId();
        view.userId = task.getUserId();
        view.schoolId = task.getSchoolId();
        view.schoolName = task.getSchoolName();
        view.studentId = task.getStudentId();
        view.studentNo = task.getStudentNo();
        view.studentName = task.getStudentName();
        view.instanceId = task.getInstanceId();
        view.activityName = task.getActivityName();
        view.gameId = task.getGameId();
        view.gameName = task.getGameName();
        view.printerId = task.getPrinterId();
        view.equipmentSn = task.getEquipmentSn();
        view.printId = task.getPrintId();
        view.orderNo = task.getOrderNo();
        view.printType = task.getPrintType();
        view.sourcePdfUrl = task.getSourcePdfUrl();
        view.sourceImageUrl = task.getSourceImageUrl();
        view.status = task.getStatus();
        view.errorMessage = task.getErrorMessage();
        view.retryCount = task.getRetryCount();
        view.beaconUuid = task.getBeaconUuid();
        view.beaconMajor = task.getBeaconMajor();
        view.beaconMinor = task.getBeaconMinor();
        view.beaconRssi = task.getBeaconRssi();
        view.beaconDistance = task.getBeaconDistance();
        view.cancelTime = task.getCancelTime();
        view.createTime = task.getCreateTime();
        view.printTime = task.getPrintTime();
        view.updateTime = task.getUpdateTime();
        return view;
    }
}
