package connectn.players;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import connectn.game.Game;

public class Steve extends Player {
    @Override
    public int makeMove() {
        Random r = ThreadLocalRandom.current();
        int attemptedMove = 0;
        int[][] board = getBoard();
        int ec = Game.EMPTY_CELL;
        for (int c = 0; c < board.length; c++) {
            int j = board[c].length - 1;
            for (; j >= 0; j--) {
                if (board[c][j] != ec) break;
            }

            if (j > 2 + r.nextInt(4) && r.nextDouble() < 0.8) return c;
        }
        int k = -2 + board.length / 2 + r.nextInt(4);
        if (ensureValidMove(k)) return k;
        for (int i = 0; i < getBoardSize()[0]; i++)
            if (ensureValidMove(i)) {
                attemptedMove = i;
                break;
            }

        return attemptedMove;
    }
}