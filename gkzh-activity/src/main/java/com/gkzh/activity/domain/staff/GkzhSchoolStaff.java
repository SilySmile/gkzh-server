package com.gkzh.activity.domain.staff;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@TableName("gkzh_school_staff")
public class GkzhSchoolStaff {
    @TableId(type = IdType.AUTO)
    private Long staffId;
    private String userName;
    @JsonIgnore
    private String password;
    private Long schoolId;
    private String staffName;
    private String status;
    private Integer canRedeem;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    @TableField(exist = false)
    private String schoolName;
}
