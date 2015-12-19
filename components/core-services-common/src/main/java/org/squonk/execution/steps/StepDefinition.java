package org.squonk.execution.steps;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.squonk.notebook.api.VariableKey;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class StepDefinition implements Serializable {

    private String implementationClass;
    private final Map<String, Object> options = new LinkedHashMap<>();
    private final Map<String, VariableKey> inputVariableMappings = new LinkedHashMap<>();
    private final Map<String, String> outputVariableMappings = new LinkedHashMap<>();

    public StepDefinition() {

    }

    public StepDefinition(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public StepDefinition(String implementationClass, Map<String, Object> options, Map<String, VariableKey> inputVariableMappings, Map<String, String> outputVariableMappings) {
        this(implementationClass);
        setOptions(options);
        setInputVariableMappings(inputVariableMappings);
        setOutputVariableMappings(outputVariableMappings);
    }
    
    public StepDefinition withOption(String name, Object value) {
        options.put(name, value);
        return this;
    }
    
    public StepDefinition withInputVariableMapping(String from, VariableKey to) {
        inputVariableMappings.put(from, to);
        return this;
    }

    public StepDefinition withOutputVariableMapping(String from, String to) {
        outputVariableMappings.put(from, to);
        return this;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options.clear();
        if (options != null) {
            this.options.putAll(options);
        }
    }

    public Map<String, VariableKey> getInputVariableMappings() {
        return inputVariableMappings;
    }

    public void setInputVariableMappings(Map<String, VariableKey> inputVariableMappings) {
        this.inputVariableMappings.clear();
        if (inputVariableMappings != null) {
            this.inputVariableMappings.putAll(inputVariableMappings);
        }
    }

    public Map<String, String> getOutputVariableMappings() {
        return outputVariableMappings;
    }

    public void setOutputVariableMappings(Map<String, String> outputVariableMappings) {
        this.outputVariableMappings.clear();
        if (outputVariableMappings != null) {
            this.outputVariableMappings.putAll(outputVariableMappings);
        }
    }

}
