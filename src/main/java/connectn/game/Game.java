package connectn.game;

import connectn.players.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jarrett on 12/07/15.
 */
public class Game {
    public final static int EMPTY_CELL = -1;

    private final static int TURNS_PER_PLAYER = 24;
    /**
     * Board is a 2d array of ints representing player ids.
     * Access as [coll][row from bottom].
     */
    private final int[][] board;
    private final List<Player> players;
    private final Map<Integer, Class<? extends Player>> idToClass;

    private int turn = 0;

    public Game(List<Player> players) {
        board = genBoard(players.size());
        this.players = players;

        for (Player player : players)
            player.setGame(this);

        idToClass = new HashMap<>();
        for (Player player : players)
            idToClass.put(player.getID(), player.getClass());
    }

    public void runGame() {
        ArrayList<Player> playerList = new ArrayList<>(this.players);

        for (int turnCount = 0; turnCount < TURNS_PER_PLAYER; turnCount++) {
            turn = turnCount + 1;

            playerList.forEach(this::doPlayerMove);
        }

    }

    public Map<Class<? extends Player>, Integer> scoreGame() {
        Map<Integer, Integer> idToScore = new HashMap<>();
        for (Player player : players)
            idToScore.put(player.getID(), 0);

        int[][] directionVectors = new int[][]{
                new int[]{1, 0},
                new int[]{1, 1},
                new int[]{-1, 1},
                new int[]{0, 1}
        };

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int currId = board[i][j];

                if (currId != EMPTY_CELL)
                    for (int[] directionVector : directionVectors)
                        if (is4InARow(i, j, directionVector))
                            idToScore.put(currId, idToScore.get(currId) + 1);
            }
        }

        Map<Class<? extends Player>, Integer> classToScore = new HashMap<>();
        idToScore.forEach((id, score) -> classToScore.put(idToClass.get(id), score));
        return classToScore;
    }

    private boolean is4InARow(int coll, int row, int[] directionVector) {
        int id = board[coll][row];

        for (int i = 0; i < 4; i++) {
            int x = coll + (directionVector[0] * i);
            int y = row + (directionVector[1] * i);
            if (!boardContains(x, y) || board[x][y] != id)
                return false;
        }

        return true;
    }

    public Class<? extends Player> getWinner() {
        Map<Class<? extends Player>, Integer> scoreCard = scoreGame();

        Map.Entry<Class<? extends Player>, Integer> winner =
                scoreCard.entrySet()
                        .stream()
                        .max((o1, o2) -> Integer.compare(
                                (int) ((Map.Entry) (o1)).getValue(),
                                (int) ((Map.Entry) (o2)).getValue())).get();

        return winner.getKey();
    }

    private void doPlayerMove(Player player) {
        int attemptedMove = player.makeMove();

        if (ensureValidMove(attemptedMove))
            move(player.getID(), attemptedMove);
    }

    private void move(int playerId, int coll) {
        for (int i = 0; i < board[coll].length; i++) {
            if (board[coll][i] == EMPTY_CELL) {
                board[coll][i] = playerId;
                return;
            }
        }
    }

    public int[][] getDeepCopyOfBoard() {
        int[][] ret = new int[board.length][board.length];

        for (int i = 0; i < ret.length; i++)
            System.arraycopy(board[i], 0, ret[i], 0, ret[i].length);

        return ret;
    }

    private static int[][] genBoard(int playerCount) {
        int size = (int) Math.ceil(Math.sqrt(playerCount * TURNS_PER_PLAYER));
        int[][] ret = new int[size][size];

        for (int i = 0; i < ret.length; i++)
            for (int j = 0; j < ret[i].length; j++)
                ret[i][j] = EMPTY_CELL;

        return ret;
    }

    public boolean ensureValidMove(int coll) {
        if (!boardContains(coll, 0))
            return false;

        return board[coll][board[coll].length - 1] == EMPTY_CELL;
    }

    public boolean boardContains(int coll, int row) {
        return coll < board.length && coll >= 0 && row < board[coll].length && row >= 0;
    }

    public int[] getBoardSize() {
        return new int[]{board.length, board[0].length};
    }

    public int getTurn() {
        return turn;
    }

    public int getTotalTurns() {
        return TURNS_PER_PLAYER;
    }

    public String prettyPrintBoard() {
        StringBuilder ret = new StringBuilder();

        int height = board[0].length - 1;

        for (int j = height; j >= 0; j--) {
            for (int[] aBoard : board) {
                ret.append(String.format("%+3d", aBoard[j]));
            }
            ret.append('\n');
        }
//                ret.append(Arrays.toString(board[i])).append('\n');

        return ret.toString();
    }
}
