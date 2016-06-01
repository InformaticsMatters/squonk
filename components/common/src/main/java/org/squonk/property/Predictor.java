package org.squonk.property;

import org.squonk.util.ExecutionStats;

/**
 * Created by timbo on 05/04/16.
 */
public abstract class Predictor<V extends Object, T extends Object, C extends Calculator<V,T>> {

    private final String[] resultNames;
    private final Property<V,T>[] propertyTypes;
    protected final ExecutionStats executionStats = new ExecutionStats();

    public Predictor(String resultName, Property<V,T> propertyType) {
        resultNames = new String[] {resultName};
        propertyTypes = new Property[] {propertyType};
    }

    public Predictor(String[] resultNames, Property<V,T>[] propertyTypes) {
        assert resultNames.length == propertyTypes.length;
        this.resultNames = resultNames;
        this.propertyTypes = propertyTypes;
    }

    public String[] getResultNames() {
        return resultNames;
    }

    public Property<V,T>[] getPropertyTypes() {
        return propertyTypes;
    }

    public abstract C[] getCalculators();

    public ExecutionStats getExecutionStats() {
        return executionStats;
    }

    protected int incrementExecutionCount(String key, int count) {
        return executionStats.incrementExecutionCount(key, count);
    }

}
