package com.gkzh.sszctop.domain;
/** 职业认知维度配置，例如工作时长、压力指数等。 */
import com.baomidou.mybatisplus.annotation.*; import lombok.Data; import java.util.Date;
@Data @TableName("gkzh_sszctop_dimension") public class SszctopDimension {
 @TableId(type=IdType.AUTO) private Long dimensionId;
 private String code,name,description,status;
 private Integer sortOrder;
 /** 维度卡片图片地址，后台可替换；为空时小程序使用内置兜底图。 */
 private String imageUrl;
 private Date createTime,updateTime;
}
