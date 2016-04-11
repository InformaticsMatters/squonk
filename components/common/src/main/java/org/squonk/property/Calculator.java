package org.squonk.property;


/**
 * Created by timbo on 10/04/16.
 */
public interface Calculator<V extends Object,T extends Object> {

    V calculate(T target);

    Class<V> getResultType();

    String getResultName();

    int getTotalCount();

    int getErrorCount();

}
