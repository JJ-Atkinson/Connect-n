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
    public static int NUMBER_OF_GAMES = 100000;


    public void runGames() {

        List<List<Player>> games = IntStream
                .range(0, NUMBER_OF_GAMES)
                .mapToObj(value -> generateNextPlayers()).collect(Collectors.toList());
        List<Class<? extends Player>> winners = new ArrayList<>();
        for (int i = 0; i < games.size(); i++) {
            winners.add(runGame(games.get(i)));
        }
//                .parallel()
//                .map(this::runGame)
//                .collect(Collectors.toList());



        Map<Class<? extends Player>, Integer> totalScore = winningCounts(winners);
        System.out.println(prettyPrintScore(totalScore));
        if (SHOW_STATISTICS)
            printStatistics(games, winners);
    }


    private Class<? extends Player> runGame(List<Player> players) {
        Game game = new Game(players);
        game.runGame();

        return game.getWinner();
    }


    private Map<Class<? extends Player>, Integer> winningCounts
                (List<Class<? extends Player>> gameResults) {

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
        List<Class<? extends Player>> allPlayers = PlayerFactory.getPlayerTypes();
        add(allPlayers);
        add(allPlayers);
        add(allPlayers);
    }}).stream().filter(lst -> new HashSet<>(lst).size() == lst.size()).collect(Collectors.toList());
    private int playerPosition = 0;

    public List<Player> generateNextPlayers() {
        playerPosition = ++playerPosition % (playerCombinations.size() - 1);

        List<Player> players = PlayerFactory.create(playerCombinations.get(playerPosition));
        Collections.shuffle(players, ThreadLocalRandom.current());

        int nextId = 1;
        for (Player player : players)
            player.setID(nextId++);

        return players;
    }


    private void printStatistics
            (List<List<Player>> gameSets,
             List<Class<? extends Player>> winners) {

        HashMap<List<Class<? extends Player>>, List<Class<? extends Player>>> lineupWinnerPairs = new HashMap<>();

        for (int i = 0; i < gameSets.size(); i++) {
            List<Class<? extends Player>> gameSet = new ArrayList<>();
            List<Player> gameSetAsPlayer = gameSets.get(i);
            for (Player player : gameSetAsPlayer)
                gameSet.add(player.getClass());

            Class<? extends Player> winner = winners.get(i);

            lineupWinnerPairs.putIfAbsent(gameSet, new ArrayList<>());
            lineupWinnerPairs.get(gameSet).add(winner);
        }

        HashMap<List<Class<? extends Player>>, Map<Class<? extends Player>, Integer>> lineupScorePairs = new HashMap<>();

        lineupWinnerPairs.forEach((classes, classes2) -> {
            Map<Class<? extends Player>, Integer> winningCount = winningCounts(classes2);

            Iterator<Map.Entry<Class<? extends Player>, Integer>> iterator = winningCount.entrySet().iterator();
            while (iterator.hasNext())
                if (iterator.next().getValue() == 0) iterator.remove();


            lineupScorePairs.put(classes, winningCount);
        });


        lineupScorePairs.forEach((key, wins) -> {
            for (Class<? extends Player> aClass : key) {
                System.out.print(aClass.getSimpleName() + ", ");
            }
            System.out.println();
            System.out.println(
                    prettyPrintScore(wins));
        });
    }
}
