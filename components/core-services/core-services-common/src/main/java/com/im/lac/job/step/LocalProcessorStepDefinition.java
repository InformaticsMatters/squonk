package com.im.lac.job.step;

import java.util.Map;

/**
 *
 * @author timbo
 */
public class LocalProcessorStepDefinition extends ConsumingStepDefinition {

    private String processorClassName;
    private Map<String, Object> configuration;
    private Map<String, String> variableMappings;

    public LocalProcessorStepDefinition() {

    }

    public LocalProcessorStepDefinition(
            String processorClassName,
            Map<String, Object> configuration,
            Map<String, String> variableMappings) {
        configureLocalProcessor(processorClassName, configuration, variableMappings);

    }

    /**
     * The implementation class that does the processing. Must implement the
     * LocalProcessorStep interface.
     *
     * @return
     */
    public String getProcessorClassName() {
        return processorClassName;
    }

    /**
     * The configuration parameters for the processing.
     *
     * @return
     */
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    /**
     * How to map the input variable names to those that are expected by the
     * implementation class
     *
     * @return
     */
    public Map<String, String> getVariableMappings() {
        return variableMappings;
    }

    public final void configureLocalProcessor(
            String processorClassName,
            Map<String, Object> configuration,
            Map<String, String> variableMappings) {
        this.processorClassName = processorClassName;
        this.configuration = configuration;
        this.variableMappings = variableMappings;
    }

}
