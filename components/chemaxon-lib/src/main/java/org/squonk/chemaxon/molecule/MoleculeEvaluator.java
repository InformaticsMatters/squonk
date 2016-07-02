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
    
    public enum Mode {

        Calculate, Filter, Transform
    }

    
    public Molecule processMolecule(Molecule mol);
    
    public MoleculeObject processMoleculeObject(MoleculeObject mol) throws MolFormatException, IOException;
    
    public Map<String,Object> getResults(Molecule mol);
    
    public Mode getMode();

    public String getKey();

    public String getMetricsCode();
    
}
