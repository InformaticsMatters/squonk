package com.im.lac.services.job.service.steps;

import com.im.lac.camel.processor.ValueTransformerProcessor;
import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.transform.TransformDefintions;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 */
public class ValueTransformerStep extends AbstractStep {

    private static final String FIELD_INPUT_DATASET = "_ValueTransformerInputDataset";
    private static final String FIELD_OUTPUT_DATASET = "_ValueTransformerOutputDataset";
    private static final String OPTION_TRANSFORMS = "Transformers";

    @Override
    public String[] getInputVariableNames() {
        return new String[]{FIELD_INPUT_DATASET};
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
        Dataset ds = fetchMappedValue(FIELD_INPUT_DATASET, Dataset.class, varman);
        if (ds == null) {
            throw new IllegalStateException("Input field not found: " + FIELD_INPUT_DATASET);
        }
        TransformDefintions txs = getOption(OPTION_TRANSFORMS, TransformDefintions.class);
        if (txs == null) {
            throw new IllegalStateException("Options not found: " + OPTION_TRANSFORMS);
        }
        ValueTransformerProcessor p = ValueTransformerProcessor.create(txs);
        p.execute(context.getTypeConverter(), ds);
        
        String outFldName = mapVariableName(FIELD_OUTPUT_DATASET);
        if (outFldName != null) {
            createVariable(outFldName, Dataset.class, ds, Variable.PersistenceType.NONE, varman);
        }
    }

}
