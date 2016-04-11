package org.squonk.property;

import com.im.lac.types.BasicObject;

/**
 * Created by timbo on 10/04/16.
 */
public interface Calculator2<V extends Object,T extends Object> {

    V calculate(T target);

    Class<V> getResultType();

    int getTotalCount();

    int getErrorCount();

}
