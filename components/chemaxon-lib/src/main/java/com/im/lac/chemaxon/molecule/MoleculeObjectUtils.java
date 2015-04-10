package com.im.lac.chemaxon.molecule;

import com.im.lac.types.MoleculeObjectIterable;
import com.im.lac.util.StreamGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Utilities for MoleculeObjects
 *
 * @author timbo
 */
public class MoleculeObjectUtils {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectUtils.class.getName());

    /**
     * Uses MRecordReader. Does not create any {@link chemaxon.struc.Molecule} instances
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static MoleculeObjectIterable createIterable(InputStream is) throws IOException {
        return new MoleculeObjectFactory(is);
    }

    /**
     * Uses MRecordReader. Does not create any {@link chemaxon.struc.Molecule} instances
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static MoleculeObjectIterable createIterable(File file) throws IOException {
        return new MoleculeObjectFactory(new FileInputStream(file));
    }

    /**
     * Generates a provider of Stream&lt;MoleculeObject&gt;.
     * Uses MRecordReader. Does not create any {@link chemaxon.struc.Molecule} instances
     * 
     * @param is The input stream. Either close this or the Stream once finished.
     * @return
     * @throws IOException 
     */
    public static StreamGenerator createStreamGenerator(InputStream is) throws IOException {
        return new MoleculeObjectStreamProviderImpl(is);
    }
   
}
