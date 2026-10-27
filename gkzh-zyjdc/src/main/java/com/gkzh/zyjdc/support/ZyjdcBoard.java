package com.gkzh.zyjdc.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 职业角斗场棋盘规则。
 *
 * <p>规则：4×4 棋盘，双方各 4 枚棋子；学生棋子在最下方一行，电脑棋子在最上方一行，
 * 中间两行为空。每次只能上下左右移动一格；攻击方等级大于等于防守方即可吃掉，
 * 老鼠可以吃大象、大象不能吃老鼠。3 分钟内吃光对方或到时比较剩余最大棋子等级。</p>
 */
public final class ZyjdcBoard {

    /** 棋盘边长。 */
    public static final int SIZE = 4;

    /** 每局总时长（秒）。 */
    public static final int TOTAL_SECONDS = 180;

    /** 8 种动物，按等级从高到低排列。 */
    public static final List<String> CODES = Collections.unmodifiableList(Arrays.asList(
            "elephant", "lion", "tiger", "leopard", "wolf", "fox", "cat", "mouse"));

    /** 动物编码到中文名称。 */
    private static final Map<String, String> NAMES = new LinkedHashMap<>();

    /** 动物编码到等级。 */
    private static final Map<String, Integer> RANKS = new LinkedHashMap<>();

    static {
        String[] names = {"大象", "狮子", "老虎", "豹子", "狼", "狐", "猫", "老鼠"};
        for (int i = 0; i < CODES.size(); i++) {
            NAMES.put(CODES.get(i), names[i]);
            RANKS.put(CODES.get(i), 8 - i);
        }
    }

    private ZyjdcBoard() {
    }

    /** 返回动物中文名称；未知编码原样返回。 */
    public static String nameOf(String code) {
        String name = NAMES.get(code);
        return name == null ? code : name;
    }

    /** 返回动物等级，大象 8 到老鼠 1；未知编码返回 0。 */
    public static int rankOf(String code) {
        Integer rank = RANKS.get(code);
        return rank == null ? 0 : rank;
    }

    /**
     * 判断攻击方能否吃掉防守方。
     * 同级可吃；老鼠吃大象；大象不能吃老鼠；其余按等级高吃低。
     */
    public static boolean canCapture(String attackerCode, String defenderCode) {
        if (attackerCode == null || defenderCode == null) {
            return false;
        }
        if ("mouse".equals(attackerCode) && "elephant".equals(defenderCode)) {
            return true;
        }
        if ("elephant".equals(attackerCode) && "mouse".equals(defenderCode)) {
            return false;
        }
        return rankOf(attackerCode) >= rankOf(defenderCode);
    }

    /** 生成棋子 token。 */
    public static String token(String side, String code) {
        return side + ":" + code;
    }

    /** 取 token 的所属方；空 token 返回 null。 */
    public static String sideOf(String token) {
        if (token == null) {
            return null;
        }
        int index = token.indexOf(':');
        return index < 0 ? null : token.substring(0, index);
    }

    /** 取 token 的动物编码；空 token 返回 null。 */
    public static String codeOf(String token) {
        if (token == null) {
            return null;
        }
        int index = token.indexOf(':');
        return index < 0 || index + 1 >= token.length() ? null : token.substring(index + 1);
    }

    /**
     * 生成新一局棋盘。
     * 学生所选角色必在学生方，其余 3 种从剩余动物随机补齐；电脑获得剩下 4 种。
     * 双方合计恰好包含 8 种动物各 1 枚。
     */
    public static ZyjdcGameState newState(String playerRole, Random random) {
        if (playerRole == null || !CODES.contains(playerRole)) {
            throw new IllegalArgumentException("角色编码无效");
        }
        Random rnd = random == null ? new Random() : random;
        List<String> remaining = new ArrayList<>(CODES);
        remaining.remove(playerRole);
        Collections.shuffle(remaining, rnd);

        List<String> playerRoles = new ArrayList<>();
        playerRoles.add(playerRole);
        playerRoles.addAll(remaining.subList(0, 3));
        Collections.shuffle(playerRoles, rnd);

        List<String> aiRoles = new ArrayList<>(remaining.subList(3, remaining.size()));
        Collections.shuffle(aiRoles, rnd);

        String[][] board = new String[SIZE][SIZE];
        for (int col = 0; col < SIZE; col++) {
            board[SIZE - 1][col] = token(ZyjdcGameState.PLAYER, playerRoles.get(col));
            board[0][col] = token(ZyjdcGameState.AI, aiRoles.get(col));
        }

        ZyjdcGameState state = new ZyjdcGameState();
        state.setBoard(board);
        state.setTurn(ZyjdcGameState.PLAYER);
        state.setStartedAt(System.currentTimeMillis());
        state.setMoveCount(0);
        state.setPlayerRole(playerRole);
        state.setPlayerRoles(playerRoles);
        state.setAiRoles(aiRoles);
        state.setMoves(new ArrayList<>());
        return state;
    }

    /** 列出某一方的全部合法移动，元素依次为 fromRow、fromCol、toRow、toCol。 */
    public static List<int[]> legalMoves(String[][] board, String side) {
        List<int[]> moves = new ArrayList<>();
        if (board == null) {
            return moves;
        }
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String token = board[row][col];
                if (!side.equals(sideOf(token))) {
                    continue;
                }
                String code = codeOf(token);
                int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                for (int[] direction : directions) {
                    int toRow = row + direction[0];
                    int toCol = col + direction[1];
                    if (toRow < 0 || toRow >= SIZE || toCol < 0 || toCol >= SIZE) {
                        continue;
                    }
                    String target = board[toRow][toCol];
                    if (target == null) {
                        moves.add(new int[]{row, col, toRow, toCol});
                    } else if (!side.equals(sideOf(target)) && canCapture(code, codeOf(target))) {
                        moves.add(new int[]{row, col, toRow, toCol});
                    }
                }
            }
        }
        return moves;
    }

    /** 判断一步移动是否合法。 */
    public static boolean isLegal(String[][] board, String side, int fromRow, int fromCol, int toRow, int toCol) {
        if (board == null || fromRow < 0 || fromRow >= SIZE || fromCol < 0 || fromCol >= SIZE
                || toRow < 0 || toRow >= SIZE || toCol < 0 || toCol >= SIZE) {
            return false;
        }
        if (Math.abs(fromRow - toRow) + Math.abs(fromCol - toCol) != 1) {
            return false;
        }
        String source = board[fromRow][fromCol];
        if (!side.equals(sideOf(source))) {
            return false;
        }
        String target = board[toRow][toCol];
        if (target == null) {
            return true;
        }
        return !side.equals(sideOf(target)) && canCapture(codeOf(source), codeOf(target));
    }

    /**
     * 执行一步合法移动并切换回合。
     * 返回被吃掉的动物编码；未吃子返回 null。
     */
    public static String apply(ZyjdcGameState state, int fromRow, int fromCol, int toRow, int toCol) {
        String[][] board = state.getBoard();
        String token = board[fromRow][fromCol];
        String side = sideOf(token);
        String capturedToken = board[toRow][toCol];
        String captured = codeOf(capturedToken);

        board[toRow][toCol] = token;
        board[fromRow][fromCol] = null;
        state.setTurn(ZyjdcGameState.PLAYER.equals(side) ? ZyjdcGameState.AI : ZyjdcGameState.PLAYER);
        state.setMoveCount(state.getMoveCount() + 1);

        ZyjdcMove move = new ZyjdcMove();
        move.setSide(side);
        move.setCode(codeOf(token));
        move.setFromRow(fromRow);
        move.setFromCol(fromCol);
        move.setToRow(toRow);
        move.setToCol(toCol);
        move.setCaptured(captured);
        if (state.getMoves() == null) {
            state.setMoves(new ArrayList<>());
        }
        state.getMoves().add(move);
        return captured;
    }

    /** 统计某一方棋子数量。 */
    public static int count(String[][] board, String side) {
        int total = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (side.equals(sideOf(board[row][col]))) {
                    total++;
                }
            }
        }
        return total;
    }

    /** 返回某一方剩余棋子中的最高等级；无棋子返回 0。 */
    public static int maxRank(String[][] board, String side) {
        int max = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (side.equals(sideOf(board[row][col]))) {
                    max = Math.max(max, rankOf(codeOf(board[row][col])));
                }
            }
        }
        return max;
    }

    /** 计算剩余秒数，最小为 0。 */
    public static int remainingSeconds(ZyjdcGameState state, long now) {
        long elapsed = Math.max(0L, now - state.getStartedAt()) / 1000L;
        return (int) Math.max(0L, TOTAL_SECONDS - elapsed);
    }

    /**
     * 3 分钟到时结算：先比较双方剩余最高等级，高者胜；
     * 最高等级相同再比较剩余棋子数量，多者胜；仍相同为平局。
     * 返回 win / lose / draw（均以学生视角）。
     */
    public static String timeoutResult(String[][] board) {
        int playerMax = maxRank(board, ZyjdcGameState.PLAYER);
        int aiMax = maxRank(board, ZyjdcGameState.AI);
        if (playerMax != aiMax) {
            return playerMax > aiMax ? "win" : "lose";
        }
        int playerCount = count(board, ZyjdcGameState.PLAYER);
        int aiCount = count(board, ZyjdcGameState.AI);
        if (playerCount != aiCount) {
            return playerCount > aiCount ? "win" : "lose";
        }
        return "draw";
    }

    /** 判断某一方是否已被吃光。 */
    public static boolean wipedOut(String[][] board, String side) {
        return count(board, side) == 0;
    }

    /** 深拷贝棋盘，供 AI 推演使用。 */
    public static String[][] copyBoard(String[][] board) {
        String[][] copy = new String[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, SIZE);
        }
        return copy;
    }

    /** 动物基础价值，用于 AI 评估；老鼠因能克大象略高于猫。 */
    public static int valueOf(String code) {
        if ("mouse".equals(code)) {
            return 28;
        }
        return rankOf(code) * 10;
    }
}
