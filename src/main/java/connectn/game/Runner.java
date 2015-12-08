package connectn.game;

import connectn.players.Player;
import connectn.util.ListUtil;

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
    private static final boolean SHOW_STATISTICS = true;
    private final static int PLAYERS_PER_GAME = 3;
    public static int MINIMUM_NUMBER_OF_GAMES = 15000;
    private static int actNumberOfRounds = -1;

    private List<Class<? extends Player>> unusedPlayers;
    private Set<Class<? extends Player>> allPlayers;

    static {
        actNumberOfRounds = Math.max(MINIMUM_NUMBER_OF_GAMES * PlayerFactory.playerCreator.size() / PLAYERS_PER_GAME + 1,
                MINIMUM_NUMBER_OF_GAMES);
    }


    {
        unusedPlayers = PlayerFactory.getPlayerTypes();
        allPlayers = new HashSet<>(PlayerFactory.getPlayerTypes());
    }


    public void runGames() {
        List<List<Player>> games = IntStream
                .range(0, actNumberOfRounds - 1)
                .mapToObj(value -> generateNextPlayers()).collect(Collectors.toList());
        List<Class<? extends Player>> winners = games.stream()
                .parallel()
                .map(this::runGame)
                .collect(Collectors.toList());

        Map<Class<? extends Player>, Integer> totalScore = winningCounts(winners);

        System.out.println(prettyPrintScore(totalScore));
    }

    private Class<? extends Player> runGame(List<Player> players) {
        Game game = new Game(players);
        game.runGame();

        return game.getWinner();
    }

    private Map<Class<? extends Player>, Integer>
            winningCounts(List<Class<? extends Player>> gameResults) {

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

    private String prettyPrintScore(Map<Class<? extends Player>, Integer> scores) {
        StringBuilder ret = new StringBuilder();

        ArrayList<Map.Entry<Class<? extends Player>, Integer>>
                scorePairsAsList = new ArrayList<>(scores.entrySet());
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

    private List<List<Class<? extends Player>>> playerCombinations = ListUtil.combinations(new ArrayList<Iterable<Class<? extends Player>>>() {{
        add(allPlayers);
        add(allPlayers);
        add(allPlayers);
    }});
    private int playerPosition = 0;

    public List<Player> generateNextPlayers() {
        playerPosition = ++playerPosition % (playerCombinations.size() - 1);

        List<Player> players = PlayerFactory.create(playerCombinations.get(playerPosition));

        int nextId = 1;
        for (Player player : players)
            player.setID(nextId++);

        return players;
    }

    // todo // FIXME: 12/08/15
//    private static void printStatistics
//            (List<List<Class<? extends Player>>> gameSets,
//             List<Class<? extends Player>> winners) {
//
//        HashMap<Set<Class<? extends Player>>, List<Class<? extends Player>>> lineupScorePairs = new HashMap<>();
//
//        for (int i = 0; i < gameSets.size(); i++) {
//            HashSet<Class<? extends Player>> gameSet = new HashSet<>(gameSets.get(i));
//            Class<? extends Player> winner = winners.get(i);
//
//            lineupScorePairs.putIfAbsent(gameSet, new ArrayList<>());
//            lineupScorePairs.get(gameSet).add(winner);
//        }

//        lineupScorePairs.forEach((key, ) -> );
//    }
}
