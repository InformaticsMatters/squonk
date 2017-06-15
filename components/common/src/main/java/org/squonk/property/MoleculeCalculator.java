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

import org.squonk.types.MoleculeObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by timbo on 10/04/16.
 */
public abstract class MoleculeCalculator<V> implements Calculator<V,MoleculeObject> {

    protected final String resultName;
    protected final Class<V> resultType;
    protected final AtomicInteger totalCount = new AtomicInteger(0);
    protected final AtomicInteger errorCount = new AtomicInteger(0);

    protected MoleculeCalculator(String resultName, Class<V> resultType) {
        this.resultName = resultName;
        this.resultType = resultType;
    }

    @Override
    public Class<V> getResultType() {
        return resultType;
    }

    @Override
    public String getResultName() {
        return resultName;
    }

    @Override
    public int getTotalCount() {
        return totalCount.get();
    }

    @Override
    public int getErrorCount() {
        return errorCount.get();
    }

    public V calculate(MoleculeObject mol, boolean storeResult) {
        return calculate(mol, storeResult, false);
    }

    public abstract V calculate(MoleculeObject mol, boolean storeResult, boolean storeMol);
}
