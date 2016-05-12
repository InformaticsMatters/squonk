package org.squonk.cdk.molecule;

import com.im.lac.types.MoleculeObject;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.squonk.util.ExecutionStats;

/**
 *
 * @author timbo
 */
public abstract class DescriptorCalculator {
    
    protected IMolecularDescriptor descriptor;
    protected String[] propNames;
    protected Class[] propTypes;
    protected final ExecutionStats executionStats = new ExecutionStats();

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

    protected int incrementExecutionCount(String prop, int count) {
        return executionStats.incrementExecutionCount(MolecularDescriptors.STATS_PREFIX + "." + prop, count);
    }
    
}
