package org.squonk.property;

import com.im.lac.types.MoleculeObject;

/**
 * Created by timbo on 10/04/16.
 */
public interface MoleculeCalculator<V> extends Calculator<V,MoleculeObject> {

    default V calculate(MoleculeObject mol, boolean storeResult) {
        return calculate(mol, storeResult, false);
    }

    V calculate(MoleculeObject mol, boolean storeResult, boolean storeMol);
}
