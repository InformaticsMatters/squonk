package org.squonk.property;

import org.squonk.types.BasicObject;

import java.util.function.Predicate;

/**
 * Created by timbo on 27/05/16.
 */
public abstract class PropertyFilter<T> implements Predicate<BasicObject> {

    protected final String propertyName;
    protected final boolean includeNull;
    protected final T min;
    protected final T max;
    protected final Class<T> dataType;


    protected PropertyFilter(Class<T> dataType, String propertyName, boolean includeNull, T min, T max) {
        this.dataType = dataType;
        this.propertyName = propertyName;
        this.includeNull = includeNull;
        this.min = min;
        this.max = max;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isIncludeNull() {
        return includeNull;
    }

    public T getMax() {
        return max;
    }

    public T getMin() {
        return min;
    }

    public Class<T> getDataType() {
        return dataType;
    }

    public boolean test(BasicObject bo) {
        return test(bo, min, max);
    }

    public boolean isActive() {
        return min != null || max != null;
    }

    public abstract boolean test(BasicObject bo, T min, T max);

    public abstract PropertyFilter<T> derrive(T min, T max);

    @Override
    public String toString() {
        return "PropertyFilter [" + getClass().getName() + " propertyName=" + propertyName + " min=" + min + " max=" + max + "]";
    }

    public static class IntegerRangeFilter extends PropertyFilter<Integer> {

        public IntegerRangeFilter(String propName, boolean includeNull, Integer min, Integer max) {
            super(Integer.class, propName, includeNull, min, max);
        }

        public boolean test(BasicObject bo, Integer min, Integer max) {
            Object o = bo.getValue(propertyName);
            if (o == null || !(o instanceof Number)) {
                return includeNull;
            }
            int value = ((Number)o).intValue();
            if (min != null && min > value) {
                return false;
            }
            if (max != null && max < value) {
                return false;
            }
            return true;
        }

        public PropertyFilter<Integer> derrive(Integer min, Integer max) {
            return new IntegerRangeFilter(propertyName, includeNull, min == null ? this.min : min, max == null ? this.max : max);
        }

    }

    public static class DoubleRangeFilter extends PropertyFilter<Double> {

        public DoubleRangeFilter(String propName, boolean includeNull, Double min, Double max) {
            super(Double.class, propName, includeNull, min, max);
        }

        public boolean test(BasicObject bo, Double min, Double max) {
            Object o = bo.getValue(propertyName);
            if (o == null || !(o instanceof Number)) {
                return includeNull;
            }
            double value = ((Number)o).doubleValue();
            if (min != null && min > value) {
                return false;
            }
            if (max != null && max < value) {
                return false;
            }
            return true;
        }

        public PropertyFilter<Double> derrive(Double min, Double max) {
            return new DoubleRangeFilter(propertyName, includeNull, min == null ? this.min : min, max == null ? this.max : max);
        }

    }

}
