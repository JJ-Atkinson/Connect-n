package connectn.players;

public class PackingBot extends Player {
    @Override
    public int makeMove() {
        int move = 0;
        int[] sizes = getBoardSize();
        if (getTurn() == 0)
            return sizes[0] / 2 + sizes[0] % 2;

        int[][] board = getBoard();
        int[] flatBoard = new int[sizes[0]];
        //Creating a flat mapping of my tokens
        for (int i = 0; i < sizes[0]; i++)
            for (int j = 0; j < sizes[1]; j++)
                if (board[i][j] != getID())
                    flatBoard[i]++;

        int max = 0;
        int range = 0;
        for (int i = 0; i < flatBoard.length; i++) {
            if (flatBoard[i] != 0)
                range++;
            if (flatBoard[i] > flatBoard[max])
                max = i;
        }

        int sens = (Math.random() > 0.5) ? 1 : -1;
        move = ((int) (Math.random() * (range + 1) * sens)) + max;

        while (!ensureValidMove(move)) {
            move = (move + 1 * sens) % sizes[0];
            if (move < 0)
                move = sizes[0] - 1;
        }
        return move;
    }


}