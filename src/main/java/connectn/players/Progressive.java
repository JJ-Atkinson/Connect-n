package connectn.players;

/**
 * http://codegolf.stackexchange.com/a/65987/42736
 */
public class Progressive extends Player {
    @Override
    public int makeMove() {
        int move = 0;

        for (int n = getBoardSize()[0]; n > 2; n -= 2)
            for (int i = 0; i < getBoardSize()[0]; i += n)
                if (ensureValidMove(i)) {
                    move = i;
                    break;
                }

        return move;
    }
}
