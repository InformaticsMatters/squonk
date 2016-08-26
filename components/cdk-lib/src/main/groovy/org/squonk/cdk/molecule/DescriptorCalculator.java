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
