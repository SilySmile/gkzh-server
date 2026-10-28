package com.gkzh.app.service;

import com.alibaba.fastjson2.JSON;
import com.gkzh.zyjdc.support.ZyjdcAi;
import com.gkzh.zyjdc.support.ZyjdcBoard;
import com.gkzh.zyjdc.support.ZyjdcGameState;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 职业角斗场棋盘规则回归：7×9 初始布局、双方各 8 枚、等级吃子、老鼠克大象、
 * 合法走法、超时结算、棋盘快照往返和 AI 走子合法性。
 */
public class ZyjdcBoardTest {

    @Test
    public void initialStateHasEightPiecesPerSideAndAllAnimals() {
        ZyjdcGameState state = ZyjdcBoard.newState("fox");
        assertEquals(9, state.getBoard().length);
        assertEquals(7, state.getBoard()[0].length);
        assertEquals(8, state.getPlayerRoles().size());
        assertEquals(8, state.getAiRoles().size());
        assertEquals(8, new HashSet<>(state.getPlayerRoles()).size());
        assertEquals(8, new HashSet<>(state.getAiRoles()).size());
        assertTrue(state.getPlayerRoles().containsAll(ZyjdcBoard.CODES));
        assertTrue(state.getAiRoles().containsAll(ZyjdcBoard.CODES));
        assertEquals("fox", state.getPlayerRole());
        assertEquals(ZyjdcGameState.PLAYER, state.getTurn());
        assertEquals(8, ZyjdcBoard.count(state.getBoard(), ZyjdcGameState.PLAYER));
        assertEquals(8, ZyjdcBoard.count(state.getBoard(), ZyjdcGameState.AI));

        for (int row = 0; row <= 1; row++) {
            assertRowFilled(state, row);
        }
        for (int row = 7; row <= 8; row++) {
            assertRowFilled(state, row);
        }
        for (int row = 2; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                assertNull(state.getBoard()[row][col], "row " + row + " col " + col + " should be empty");
            }
        }
    }

    private static void assertRowFilled(ZyjdcGameState state, int row) {
        for (int col = 1; col <= 4; col++) {
            assertNotNull(state.getBoard()[row][col], "row " + row + " col " + col + " should have a piece");
        }
        assertNull(state.getBoard()[row][0]);
        assertNull(state.getBoard()[row][5]);
        assertNull(state.getBoard()[row][6]);
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
        String[][] board = new String[9][7];
        board[8][3] = "player:elephant";
        assertEquals(3, movesFrom(board, "player", 8, 3));
        assertFalse(ZyjdcBoard.isLegal(board, "player", 8, 3, 7, 2));

        board[7][3] = "player:lion";
        assertEquals(2, movesFrom(board, "player", 8, 3));

        board[8][2] = "ai:mouse";
        assertEquals(1, movesFrom(board, "player", 8, 3));
        assertFalse(ZyjdcBoard.isLegal(board, "player", 8, 3, 8, 2));

        board[8][2] = "ai:lion";
        assertEquals(2, movesFrom(board, "player", 8, 3));
        assertTrue(ZyjdcBoard.isLegal(board, "player", 8, 3, 8, 2));
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
        String[][] board = new String[9][7];
        board[8][3] = "player:elephant";
        board[7][3] = "ai:lion";
        ZyjdcGameState state = new ZyjdcGameState();
        state.setBoard(board);
        state.setTurn(ZyjdcGameState.PLAYER);
        state.setStartedAt(System.currentTimeMillis());
        state.setMoves(new java.util.ArrayList<>());

        String captured = ZyjdcBoard.apply(state, 8, 3, 7, 3);
        assertEquals("lion", captured);
        assertEquals("player:elephant", state.getBoard()[7][3]);
        assertNull(state.getBoard()[8][3]);
        assertEquals(ZyjdcGameState.AI, state.getTurn());
        assertEquals(1, state.getMoveCount());
        assertEquals(1, state.getMoves().size());
        assertEquals("lion", state.getMoves().get(0).getCaptured());
    }

    @Test
    public void timeoutComparesHighestRankThenCountThenDraw() {
        String[][] winByRank = new String[9][7];
        winByRank[8][0] = "player:elephant";
        winByRank[0][0] = "ai:lion";
        assertEquals("win", ZyjdcBoard.timeoutResult(winByRank));

        String[][] loseByRank = new String[9][7];
        loseByRank[8][0] = "player:cat";
        loseByRank[0][0] = "ai:tiger";
        assertEquals("lose", ZyjdcBoard.timeoutResult(loseByRank));

        String[][] winByCount = new String[9][7];
        winByCount[8][0] = "player:lion";
        winByCount[8][1] = "player:cat";
        winByCount[0][0] = "ai:lion";
        assertEquals("win", ZyjdcBoard.timeoutResult(winByCount));

        String[][] draw = new String[9][7];
        draw[8][0] = "player:lion";
        draw[0][0] = "ai:lion";
        assertEquals("draw", ZyjdcBoard.timeoutResult(draw));
    }

    @Test
    public void stateJsonRoundTripKeepsBoardAndTimer() {
        ZyjdcGameState state = ZyjdcBoard.newState("mouse");
        String json = JSON.toJSONString(state);
        ZyjdcGameState parsed = JSON.parseObject(json, ZyjdcGameState.class);
        assertNotNull(parsed);
        assertArrayEquals(state.getBoard()[0], parsed.getBoard()[0]);
        assertArrayEquals(state.getBoard()[8], parsed.getBoard()[8]);
        assertEquals(state.getTurn(), parsed.getTurn());
        assertEquals(state.getStartedAt(), parsed.getStartedAt());
        assertEquals(state.getPlayerRole(), parsed.getPlayerRole());
        assertEquals(state.getPlayerRoles(), parsed.getPlayerRoles());
        assertEquals(state.getAiRoles(), parsed.getAiRoles());
    }

    @Test
    public void aiReturnsLegalMoveOnInitialBoardQuickly() {
        ZyjdcGameState state = ZyjdcBoard.newState("elephant");
        assertTimeout(Duration.ofSeconds(3), () -> {
            int[] move = ZyjdcAi.choose(state.getBoard(), new Random(1));
            assertNotNull(move);
            assertTrue(ZyjdcBoard.isLegal(state.getBoard(), ZyjdcGameState.AI,
                    move[0], move[1], move[2], move[3]));
        });
    }

    @Test
    public void aiHandlesNoMove() {
        String[][] noMove = new String[9][7];
        noMove[0][0] = "ai:cat";
        noMove[1][0] = "player:lion";
        noMove[0][1] = "player:tiger";
        assertNull(ZyjdcAi.choose(noMove, new Random(1)));
    }

    @Test
    public void codesContainAllEightAnimals() {
        Set<String> expected = new HashSet<>(Arrays.asList(
                "elephant", "lion", "tiger", "leopard", "wolf", "fox", "cat", "mouse"));
        assertEquals(expected, new HashSet<>(ZyjdcBoard.CODES));
        List<String> back = ZyjdcBoard.BACK_ROW;
        List<String> front = ZyjdcBoard.FRONT_ROW;
        Set<String> all = new HashSet<>(back);
        all.addAll(front);
        assertEquals(expected, all);
    }
}
