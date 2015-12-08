package connectn.players;

import connectn.game.Game;
import java.util.ArrayList;
import java.util.List;

public class RowBot extends Player {

    @Override
    public int makeMove() {
        int[][] board = getBoard();
        int best = -1;
        int bestScore = -10;
        for (int col = 0; col < board.length; col++) {
            if (ensureValidMove(col)) {
                int score = score(board, col, false);
                score -= score(board, col, true);
                if (score > bestScore) {
                    bestScore = score;
                    best = col;
                }
            }
        }
        return best;
    }

    private int score(int[][] board, int col, boolean simulateMode) {
        int me = getID();
        int row = getLowestEmptyRow(board, col);
        List<Score> scores = new ArrayList<>();
        if (!simulateMode) {
            scores.add(getScoreVertical(board, col, row));
        } else {
            row += 1;
        }
        scores.addAll(getScoreHorizontal(board, col, row));
        scores.addAll(getScoreDiagonal(board, col, row));
        int score = 0;
        for (Score s : scores) {
            if (s.player == me) {
                score += s.points > 2 ? 100 : s.points * 5;
            } else if (s.player != Game.EMPTY_CELL) {
                score += s.points > 2 ? 50 : 0;
            } else {
                score += 1;
            }
        }
        return score;
    }

    private Score getScoreVertical(int[][] board, int col, int row) {
        return getScore(board, col, row, 0, -1);
    }

    private List<Score> getScoreHorizontal(int[][] board, int col, int row) {
        List<Score> scores = new ArrayList<>();

        Score left = getScore(board, col, row, -1, 0);
        Score right = getScore(board, col, row, 1, 0);
        if (left.player == right.player) {
            left.points += right.points;
            scores.add(left);
        } else {
            scores.add(left);
            scores.add(right);
        }
        return scores;
    }

    private List<Score> getScoreDiagonal(int[][] board, int col, int row) {
        List<Score> scores = new ArrayList<>();

        Score leftB = getScore(board, col, row, -1, -1);
        Score rightU = getScore(board, col, row, 1, 1);
        Score leftBottomToRightUp = leftB;
        if (leftB.player == rightU.player) {
            leftBottomToRightUp.points += rightU.points;
        } else if (leftB.points < rightU.points || leftB.player == Game.EMPTY_CELL) {
            leftBottomToRightUp = rightU;
        }

        Score leftU = getScore(board, col, row, -1, 1);
        Score rightB = getScore(board, col, row, 1, -1);
        Score rightBottomToLeftUp = leftU;
        if (leftU.player == rightB.player) {
            rightBottomToLeftUp.points += rightB.points;
        } else if (leftU.points < rightB.points || leftU.player == Game.EMPTY_CELL) {
            rightBottomToLeftUp = rightB;
        }

        if (leftBottomToRightUp.player == rightBottomToLeftUp.player) {
            leftBottomToRightUp.points += rightBottomToLeftUp.points;
            scores.add(leftBottomToRightUp);
        } else {
            scores.add(leftBottomToRightUp);
            scores.add(rightBottomToLeftUp);
        }
        return scores;
    }

    private Score getScore(int[][] board, int initCol, int initRow, int colOffset, int rowOffset) {
        Score score = new Score();
        outerLoop: for (int c = initCol + colOffset;; c += colOffset) {
            for (int r = initRow + rowOffset;; r += rowOffset) {
                if (outside(c, r) || board[c][r] == Game.EMPTY_CELL) {
                    break outerLoop;
                }
                if (score.player == Game.EMPTY_CELL) {
                    score.player = board[c][r];
                }

                if (score.player == board[c][r]) {
                    score.points++;
                } else {
                    break outerLoop;
                }

                if (rowOffset == 0) {
                    break;
                }
            }
            if (colOffset == 0) {
                break;
            }
        }
        return score;
    }

    private boolean outside(int col, int row) {
        return !boardContains(col, row);
    }

    private int getLowestEmptyRow(int[][] board, int col) {
        int[] rows = board[col];
        for (int row = 0; row < rows.length; row++) {
            if (rows[row] == Game.EMPTY_CELL){
                return row;
            }
        }
        return -1;
    }

    private class Score {
        private int player = Game.EMPTY_CELL;
        private int points = 0;
    }
}