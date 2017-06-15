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

import org.squonk.util.ExecutionStats;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by timbo on 05/04/16.
 */
public abstract class Predictor<V extends Object, T extends Object, C extends Calculator<V,T>> {

    private final String[] resultNames;
    private final Property<V,T>[] propertyTypes;
    protected final Map<String,Integer> executionStats = new HashMap<>();

    public Predictor(String resultName, Property<V,T> propertyType) {
        resultNames = new String[] {resultName};
        propertyTypes = new Property[] {propertyType};
    }

    public Predictor(String[] resultNames, Property<V,T>[] propertyTypes) {
        assert resultNames.length == propertyTypes.length;
        this.resultNames = resultNames;
        this.propertyTypes = propertyTypes;
    }

    public String[] getResultNames() {
        return resultNames;
    }

    public Property<V,T>[] getPropertyTypes() {
        return propertyTypes;
    }

    public abstract C[] getCalculators();

    public Map<String,Integer> getExecutionStats() {
        return executionStats;
    }

    protected int incrementExecutionCount(String key, int count) {
        return ExecutionStats.increment(executionStats, key, count);
    }

}
