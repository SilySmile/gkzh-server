package com.gkzh.sszctop.domain;
/** 可被抽取参与排序的职业基础资料。 */
import com.baomidou.mybatisplus.annotation.*; import lombok.Data; import java.util.Date;
@Data @TableName("gkzh_sszctop_career") public class SszctopCareer {
 @TableId(type=IdType.AUTO) private Long careerId;
 private String name,major,description,status;
 private Integer sortOrder;
 /** 职业卡片图片地址，后台可替换；为空时小程序使用内置兜底图。 */
 private String imageUrl;
 private Date createTime,updateTime;
}
