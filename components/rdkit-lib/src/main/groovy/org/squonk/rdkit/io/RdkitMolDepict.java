package org.squonk.rdkit.io;

import org.squonk.io.AbstractMolDepict;
import org.squonk.io.DepictionParameters;
import org.squonk.rdkit.mol.MolReader;

import java.awt.image.BufferedImage;

/**
 * Created by timbo on 24/01/2016.
 */
public class RdkitMolDepict extends AbstractMolDepict {
    @Override
    public Object smilesToMolecule(String smiles) throws Exception {
        return MolReader.generateMolFromSmiles(smiles);
    }

    @Override
    public Object v2000ToMolecule(String molecule) throws Exception {
        return MolReader.generateMolFromMolfile(molecule);
    }

    @Override
    public Object v3000ToMolecule(String molecule) throws Exception {
        return MolReader.generateMolFromMolfile(molecule);
    }

    @Override
    public Object stringToMolecule(String molecule) throws Exception {
        return MolReader.generateMolFromString(molecule, null);
    }

    @Override
    public BufferedImage moleculeToImage(Object molecule, DepictionParameters params) throws Exception {
        return null;
    }

    @Override
    public String moleculeToSVG(Object molecule, DepictionParameters params) throws Exception {
        return null;
    }
}
