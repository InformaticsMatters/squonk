package com.im.lac.chemaxon.molecule;

import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Utilities for MoleculeObjects
 *
 * @author timbo
 */
public class MoleculeObjectUtils {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectUtils.class.getName());

    /**
     * Uses MRecordReader. Does not create any chemaxon.struc.Molecule instances
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static MoleculeObjectIterable createIterable(InputStream is) throws IOException {
        return new MoleculeObjectFactory(is);
    }

    /**
     * Uses MRecordReader. Does not create any chemaxon.struc.Molecule instances
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static MoleculeObjectIterable createIterable(File file) throws IOException {
        return new MoleculeObjectFactory(new FileInputStream(file));
    }
    
    public static Stream<MoleculeObject> createStream(InputStream is, boolean parallel) throws IOException {
        return new MoleculeObjectSpliterator(is).asStream(parallel);
    }
    
    public static Stream<MoleculeObject> createStream(InputStream is, boolean parallel, int batchSize) throws IOException {
        return new MoleculeObjectSpliterator(is, batchSize).asStream(parallel);
    }
    
    /**
     * Sequential stream with default batch size 
     * @param is
     * @return
     * @throws IOException 
     */
    public static Stream<MoleculeObject> createStream(InputStream is) throws IOException {
        return new MoleculeObjectSpliterator(is).asStream(false);
    }

}
