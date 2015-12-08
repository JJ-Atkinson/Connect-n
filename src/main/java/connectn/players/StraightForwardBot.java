package connectn.players;

import connectn.players.Player;

public class StraightForwardBot extends Player {
    private int lastMove = 0;

    @Override
    public int makeMove() {
        for (int i = lastMove + 1; i < getBoardSize()[0]; i++) {
            if (ensureValidMove(i)) {
                lastMove = i;
                return i;
            }
        }
        for (int i = 0; i < lastMove; i++) {
            if (ensureValidMove(i)) {
                lastMove = i;
                return i;
            }
        }
        return 0;
    }
}