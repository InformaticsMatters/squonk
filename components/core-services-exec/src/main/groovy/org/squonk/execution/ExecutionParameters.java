package org.squonk.execution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExecutionParameters implements Serializable {

    private final String serviceDescriptorId;

    /** User specified options for the execution of the cell
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private final Map<String, Object> options;


    public ExecutionParameters(
            @JsonProperty("serviceDescriptorId") String serviceDescriptorId,
            @JsonProperty("options") Map<String, Object> options) {
        this.serviceDescriptorId = serviceDescriptorId;
        this.options = options;
    }

    public String getServiceDescriptorId() {
        return serviceDescriptorId;
    }

    public Map<String, Object> getOptions() {
        return options;
    }
}
