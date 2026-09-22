package com.gkzh.zycck.dto;

import com.gkzh.zycck.domain.ZycckPrintTask;
import lombok.Data;

import java.util.Date;

@Data
public class ZycckPrintTaskView {
    private Long taskId;
    private Long recordId;
    private Long printerId;
    private String equipmentSn;
    private String printId;
    private String orderNo;
    private String printType;
    private String sourcePdfUrl;
    private String sourceImageUrl;
    private String status;
    private String errorMessage;
    private Integer retryCount;
    private Date createTime;
    private Date printTime;
    private Date updateTime;

    public static ZycckPrintTaskView from(ZycckPrintTask task) {
        ZycckPrintTaskView view = new ZycckPrintTaskView();
        view.taskId = task.getTaskId();
        view.recordId = task.getRecordId();
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
        view.createTime = task.getCreateTime();
        view.printTime = task.getPrintTime();
        view.updateTime = task.getUpdateTime();
        return view;
    }
}
