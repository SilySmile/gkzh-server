package com.gkzh.zytj.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职愿探究橱窗物品实体。
 * 四列各两个物品，每列对应一个 MBTI 维度：1=外向(E)/内向(I)、2=实感(S)/直觉(N)、
 * 3=思考(T)/情感(F)、4=判断(J)/感知(P)。物品编码即该物品代表的维度字母。
 */
@Data
@TableName("gkzh_zytj_item")
public class ZytjItem {

    /** 物品编号。 */
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    /** 物品对应的性格维度代码：E/I/S/N/T/F/J/P。 */
    private String itemCode;

    /** 物品中文名，例如“派对气球”。 */
    private String itemName;

    /** 所属维度：EI、SN、TF、JP。 */
    private String dimensionCode;

    /** 所在列序号 1-4，决定该物品归属的性格维度。 */
    private Integer columnIndex;

    /** 所在行序号 1-2，学生端 2 行 4 列橱窗按此排布。 */
    private Integer rowIndex;

    /** 同列内展示顺序，数值越小越靠前。 */
    private Integer sortOrder;

    /** 物品图片地址；为空时学生端显示名称占位。 */
    private String imageUrl;

    /** 隐喻说明，解释该物品为什么代表这个性格维度。 */
    private String metaphor;

    /** 状态：0 启用，1 停用。 */
    private String status;

    /** 备注。 */
    private String remark;

    /** 创建者。 */
    private String createBy;

    /** 创建时间。 */
    private Date createTime;

    /** 更新者。 */
    private String updateBy;

    /** 最后更新时间。 */
    private Date updateTime;
}
