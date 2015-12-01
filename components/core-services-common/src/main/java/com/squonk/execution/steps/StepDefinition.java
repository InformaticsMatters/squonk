package com.squonk.execution.steps;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private final Map<String, String> fieldMappings = new LinkedHashMap<>();

    public StepDefinition() {

    }

    public StepDefinition(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public StepDefinition(String implementationClass, Map<String, Object> options, Map<String, String> fieldMappings) {
        this(implementationClass);
        setOptions(options);
        setFieldMappings(fieldMappings);
    }
    
    public StepDefinition withOption(String name, Object value) {
        options.put(name, value);
        return this;
    }
    
    public StepDefinition withFieldMapping(String from, String to) {
        fieldMappings.put(from, to);
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

    public Map<String, String> getFieldMappings() {
        return fieldMappings;
    }

    public void setFieldMappings(Map<String, String> fieldMappings) {
        this.fieldMappings.clear();
        if (fieldMappings != null) {
            this.fieldMappings.putAll(fieldMappings);
        }
    }

}
