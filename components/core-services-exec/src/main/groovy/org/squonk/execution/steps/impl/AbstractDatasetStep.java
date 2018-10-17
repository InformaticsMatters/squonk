package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.AbstractThinDatasetStep;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.BasicObject;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

public abstract class AbstractDatasetStep<P extends BasicObject, Q extends BasicObject>
        extends AbstractThinDatasetStep {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetStep.class.getName());

    /** Specifies whether an input dataset is expected, which is typically the case.
     * Set to false if there is output but no input.
     *
     */
    protected boolean inputRequired = true;


    /**
     * Executes assuming a single input dataset and a single output dataset.
     * This method is used when executing cells from the notebook
     *
     * @param varman
     * @param context
     * @throws Exception
     */
    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        statusMessage = MSG_PREPARING_INPUT;
        Dataset<P> input = (Dataset)fetchMappedInput("input", Dataset.class, varman);
        if (input == null && inputRequired) {
            throw new IllegalStateException("Input variable not found: input");
        }
        LOG.fine("Input Dataset: " + input);

        Dataset<Q> results = doExecuteWithDataset(input, context);

        String outFldName = mapOutputVariable("output");
        if (outFldName != null) {
            createVariable(outFldName, Dataset.class, results, varman);
        }
        LOG.info("Results: " + results.getMetadata());
    }

    /** Execute with the given inputs
     * This method is used when executing with externally provided data.
     *
     * @param inputs
     * @return
     * @throws Exception
     */
    @Override
    public Map<String,Object> executeWithData(Map<String,Object> inputs, CamelContext camelContext) throws Exception {
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("Single dataset expected");
        }
        Map.Entry<String,Object> entry = inputs.entrySet().iterator().next();
        String key = entry.getKey(); // value ignored, but presumed to be "input"
        Object value = entry.getValue();
        if (value instanceof Dataset) {
            Dataset<Q> results = doExecuteWithDataset((Dataset)value, camelContext);
            return Collections.singletonMap("output", results);
        } else {
            throw new IllegalArgumentException("Input expected to be a Dataset");
        }
    }

    /** Override this method to implement the required functionality
     *
     * @param input
     * @param camelContext
     * @return
     * @throws Exception
     */
    protected abstract Dataset<Q> doExecuteWithDataset(Dataset<P> input, CamelContext camelContext) throws Exception;
}
