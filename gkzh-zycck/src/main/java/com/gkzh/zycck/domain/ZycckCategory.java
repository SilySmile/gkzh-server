package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/** 职业猜猜看职业大类实体。 */
@Data
@TableName("gkzh_zycck_category")
public class ZycckCategory {
    /** 职业大类主键。 */
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;
    /** 职业大类唯一编码。 */
    private String code;
    /** 职业大类名称。 */
    private String name;
    /** 职业大类说明。 */
    private String description;
    /** 抽题模式：fixed 固定抽取，random 随机抽取。 */
    private String drawMode;
    /** 显示顺序，数值越小越靠前。 */
    private Integer sortOrder;
    /** 状态：0 正常，1 停用。 */
    private String status;
    /** 创建时间。 */
    private Date createTime;
    /** 最后更新时间。 */
    private Date updateTime;
}
