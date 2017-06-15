/*
 * Copyright (c) 2017 Informatics Matters Ltd.
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

package org.squonk.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** A numeric value bounded by upper and lower bounds e.g. a number with error bars.
 * Is represented as three values, one (value) specifying the value itself,
 * one (lowerBound) specifying the lower bound in absolute terms
 * and one (upperBound) specifying the upper bound in absolute terms.
 * Null values are not permitted.
 * Can be represented as a String using syntax of value|lowerBound|upperBound
 *
 * Created by timbo on 04/08/16.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class BoundedValue<T extends Number & Comparable<T>>
        implements Serializable, Comparable<BoundedValue<T>>, CompositeType {

    private static final String PROP_VALUE = "Value";
    private static final String PROP_LOWER = "LowerBound";
    private static final String PROP_UPPER = "UpperBound";

    private final T value;
    private final T lowerBound;
    private final T upperBound;

    private BoundedValue(T value, T lowerBound, T upperBound) {
        this.value = value;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        validate();
    }

    private BoundedValue(String str) {
        String[] parts = str.split("\\|");
        if (parts.length == 3) {
            if (parts[0].trim().length() > 0) {
                value = convert(parts[0]);
            } else {
                throw new IllegalArgumentException("Can't parse value: " + str);
            }
            if (parts[1].trim().length() > 0) {
                lowerBound = convert(parts[1]);
            } else {
                throw new IllegalArgumentException("Can't parse lowerBound: " + str);
            }
            if (parts[2].trim().length() > 0) {
                upperBound = convert(parts[2]);
            } else {
                throw new IllegalArgumentException("Can't parse upperBound: " + str);
            }
        } else {
            throw new IllegalArgumentException("Can't parse: " + str);
        }
        validate();
    }

    private void validate() {
        if (value == null || upperBound == null || lowerBound == null) {
            throw new NullPointerException("Null values not permitted");
        }
        if (value.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("Upper bound " + upperBound + " is less than value " + value);
        }
        if (value.compareTo(lowerBound) < 0) {
            throw new IllegalArgumentException("Lower bound " + lowerBound + " is greater than value " + value);
        }
    }

    protected abstract T convert(Object o);

    @JsonIgnore
    public abstract Class getType();

    @Override
    public String toString() {
        return value + "|" + lowerBound + "|" + upperBound;
    }

    public T getValue() { return value; }

    public T getUpperBound() { return upperBound; }

    public T getLowerBound() { return lowerBound; }

    @Override
    @JsonIgnore
    public Map<String, Class> getSimpleTypeDefinitions() {
        Map<String,Class> map = new HashMap<>();
        map.put(PROP_VALUE, getType());
        map.put(PROP_LOWER, getType());
        map.put(PROP_UPPER, getType());
        return map;
    }

    @Override
    @JsonIgnore
    public Map<String, Object> getSimpleTypes() {
        Map<String,Object> map = new HashMap<>();
        map.put(PROP_VALUE, value);
        map.put(PROP_LOWER, lowerBound);
        map.put(PROP_UPPER, upperBound);
        return map;
    }



    @Override
    public int compareTo(BoundedValue<T> o) {
        return value.compareTo(o.getValue());
    }

    /** Bounded Value for Doubles
     *
     */
    public static class Double extends BoundedValue<java.lang.Double> {

        public Double(
                @JsonProperty("value") java.lang.Double value,
                @JsonProperty("lowerBound") java.lang.Double lowerBound,
                @JsonProperty("upperBound") java.lang.Double upperBound
                ) {
            super(value, lowerBound, upperBound);
        }

        public Double(String str) {
            super(str);
        }

        public Class getType() {
            return java.lang.Double.class;
        }

        protected java.lang.Double convert(Object o) {
            return TypesUtils.convertDouble(o);
        }
    }

    /** Bounded Value for Floats
     *
     */
    public static class Float extends BoundedValue<java.lang.Float> {

        public Float(
                @JsonProperty("value") java.lang.Float value,
                @JsonProperty("lowerBound") java.lang.Float lowerBound,
                @JsonProperty("upperBound") java.lang.Float upperBound) {
            super(value, lowerBound, upperBound);
        }

        public Float(String str) {
            super(str);
        }

        public Class getType() {
            return java.lang.Float.class;
        }

        protected java.lang.Float convert(Object o) {
            return TypesUtils.convertFloat(o);
        }
    }

}
