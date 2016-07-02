package org.squonk.property;

import org.squonk.util.Metrics;

/**
 * Created by timbo on 05/04/16.
 */
public class LogPProperty extends MoleculeObjectProperty {

    public static final String METRICS_CODE = Metrics.METRICS_LOGP;

    public LogPProperty() {
        super("LogP", "Octanol water partition coefficient", METRICS_CODE, Float.class);
    }
}
