package org.squonk.types;

/**
 * Created by timbo on 23/03/2016.
 */
public class PDBFileHandler extends FileHandler<PDBFile> {

    public PDBFileHandler() {
        super(PDBFile.class, "pdb.gz");
    }

}
