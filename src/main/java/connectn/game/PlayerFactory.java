package connectn.game;

import connectn.players.*;

import java.util.*;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toCollection;

/**
 * Created by Jarrett on 12/07/15.
 */
public class PlayerFactory {
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
        playerCreator.put(StraightForwardBot.class, StraightForwardBot::new);
        playerCreator.put(PackingBot.class, PackingBot::new);
        playerCreator.put(JealousBot.class, JealousBot::new);
        playerCreator.put(RowBot.class, RowBot::new);
        playerCreator.put(Steve.class, Steve::new);
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
