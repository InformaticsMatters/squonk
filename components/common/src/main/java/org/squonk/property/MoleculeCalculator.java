package org.squonk.property;

import com.im.lac.types.MoleculeObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by timbo on 10/04/16.
 */
public abstract class MoleculeCalculator<V> implements Calculator<V,MoleculeObject> {

    protected final String resultName;
    protected final Class<V> resultType;
    protected final AtomicInteger totalCount = new AtomicInteger(0);
    protected final AtomicInteger errorCount = new AtomicInteger(0);

    protected MoleculeCalculator(String resultName, Class<V> resultType) {
        this.resultName = resultName;
        this.resultType = resultType;
    }

    @Override
    public Class<V> getResultType() {
        return resultType;
    }

    @Override
    public String getResultName() {
        return resultName;
    }

    @Override
    public int getTotalCount() {
        return totalCount.get();
    }

    @Override
    public int getErrorCount() {
        return errorCount.get();
    }

    public V calculate(MoleculeObject mol, boolean storeResult) {
        return calculate(mol, storeResult, false);
    }

    public abstract V calculate(MoleculeObject mol, boolean storeResult, boolean storeMol);
}
