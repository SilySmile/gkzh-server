package com.gkzh.zyjdc.support;

import java.util.List;
import java.util.Random;

/**
 * 职业角斗场电脑走子。
 *
 * <p>采用两层推演：先枚举电脑所有合法走法，再枚举学生的最佳回应，
 * 以“棋子价值 + 推进程度 + 机动性 + 中心控制”评估局面，并在同分走法中加入少量随机，
 * 保证电脑有基本攻防能力但不会不可战胜。</p>
 */
public final class ZyjdcAi {

    /** 电脑直接吃光学生或学生无子可动时的必胜分。 */
    private static final int WIN_SCORE = 1_000_000;

    private ZyjdcAi() {
    }

    /**
     * 为电脑选择一步走法。
     *
     * @param board  当前棋盘
     * @param random 随机源；为空时使用默认随机
     * @return fromRow、fromCol、toRow、toCol；电脑无合法走法时返回 null
     */
    public static int[] choose(String[][] board, Random random) {
        Random rnd = random == null ? new Random() : random;
        List<int[]> moves = ZyjdcBoard.legalMoves(board, ZyjdcGameState.AI);
        if (moves.isEmpty()) {
            return null;
        }

        int[] best = null;
        double bestScore = -Double.MAX_VALUE;
        for (int[] move : moves) {
            String[][] next = simulate(board, move);
            if (ZyjdcBoard.wipedOut(next, ZyjdcGameState.PLAYER)) {
                return move;
            }
            List<int[]> replies = ZyjdcBoard.legalMoves(next, ZyjdcGameState.PLAYER);
            if (replies.isEmpty()) {
                return move;
            }

            // 学生最佳回应后的最差局面；两层推演可避免明显送子。
            double worst = Double.MAX_VALUE;
            for (int[] reply : replies) {
                String[][] after = simulate(next, reply);
                worst = Math.min(worst, evaluate(after));
            }
            double score = evaluate(next) * 0.45 + worst * 0.55 + rnd.nextDouble() * 3.0;

            // 吃子本身给一点即时奖励，鼓励电脑主动交换。
            String captured = ZyjdcBoard.codeOf(board[move[2]][move[3]]);
            if (captured != null) {
                score += ZyjdcBoard.valueOf(captured) * 0.35;
            }
            if (score > bestScore) {
                bestScore = score;
                best = move;
            }
        }
        return best;
    }

    /** 在棋盘副本上执行一步走法，不修改原棋盘。 */
    private static String[][] simulate(String[][] board, int[] move) {
        String[][] next = ZyjdcBoard.copyBoard(board);
        String token = next[move[0]][move[1]];
        next[move[2]][move[3]] = token;
        next[move[0]][move[1]] = null;
        return next;
    }

    /** 从电脑视角评估局面，分值越高对电脑越有利。 */
    private static double evaluate(String[][] board) {
        double material = 0;
        double advance = 0;
        int centerRow = ZyjdcBoard.ROWS / 2;
        int centerCol = ZyjdcBoard.COLS / 2;
        for (int row = 0; row < ZyjdcBoard.ROWS; row++) {
            for (int col = 0; col < ZyjdcBoard.COLS; col++) {
                String token = board[row][col];
                String side = ZyjdcBoard.sideOf(token);
                if (side == null) {
                    continue;
                }
                String code = ZyjdcBoard.codeOf(token);
                if (ZyjdcGameState.AI.equals(side)) {
                    material += ZyjdcBoard.valueOf(code);
                    // 电脑棋子越靠近学生底线越有压迫力。
                    advance += row * 3.0;
                } else {
                    material -= ZyjdcBoard.valueOf(code);
                    advance -= (ZyjdcBoard.ROWS - 1 - row) * 3.0;
                }
                if (Math.abs(row - centerRow) <= 1 && Math.abs(col - centerCol) <= 1) {
                    advance += ZyjdcGameState.AI.equals(side) ? 1.5 : -1.5;
                }
            }
        }
        int aiMoves = ZyjdcBoard.legalMoves(board, ZyjdcGameState.AI).size();
        int playerMoves = ZyjdcBoard.legalMoves(board, ZyjdcGameState.PLAYER).size();
        return material + advance + (aiMoves - playerMoves) * 2.5;
    }
}
