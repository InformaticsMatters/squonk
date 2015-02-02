package com.im.lac.chemaxon.molecule;

import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.molecule.MoleculeIterable;
import java.util.Iterator;

/**
 * Simple MoleculeInterable that wraps a Iterable<Molecule>
 * 
 * @author timbo
 */
public class SimpleMoleculeIterable implements MoleculeIterable {
    
    private final Iterable<Molecule> source;
    
    public SimpleMoleculeIterable(Iterable<Molecule> source) {
        this.source = source;
    }

    @Override
    public Iterator<Molecule> iterator() {
        return source.iterator();
    }
    
}
