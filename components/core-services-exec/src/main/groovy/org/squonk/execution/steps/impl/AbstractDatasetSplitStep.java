package org.squonk.execution.steps.impl;

import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.io.IODescriptor;
import org.squonk.types.BasicObject;

import java.util.Map;
import java.util.logging.Logger;

public abstract class AbstractDatasetSplitStep<P extends BasicObject> extends AbstractStep  {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetSplitStep.class.getName());

    protected Map<String, Object> doExecute(Map<String, Object> inputs) throws Exception {
        if (inputs.size() == 0) {
            throw new IllegalArgumentException("Single dataset expected - found none");
        } else if (inputs.size() > 1) {
            throw new IllegalArgumentException("Single dataset expected - found " + inputs.size());
        }
        if (getOutputs() != null) {
            for (IODescriptor iod : getOutputs()) {
                LOG.info("Found output " + iod.getName());
            }
        } else {
            LOG.warning("Outputs not defined");
        }

        Dataset<P> input = (Dataset<P>)inputs.values().iterator().next();
        Map<String,Object> results = doExecuteWithDataset(input);
        return results;
    }

    protected abstract Map<String,Object> doExecuteWithDataset(Dataset<P> input) throws Exception;
}
