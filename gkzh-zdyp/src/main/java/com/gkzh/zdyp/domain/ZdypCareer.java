package com.gkzh.zdyp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职得一拼岗位配置实体。
 * 每套岗位对应一组四维度卡片（工作内容、核心技能、职业缺点、发展前景），
 * 并附带岗位定义、行业价值、新生入门建议三段解析文案。
 */
@Data
@TableName("gkzh_zdyp_career")
public class ZdypCareer {

    /** 岗位编号。 */
    @TableId(value = "career_id", type = IdType.AUTO)
    private Long careerId;

    /** 岗位编码，全小写下划线，用于记录快照与统计，创建后不可修改。 */
    private String careerCode;

    /** 岗位中文名，学生端与后台展示使用。 */
    private String careerName;

    /** 岗位定义，解析页展示，说明该岗位做什么。 */
    private String intro;

    /** 行业价值，解析页展示，说明该岗位在行业中的作用。 */
    private String industryValue;

    /** 新生入门建议，解析页展示，说明在校期间如何准备。 */
    private String newcomerAdvice;

    /** 学生端职业列表展示顺序，数值越小越靠前。 */
    private Integer sortOrder;

    /** 状态：0 启用，1 停用；停用岗位不出现在学生端，也不能被选择。 */
    private String status;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
