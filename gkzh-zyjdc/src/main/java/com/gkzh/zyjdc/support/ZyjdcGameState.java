package com.gkzh.zyjdc.support;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 职业角斗场服务端对局状态。
 * 该对象整体序列化到 gkzh_zyjdc_record.board_json，客户端只提交起点和终点，
 * 棋盘、回合、计时起点和落子记录全部以服务端保存为准，避免客户端篡改。
 */
@Data
public class ZyjdcGameState {

    /** 学生方标识。 */
    public static final String PLAYER = "player";

    /** 电脑方标识。 */
    public static final String AI = "ai";

    /** 4×4 棋盘；空格为 null，棋子 token 形如 player:elephant、ai:mouse。 */
    private String[][] board;

    /** 当前轮到哪一方：player 或 ai。 */
    private String turn;

    /** 本局开始时间（毫秒时间戳），用于服务端 3 分钟计时。 */
    private long startedAt;

    /** 本局双方累计落子步数。 */
    private int moveCount;

    /** 本局落子记录，按时间顺序保存，便于复盘和排查。 */
    private List<ZyjdcMove> moves = new ArrayList<>();

    /** 学生所选角色编码。 */
    private String playerRole;

    /** 学生方 4 枚动物编码。 */
    private List<String> playerRoles = new ArrayList<>();

    /** 电脑方 4 枚动物编码。 */
    private List<String> aiRoles = new ArrayList<>();
}
