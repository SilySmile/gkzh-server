package com.gkzh.app.service;

import com.alibaba.fastjson2.JSON;
import com.gkzh.zyjdc.support.ZyjdcAi;
import com.gkzh.zyjdc.support.ZyjdcBoard;
import com.gkzh.zyjdc.support.ZyjdcGameState;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 职业角斗场棋盘规则回归：初始布局、等级吃子、老鼠克大象、合法走法、超时结算和棋盘快照往返。
 */
public class ZyjdcBoardTest {

    @Test
    public void initialStateContainsAllEightAnimalsOnceAndPlayerRole() {
        ZyjdcGameState state = ZyjdcBoard.newState("fox", new Random(7));
        Set<String> all = new HashSet<>(state.getPlayerRoles());
        all.addAll(state.getAiRoles());
        assertEquals(8, all.size());
        assertTrue(all.containsAll(ZyjdcBoard.CODES));
        assertTrue(state.getPlayerRoles().contains("fox"));
        assertEquals(4, ZyjdcBoard.count(state.getBoard(), ZyjdcGameState.PLAYER));
        assertEquals(4, ZyjdcBoard.count(state.getBoard(), ZyjdcGameState.AI));
        for (int col = 0; col < 4; col++) {
            assertEquals(ZyjdcGameState.PLAYER, ZyjdcBoard.sideOf(state.getBoard()[3][col]));
            assertEquals(ZyjdcGameState.AI, ZyjdcBoard.sideOf(state.getBoard()[0][col]));
            assertNull(state.getBoard()[1][col]);
            assertNull(state.getBoard()[2][col]);
        }
        assertEquals(ZyjdcGameState.PLAYER, state.getTurn());
    }

    @Test
    public void higherOrEqualRankCanCaptureAndMouseBeatsElephant() {
        assertTrue(ZyjdcBoard.canCapture("elephant", "lion"));
        assertFalse(ZyjdcBoard.canCapture("lion", "elephant"));
        assertTrue(ZyjdcBoard.canCapture("tiger", "tiger"));
        assertTrue(ZyjdcBoard.canCapture("mouse", "elephant"));
        assertFalse(ZyjdcBoard.canCapture("elephant", "mouse"));
        assertTrue(ZyjdcBoard.canCapture("cat", "mouse"));
    }

    @Test
    public void legalMovesAreOrthogonalAndRespectOwnPiecesAndCaptureRules() {
        String[][] board = new String[4][4];
        board[3][3] = "player:elephant";
        List<int[]> moves = ZyjdcBoard.legalMoves(board, "player");
        assertEquals(2, moves.size());
        assertEquals(2, movesFrom(board, "player", 3, 3));

        board[2][3] = "player:lion";
        assertEquals(1, movesFrom(board, "player", 3, 3));
        assertFalse(ZyjdcBoard.isLegal(board, "player", 3, 3, 2, 2));

        board[3][2] = "ai:mouse";
        assertEquals(0, movesFrom(board, "player", 3, 3));
        assertFalse(ZyjdcBoard.isLegal(board, "player", 3, 3, 3, 2));

        board[3][2] = "ai:lion";
        assertEquals(1, movesFrom(board, "player", 3, 3));
        assertTrue(ZyjdcBoard.isLegal(board, "player", 3, 3, 3, 2));
    }

    /** 统计指定格子上的棋子有多少个合法落点，避免把其他棋子的走法算进来。 */
    private static int movesFrom(String[][] board, String side, int row, int col) {
        int total = 0;
        for (int[] move : ZyjdcBoard.legalMoves(board, side)) {
            if (move[0] == row && move[1] == col) {
                total++;
            }
        }
        return total;
    }

    @Test
    public void applyingMoveCapturesAndSwitchesTurn() {
        String[][] board = new String[4][4];
        board[3][3] = "player:elephant";
        board[2][3] = "ai:lion";
        ZyjdcGameState state = new ZyjdcGameState();
        state.setBoard(board);
        state.setTurn(ZyjdcGameState.PLAYER);
        state.setStartedAt(System.currentTimeMillis());
        state.setMoves(new java.util.ArrayList<>());

        String captured = ZyjdcBoard.apply(state, 3, 3, 2, 3);
        assertEquals("lion", captured);
        assertEquals("player:elephant", state.getBoard()[2][3]);
        assertNull(state.getBoard()[3][3]);
        assertEquals(ZyjdcGameState.AI, state.getTurn());
        assertEquals(1, state.getMoveCount());
        assertEquals(1, state.getMoves().size());
        assertEquals("lion", state.getMoves().get(0).getCaptured());
    }

    @Test
    public void timeoutComparesHighestRankThenCountThenDraw() {
        String[][] winByRank = new String[4][4];
        winByRank[3][0] = "player:elephant";
        winByRank[0][0] = "ai:lion";
        assertEquals("win", ZyjdcBoard.timeoutResult(winByRank));

        String[][] loseByRank = new String[4][4];
        loseByRank[3][0] = "player:cat";
        loseByRank[0][0] = "ai:tiger";
        assertEquals("lose", ZyjdcBoard.timeoutResult(loseByRank));

        String[][] winByCount = new String[4][4];
        winByCount[3][0] = "player:lion";
        winByCount[3][1] = "player:cat";
        winByCount[0][0] = "ai:lion";
        assertEquals("win", ZyjdcBoard.timeoutResult(winByCount));

        String[][] draw = new String[4][4];
        draw[3][0] = "player:lion";
        draw[0][0] = "ai:lion";
        assertEquals("draw", ZyjdcBoard.timeoutResult(draw));
    }

    @Test
    public void stateJsonRoundTripKeepsBoardAndTimer() {
        ZyjdcGameState state = ZyjdcBoard.newState("mouse", new Random(3));
        String json = JSON.toJSONString(state);
        ZyjdcGameState parsed = JSON.parseObject(json, ZyjdcGameState.class);
        assertNotNull(parsed);
        assertArrayEquals(state.getBoard()[0], parsed.getBoard()[0]);
        assertArrayEquals(state.getBoard()[3], parsed.getBoard()[3]);
        assertEquals(state.getTurn(), parsed.getTurn());
        assertEquals(state.getStartedAt(), parsed.getStartedAt());
        assertEquals(state.getPlayerRole(), parsed.getPlayerRole());
        assertEquals(state.getPlayerRoles(), parsed.getPlayerRoles());
        assertEquals(state.getAiRoles(), parsed.getAiRoles());
    }

    @Test
    public void aiReturnsLegalMoveAndHandlesNoMove() {
        String[][] board = new String[4][4];
        board[0][0] = "ai:elephant";
        board[3][3] = "player:mouse";
        int[] move = ZyjdcAi.choose(board, new Random(1));
        assertNotNull(move);
        assertTrue(ZyjdcBoard.isLegal(board, ZyjdcGameState.AI, move[0], move[1], move[2], move[3]));

        String[][] noMove = new String[4][4];
        noMove[0][0] = "ai:cat";
        noMove[1][0] = "player:lion";
        noMove[0][1] = "player:tiger";
        assertNull(ZyjdcAi.choose(noMove, new Random(1)));
    }
}
