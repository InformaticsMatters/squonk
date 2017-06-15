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

package org.squonk.cdk.molecule;

import org.squonk.types.MoleculeObject;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.squonk.util.ExecutionStats;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author timbo
 */
public abstract class DescriptorCalculator {
    
    protected IMolecularDescriptor descriptor;
    protected String key;
    protected String[] propNames;
    protected String[] descriptions;
    protected Class[] propTypes;
    protected final Map<String,Integer> executionStats = new HashMap<>();

    /** The key to use when generating usage stats
     *
     * @return
     */
    public String getKey() {
        return key;
    }

    public String[] getPropertyNames() {
        return propNames;
    }

    public String[] getDescriptions() {
        return descriptions;
    }

    public Class[] getPropertyTypes() {
        return propTypes;
    }

    public abstract void calculate(MoleculeObject mo) throws Exception;
    
    public abstract IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception;

    public Map<String,Integer> getExecutionStats() {
         return executionStats;
    }

    protected int incrementExecutionCount(int count) {
        return ExecutionStats.increment(executionStats, MolecularDescriptors.STATS_PREFIX + "." + getKey(), count);
    }
    
}
