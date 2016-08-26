package org.squonk.property;

import org.squonk.util.Metrics;

/**
 * Created by timbo on 05/04/16.
 */
public class AqueousSolubilityProperty extends MoleculeObjectProperty {

    public static final String METRICS_CODE = Metrics.METRICS_LOGS;
    public static final String PROP_NAME = "Solubility";
    public static final String PROP_DESC = "Aqueous Solubility";

    public AqueousSolubilityProperty() {
        super(PROP_NAME, PROP_DESC, METRICS_CODE, Float.class);
    }
}
