package org.squonk.property;

import org.squonk.util.Metrics;

/**
 * Created by timbo on 05/04/16.
 */
public class AqueousSolubilityProperty extends MoleculeObjectProperty {

    public static final String METRICS_CODE = Metrics.METRICS_LOGS;

    public AqueousSolubilityProperty() {
        super("Solubility", "Aqueous solubility", METRICS_CODE, Float.class);
    }
}
