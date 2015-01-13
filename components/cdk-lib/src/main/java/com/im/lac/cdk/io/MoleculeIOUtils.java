package com.im.lac.cdk.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.FormatFactory;
import org.openscience.cdk.io.SMILESReader;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 *
 * @author timbo
 */
public class MoleculeIOUtils {

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

}
