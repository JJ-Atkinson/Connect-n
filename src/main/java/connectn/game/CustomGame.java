package connectn.game;

import connectn.players.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jarrett on 12/09/15.
 */
public class CustomGame {
    public static void main(String[] args) {
        List<Player> players = genPlayers();
        Game game = new Game(players);
        game.runGame();

        System.out.println(game.prettyPrintBoard());
        System.out.println(game.scoreGame());
    }

    private static List<Player> genPlayers() {
        List<Player> players = new ArrayList<Player>() {
            {
                add(new UserBot());
//                add(new UserBot2());
                add(new BasicBlockBot());
                add(new MaxGayne());
            }
        };


        int nextId = 1;
        for (Player player : players)
            player.setID(nextId++);

        return players;
    }
}
