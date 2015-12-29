package org.squonk.execution.steps.impl;

import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;
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
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        // the assumption is that the dataset has PersistenceType.NONE
        DatasetProvider p = fetchMappedInput(VAR_INPUT_DATASET, DatasetProvider.class, PersistenceType.DATASET, varman);
        Dataset ds = p.getDataset();
        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, ds, PersistenceType.DATASET, varman);
    }

}
