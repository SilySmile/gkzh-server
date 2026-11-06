package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职业猜猜看报告打印任务。
 * 学校、学生、活动和游戏名称保存的是提交打印时的快照，
 * 后续即使基础资料改名，也能追溯当时实际打印的人和活动。
 */
@Data
@TableName("gkzh_zycck_print_task")
public class ZycckPrintTask {
    /** 本地打印任务主键。 */
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;
    /** 职业探索报告记录编号。 */
    private Long recordId;
    /** 用户编号，用于执行“每位用户只可成功打印一次”。 */
    private Long userId;
    /** 打印时的学校编号快照。 */
    private Long schoolId;
    /** 打印时的学校名称快照。 */
    private String schoolName;
    /** 打印时的学生编号快照。 */
    private Long studentId;
    /** 打印时的学号快照。 */
    private String studentNo;
    /** 打印时的学生姓名快照。 */
    private String studentName;
    /** 打印时的活动实例编号快照。 */
    private Long instanceId;
    /** 打印时的活动名称快照。 */
    private String activityName;
    /** 打印时的游戏编号快照。 */
    private Long gameId;
    /** 打印时的游戏名称快照。 */
    private String gameName;
    /** 本地打印机主键。 */
    private Long printerId;
    /** 汉印云打印设备序列号 SN。 */
    private String equipmentSn;
    /** 汉印云端任务编号，查询、取消和回调同步都使用它。 */
    private String printId;
    /** 本系统提交给汉印的唯一订单号。 */
    private String orderNo;
    /** 打印指令类型，当前为 TSPL。 */
    private String printType;
    /** 生成后的报告 PDF 相对访问地址。 */
    private String sourcePdfUrl;
    /** 生成后的报告首页图片相对访问地址。 */
    private String sourceImageUrl;
    /** pending 待提交、submitted 已提交、printing 打印中、success 成功、failed 失败、cancelled 已取消。 */
    private String status;
    /** 任务失败、取消或云端返回的原因。 */
    private String errorMessage;
    /** 从历史失败任务重试的次数。 */
    private Integer retryCount;
    /** 用户提交打印时实际检测到的 iBeacon UUID。 */
    private String beaconUuid;
    /** 用户提交打印时实际检测到的 iBeacon Major。 */
    private Integer beaconMajor;
    /** 用户提交打印时实际检测到的 iBeacon Minor。 */
    private Integer beaconMinor;
    /** 用户提交打印时实际检测到的 iBeacon 信号强度。 */
    private Integer beaconRssi;
    /** 手机估算的信标距离，单位米。 */
    private Double beaconDistance;
    /** 任务取消时间。 */
    private Date cancelTime;
    /** 任务创建时间。 */
    private Date createTime;
    /** 云端确认成功打印的时间。 */
    private Date printTime;
    /** 任务最后更新时间。 */
    private Date updateTime;
}
