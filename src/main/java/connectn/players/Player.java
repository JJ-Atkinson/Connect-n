package connectn.players;

import connectn.game.Game;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Jarrett on 12/07/15.
 */
public abstract class Player {
    public final static int EMPTY_CELL = Game.EMPTY_CELL;

    private int ID;
    private Game game;

    public abstract int makeMove();

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int[][] getBoard() {
        return game.getDeepCopyOfBoard();
    }

    public boolean ensureValidMove(int coll) {
        return game.ensureValidMove(coll);
    }

    /**
     * return {coll count, row count}
     */
    public int[] getBoardSize() {
        return game.getBoardSize();
    }

    public int getTurn() {
        return game.getTurn();
    }

    public int getTotalTurns() {
        return game.getTotalTurns();
    }

    public double random() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public boolean boardContains(int coll, int row) {
        return game.boardContains(coll, row);
    }
}
