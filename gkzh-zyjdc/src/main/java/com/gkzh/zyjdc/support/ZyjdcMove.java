package com.gkzh.zyjdc.support;

import lombok.Data;

/**
 * 职业角斗场单步落子记录。
 * 服务端在学生落子和电脑落子后分别写入，最终随棋盘快照一起保存。
 */
@Data
public class ZyjdcMove {

    /** 落子方：player 学生，ai 电脑。 */
    private String side;

    /** 落子动物编码。 */
    private String code;

    /** 起点行，0-3。 */
    private int fromRow;

    /** 起点列，0-3。 */
    private int fromCol;

    /** 终点行，0-3。 */
    private int toRow;

    /** 终点列，0-3。 */
    private int toCol;

    /** 本步吃掉的对方动物编码；未吃子为 null。 */
    private String captured;
}
