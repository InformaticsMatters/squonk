/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac.chemaxon.molecule;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author timbo
 */
class MoleculeFactory implements MoleculeIterable, Closeable {
    final Iterator<Molecule> iterator;
    final MolImporter importer;

    MoleculeFactory(Iterator<Molecule> iterator) {
        this.iterator = iterator;
        importer = null;
    }

    MoleculeFactory(MolImporter importer) {
        this.iterator = importer.iterator();
        this.importer = importer;
    }

    @Override
    public Iterator<Molecule> iterator() {
        return iterator;
    }

    @Override
    public void close() throws IOException {
        if (importer != null) {
            importer.close();
        }
    }
    
}
