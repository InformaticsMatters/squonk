/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac.chemaxon.molecule;

import chemaxon.struc.Molecule;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author timbo
 */
class MoleculeCollectionIterableAdapter implements MoleculeIterable {
    final Collection<Molecule> mols;

    MoleculeCollectionIterableAdapter(Collection<Molecule> mols) {
        this.mols = mols;
    }

    @Override
    public Iterator<Molecule> iterator() {
        return mols.iterator();
    }
    
}
