import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.*;
import java.util.stream.IntStream;


/**
 * Created by Jarrett on 12/05/15.
 */
public class ConnectNController {
    public static void main(String[] args) {
        System.out.println("Starting simulation");
        Runner.runGames();
    }

    static class Runner {
        private final static int PLAYERS_PER_GAME = 3;
        public static int MINIMUM_NUMBER_OF_GAMES  = 15000;
        private static int actNumberOfRounds = -1;

        private static List<Class<? extends Player>> unusedPlayers;
        private static Set<Class<? extends Player>> allPlayers;

        static {
            actNumberOfRounds = Math.max(MINIMUM_NUMBER_OF_GAMES*PlayerFactory.playerCreator.size()/PLAYERS_PER_GAME + 1,
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
                                            (int)((Map.Entry)(o1)).getValue(),
                                            (int)((Map.Entry)(o2)).getValue()));

            for (Map.Entry<Class<? extends Player>, Integer> scorePair : scorePairsAsList)
                ret     .append (scorePair.getKey().getSimpleName())
                        .append ( " -> ")
                        .append (scorePair.getValue())
                        .append ('\n');

            return ret.toString();
        }

        private static List<Player> generateNextPlayers(){
            List<Player> players = new ArrayList<>();
            while (players.size() < PLAYERS_PER_GAME){
                int playerDeficient = PLAYERS_PER_GAME - players.size();
                List<Class<? extends Player>> toUse;
                if (unusedPlayers.size() <= playerDeficient){
                    toUse = unusedPlayers;
                } else {
                    Collections.shuffle(unusedPlayers, ThreadLocalRandom.current());
                    toUse = unusedPlayers.subList(0, playerDeficient);
                }
                players.addAll(PlayerFactory.create(toUse));
                unusedPlayers.removeAll(toUse);
                if (unusedPlayers.size() == 0){
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

    static class PlayerFactory {
        public static Set<Player> createAllPlayers() {
            Set<Player> ret = new HashSet<>();

            playerCreator.forEach((aClass, supplier) -> ret.add(supplier.get()));

            int nextId = 1;
            for (Player player : ret)
                player.setID(nextId++);

            return ret;
        }

        public final static Map<Class<? extends Player>, Supplier<? extends Player>> playerCreator = new HashMap<>();

        static {
            playerCreator.put(RandomBot.class, RandomBot::new);
            playerCreator.put(OnePlayBot.class, OnePlayBot::new);
            playerCreator.put(BuggyBot.class, BuggyBot::new);
            playerCreator.put(BasicBlockBot.class, BasicBlockBot::new);
            playerCreator.put(Progressive.class, Progressive::new);
        }

        public static List<Class<? extends Player>> getPlayerTypes() {
            ArrayList<Class<? extends Player>> ret = new ArrayList<>();
            playerCreator.forEach((aClass, supplier) -> ret.add(aClass));
            return ret;
        }

        public static List<Player> create(Collection<Class<? extends Player>> toUse) {
            return toUse
                    .stream()
                    .map(playerCreator::get)
                    .map(Supplier::get)
                    .collect(toCollection(ArrayList<Player>::new));
        }
    }

    static class Game {

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

            int[][] directionVectors = new int[][] {
                    new int[] {1, 0},
                    new int[] {1, 1},
                    new int[] {0, 1}
            };

            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    int currId = board[i][j];

                    if (currId != -1)
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
                if (!boardContains(x,y) || board[x][y] != id)
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
                if (board[coll][i] == -1) {
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
            int size = (int)Math.ceil(Math.sqrt(playerCount * TURNS_PER_PLAYER));
            int[][] ret = new int[size][size];

            for (int i = 0; i < ret.length; i++)
                for (int j = 0; j < ret[i].length; j++)
                    ret[i][j] = -1;

            return ret;
        }

        public boolean ensureValidMove(int coll) {
            if (!boardContains(coll, 0))
                return false;

            return board[coll][board[coll].length - 1] == -1;
        }

        public boolean boardContains(int coll, int row) {
            return coll < board.length && coll >= 0 && row < board[coll].length && row >= 0;
        }

        public int[] getBoardSize() {
            return new int[] {board.length, board[0].length};
        }

        public int getTurn() {
            return turn;
        }

        public int getTotalTurns() {
            return TURNS_PER_PLAYER;
        }

        public String prettyPrintBoard() {
            StringBuilder ret = new StringBuilder();

            int height = board[0].length-1;

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

    static abstract class Player {
        private int ID;
        private Game game;

        abstract int makeMove();

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

        /** return {coll count, row count} */
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

    static class RandomBot extends Player {
        @Override
        int makeMove() {
            int attemptedMove = (int)Math.round(random() * getBoardSize()[0]);
            while (!ensureValidMove(attemptedMove))
                attemptedMove = (int)Math.round(random() * getBoardSize()[0]);

            return attemptedMove;
        }
    }

    static class OnePlayBot extends Player {
        @Override
        int makeMove() {
            int attemptedMove = 0;

            for (int i = 0; i < getBoardSize()[0]; i++)
                if (ensureValidMove(i)) {
                    attemptedMove = i;
                    break;
                }

            return attemptedMove;
        }
    }

    static class BasicBlockBot extends Player {
        @Override
        int makeMove() {
            List<Integer> inARows = detectInARows();
            double chanceOfBlock = 0.5;

            if (inARows.isEmpty())
                chanceOfBlock = 0;

            if (random() < chanceOfBlock) {
                return inARows.get((int)Math.round(random() * (inARows.size() - 1)));
            } else {
                return (int)Math.round(random() * getBoardSize()[0]);
            }
        }


        /**
         * Very limited - just detects vertical in a rows
         *
         * @return A list of colls that have 4 in a row vertical
         */
        private List<Integer> detectInARows() {
            List<Integer> ret = new ArrayList<>();
            int[][] board = getBoard();

            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    int currId = board[i][j];
                    if (currId != -1 && is4InARowVertical(i, j, board)) {
                        ret.add(i);
                    }
                }
            }

            return ret;
        }

        private boolean is4InARowVertical(int coll, int row, int[][] board) {
            int id = board[coll][row];

            for (int i = 0; i < 4; i++) {
                int y = row + i;
                if (!boardContains(coll,y) || board[coll][y] != id)
                    return false;
            }
            return true;
        }

    }

    static class BuggyBot extends Player {
        @Override
        int makeMove() {
            return getBoardSize()[1] - 1;
        }
    }


    /**
     * http://codegolf.stackexchange.com/a/65987/42736
     */
    static class Progressive extends Player {
        @Override
        int makeMove() {
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
}