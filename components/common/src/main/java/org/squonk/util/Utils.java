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
     * @return true is a and b are both not null and a.equals(b)
     */
    public static boolean safeEquals(Object a, Object b) {
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    /**
     * @param a
     * @param b
     * @return true is a and b are both null or a.equals(b)
     */
    public static boolean safeEqualsIncludeNull(Object a, Object b) {
        if (a == null && b == null) {
            return true;
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

    public static boolean parseBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            try {
                return Boolean.parseBoolean(value.toString());
            } catch (Exception e) {
                LOG.warning("Failed to parse boolean: " + value);
                return defaultValue;
            }
        }
    }

    public static Integer parseInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(value.toString());
            } catch (Exception e) {
                LOG.warning("Failed to parse int: " + value);
                return defaultValue;
            }
        }
    }

    public static Float parseFloat(Object value, Float defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            try {
                return Float.parseFloat(value.toString());
            } catch (Exception e) {
                LOG.warning("Failed to parse float: " + value);
                return defaultValue;
            }
        }
    }


}
