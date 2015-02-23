package com.im.lac.cdk.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 *
 * @author timbo
 */
public class CDKMoleculeIOUtils {
    

    public static Iterator<IAtomContainer> moleculeIterator(final InputStream is)
            throws IOException, ClassNotFoundException, CDKException, InstantiationException, IllegalAccessException {
        Iterable iter = moleculeIterable(is);
        return iter.iterator();
    }

    public static Iterable<IAtomContainer> moleculeIterable(final InputStream is)
            throws IOException, ClassNotFoundException, CDKException, InstantiationException, IllegalAccessException {
        BufferedInputStream bis = new BufferedInputStream(is);
        bis.mark(10000);
        IChemFormat format = new FormatFactory().guessFormat(bis);
        bis.reset();
        ISimpleChemObjectReader reader;
        if (format == null) {
            // let's try smiles as FormatFactory cannot detect that
            reader = new SMILESReader();
        } else {
            reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
        }
        reader.setReader(bis);
        ChemFile chemFile = reader.read(new ChemFile());
        List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
        return containersList;
    }

    public static List<IAtomContainer> importMolecules(String s) throws IOException, CDKException {
        ISimpleChemObjectReader reader = createReader(s);
        if (reader == null) {
            throw new IOException("Unsupported format");
        } else {
            ChemFile chemFile = reader.read(new ChemFile());
            List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
            return containersList;
        }
    }

    public static ISimpleChemObjectReader createReader(String input) throws IOException, CDKException {
        FormatFactory factory = new FormatFactory();
        IChemFormat format = factory.guessFormat(new StringReader(input));
        ISimpleChemObjectReader reader = null;
        if (format == null) {
            if (input.startsWith("InChI=")) {
                reader = new INChIReader();
            } else {
                // give up and assume smiles
                reader = new SMILESReader();
            }
        } else {
            try {
                reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                throw new IllegalStateException("Failed to create reader");
            }
        }
        reader.setReader(new ByteArrayInputStream(input.getBytes()));
        return reader;
    }

}
