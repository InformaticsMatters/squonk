/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac.chemaxon.io;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.molecule.MoleculeIterable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 *
 * @author timbo
 */
public class InputStreamMoleculeIterable implements MoleculeIterable {

    final MolImporter importer;

    public InputStreamMoleculeIterable(InputStream is) throws IOException {
        importer = new MolImporter(is);
    }

    @Override
    public Iterator<Molecule> iterator() {
        return importer.iterator();
    }

}
