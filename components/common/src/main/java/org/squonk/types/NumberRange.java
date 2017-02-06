package org.squonk.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/** A numeric range bounded by min and max values. Also allows values to be tested to determine in they are within those
 * bounds (inclusive of the bounds e.g. 1 is within the range 1 to 10).
 *
 * Created by timbo on 04/08/16.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class NumberRange<T extends Number & Comparable<T>> implements Serializable, Predicate, CompositeType {

    private static final String PROP_MIN_VALUE = "MinValue";
    private static final String PROP_MAX_VALUE = "MaxValue";

    protected T minValue;
    protected T maxValue;

    private NumberRange() {}
    public NumberRange(T minValue, T maxValue) {
        validate(minValue, maxValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    private NumberRange(String str) {
        T min = null, max = null;
        String[] parts = str.split("\\|");
        if (parts.length > 0) {
            if (parts[0].trim().length() > 0) {
                min = convert(parts[0]);
            }
            if (parts.length == 2 && parts[1].trim().length() > 0) {
                max = convert(parts[1]);
            }
        } else if ("|".equals(str)) {
            // this is OK - min and max are undefined
        } else {
            throw new IllegalArgumentException("Can't parse range value: " + str);
        }
        validate(min, max);
        this.minValue = min;
        this.maxValue = max;
    }

    private void validate(T min, T max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Min value must be less than max value");
        }
    }

    /** Create a new instance of the appropriate type.
     *
     * @param type Class for the type which can either be the data class (e.g. java.lang.Integer) or the range class (e.g.
     *             the Integer inner class of this class)
     * @return
     */
    // TODO - should be possible to generify this properly?
    public static NumberRange create(Class type) {
        if (type == java.lang.Integer.class || type == Integer.class) {
                return new Integer();
        } else if (type == java.lang.Float.class || type == Float.class) {
            return new Float();
        } else if (type == java.lang.Double.class || type == Double.class) {
            return new Double();
        } else {
            throw new IllegalArgumentException("Number range of type " + type.getName() + " not supported");
        }
    }

    protected abstract T convert(Object o);

    @JsonIgnore
    public abstract Class<T> getType();

    @Override
    public String toString() {
        return (minValue == null ? "" : minValue) + "|" + (maxValue == null ? "" : maxValue);
    }

    public T getMinValue() { return minValue; }
    public void setMinValue(T value) {
        validate(value, maxValue);
        this.minValue = value;
    }

    public T getMaxValue() { return maxValue; }
    public void setMaxValue(T value) {
        validate(minValue, value);
        this.maxValue = value;
    }

    /** Is the specified value within the range.
     * All comparisons are performed as the double values to allow comparison of different types of number to be performed.
     * If a min or max value is not defined then that bound is ignored.
     * Examples:
     * <ul>
     *     <li>Integer(5) test: NumberRange.Integer(1,10) -> true</li>
     *     <li>Integer(15) test: NumberRange.Integer(1,10) -> false</li>
     *     <li>Integer(5) test: NumberRange.Integer(1,null) -> true</li>
     *     <li>Float(1.1) test: NumberRange.Integer(1,10) -> true</li>
     *     <li>Float(0.9) test: NumberRange.Integer(1,10) -> false</li>
     *     <li>Float(1.1) test: NumberRange.Double(3.2,10.1) -> false</li>
     *     <li>Float(1.1) test: NumberRange.Integer(null,null) -> true (always true)</li>
     * </ul>
     *
     * @param o
     * @return
     */
    @Override
    public boolean test(Object o) {
        try {
            java.lang.Double n = TypesUtils.convertDouble(o);
            if (n == null) {
                return false;
            }
            if (minValue != null && minValue.doubleValue() > n.doubleValue()) {
                return false;
            }
            if (maxValue != null && maxValue.doubleValue() < n.doubleValue()) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /** Derive a new NumberRange based on the provided values, or if null, the corresponding values from this instance.
     *
     * @param min
     * @param max
     * @return
     */
    public abstract NumberRange derive(T min, T max);


    /** Derive a new NumberRange based on the provided toString() representation (e..g 1|10), or if either valye is null,
     * the corresponding values from this instance.
     *
     * @param s
     * @return
     */
    public abstract NumberRange derive(String s);


    protected void updateIfNull(NumberRange neu) {
        if (neu.minValue == null) {
            neu.minValue = this.minValue;
        }
        if (neu.maxValue == null) {
            neu.maxValue = this.maxValue;
        }
    }

    @Override
    @JsonIgnore
    public Map<String, Class> getSimpleTypeDefinitions() {
        Map<String,Class> map = new HashMap<>();
        map.put(PROP_MIN_VALUE, getType());
        map.put(PROP_MAX_VALUE, getType());
        return map;
    }

    @Override
    @JsonIgnore
    public Map<String, Object> getSimpleTypes() {
        Map<String,Object> map = new HashMap<>();
        map.put(PROP_MIN_VALUE, minValue);
        map.put(PROP_MAX_VALUE, maxValue);
        return map;
    }

    /** Number range for Integers
     *
     */
    public static class Integer extends NumberRange<java.lang.Integer> {

        private Integer() {}
        public Integer(
                @JsonProperty("minValue") java.lang.Integer minValue,
                @JsonProperty("maxValue") java.lang.Integer maxValue) {
            super(minValue, maxValue);
        }
        public Integer(String str) {
            super(str);
        }

        public Class getType() {
            return java.lang.Integer.class;
        }

        public NumberRange derive(java.lang.Integer min, java.lang.Integer max) {
            NumberRange.Integer neu = new NumberRange.Integer(min, max);
            updateIfNull(neu);
            return neu;
        }

        public NumberRange derive(String s) {
            NumberRange.Integer neu = new NumberRange.Integer(s);
            updateIfNull(neu);
            return neu;
        }

        protected java.lang.Integer convert(Object o) {
            return TypesUtils.convertInteger(o);
        }
    }

    /** Number range for Doubles
     *
     */
    public static class Double extends NumberRange<java.lang.Double> {

        private Double() {}
        public Double(
                @JsonProperty("minValue") java.lang.Double minValue,
                @JsonProperty("maxValue") java.lang.Double maxValue) {
            super(minValue, maxValue);
        }
        public Double(String str) {
            super(str);
        }

        public Class getType() {
            return java.lang.Double.class;
        }

        public NumberRange derive(java.lang.Double min, java.lang.Double max) {
            return new NumberRange.Double(min == null ? this.minValue : min, max == null ? this.maxValue : max);
        }

        public NumberRange derive(String s) {
            NumberRange.Double neu = new NumberRange.Double(s);
            updateIfNull(neu);
            return neu;
        }


        protected java.lang.Double convert(Object o) {
            return TypesUtils.convertDouble(o);
        }
    }

    /** Number range for Floats
     *
     */
    public static class Float extends NumberRange<java.lang.Float> {

        private Float() {}
        public Float(
                @JsonProperty("minValue") java.lang.Float minValue,
                @JsonProperty("maxValue") java.lang.Float maxValue) {
            super(minValue, maxValue);
        }
        public Float(String str) {
            super(str);
        }

        public Class getType() {
            return java.lang.Float.class;
        }

        public NumberRange derive(java.lang.Float min, java.lang.Float max) {
            return new NumberRange.Float(min == null ? this.minValue : min, max == null ? this.maxValue : max);
        }

        public NumberRange derive(String s) {
            NumberRange.Float neu = new NumberRange.Float(s);
            updateIfNull(neu);
            return neu;
        }

        protected java.lang.Float convert(Object o) {
            return TypesUtils.convertFloat(o);
        }
    }
}
