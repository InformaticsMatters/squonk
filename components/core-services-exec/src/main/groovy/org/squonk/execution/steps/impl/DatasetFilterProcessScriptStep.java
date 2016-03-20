package org.squonk.execution.steps.impl;

import com.im.lac.types.BasicObject;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;
import org.squonk.util.GroovyScriptExecutor;

import javax.script.ScriptEngine;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 29/12/15.
 */
public class DatasetFilterProcessScriptStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(DatasetFilterProcessScriptStep.class.getName());

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    public static final String OPTION_SCRIPT = StepDefinitionConstants.TrustedGroovyDataset.OPTION_SCRIPT;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        Dataset input = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, PersistenceType.DATASET, varman, true);
        LOG.info("Input Dataset: " + input);
        String script = getOption(OPTION_SCRIPT, String.class);
        if (script == null) {
            throw new IllegalStateException("Script not defined. Should be present as option named " + OPTION_SCRIPT);
        }
        LOG.info("Script: " + script);

        GroovyClassLoader gcl = new GroovyClassLoader();
        String clsDef = buildClassDefinition(script);
        LOG.info("Built predicate class:\n" + clsDef);
        Class<Predicate> cls = gcl.parseClass(clsDef);
        Predicate predicate = cls.newInstance();

        Stream output = input.getStream().filter(predicate);
        Dataset results = new Dataset(input.getType(), output, deriveOutputDatasetMetadata(input.getMetadata()));

        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, results, PersistenceType.DATASET, varman);

        LOG.info("Results: " + results.getMetadata());
    }

    protected DatasetMetadata deriveOutputDatasetMetadata(DatasetMetadata input) {
        if (input == null) {
            return new DatasetMetadata(BasicObject.class);
        } else {
            return new DatasetMetadata(input.getType(), input.getValueClassMappings(), 0, input.getProperties());
        }
    }

    private String buildClassDefinition(String script) {
        return "class Filter implements java.util.function.Predicate {\n  boolean test(def bo) {\n    bo.values.with {      \n" +
                script + "\n}}}";
    }
}
