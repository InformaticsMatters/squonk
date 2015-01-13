package com.im.lac.util;

import java.util.AbstractList;
import java.util.List;

/**
 *
 * @author timbo
 */
public class CollectionUtils {

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

}
