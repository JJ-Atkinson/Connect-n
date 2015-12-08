package connectn.players;

/**
 * Created by Jarrett on 12/07/15.
 */
public class BuggyBot extends Player {
    @Override
    public int makeMove() {
        return getBoardSize()[1] - 1;
    }
}
