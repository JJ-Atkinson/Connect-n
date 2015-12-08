package connectn.players;

/**
 * Created by Jarrett on 12/07/15.
 */
public class RandomBot extends Player {
    @Override
    public int makeMove() {
        int attemptedMove = (int) Math.round(random() * getBoardSize()[0]);
        while (!ensureValidMove(attemptedMove))
            attemptedMove = (int) Math.round(random() * getBoardSize()[0]);

        return attemptedMove;
    }
}
