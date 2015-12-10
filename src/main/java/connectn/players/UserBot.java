package connectn.players;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jarrett on 12/09/15.
 */
public class UserBot extends Player {

    private BufferedReader reader;

    private List<Character> idToChr = Arrays.asList('/', '+', '~');

    {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public int makeMove() {
        System.out.println("You are: " + getChrForId(getID()));
        System.out.println("Last move: " + getLastMove(2) + ", " + getLastMove(1));
        System.out.println("Turns left: " + (getTotalTurns() - getTurn()));
        printBoard();

        int input = -1;
        try {
            input = Integer.parseInt(reader.readLine());
        } catch (Exception e) {}

        if (!ensureValidMove(input))
            System.err.println("Turn skipped");

        return input;
    }

    private String getLastMove(int dist) {
        try {
            List<Pair<Integer, Integer>> history = getGame().getHistory();
            return history.get(history.size() - dist).getValue().toString();
        } catch (Exception e) {}
        return "";
    }

    private void printBoard() {
        for (int i = 0; i < 9; i++)
            System.out.print(" " + i);

        System.out.println();

        for (int i = 0; i < 9; i++) {
            System.out.print("--");
        }
        System.out.println();
        System.out.println(prettyPrintBoard());
    }

    private String prettyPrintBoard() {
        int[][] board = getBoard();
        StringBuilder ret = new StringBuilder();
        int height = board[0].length - 1;
        for (int j = height; j >= 0; j--) {
            for (int i = 0; i < board.length; i++)
                ret.append(getChrForId(board[i][j]));

            ret.append('\n');
        }
        return ret.toString();
    }

    private String getChrForId(int id) {
        switch (id) {
            case -1:
                return " _";
            case 0:
                return " *";
            case 1:
                return " +";
            case 2:
                return " ~";
        }
        return "";
    }

    @Override
    public int hashCode() {
        return ((Integer) getID()).hashCode();
    }
}
