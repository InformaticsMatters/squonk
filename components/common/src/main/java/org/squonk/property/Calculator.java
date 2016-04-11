package org.squonk.property;

import com.im.lac.types.BasicObject;
import com.im.lac.types.MoleculeObject;

/**
 * Created by timbo on 10/04/16.
 */
public interface Calculator<V,T extends BasicObject> {

    default V calculate(T target) {
        return calculate(target, false);
    }

    V calculate(T target, boolean storeResult);

    int getTotalCount();

    int getErrorCount();

}
