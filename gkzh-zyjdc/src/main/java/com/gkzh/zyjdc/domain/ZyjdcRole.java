package com.gkzh.zyjdc.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职业角斗场动物角色配置实体。
 * 固定 8 个角色，数据库中不允许新增或删除，只允许后台维护名称、定位、文案、头像和状态。
 */
@Data
@TableName("gkzh_zyjdc_role")
public class ZyjdcRole {

    /** 角色编号。 */
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    /** 动物编码：elephant、lion、tiger、leopard、wolf、fox、cat、mouse。 */
    private String roleCode;

    /** 动物名称，例如大象、狮子。 */
    private String roleName;

    /** 职场定位，例如首席执行官（CEO）。 */
    private String jobTitle;

    /** 对弈等级：大象 8、狮子 7、老虎 6、豹子 5、狼 4、狐 3、猫 2、老鼠 1。 */
    private Integer rankLevel;

    /** 角色头像地址，支持完整 URL 或相对路径。 */
    private String imageUrl;

    /** 该动物角色的生存法则。 */
    private String survivalRule;

    /** 该动物角色对应的职场能力。 */
    private String careerAbility;

    /** 学生选择该角色后的个人解读。 */
    private String roleInterpretation;

    /** 该角色对应的职场定位说明。 */
    private String positioning;

    /** 该角色对应的能力优势。 */
    private String advantage;

    /** 显示顺序，数值越小越靠前。 */
    private Integer sortOrder;

    /** 状态：0 启用，1 停用。 */
    private String status;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
