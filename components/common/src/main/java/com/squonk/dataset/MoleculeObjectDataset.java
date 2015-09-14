package com.squonk.dataset;

import com.im.lac.types.MoleculeObject;

/** MoleculeObject specific wrapper for Dataset to get round problem with generics.
 * e.g. you can say MoleculeObjectDataset.class but you can't say Dataset&lt;MoleculeObject&gt;.class
 * 
 *
 * @author timbo
 */
public class MoleculeObjectDataset {
    
    private final Dataset<MoleculeObject> mods;
    
    public MoleculeObjectDataset(Dataset<MoleculeObject> mods) {
        this.mods = mods;
    }
    
    public Dataset<MoleculeObject> getDataset() {
        return mods;
    }
   
}
