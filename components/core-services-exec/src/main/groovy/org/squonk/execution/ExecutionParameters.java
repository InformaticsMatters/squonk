package org.squonk.execution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.squonk.core.ServiceDescriptor;

import java.io.Serializable;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExecutionParameters implements Serializable {

    /** The service descriptor defining what to execute
     */
    private final ServiceDescriptor serviceDescriptor;

    /** User specified options for the execution of the cell
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private final Map<String, Object> options;


    public ExecutionParameters(
            @JsonProperty("serviceDescriptor") ServiceDescriptor serviceDescriptor,
            @JsonProperty("options") Map<String, Object> options) {
        this.serviceDescriptor = serviceDescriptor;
        this.options = options;

    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    public Map<String, Object> getOptions() {
        return options;
    }
}
