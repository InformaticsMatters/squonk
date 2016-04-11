package org.squonk.property;

/**
 * Created by timbo on 05/04/16.
 */
public abstract class Predictor<V extends Object, T extends Object, C extends Calculator<V,T>> {

    private final String[] resultNames;
    private final Property<V,T>[] propertyTypes;

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

}
