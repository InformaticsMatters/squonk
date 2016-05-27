package org.squonk.property;

import com.im.lac.types.BasicObject;

import java.util.function.Predicate;

/**
 * Created by timbo on 27/05/16.
 */
public abstract class PropertyFilter implements Predicate<BasicObject> {

    protected final String propName;
    protected final boolean includeNull;


    protected PropertyFilter(String propName, boolean includeNull) {
        this.propName = propName;
        this.includeNull = includeNull;
    }

    public abstract boolean test(BasicObject bo);


    public static class IntegerRangeFilter extends PropertyFilter {

        private final Integer min;
        private final Integer max;

        public IntegerRangeFilter(String propName, boolean includeNull, Integer min, Integer max) {
            super(propName, includeNull);
            this.min = min;
            this.max = max;
        }

        public boolean test(BasicObject bo) {
            Object o = bo.getValue(propName);
            if (o == null || !(o instanceof Number)) {
                return includeNull;
            }
            int value = ((Number)o).intValue();
            if (min != null && min >= value) {
                return false;
            }
            if (max != null && max <= value) {
                return false;
            }
            return true;
        }

    }

    public static class DoubleRangeFilter extends PropertyFilter {

        private final Double min;
        private final Double max;

        public DoubleRangeFilter(String propName, boolean includeNull, Double min, Double max) {
            super(propName, includeNull);
            this.min = min;
            this.max = max;
        }

        public boolean test(BasicObject bo) {
            Object o = bo.getValue(propName);
            if (o == null || !(o instanceof Number)) {
                return includeNull;
            }
            double value = ((Number)o).doubleValue();
            if (min != null && min >= value) {
                return false;
            }
            if (max != null && max <= value) {
                return false;
            }
            return true;
        }

    }

}
