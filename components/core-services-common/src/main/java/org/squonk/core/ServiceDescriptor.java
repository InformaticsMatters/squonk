package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.options.OptionDescriptor;

import java.io.Serializable;
import java.util.Date;

/**
 * Descriptor of a service that can be used to parameterise a request to this service. The basic
 * process goes as follows:
 * <ol>
 * <li>The service implementation provides a URL that returns a List of ServiceDescriptors for
 * services it supports.</li>
 * <li>The administrator of the service registers this URL into the system to make the system aware
 * of the services</li>
 * <li>At runtime the system looks up the registered services, retrieves their ServiceDescriptors,
 * and makes those services available to the user</li>
 * <li>The user chooses to use a service. A UI is generated to allow them to define the appropriate
 * parameters for execution.</li>
 * <li>When user chooses to submit the appropriate JobDefintion is created and POSTed to the job
 * service</li>
 * <li>A JobStatus is immediately returned that allows the job to be monitored and handled.</li>
 * </ol>
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServiceDescriptor implements Serializable {

    public enum DataType {

        ITEM,   // input a single item
        STREAM, // input a stream of items
        OPTION  // read from one of the options with the key of 'body'
    }

    public enum Status {
        ACTIVE, INACTIVE, UNKNOWN
    }

    private final String id;
    private final String name;
    private final String description;
    private final String[] tags;
    private final String resourceUrl;
    private final Class inputClass;
    private final Class outputClass;
    private final DataType inputType;
    private final DataType outputType;
    private final String icon;
    private final String executionEndpoint;
    private final boolean endpointRelative;
    private final OptionDescriptor[] options;
    private final String executorClassName;
    private Status status;
    private Date statusLastChecked;

    @JsonCreator
    public ServiceDescriptor(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("tags") String[] tags,
            @JsonProperty("resourceUrl") String resourceUrl,
            @JsonProperty("inputClass") Class inputClass,
            @JsonProperty("outputClass") Class outputClass,
            @JsonProperty("inputType") DataType inputType,
            @JsonProperty("outputType") DataType outputType,
            @JsonProperty("icon") String icon,
            @JsonProperty("executionEndpoint") String executionEndpoint,
            @JsonProperty("endpointRelative") boolean endpointRelative,
            @JsonProperty("status") Status status,
            @JsonProperty("statusLastChecked") Date statusLastChecked,
            @JsonProperty("options") OptionDescriptor[] options,
            @JsonProperty("executorClassName") String executorClassName
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.resourceUrl = resourceUrl;
        this.inputClass = inputClass;
        this.outputClass = outputClass;
        this.inputType = inputType;
        this.outputType = outputType;
        this.icon = icon;
        this.executionEndpoint = executionEndpoint;
        this.endpointRelative = endpointRelative;
        this.status = status;
        this.options = options;
        this.executorClassName = executorClassName;
    }

    public ServiceDescriptor(
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            Class inputClass,
            Class outputClass,
            DataType inputType,
            DataType outputType,
            String icon,
            String executionEndpoint,
            boolean endpointRelative,
            OptionDescriptor[] options,
            String executorClassName
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.resourceUrl = resourceUrl;
        this.inputClass = inputClass;
        this.outputClass = outputClass;
        this.inputType = inputType;
        this.outputType = outputType;
        this.icon = icon;
        this.executionEndpoint = executionEndpoint;
        this.endpointRelative = endpointRelative;
        this.status = Status.UNKNOWN;
        this.options = options;
        this.executorClassName = executorClassName;
    }


    /**
     * The ID of this service
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * The short name of this service
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * A meaningful description of the service
     *
     * @return
     */
    public String getDescription() {
        return description;
    }


    /**
     * A set of tags that describe the service and can be used for searching/discovery
     *
     * @return
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * The URL to call to get documentation on the service. e.g. the services "home page"
     *
     * @return
     */
    public String getResourceUrl() {
        return resourceUrl;
    }

    /**
     * The type(s) of object this service can process. e.g. MoleculeObject
     *
     * @return
     */
    public Class getInputClass() {
        return inputClass;
    }

    /**
     * Often the same as the inputClass, but not always
     *
     * @return
     */
    public Class getOutputClass() {
        return outputClass;
    }

    /**
     * Single Item or Stream of multiple Items
     *
     * @return
     */
    public DataType getInputType() {
        return inputType;
    }

    /**
     * Usually the same as the inputType, but not always
     *
     * @return
     */
    public DataType getOutputType() {
        return outputType;
    }

    /**
     * The relative or absolute path to an icon that describes the service
     *
     * @return
     */
    public String getIcon() {
        return icon;
    }

    public String getExecutionEndpoint() {
        return executionEndpoint;
    }

    public boolean isEndpointRelative() {
        return endpointRelative;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getStatusLastChecked() {
        return statusLastChecked;
    }

    public void setStatusLastChecked(Date statusLastChecked) {
        this.statusLastChecked = statusLastChecked;
    }

    public OptionDescriptor[] getOptions() {
        return options;
    }

    public String getExecutorClassName() {
        return executorClassName;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("ServiceDescriptor[")
                .append("id:").append(id)
                .append(" name:").append(name)
                .append(" endpoint:").append(executionEndpoint)
                .append("]");
        return b.toString();
    }


}
