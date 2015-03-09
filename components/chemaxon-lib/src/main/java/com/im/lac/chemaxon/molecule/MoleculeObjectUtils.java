package com.im.lac.chemaxon.molecule;

import com.im.lac.types.MoleculeObjectIterable;
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

}
