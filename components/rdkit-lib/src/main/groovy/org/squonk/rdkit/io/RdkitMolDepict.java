package org.squonk.rdkit.io;

import org.RDKit.MolDraw2DSVG;
import org.RDKit.ROMol;
import org.squonk.io.AbstractMolDepict;
import org.squonk.io.DepictionParameters;
import org.squonk.rdkit.mol.MolReader;

import java.awt.image.BufferedImage;

/**
 * Created by timbo on 24/01/2016.
 */
public class RdkitMolDepict extends AbstractMolDepict<ROMol> {

    @Override
    public ROMol smilesToMolecule(String smiles) throws Exception {
        return MolReader.generateMolFromSmiles(smiles);
    }

    @Override
    public ROMol v2000ToMolecule(String molecule) throws Exception {
        return MolReader.generateMolFromMolfile(molecule);
    }

    @Override
    public ROMol v3000ToMolecule(String molecule) throws Exception {
        return MolReader.generateMolFromMolfile(molecule);
    }

    @Override
    public ROMol stringToMolecule(String molecule) throws Exception {
        return MolReader.generateMolFromString(molecule, null);
    }

    @Override
    public BufferedImage moleculeToImage(ROMol molecule, DepictionParameters params) throws Exception {
        return null;
    }

    @Override
    public String moleculeToSVG(ROMol molecule, DepictionParameters params) throws Exception {
        MolDraw2DSVG o = new MolDraw2DSVG(params.getWidth(), params.getHeight());
        o.drawMolecule(molecule);
        return o.getDrawingText();
    }
}
