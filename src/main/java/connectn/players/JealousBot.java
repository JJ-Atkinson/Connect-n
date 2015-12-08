package connectn.players;

public class JealousBot extends Player {

    @Override
    public int makeMove() {
        int move = 0;
        boolean madeMove = false;
        int[] boardSize = getBoardSize();
        int id = getID();
        int[][] board = getBoard();

        if (getTurn() != 0) {
            for (int col = 0; col < boardSize[0]; col++) {
                for (int row = 0; row < boardSize[1]; row++) {
                    if (ensureValidMove(col)) {
                        if (board[col][row] != EMPTY_CELL && board[col][row] != id) {
                            move = col;
                            madeMove = true;
                            break;
                        }
                    }
                }
                if (madeMove) break;
            }

            if (!madeMove) {
                int temp = (int) Math.round(random() * boardSize[0]);
                while (madeMove != true) {
                    temp = (int) Math.round(random() * boardSize[0]);
                    if (ensureValidMove(temp)) {
                        madeMove = true;
                    }
                }
                move = temp;
            }
        } else {
            move = (int) Math.round(random() * boardSize[0]);
        }

        return move;
    }
}