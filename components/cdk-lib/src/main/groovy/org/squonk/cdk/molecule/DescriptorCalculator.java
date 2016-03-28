package org.squonk.cdk.molecule;

import com.im.lac.types.MoleculeObject;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.IMolecularDescriptor;

/**
 *
 * @author timbo
 */
public abstract class DescriptorCalculator {
    
    IMolecularDescriptor descriptor;
    String[] propNames;
    Class[] propTypes;

    public String[] getPropertyNames() {
        return propNames;
    }

    public Class[] getPropertyTypes() {
        return propTypes;
    }

    public abstract void calculate(MoleculeObject mo) throws Exception;
    
    public abstract IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception;
    
}
