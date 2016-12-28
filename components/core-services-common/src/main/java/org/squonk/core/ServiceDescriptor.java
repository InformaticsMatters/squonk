package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.io.IODescriptor;
import org.squonk.options.OptionDescriptor;

import java.io.Serializable;
import java.util.Date;

/** Descriptor of some sort of service that can be executed. Currently there are two types:
 * <ol>
 *     <li>REST (or plain HTTP) services</li>
 *     <li>Docker containers</li>
 * </ol>
 *
 * The basic process for REST services goes as follows:
 * <ol>
 * <li>The service implementation provides a URL that returns a List of ServiceDescriptors for
 * services it supports.</li>
 * <li>The administrator of the service registers this URL into the system to make the system aware
 * of the services</li>
 * <li>At runtime the system looks up the registered services, retrieves their ServiceDescriptors,
 * and makes those services available to the user</li>
 * <li>The user chooses to use a service. A UI is generated to allow them to define the appropriate
 * options for execution (see @{link #getOptionDescriptors()} for the defintion of the options.</li>
 * <li>When user chooses to submit the appropriate JobDefintion is created and POSTed to the job
 * service, using the executorClassName to orchestrate the process</li>
 * <li>A JobStatus is immediately returned that allows the job to be monitored and handled.</li>
 * </ol>
 * <p>
 * For Docker containers the process is similar, but the executionEndpoint property is used to define the default docker
 * image name to use, though executors may allow this to be overridden with one ot the options. The
 * orchestration is handled by the defined executorClassName.
 *
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServiceDescriptor implements Serializable {

    public enum Status {
        ACTIVE, INACTIVE, UNKNOWN
    }

    private final String id;
    private final String name;
    private final String description;
    private final String[] tags;
    private final String resourceUrl;
    private final String icon;
    private Status status;
    private Date statusLastChecked;
    private final IODescriptor[] inputDescriptors;
    private final IODescriptor[] outputDescriptors;
    private final OptionDescriptor[] options;
    private final String executorClassName;
    private final String executionEndpoint;

    @JsonCreator
    public ServiceDescriptor(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("tags") String[] tags,
            @JsonProperty("resourceUrl") String resourceUrl,
            @JsonProperty("icon") String icon,
            @JsonProperty("status") Status status,
            @JsonProperty("statusLastChecked") Date statusLastChecked,
            @JsonProperty("inputDescriptors") IODescriptor[] inputDescriptors,
            @JsonProperty("outputDescriptors") IODescriptor[] outputDescriptors,
            @JsonProperty("options") OptionDescriptor[] options,
            @JsonProperty("executorClassName") String executorClassName,
            @JsonProperty("executionEndpoint") String executionEndpoint) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.resourceUrl = resourceUrl;
        this.icon = icon;
        this.status = status;
        this.statusLastChecked = statusLastChecked;
        this.inputDescriptors = inputDescriptors;
        this.outputDescriptors = outputDescriptors;
        this.options = options;
        this.executorClassName = executorClassName;
        this.executionEndpoint = executionEndpoint;
    }

    public ServiceDescriptor(
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            IODescriptor[] inputDescriptors,
            IODescriptor[] outputDescriptors,
            OptionDescriptor[] options,
            String executorClassName,
            String executionEndpoint) {
        this(id, name, description, tags, resourceUrl, icon, Status.UNKNOWN, null, inputDescriptors, outputDescriptors, options, executorClassName, executionEndpoint);
    }

//    public ServiceDescriptor(
//            String id,
//            String name,
//            String description,
//            String[] tags,
//            String resourceUrl,
//            String icon,
//            Status status,
//            Date statusLastChecked,
//            Class inputClass,
//            Class outputClass,
//            IOType inputType,
//            IOType outputType,
//            OptionDescriptor[] options,
//            String executorClassName,
//            String executionEndpoint) {
//        this(id, name, description, tags, resourceUrl, icon, status, statusLastChecked,
//                new IODescriptor[] { new IODescriptor("input", inputClass, inputType, IOMode.STREAM) },
//                new IODescriptor[] { new IODescriptor("output", outputClass, outputType, IOMode.STREAM) },
//                options, executorClassName, executionEndpoint);
//    }
//
//    public ServiceDescriptor(
//            String id,
//            String name,
//            String description,
//            String[] tags,
//            String resourceUrl,
//            String icon,
//            Class inputClass,
//            Class outputClass,
//            IOType inputType,
//            IOType outputType,
//            OptionDescriptor[] options,
//            String executorClassName,
//            String executionEndpoint) {
//        this(id, name, description, tags, resourceUrl, icon, Status.UNKNOWN, null, inputClass, outputClass, inputType, outputType, options, executorClassName, executionEndpoint);
//    }



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

    /** Get the definitions of the inputs for this service. Typically there will be one input and it will be named 'input'
     * but there can be more complex scenarios
     *
     * @return
     */
    public IODescriptor[] getInputDescriptors() {
        return inputDescriptors;
    }

    /** Get the definitions of the outputs for this service. Typically there will be one output and it will be named 'output'
     * but there can be more complex scenarios
     *
     * @return
     */
    public IODescriptor[] getOutputDescriptors() {
        return outputDescriptors;
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
                .append(" executor:").append(executorClassName)
                .append(" endpoint:").append(executionEndpoint)
                .append("]");
        return b.toString();
    }


}
