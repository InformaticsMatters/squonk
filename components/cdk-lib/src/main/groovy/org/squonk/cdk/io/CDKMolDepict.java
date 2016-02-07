package org.squonk.cdk.io;

import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.squonk.io.AbstractMolDepict;
import org.squonk.io.DepictionParameters;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by timbo on 17/01/2016.
 */
public class CDKMolDepict extends AbstractMolDepict<IAtomContainer> {


    private final DepictionGenerator generator;

    private static final SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());

    public CDKMolDepict() {
        super();
        generator = createDepictionGenerator(params);
    }

    public CDKMolDepict(DepictionParameters params) {
        super(params);
        generator = createDepictionGenerator(params);
    }

    DepictionGenerator createDepictionGenerator(DepictionParameters params) {
        DepictionGenerator dg = new DepictionGenerator()
                .withAtomColors()
                .withTerminalCarbons()
                //.withParam(BasicAtomGenerator.ShowExplicitHydrogens.class, true)
                .withBackgroundColor(params.getBackgroundColor() != null ? params.getBackgroundColor() : DEFAULT_BACKGROUND);

        if (params.getWidth() != null || params.getHeight() != null) {
            dg = dg.withSize(params.getWidth(), params.getHeight());
        } else {
            dg = dg.withSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
        if (params.isExpandToFit()) {
            dg = dg.withFillToFit();
        }
        return dg;
    }

    DepictionGenerator getDepictionGenerator(DepictionParameters params) {
        if (params == null) {
            return generator;
        } else {
            return createDepictionGenerator(params);
        }
    }


    @Override
    public String moleculeToSVG(IAtomContainer mol, DepictionParameters params) throws CDKException {
        if (mol == null) {
            return null;
        }
        Depiction depiction = depict(mol, params);
        return depiction.toSvgStr();
    }

    @Override
    public BufferedImage moleculeToImage(IAtomContainer mol, DepictionParameters params) throws CDKException, IOException {
        if (mol == null) {
            return null;
        }
        Depiction depiction = depict(mol, params);
        return depiction.toImg();
    }


    public Depiction depict(IAtomContainer mol, DepictionParameters params) throws CDKException {
        Depiction depiction = getDepictionGenerator(params).depict(mol);
        return depiction;
    }

    @Override
    public IAtomContainer v2000ToMolecule(String molfile) throws CDKException {
        MDLV2000Reader v2000Parser = new MDLV2000Reader(new ByteArrayInputStream(molfile.getBytes()));
        return v2000Parser.read(new AtomContainer());
    }

    @Override
    public IAtomContainer v3000ToMolecule(String molfile) throws CDKException {
        MDLV3000Reader v3000Parser = new MDLV3000Reader(new ByteArrayInputStream(molfile.getBytes()));
        return v3000Parser.read(new AtomContainer());
    }

    @Override
    public IAtomContainer smilesToMolecule(String smiles) throws CDKException {
        return smilesParser.parseSmiles(smiles);
    }

    @Override
    public IAtomContainer stringToMolecule(String s) throws CDKException {
        return CDKMoleculeIOUtils.readMolecule(s);
    }

}


