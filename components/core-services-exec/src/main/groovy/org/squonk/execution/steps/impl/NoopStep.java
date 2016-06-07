package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import java.util.logging.Logger;

/** Simple step used for testing that reads a dataset as input and writes it to output
 *
 * Created by timbo on 06/01/16.
 */
public class NoopStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(NoopStep.class.getName());

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        Dataset input = fetchMappedInput(StepDefinitionConstants.VARIABLE_INPUT_DATASET, Dataset.class, varman, true);
        if (input == null) {
            throw new IllegalStateException("Input variable not found");
        }
        createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, Dataset.class, input, varman);
        LOG.info("Wrote input as output");
    }
}
