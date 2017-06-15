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

/**
 * Created by timbo on 05/04/16.
 */
public abstract class Property<V,T> {

    private final String propertyName;
    private final String description;
    private final String metricsCode;
    private final Class<V> valueClass;
    private final Class<T> targetClass;

    public Property(String propertyName, String description, String metricsCode, Class<V> valueClass, Class<T> targetClass) {
        this.propertyName = propertyName;
        this.description = description;
        this.metricsCode = metricsCode;
        this.valueClass = valueClass;
        this.targetClass = targetClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getDescription() {
        return description;
    }

    public String getMetricsCode() {
        return metricsCode;
    }

    public Class<V> getValueClass() {
        return valueClass;
    }
}
