package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("gkzh_zycck_category")
public class ZycckCategory {
    @TableId(value = "category_id", type = IdType.AUTO) private Long categoryId;
    private String code;
    private String name;
    private String drawMode;
    private Integer sortOrder;
    private String status;
    private Date createTime;
    private Date updateTime;
}
