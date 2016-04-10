package org.squonk.property;

import com.im.lac.types.MoleculeObject;

/**
 * Created by timbo on 10/04/16.
 */
public interface Calculator<V> {

    default V calculate(MoleculeObject mo) {
        return calculate(mo, false, false);
    }

    V calculate(MoleculeObject mo, boolean storeMol, boolean storeResult);

    int getTotalCount();

    int getErrorCount();

}
