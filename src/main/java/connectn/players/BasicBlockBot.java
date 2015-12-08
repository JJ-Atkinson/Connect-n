package connectn.players;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jarrett on 12/07/15.
 */
public class BasicBlockBot extends Player {
    @Override
    public int makeMove() {
        List<Integer> inARows = detectInARows();
        double chanceOfBlock = 0.5;

        if (inARows.isEmpty())
            chanceOfBlock = 0;

        if (random() < chanceOfBlock) {
            return inARows.get((int) Math.round(random() * (inARows.size() - 1)));
        } else {
            return (int) Math.round(random() * getBoardSize()[0]);
        }
    }


    /**
     * Very limited - just detects vertical in a rows
     *
     * @return A list of colls that have 4 in a row vertical
     */
    private List<Integer> detectInARows() {
        List<Integer> ret = new ArrayList<>();
        int[][] board = getBoard();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int currId = board[i][j];
                if (currId != -1 && is4InARowVertical(i, j, board)) {
                    ret.add(i);
                }
            }
        }

        return ret;
    }

    private boolean is4InARowVertical(int coll, int row, int[][] board) {
        int id = board[coll][row];

        for (int i = 0; i < 4; i++) {
            int y = row + i;
            if (!boardContains(coll, y) || board[coll][y] != id)
                return false;
        }
        return true;
    }

}
