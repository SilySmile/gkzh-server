package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("gkzh_zycck_record")
public class ZycckRecord {
    @TableId(value = "record_id", type = IdType.AUTO) private Long recordId;
    private Long schoolId;
    private Long instanceId;
    private Long gameId;
    private Long userId;
    private Long studentId;
    private Long departmentId;
    private String major;
    private String gender;
    private String gameType;
    private String status;
    private String stage;
    private Integer currentQuestionNo;
    private String questionIds;
    private String questionOrder;
    private String careerIds;
    private String categoryIds;
    private String optionSnapshotJson;
    private String answerJson;
    private String awarenessJson;
    private String viewedCareerIds;
    private String explorationCareerIds;
    private String configVersion;
    private Date questionStartTime;
    private Integer questionElapsedSeconds;
    private Date scanTime;
    private Date startTime;
    private Date finishTime;
    private Date createTime;
    private Date updateTime;
}
