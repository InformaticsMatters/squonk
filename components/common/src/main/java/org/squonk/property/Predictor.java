package org.squonk.property;

import com.im.lac.types.BasicObject;

/**
 * Created by timbo on 05/04/16.
 */
public abstract class Predictor<V, T extends BasicObject, C extends Calculator<V,T>> {

private final String resultName;
    private final Property<V,T> propertyType;

    public Predictor(String resultName, Property<V,T> propertyType) {
        this.resultName = resultName;
        this.propertyType = propertyType;
    }

    public String getResultName() {
        return resultName;
    }

    public Property<V,T> getPropertyType() {
        return propertyType;
    }

    public abstract C getCalculator();

}
