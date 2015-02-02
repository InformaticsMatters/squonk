package com.im.lac.chemaxon.molecule;

import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author timbo
 */
public interface MoleculeEvaluator {
    
    public Molecule processMolecule(Molecule mol);
    
    public MoleculeObject processMoleculeObject(MoleculeObject mol) throws MolFormatException, IOException;
    
    public Map<String,Object> getResults(Molecule mol);
    
}
