package org.squonk.cdk.io;

import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.renderer.color.CDK2DAtomColors;
import org.openscience.cdk.renderer.color.CPKAtomColors;
import org.openscience.cdk.renderer.color.IAtomColorer;
import org.openscience.cdk.renderer.color.UniColor;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.squonk.io.AbstractMolDepict;
import org.squonk.io.DepictionParameters;
import org.squonk.io.DepictionParameters.ColorScheme;
import org.squonk.io.DepictionParameters.AtomHighlight;
import org.squonk.io.DepictionParameters.Highlight;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 17/01/2016.
 */
public class CDKMolDepict extends AbstractMolDepict<IAtomContainer> {

    private static final Logger LOG = Logger.getLogger(CDKMolDepict.class.getName());

    private static Map<ColorScheme, IAtomColorer> colorers = new HashMap<>();

    static {
        colorers.put(ColorScheme.black, new UniColor(Color.BLACK));
        colorers.put(ColorScheme.white, new UniColor(Color.WHITE));
        colorers.put(ColorScheme.cpk, new CPKAtomColors());
        colorers.put(ColorScheme.toolkit_default, new CDK2DAtomColors());
    }


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

        Integer w = params.getWidth();
        Integer h = params.getHeight();
        double m = params.getMargin();
        ColorScheme colorScheme = params.getColorScheme();

        DepictionGenerator dg = new DepictionGenerator()
                .withTerminalCarbons()
                //.withParam(BasicAtomGenerator.ShowExplicitHydrogens.class, true)
                .withBackgroundColor(params.getBackgroundColor() != null ? params.getBackgroundColor() : DEFAULT_BACKGROUND)
                .withSize(w == null ? DEFAULT_WIDTH: w, h == null ? DEFAULT_HEIGHT : h)
                .withAtomColors(colorers.get(colorScheme == null ? ColorScheme.toolkit_default : colorScheme))
                .withMargin(m > 0 ? m : 0);

        if (params.isExpandToFit()) {
            dg = dg.withFillToFit();
        }
        return dg;
    }

    DepictionGenerator configureForMolecule(IAtomContainer mol, DepictionParameters params, DepictionGenerator dg) {

        for (Highlight highlight : params.getHighlights()) {
            if (highlight instanceof AtomHighlight) {
                AtomHighlight atomHighlight = (AtomHighlight) highlight;
                if (atomHighlight.getAtomIndexes() == null || atomHighlight.getAtomIndexes().length == 0) {
                    LOG.warning("No atom indexes found");
                    continue;
                }
                if (atomHighlight.getColor() == null) {
                    LOG.warning("No color found");
                    continue;
                }

                List<IAtom> atoms = new ArrayList<>();
                for (int index : atomHighlight.getAtomIndexes()) {
                    if (index >= 0 && index < mol.getAtomCount()) {
                        IAtom atom = mol.getAtom(index);
                        if (atom != null) {
                            atoms.add(atom);
                        }
                    }
                }
                List<IBond> bonds = new ArrayList<>();
                if (atomHighlight.isHighlightBonds()) {
                    for (int bondIndex = 0; bondIndex < mol.getBondCount(); ++bondIndex) {
                        IBond bond = mol.getBond(bondIndex);
                        boolean allAtomsMatch = true;
                        for (IAtom atom : bond.atoms()) {
                            if (!atoms.contains(atom)) {
                                allAtomsMatch = false;
                                break;
                            }
                        }
                        if (allAtomsMatch) {
                            bonds.add(bond);
                        }
                    }
                }

                if (atoms.size() > 0) {
                    List<IChemObject> highlights = new ArrayList<>();
                    highlights.addAll(atoms);
                    highlights.addAll(bonds);
                    dg = dg.withHighlight(highlights, atomHighlight.getColor());
                    if (atomHighlight.getMode() == DepictionParameters.HighlightMode.region) {
                        dg = dg.withOuterGlowHighlight();
                    }
                }
            }
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
        DepictionGenerator dg = getDepictionGenerator(params);
        dg = configureForMolecule(mol, params, dg);
        Depiction depiction = dg.depict(mol);
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


