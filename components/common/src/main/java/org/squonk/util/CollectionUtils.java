package org.squonk.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class CollectionUtils {

    private static final Logger LOG = Logger.getLogger(CollectionUtils.class.getName());

    public static List<Integer> asIntegerList(final int[] is) {
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int i) {
                return is[i];
            }

            @Override
            public int size() {
                return is.length;
            }
        };
    }

    /**
     * Create an Iterator that enumerates over all permutations of the sources.
     * The Iterator is backed by a blocking queue of the specified size
     *
     * @param <T>
     * @param queueSize
     * @param sources Iterables of the inputs. These are consolidated into Lists
     * before iteration.
     *
     * @return
     */
    public static <T> Iterator<List<T>> combinatorialIterator(int queueSize, Iterable<T>... sources) {

        // TODO - the first iterable does not need to be collected as its only iterated once.
        // this could be optimised to increase speed and reduce memory use
        List<T>[] lists = new List[sources.length];
        int i = 0;
        for (Iterable<T> mols : sources) {
            List<T> list = new ArrayList<>();
            for (T mo : mols) {
                list.add(mo);
            }
            lists[i] = list;
            i++;
            IOUtils.closeIfCloseable(mols);
        }

        return combinatorialIterator(queueSize, lists);
    }

    /**
     * Create an Iterator that enumerates over all permutations of the sources.
     * The Iterator is backed by a blocking queue of the specified size
     *
     * @param <T>
     * @param queueSize
     * @param sources Lists of the inputs
     * @return
     */
    public static <T> Iterator<List<T>> combinatorialIterator(int queueSize, List<T>... sources) {

        final CloseableQueue<List<T>> molsQ = new CloseableQueue<>(queueSize);
        Thread t = new Thread(() -> {
            try {
                List<T> items = new ArrayList<>(Collections.nCopies(sources.length, null));
                iterate(molsQ, 0, items, sources);
            } finally {
                molsQ.close();
            }
        }
        );
        t.start();

        return molsQ;
    }

    protected static <T> void iterate(CloseableQueue<List<T>> q, int level, List<T> items, List<T>... sources) {
        Iterator<T> it = sources[level].iterator();
        int i = 0;
        while (it.hasNext()) {
            i++;
            items.set(level, it.next());
            if (sources.length > level + 1) {
                iterate(q, level + 1, items, sources);
            } else {
                List<T> clone = new ArrayList<>();

                clone.addAll(items);
                //LOG.info("Adding molecules " + clone);
                //LOG.log(Level.INFO, "Queue size is {0}", q.currentQueueSize());
                q.add(clone);
            }
        }
    }

}
