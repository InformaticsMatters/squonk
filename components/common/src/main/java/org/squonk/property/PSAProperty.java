package org.squonk.property;

import org.squonk.util.Metrics;

/**
 * Created by timbo on 05/04/16.
 */
public class PSAProperty extends MoleculeObjectProperty {

    public static final String METRICS_CODE = Metrics.METRICS_PSA;

    public PSAProperty() {
        super("PSA", "Polar surface area", METRICS_CODE, Float.class);
    }
}
