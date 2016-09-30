package org.squonk.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 17/12/15.
 */
public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    /**
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

    public static String intArrayToString(int[] values, String separator) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) b.append(separator);
            b.append(values[i]);
        }

        return b.toString();
    }

    public static int[] stringToIntArray(String s) {
        if (s == null) return new int[0];
        try {
            String[] vals = s.split(" *, *");
            int[] ints = new int[vals.length];
            for (int i = 0; i < vals.length; i++) {
                ints[i] = Integer.valueOf(vals[i]);
            }
            return ints;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to parse int array: " + s, e.getLocalizedMessage());
            return new int[0];
        }
    }

}
