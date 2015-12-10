package connectn.game;

import connectn.players.*;

import java.util.*;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toCollection;

/**
 * Created by Jarrett on 12/07/15.
 */
public class PlayerFactory {
    public final static Map<Class<? extends Player>, Supplier<? extends Player>> playerCreator =
            new HashMap<Class<? extends Player>, Supplier<? extends Player>>() {{

                put(RandomBot.class, RandomBot::new);
                put(OnePlayBot.class, OnePlayBot::new);
                put(BuggyBot.class, BuggyBot::new);
                put(BasicBlockBot.class, BasicBlockBot::new);
                put(Progressive.class, Progressive::new);
                put(StraightForwardBot.class, StraightForwardBot::new);
                put(PackingBot.class, PackingBot::new);
                put(JealousBot.class, JealousBot::new);
                put(RowBot.class, RowBot::new);
                put(Steve.class, Steve::new);
                put(MaxGayne.class, MaxGayne::new);
            }};


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
