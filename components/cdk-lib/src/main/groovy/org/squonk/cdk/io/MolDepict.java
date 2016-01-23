package org.squonk.cdk.io;

import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by timbo on 17/01/2016.
 */
public class MolDepict {

    private final DepictionParameters params;
    private final DepictionGenerator generator;

    private static final SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());



    public MolDepict(DepictionParameters params) {
        this.params = params;
        DepictionGenerator dg = new DepictionGenerator()
                .withAtomColors()
                .withBackgroundColor(params.getBackgroundColor() != null ? params.getBackgroundColor() : new Color(255, 255, 255, 0));

        if (params.getSize() != null) {
            dg = dg.withSize(params.getSize().width, params.getSize().height);
        }
        if (params.isExpandToFit()) {
            dg = dg.withFillToFit();
        }
        generator = dg;
    }

    public String v2000ToSVG(String molfile) throws CDKException {
        MDLV2000Reader v2000Parser = new MDLV2000Reader(new ByteArrayInputStream(molfile.getBytes()));
        IAtomContainer mol = v2000Parser.read(new AtomContainer());
        return generateSVG(mol);
    }

    public String v3000ToSVG(String molfile) throws CDKException {
        MDLV3000Reader v3000Parser = new MDLV3000Reader(new ByteArrayInputStream(molfile.getBytes()));
        IAtomContainer mol = v3000Parser.read(new AtomContainer());
        return generateSVG(mol);
    }

    public String moleculeToSVG(String molecule) throws CDKException {
        IAtomContainer mol = CDKMoleculeIOUtils.readMolecule(molecule);
        return generateSVG(mol);
    }

    public String smilesToSVG(String smiles)
            throws CDKException {
        IAtomContainer mol = smilesParser.parseSmiles(smiles);
        return generateSVG(mol);
    }

    public String generateSVG(IAtomContainer mol)
            throws CDKException {
        if (mol == null) {
            return null;
        }
        Depiction depiction = depict(mol);
        String svg = depiction.toSvgStr();
        return svg;
    }

    public Depiction depict(IAtomContainer mol)
            throws CDKException {

        Depiction depiction = generator.depict(mol);
        return depiction;
    }




}
