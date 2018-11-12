/*
 * Copyright (c) 2018 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.util;

import org.squonk.io.SquonkDataSource;
import org.squonk.io.StringDataSource;
import org.squonk.types.StreamType;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
     * @return true if a and b are both null or a.equals(b)
     */
    public static boolean safeEqualsIncludeNull(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        return a != null && a.equals(b);
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

    /** Instantiate an instance of a class by using a constructor for the specified types.
     * If no such constructor is defined null is returned. If the constructor exists but instantiation of it fails then
     * the exception associated with the failure is thrown
     *
     * @param type The class to instantiate
     * @param constructorArgs The constructor arguments to look for (in order). Values must not be null.
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static <T> T instantiate(Class<T> type, Class[] constructorTypes, Object[] constructorArgs)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor;
        try {
            constructor = type.getConstructor(constructorTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
        return constructor.newInstance(constructorArgs);
    }

}
