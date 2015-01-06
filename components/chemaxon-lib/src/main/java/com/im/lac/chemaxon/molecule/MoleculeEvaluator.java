package com.im.lac.chemaxon.molecule;

import chemaxon.struc.Molecule;
import java.util.Map;

/**
 *
 * @author timbo
 */
public interface MoleculeEvaluator {
    
    public Molecule processMolecule(Molecule mol);
    
    public Map<String,Object> getResults(Molecule mol);
    
}
