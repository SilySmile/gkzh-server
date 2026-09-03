package com.gkzh.sszctop.domain;
/** 可被抽取参与排序的职业基础资料。 */
import com.baomidou.mybatisplus.annotation.*; import lombok.Data; import java.util.Date;
@Data @TableName("gkzh_sszctop_career") public class SszctopCareer { @TableId(type=IdType.AUTO) private Long careerId; private String name,major,description,status; private Integer sortOrder; private Date createTime,updateTime; }
