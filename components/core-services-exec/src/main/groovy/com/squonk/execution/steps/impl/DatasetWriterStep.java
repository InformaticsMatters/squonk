package com.squonk.execution.steps.impl;

import com.squonk.execution.steps.AbstractStep;
import com.squonk.execution.steps.StepDefinitionConstants;
import com.squonk.execution.variable.Variable;
import com.squonk.execution.variable.VariableManager;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetProvider;
import org.apache.camel.CamelContext;

/** Reads a dataset and writes it. The only real purpose of this is to take a temporary
 * dataset (PersistenceType.NONE) and make it persistent (PersistenceType.DATASET).
 * Generally you should not need to do this, but its available should you need to.
 *
 * @author timbo
 */
public class DatasetWriterStep extends AbstractStep {

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public String[] getInputVariableNames() {
        return new String[]{VAR_INPUT_DATASET};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{VAR_OUTPUT_DATASET};
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        // the assumption is that the dataset has PersistenceType.NONE
        DatasetProvider p = fetchMappedValue(VAR_INPUT_DATASET, DatasetProvider.class, varman);
        Dataset ds = p.getDataset();
        createMappedVariable(VAR_OUTPUT_DATASET, Dataset.class, ds, Variable.PersistenceType.DATASET, varman);
    }

}
