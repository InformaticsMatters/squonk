package org.squonk.execution.steps.impl;

import org.squonk.types.BasicObject;
import groovy.lang.GroovyClassLoader;
import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 29/12/15.
 */
public class DatasetFilterGroovyStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(DatasetFilterGroovyStep.class.getName());

    public static final String OPTION_SCRIPT = StepDefinitionConstants.TrustedGroovyDataset.OPTION_SCRIPT;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_INPUT;
        Dataset input = fetchMappedInput("input", Dataset.class, varman, true);
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
        statusMessage = "Filtering ...";
        Stream output = input.getStream().filter(predicate);
        Dataset results = new Dataset(output, deriveOutputDatasetMetadata(input.getMetadata()));

        createMappedOutput("output", Dataset.class, results, varman);
        statusMessage = generateStatusMessage(input.getSize(), results.getSize(), -1);
        LOG.info("Results: " + results.getMetadata());;
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
