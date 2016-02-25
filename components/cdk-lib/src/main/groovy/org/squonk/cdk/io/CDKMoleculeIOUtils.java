package org.squonk.cdk.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.FormatFactory;
import org.openscience.cdk.io.INChIReader;
import org.openscience.cdk.io.SMILESReader;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * @author timbo
 */
public class CDKMoleculeIOUtils {


    public static Iterator<IAtomContainer> moleculeIterator(final InputStream is)
            throws IOException, CDKException {
        Iterable iter = moleculeIterable(is);
        return iter.iterator();
    }

    public static Iterable<IAtomContainer> moleculeIterable(InputStream is)
            throws IOException, CDKException {
        BufferedInputStream bis = new BufferedInputStream(is);
        bis.mark(10000);
        IChemFormat format = new FormatFactory().guessFormat(bis);
        bis.reset();

        ISimpleChemObjectReader reader;
        if (format == null) {
            // let's try smiles as FormatFactory cannot detect that
            //System.out.println("Trying as smiles");
            reader = new SMILESReader();
        } else {
            //System.out.println("Trying as " + format.getReaderClassName());
            try {
                reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new CDKException("Failed to instantiate reader", e);
            }
        }
        reader.setReader(bis);

        ChemFile chemFile = reader.read(new ChemFile());
        List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
        return containersList;
    }

    public static IAtomContainer readMolecule(String mol) throws CDKException {

        if (mol == null) {
            return null;
        }

        ISimpleChemObjectReader reader = null;
        FormatFactory factory = new FormatFactory();
        try {
            IChemFormat format = factory.guessFormat(new StringReader(mol));
            if (format == null) {
                if (mol.startsWith("InChI=")) {
                    reader = new INChIReader();
                } else {
                    // give up and assume smiles
                    SmilesParser parser = new SmilesParser(SilentChemObjectBuilder.getInstance());
                    return parser.parseSmiles(mol);
                }
            } else {
                reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | IOException ex) {
            throw new CDKException("Failed to create reader", ex);
        }
        if (reader != null) {
            reader.setReader(new ByteArrayInputStream(mol.getBytes()));
            return reader.read(new AtomContainer());
        }
        return null;
    }

//    public static ISimpleChemObjectReader createReader(InputStream is)
//            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, CDKException {
//        BufferedInputStream bis = new BufferedInputStream(is);
//        bis.mark(10000);
//        IChemFormat format = new FormatFactory().guessFormat(bis);
//        bis.reset();
//        ISimpleChemObjectReader reader;
//        if (format == null) {
//            // let's try smiles as FormatFactory cannot detect that
//            System.out.println("Trying as smiles");
//            reader = new SMILESReader();
//        } else {
//            System.out.println("Trying as " + format.getReaderClassName());
//            reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
//        }
//        reader.setReader(bis);
//        return reader;
//    }

//    public static ISimpleChemObjectReader createReader(String input) throws CDKException {
//        FormatFactory factory = new FormatFactory();
//        try {
//            IChemFormat format = factory.guessFormat(new StringReader(input));
//            ISimpleChemObjectReader reader = null;
//            if (format == null) {
//                if (input.startsWith("InChI=")) {
//                    reader = new INChIReader();
//                } else {
//                    // give up and assume smiles
//                    reader = new SMILESReader();
//                }
//            } else {
//                reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
//            }
//            reader.setReader(new StringReader(input));
//            return reader;
//        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
//            throw new CDKException("Failed to create reader", ex);
//        }
//    }

    public static List<IAtomContainer> importMolecules(String s) throws CDKException {
        Iterable<IAtomContainer> iter = null;
        try {
            iter = moleculeIterable(new ByteArrayInputStream(s.getBytes()));
        } catch (IOException e) {
            throw new CDKException("Failed to read molecules", e);
        }
        List<IAtomContainer> mols = new ArrayList<>();
        if (iter != null) {
            for (IAtomContainer mol : iter) {
                mols.add(mol);
            }
        }
        return mols;
    }


}
