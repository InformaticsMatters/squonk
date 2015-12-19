package org.squonk.execution.steps.impl;

import org.squonk.execution.steps.AbstractStep;
import com.im.lac.camel.processor.ValueTransformerProcessor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.transform.TransformDefintions;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 */
public class ValueTransformerStep extends AbstractStep {

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    public static final String OPTION_TRANSFORMS = "Transformers";

    @Override
    public String[] getInputVariableNames() {
        return new String[]{VAR_INPUT_DATASET};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{};
    }

    /**
     * Add the transforms to the dataset Stream. NOTE: transforms will not occur
     * until a terminal operation is performed on the Stream. Normally no output is
     * created as the transforms are added to the input dataset which will be
     * transient, however if an output field is needed then specify a mapping for the 
     * field named FIELD_OUTPUT_DATASET. 
     *
     * @param varman
     * @param context
     * @throws Exception
     */
    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        Dataset ds = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, PersistenceType.DATASET, varman);
        if (ds == null) {
            throw new IllegalStateException("Input field not found: " + VAR_INPUT_DATASET);
        }
        TransformDefintions txs = getOption(OPTION_TRANSFORMS, TransformDefintions.class);
        if (txs == null) {
            throw new IllegalStateException("Options not found: " + OPTION_TRANSFORMS);
        }
        ValueTransformerProcessor p = ValueTransformerProcessor.create(txs);
        p.execute(context.getTypeConverter(), ds);
        
        String outFldName = mapOutputVariable(VAR_OUTPUT_DATASET);
        if (outFldName != null) {
            createVariable(outFldName, Dataset.class, ds, PersistenceType.NONE, varman);
        }
    }

}
