package org.squonk.property;


import org.squonk.types.MoleculeObject;

/**
 * Created by timbo on 05/04/16.
 */
public abstract class MoleculeObjectProperty<V> extends Property<V,MoleculeObject> {


    public MoleculeObjectProperty(String standardName, String description, String metricsCode, Class<V> valueClass) {
        super(standardName, description, metricsCode, valueClass, MoleculeObject.class);
    }


}
