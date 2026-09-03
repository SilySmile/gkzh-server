package com.gkzh.sszctop.domain;
/** 某个维度下职业的正确名次和解释文案。 */
import com.baomidou.mybatisplus.annotation.*; import lombok.Data; import java.util.Date;
@Data @TableName("gkzh_sszctop_dimension_rank") public class SszctopDimensionRank { @TableId(type=IdType.AUTO) private Long rankId; private Long dimensionId; private Long careerId; private Integer rankOrder; private String description,status; private Date createTime,updateTime; @TableField(exist=false) private String dimensionName,careerName; }
