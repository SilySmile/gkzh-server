package com.gkzh.activity.domain.week;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 游戏配置
 */
@Data
@TableName("gkzh_game_config")
public class GkzhGameConfig {

    @TableId(type = IdType.AUTO)
    private Long configId;

    private String gameType;

    private String category;

    private String route;

    private String gameName;

    private String description;

    private String configJson;

    /** 是否将该游戏记录纳入后续人物画像，当前 zycck 仅保存标记。 */
    private String participatePortrait;

    /** Web 端查看模板：generic=通用，mind-window=心愿橱窗统计。 */
    private String viewType;

    private String status;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
