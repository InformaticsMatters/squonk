package org.squonk.types;

/**
 * Created by timbo on 23/03/2016.
 */
public class Mol2FileHandler extends FileHandler<Mol2File>{

    public Mol2FileHandler() {
        super(Mol2File.class, "mol2.gz");
    }

}
