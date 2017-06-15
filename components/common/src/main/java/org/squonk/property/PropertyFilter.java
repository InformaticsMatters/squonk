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

package org.squonk.property;

import org.squonk.types.BasicObject;
import org.squonk.types.NumberRange;

import java.util.function.Predicate;

/** Allows to filter values based on a numeric range.
 *
 * Created by timbo on 27/05/16.
 */
public class PropertyFilter implements Predicate<BasicObject> {

    protected final String propertyName;
    protected final boolean includeNull;
    protected final NumberRange range;

    public PropertyFilter(String propertyName, boolean includeNull, NumberRange range) {
        this.propertyName = propertyName;
        this.includeNull = includeNull;
        this.range = range;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isIncludeNull() {
        return includeNull;
    }

    public Predicate getRange() {
        return range;
    }

    public boolean isActive() {
        return range != null && (range.getMinValue() != null || range.getMaxValue() != null);
    }

    public boolean test(BasicObject bo) {
        Object o = bo.getValue(propertyName);
        if (o == null) {
            return includeNull;
        }
        return range.test(o);
    }

    public PropertyFilter derive(String str) {
        return new PropertyFilter(propertyName, includeNull, range.derive(str));
    }

    public PropertyFilter derive(Number min, Number max) {
        return new PropertyFilter(propertyName, includeNull, range.derive(min, max));
    }

    public String asText() {
        if (range.getMinValue() != null && range.getMaxValue() != null) {
            return range.getMinValue() + " <= " + propertyName + " >= " + range.getMaxValue();
        } else if (range.getMinValue() == null && range.getMaxValue() != null) {
            return propertyName + " >= " + range.getMaxValue();
        }  else if (range.getMinValue() != null && range.getMaxValue() == null) {
            return range.getMinValue() + " <= " + propertyName;
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return "PropertyFilter [" + getClass().getName() + " propertyName=" + propertyName + " range=" + range + "]";
    }

}
