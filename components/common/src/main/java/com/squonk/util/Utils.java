package com.squonk.util;

/**
 * Created by timbo on 17/12/15.
 */
public class Utils {

    /**
     *
     * @param a
     * @param b
     * @return true is a and b are bothy not null and a.equals(b)
     */
    public static boolean safeEquals(Object a, Object b) {
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }


}
