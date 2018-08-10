package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.TypeConverter;
import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.AbstractStandardStep;
import org.squonk.execution.steps.ExternallyExecutableStep;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.BasicObject;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

public abstract class AbstractDatasetStandardStep<P extends BasicObject, Q extends BasicObject>
        extends AbstractStandardStep implements ExternallyExecutableStep {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetStandardStep.class.getName());


    /**
     * Execute the dataset generating a single dataset as the output.
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
        if (input == null) {
            throw new IllegalStateException("Input variable not found: input");
        }
        LOG.fine("Input Dataset: " + input);

        Dataset<Q> results = doExecute(input, options, context == null ? null : context.getTypeConverter());

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
    public Map<String,Object> doExecute(Map<String,Object> inputs, Map<String,Object> options, TypeConverter convertor) throws Exception {
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("Single dataset expected");
        }
        Map.Entry<String,Object> entry = inputs.entrySet().iterator().next();
        String key = entry.getKey(); // value ignored, but presumed to be "input"
        Object value = entry.getValue();
        if (value instanceof Dataset) {
            Dataset<Q> results = doExecute((Dataset)value, options, convertor);
            return Collections.singletonMap("output", results);
        } else {
            throw new IllegalArgumentException("Input expected to be a Dataset");
        }
    }

    protected abstract Dataset<Q> doExecute(Dataset<P> input, Map<String,Object> options, TypeConverter convertor) throws Exception;
}
