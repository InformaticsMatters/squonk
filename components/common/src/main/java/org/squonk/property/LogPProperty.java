package org.squonk.property;

import org.squonk.util.Metrics;

/**
 * Created by timbo on 05/04/16.
 */
public class LogPProperty extends MoleculeObjectProperty {

    public static final String METRICS_CODE = Metrics.METRICS_LOGP;
    public static final String PROP_NAME = "LogP";
    public static final String PROP_DESC = "Octanol water partition coefficient";

    public LogPProperty() {
        super(PROP_NAME, PROP_DESC, METRICS_CODE, Float.class);
    }
}
