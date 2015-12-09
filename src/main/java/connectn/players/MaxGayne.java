package connectn.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MaxGayne extends Player {
    private static final int PLAYERS = 3;

    private static class Result {
        protected final int[] score;
        protected int lastCol;

        public Result(int[] score, int lastCol) {
            super();
            this.score = score;
            this.lastCol = lastCol;
        }

        public Result() {
            this(new int[PLAYERS], -1);
        }

        public Result(Result other) {
            this(new int[PLAYERS], other.lastCol);
            System.arraycopy(other.score, 0, this.score, 0, PLAYERS);
        }

        public int getRelativeScore(int player) {
            int max = Integer.MIN_VALUE;
            for (int i = 0; i < PLAYERS; ++ i) {
                if (i != player && score[i] > max) {
                    max = score[i];
                }
            }
            return score[player] - max;
        }
    }

    private static class Board extends Result {
        private final int cols;
        private final int rows;
        private final int[] data;
        private final int[] used;

        public Board(int cols, int rows) {
            super();
            this.cols = cols;
            this.rows = rows;
            this.data = new int[cols * rows];
            Arrays.fill(this.data, -1);
            this.used = new int[cols];
        }

        public Board(Board other) {
            super(other);
            this.cols = other.cols;
            this.rows = other.rows;
            this.data = new int[cols * rows];
            System.arraycopy(other.data, 0, this.data, 0, this.data.length);
            this.used = new int[cols];
            System.arraycopy(other.used, 0, this.used, 0, this.used.length);
        }

        private void updatePartScore(int player, int length, int open, int factor) {
            switch (length) {
                case 1:
                    score[player] += factor * open;
                    break;
                case 2:
                    score[player] += factor * (100 + open * 10);
                    break;
                case 3:
                    score[player] += factor * (10_000 + open * 1_000);
                    break;
                default:
                    score[player] += factor * ((length - 3) * 1_000_000 + open * 100_000);
                    break;
            }
        }

        private void updateLineScore(int col, int row, int colOff, int rowOff, int length, int factor) {
            int open = 0;
            int player = -1;
            int partLength = 0;
            for (int i = 0; i < length; ++ i) {
                int newPlayer = data[(col + i * colOff) * rows + row + i * rowOff];
                if (newPlayer < 0) {
                    if (player < 0) {
                        if (i == 0) {
                            open = 1;
                        }
                    } else {
                        updatePartScore(player, partLength, open + 1, factor);
                        open = 1;
                        player = newPlayer;
                        partLength = 0;
                    }
                } else {
                    if (newPlayer == player) {
                        ++ partLength;
                    } else {
                        if (player >= 0) {
                            updatePartScore(player, partLength, open, factor);
                            open = 0;
                        }
                        player = newPlayer;
                        partLength = 1;
                    }
                }
            }
            if (player >= 0) {
                updatePartScore(player, partLength, open, factor);
            }
        }

        private void updateIntersectionScore(int col, int row, int factor) {
            updateLineScore(col, 0, 0, 1, rows, factor);
            updateLineScore(0, row, 1, 0, cols, factor);
            if (row > col) {
                updateLineScore(0, row - col, 1, 1, Math.min(rows - row, cols), factor);
            } else {
                updateLineScore(col - row, 0, 1, 1, Math.min(cols - col, rows), factor);
            }
            if (row > cols - col - 1) {
                updateLineScore(cols - 1, row - (cols - col - 1), -1, 1, Math.min(rows - row, cols), factor);
            } else {
                updateLineScore(col + row, 0, -1, 1, Math.min(col + 1, rows), factor);
            }
        }

        private void updatePiece(int player, int col, int row) {
            updateIntersectionScore(col, row, -1);
            data[col * rows + row] = player;
            ++ used[col];
            lastCol = col;
            updateIntersectionScore(col, row, 1);
        }

        public Board updatePiece(int player, int col) {
            int row = used[col];
            if (row >= rows) {
                return null;
            } else {
                Board result = new Board(this);
                result.updatePiece(player, col, row);
                return result;
            }
        }

        private void updateBoard(int[][] board) {
            for (int col = 0; col < cols; ++ col) {
                for (int row = 0; row < rows; ++ row) {
                    int oldPlayer = data[col * rows + row];
                    int newPlayer = board[col][row] - 1;
                    if (newPlayer < 0) {
                        if (oldPlayer < 0) {
                            break;
                        } else {
                            throw new RuntimeException("[" + col + ", " + row + "] == "  + oldPlayer + " >= 0");
                        }
                    } else {
                        if (oldPlayer < 0) {
                            updatePiece(newPlayer, col, row);
                        } else if (newPlayer != oldPlayer) {
                            throw new RuntimeException("[" + col + ", " + row + "] == "  + oldPlayer + " >= " + newPlayer);
                        }
                    }
                }
            }
        }

        private Result bestMove(int depth, int player) {
            List<Board> boards = new ArrayList<>();
            for (int col = 0; col < cols; ++ col) {
                Board board = updatePiece(player, col);
                if (board != null) {
                    boards.add(board);
                }
            }
            if (boards.isEmpty()) {
                return null;
            }
            Collections.sort(boards, (o1, o2) -> Integer.compare(o2.getRelativeScore(player), o1.getRelativeScore(player)));
            if (depth <= 1) {
                return new Result(boards.get(0).score, boards.get(0).lastCol);
            }
            List<Result> results = new ArrayList<>();
            for (int i = 0; i < 3 && i < boards.size(); ++ i) {
                Board board = boards.get(i);
                Result result = board.bestMove(depth - 1, (player + 1) % PLAYERS);
                if (result == null) {
                    results.add(new Result(board.score, board.lastCol));
                } else {
                    results.add(new Result(result.score, board.lastCol));
                }
            }
            Collections.sort(results, (o1, o2) -> Integer.compare(o2.getRelativeScore(player), o1.getRelativeScore(player)));
            return results.get(0);
        }
    }

    private Board board = null;

    @Override
    public int makeMove() {
        if (board == null) {
            int[][] data = getBoard();
            board = new Board(data.length, data[0].length);
            board.updateBoard(data);
        } else {
            board.updateBoard(getBoard());
        }

        Result result = board.bestMove(3, getID() - 1);
        return result == null ? -1 : result.lastCol;
    }
}