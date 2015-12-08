package connectn.game;

import connectn.players.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

/**
 * Created by Jarrett on 12/07/15.
 */
public class Runner {
    private final static int PLAYERS_PER_GAME = 3;
    public static int MINIMUM_NUMBER_OF_GAMES = 15000;
    private static int actNumberOfRounds = -1;

    private static List<Class<? extends Player>> unusedPlayers;
    private static Set<Class<? extends Player>> allPlayers;

    static {
        actNumberOfRounds = Math.max(MINIMUM_NUMBER_OF_GAMES * PlayerFactory.playerCreator.size() / PLAYERS_PER_GAME + 1,
                MINIMUM_NUMBER_OF_GAMES);
        unusedPlayers = PlayerFactory.getPlayerTypes();
        allPlayers = new HashSet<>(PlayerFactory.getPlayerTypes());
    }

    public static void runGames() {
        List<List<Player>> games = IntStream
                .range(0, actNumberOfRounds - 1)
                .mapToObj(value -> generateNextPlayers()).collect(Collectors.toList());
        List<Class<? extends Player>> winners = games.stream()
                .parallel()
                .map(Runner::runGame)
                .collect(Collectors.toList());

        Map<Class<? extends Player>, Integer> totalScore = winningCounts(winners);

        System.out.println(prettyPrintScore(totalScore));
    }

    private static Class<? extends Player> runGame(List<Player> players) {
        Game game = new Game(players);
        game.runGame();

        return game.getWinner();
    }

    private static Map<Class<? extends Player>, Integer> winningCounts(List<Class<? extends Player>> gameResults) {
        HashSet<Class<? extends Player>> losers = new HashSet<>(PlayerFactory.getPlayerTypes());
        losers.removeAll(gameResults);

        Map<Class<? extends Player>, Integer> winners =
                gameResults
                        .stream()
                        .collect(
                                groupingBy(
                                        Function.identity(),
                                        summingInt(e -> 1)));

        for (Class<? extends Player> loser : losers)
            winners.put(loser, 0);


        return winners;
    }

    private static String prettyPrintScore(Map<Class<? extends Player>, Integer> scores) {
        StringBuilder ret = new StringBuilder();

        ArrayList<Map.Entry<Class<? extends Player>, Integer>> scorePairsAsList = new ArrayList<>(scores.entrySet());
        Collections.sort(scorePairsAsList,
                (o1, o2) -> -Integer.compare(
                        (int) ((Map.Entry) (o1)).getValue(),
                        (int) ((Map.Entry) (o2)).getValue()));

        for (Map.Entry<Class<? extends Player>, Integer> scorePair : scorePairsAsList)
            ret.append(scorePair.getKey().getSimpleName())
                    .append(" -> ")
                    .append(scorePair.getValue())
                    .append('\n');

        return ret.toString();
    }

    private static List<Player> generateNextPlayers() {
        List<Player> players = new ArrayList<>();
        while (players.size() < PLAYERS_PER_GAME) {
            int playerDeficient = PLAYERS_PER_GAME - players.size();
            List<Class<? extends Player>> toUse;
            if (unusedPlayers.size() <= playerDeficient) {
                toUse = unusedPlayers;
            } else {
                Collections.shuffle(unusedPlayers, ThreadLocalRandom.current());
                toUse = unusedPlayers.subList(0, playerDeficient);
            }
            players.addAll(PlayerFactory.create(toUse));
            unusedPlayers.removeAll(toUse);
            if (unusedPlayers.size() == 0) {
                unusedPlayers.addAll(allPlayers);
            }
        }
        Collections.shuffle(players, ThreadLocalRandom.current());

        int nextId = 1;
        for (Player player : players)
            player.setID(nextId++);

        return players;
    }
}
