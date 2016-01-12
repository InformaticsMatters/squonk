package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;

import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;

import java.util.logging.Logger;

/** Simple step used for testing that reads text input and writes it to output
 *
 * Created by timbo on 06/01/16.
 */
public class EchoStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(EchoStep.class.getName());

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        String input = fetchMappedInput("input", String.class, PersistenceType.TEXT, varman);
        if (input == null) {
            throw new IllegalStateException("Input variable not found");
        }
        LOG.info("Input: " + input);

        createMappedOutput("output", String.class, input, PersistenceType.TEXT, varman);
        LOG.info("Wrote input as output");
    }
}
