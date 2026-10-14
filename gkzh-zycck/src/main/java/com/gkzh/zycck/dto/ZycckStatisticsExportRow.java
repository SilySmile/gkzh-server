package com.gkzh.zycck.dto;

import com.gkzh.zycck.domain.ZycckRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

/** Excel 导出的学生参与数据；基础流程字段继承参与记录，关联信息一次查询补齐。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ZycckStatisticsExportRow extends ZycckRecord {
    /** 学校名称。 */
    private String schoolName;
    /** 活动实例名称。 */
    private String instanceName;
    /** 当前活动内游戏名称。 */
    private String gameName;
    /** 学生姓名；资料删除时为空，不借用其他学校的学生资料。 */
    private String studentName;
    /** 学号，以文本导出保留前导零。 */
    private String studentNo;
    /** 实际关联到的学生编号，兼容旧记录未保存学生编号的情况。 */
    private Long effectiveStudentId;
    /** 实际院系或专业部门编号，优先使用记录快照。 */
    private Long effectiveDepartmentId;
    /** 院系或专业部门名称。 */
    private String departmentName;
    /** 学生资料中的性别，作为记录快照缺失时的回退值。 */
    private String studentGender;
    /** 反馈主键；为空表示尚未提交反馈。 */
    private Long feedbackRecordId;
    /** 已提交问卷的版本。 */
    private String feedbackVersion;
    /** 反馈第1题多选编码数组 JSON。 */
    private String feedbackQuestion1Answers;
    /** 反馈第1题其他补充文字。 */
    private String feedbackQuestion1Other;
    /** 已提交反馈第2题是否适用：1适用，0不适用。 */
    private String feedbackShowQuestion2;
    /** 反馈第2题单选编码。 */
    private String feedbackQuestion2Answer;
    /** 反馈第2题其他补充文字。 */
    private String feedbackQuestion2Other;
    /** 反馈第3题多选编码数组 JSON。 */
    private String feedbackQuestion3Answers;
    /** 反馈第3题其他补充文字。 */
    private String feedbackQuestion3Other;
    /** 反馈首次成功提交时间。 */
    private Date feedbackSubmittedTime;
}
