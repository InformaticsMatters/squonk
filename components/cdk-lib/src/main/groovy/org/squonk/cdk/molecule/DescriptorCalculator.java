package org.squonk.cdk.molecule;

import org.squonk.types.MoleculeObject;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.squonk.util.ExecutionStats;

/**
 *
 * @author timbo
 */
public abstract class DescriptorCalculator {
    
    protected IMolecularDescriptor descriptor;
    protected String key;
    protected String[] propNames;
    protected Class[] propTypes;
    protected final ExecutionStats executionStats = new ExecutionStats();

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

    public Class[] getPropertyTypes() {
        return propTypes;
    }

    public abstract void calculate(MoleculeObject mo) throws Exception;
    
    public abstract IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception;

    public ExecutionStats getExecutionStats() {
         return executionStats;
    }

    protected int incrementExecutionCount(int count) {
        return executionStats.incrementExecutionCount(MolecularDescriptors.STATS_PREFIX + "." + getKey(), count);
    }
    
}
