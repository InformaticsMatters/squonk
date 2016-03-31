package org.squonk.execution.steps.impl;

import org.squonk.dataset.Dataset;
import org.squonk.util.GroovyScriptExecutor;
import org.apache.camel.CamelContext;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import javax.script.ScriptEngine;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 29/12/15.
 */
public class TrustedGroovyDatasetScriptStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(TrustedGroovyDatasetScriptStep.class.getName());

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    public static final String OPTION_SCRIPT = StepDefinitionConstants.TrustedGroovyDataset.OPTION_SCRIPT;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        Dataset input = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, varman, true);
        LOG.info("Input Dataset: " + input);
        String script = getOption(OPTION_SCRIPT, String.class);
        if (script == null) {
            throw new IllegalStateException("Script not defined. Should be present as option named " + OPTION_SCRIPT);
        }
        LOG.info("Script: " + script);

        Map bindings = Collections.singletonMap("input", input);

        ScriptEngine engine = GroovyScriptExecutor.createScriptEngine(this.getClass().getClassLoader());
        Dataset output = GroovyScriptExecutor.executeAndReturnValue(Dataset.class, engine, script, bindings);
        LOG.info("Script executed");

        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, output, varman);

        LOG.info("Results: " + output.getMetadata());
    }
}
