package org.squonk.property;

import org.squonk.util.Metrics;

/**
 * Created by timbo on 05/04/16.
 */
public class PSAProperty extends MoleculeObjectProperty {

    public static final String METRICS_CODE = Metrics.METRICS_PSA;
    public static final String PROP_NAME = "PSA";
    public static final String PROP_DESC = "Polar surface area";

    public PSAProperty() {
        super(PROP_NAME, PROP_DESC, METRICS_CODE, Float.class);
    }
}
