package connectn.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Jarrett on 12/08/15.
 */
public class ListUtil {
    /**
     * Stolen from groovy.util.GroovyCollections ;)
     *
     * Finds all combinations of items from the given Iterable aggregate of collections.
     * So, <code>combinations([[true, false], [true, false]])</code>
     * is <code>[[true, true], [false, true], [true, false], [false, false]]</code>
     * and <code>combinations([['a', 'b'],[1, 2, 3]])</code>
     * is <code>[['a', 1], ['b', 1], ['a', 2], ['b', 2], ['a', 3], ['b', 3]]</code>.
     * If a non-collection item is given, it is treated as a singleton collection,
     * i.e. <code>combinations([[1, 2], 'x'])</code> is <code>[[1, 'x'], [2, 'x']]</code>.
     *
     * @param collections the Iterable of given collections
     * @return a List of the combinations found
     * @since 2.2.0
     */
    public static <T> List<List<T>> combinations(Iterable<Iterable<T>> collections) {
        List collectedCombos = new ArrayList();
        for (Iterable items : collections) {
            if (collectedCombos.isEmpty()) {
                for (Object item : items) {
                    List l = new ArrayList();
                    l.add(item);
                    collectedCombos.add(l);
                }
            } else {
                List savedCombos = new ArrayList(collectedCombos);
                List newCombos = new ArrayList();
                for (Object value : items) {
                    for (Object savedCombo : savedCombos) {
                        List oldList = new ArrayList((List) savedCombo);
                        oldList.add(value);
                        newCombos.add(oldList);
                    }
                }
                collectedCombos = newCombos;
            }
        }
        return collectedCombos;
    }
}
