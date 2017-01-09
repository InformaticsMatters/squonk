package org.squonk.execution.steps;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.squonk.io.IODescriptor;
import org.squonk.core.ServiceDescriptor;
import org.squonk.notebook.api.VariableKey;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class StepDefinition implements Serializable {

    private String implementationClass;

    /**
     *  The input definitions
     */
    private IODescriptor[] inputs;

    /**
     *  The output definitions
     */
    private IODescriptor[] outputs;

    /**
     * Mapping of an input to its source which is usually from an upstream cell. The keys are the names of the variables in this
     * step, and the values are the @{link VariableKey}s that define the producer cell and the variable name to read.
     * A mapping must be present for all inputs that are defined.
     */
    private final Map<String, VariableKey> inputVariableMappings = new LinkedHashMap<>();

    /**
     * Optional mapping for output names that allows an output variable to be renamed. Usually this is not necessary.
     */
    private final Map<String, String> outputVariableMappings = new LinkedHashMap<>();

    /**
     * User specified options for the execution of the cell
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private final Map<String, Object> options = new LinkedHashMap<>();

    /** the ID of the service to execute (if any)
     */
    private String serviceId;

    /**
     * Optional descriptor that defines execution criteria for cells that use external services (e.g. REST or Docker)
     *
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private ServiceDescriptor serviceDescriptor;

    public StepDefinition() {
    }

    public StepDefinition(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public StepDefinition(String implementationClass, String serviceId) {
        this.implementationClass = implementationClass;
        this.serviceId = serviceId;
    }

    public StepDefinition(
            String implementationClass,
            Map<String, Object> options,
            ServiceDescriptor serviceDescriptor) {
        this.implementationClass = implementationClass;
        setOptions(options);
        this.serviceDescriptor = serviceDescriptor;
        this.serviceId = serviceDescriptor == null ? null : serviceDescriptor.getServiceConfig().getId();
    }

    public StepDefinition(
            String implementationClass,
            Map<String, Object> options,
            IODescriptor[] inputs,
            IODescriptor[] outputs,
            Map<String, VariableKey> inputVariableMappings,
            Map<String, String> outputVariableMappings) {
        this(implementationClass, options, inputs, outputs, inputVariableMappings, outputVariableMappings, null);
    }

    public StepDefinition(String implementationClass, Map<String, Object> options, IODescriptor[] inputs, IODescriptor[] outputs, Map<String, VariableKey> inputVariableMappings, Map<String, String> outputVariableMappings, ServiceDescriptor serviceDescriptor) {
        this(implementationClass, options, serviceDescriptor);
        this.inputs = inputs;
        this.outputs = outputs;
        setInputVariableMappings(inputVariableMappings);
        setOutputVariableMappings(outputVariableMappings);
    }

    public StepDefinition(String implementationClass, Map<String, Object> options, Map<IODescriptor, VariableKey> inputTypesAndMappings, Map<IODescriptor, String> outputTypesAndMappings) {
        this(implementationClass, options, inputTypesAndMappings, outputTypesAndMappings, null);
    }

    /**
     * Convenience constructor allowing types and mappings to be specified together
     *
     * @param implementationClass
     * @param options
     * @param inputTypesAndMappings
     * @param outputTypesAndMappings
     * @param serviceDescriptor
     */
    public StepDefinition(String implementationClass, Map<String, Object> options, Map<IODescriptor, VariableKey> inputTypesAndMappings, Map<IODescriptor, String> outputTypesAndMappings, ServiceDescriptor serviceDescriptor) {
        this(implementationClass, options, serviceDescriptor);
        inputs = inputTypesAndMappings == null ? null : new IODescriptor[inputTypesAndMappings.size()];
        outputs = outputTypesAndMappings == null ? null : new IODescriptor[outputTypesAndMappings.size()];
        fillInputsOutputs(inputTypesAndMappings, inputs, inputVariableMappings);
        fillInputsOutputs(outputTypesAndMappings, outputs, outputVariableMappings);
    }

    /** Convenience constructor allowing input mappings and output types to be specified easily (no name mappings)
     *
     * @param implementationClass
     * @param options
     * @param inputTypesAndMappings
     * @param outputs
     * @param serviceDescriptor
     */
    public StepDefinition(String implementationClass, Map<String, Object> options, Map<IODescriptor, VariableKey> inputTypesAndMappings, IODescriptor[] outputs, ServiceDescriptor serviceDescriptor) {
        this(implementationClass, options, serviceDescriptor);
        inputs = inputTypesAndMappings == null ? null : new IODescriptor[inputTypesAndMappings.size()];
        this.outputs = outputs;
        fillInputsOutputs(inputTypesAndMappings, inputs, inputVariableMappings);
    }

    private void fillInputsOutputs(Map<IODescriptor, ? extends Object> typesAndMappings, IODescriptor[] types, Map map) {
        if (typesAndMappings != null) {
            int i = 0;
            for (Map.Entry<IODescriptor, ? extends Object> e : typesAndMappings.entrySet()) {
                types[i] = e.getKey();
                if (e.getValue() != null) {
                    map.put(e.getKey().getName(), e.getValue());
                }
                i++;
            }
        }
    }

    public IODescriptor[] getInputs() {
        return inputs;
    }

    public void setInputs(IODescriptor[] inputs) {
        this.inputs = inputs;
    }

    public IODescriptor[] getOutputs() {
        return outputs;
    }

    public void setOutputs(IODescriptor[] outputs) {
        this.outputs = outputs;
    }

    public StepDefinition withInputs(IODescriptor[] inputs) {
        this.inputs = inputs;
        return this;
    }

    public StepDefinition withOutputs(IODescriptor[] outputs) {
        this.outputs = outputs;
        return this;
    }

    public StepDefinition withOption(String name, Object value) {
        options.put(name, value);
        return this;
    }

    public StepDefinition withOptions(Map<String, Object> options) {
        this.options.putAll(options);
        return this;
    }

    public StepDefinition withInputVariableMapping(String from, VariableKey to) {
        inputVariableMappings.put(from, to);
        return this;
    }

    public StepDefinition withInputVariableMappings(Map<String, VariableKey> mappings) {
        inputVariableMappings.putAll(mappings);
        return this;
    }

    public StepDefinition withOutputVariableMapping(String from, String to) {
        outputVariableMappings.put(from, to);
        return this;
    }

    public StepDefinition withOutputVariableMappings(Map<String, String> mappings) {
        outputVariableMappings.putAll(mappings);
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

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public StepDefinition withServiceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    public void setServiceDescriptor(ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }

    public StepDefinition withServiceDescriptor(ServiceDescriptor serviceDescriptor) {
        if (serviceId != null) {
            if (!serviceId.equals(serviceDescriptor.getServiceConfig().getId())) {
                throw new IllegalStateException("ServiceDescriptor ID does not match the service ID that is already defined");
            }
        }
        this.serviceDescriptor = serviceDescriptor;
        return this;
    }
}
