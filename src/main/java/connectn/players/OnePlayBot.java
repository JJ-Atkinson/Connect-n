package connectn.players;

/**
 * Created by Jarrett on 12/07/15.
 */
public class OnePlayBot extends Player {
    @Override
    public int makeMove() {
        int attemptedMove = 0;

        for (int i = 0; i < getBoardSize()[0]; i++)
            if (ensureValidMove(i)) {
                attemptedMove = i;
                break;
            }

        return attemptedMove;
    }
}
