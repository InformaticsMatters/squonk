package org.squonk.chemaxon.molecule;

import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;
import org.squonk.types.MoleculeObject;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author timbo
 */
public interface MoleculeEvaluator {
    
    enum Mode {

        Calculate, Filter, Transform
    }

    
    Molecule processMolecule(Molecule mol, Map<String,Integer> stats);
    
    MoleculeObject processMoleculeObject(MoleculeObject mol, Map<String,Integer> stats) throws IOException;
    
    Map<String,Object> getResults(Molecule mol);
    
    Mode getMode();

    String getKey();

    String getMetricsCode();

    String getDescription();

    /** Return the name of the field that is created by execution of this evaluator.
     * This will be null when filtering or transforms are involved
     *
     * @return The property name, which might be null.
     */
    String getPropName();
    
}
