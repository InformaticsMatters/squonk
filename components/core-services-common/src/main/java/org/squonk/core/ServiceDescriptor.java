package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.io.ExecutableDescriptor;
import org.squonk.io.IODescriptor;
import org.squonk.options.OptionDescriptor;

import java.io.Serializable;
import java.util.Date;

/**
 * Descriptor of some sort of service that can be executed. Currently there are two types:
 * <ol>
 * <li>REST (or plain HTTP) services</li>
 * <li>Docker containers</li>
 * </ol>
 * <p>
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
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServiceDescriptor extends ExecutableDescriptor implements Serializable {

    public enum Status {
        ACTIVE, INACTIVE, UNKNOWN
    }

    private Status status;
    private Date statusLastChecked;
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

        super(id, name, description, tags, resourceUrl, icon, inputDescriptors, outputDescriptors, options);
        this.status = status;
        this.statusLastChecked = statusLastChecked;
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

    public ServiceDescriptor(
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            IODescriptor inputDescriptor,
            IODescriptor outputDescriptor,
            OptionDescriptor[] options,
            String executorClassName,
            String executionEndpoint) {
        this(id, name, description, tags, resourceUrl, icon,
                new IODescriptor[]{inputDescriptor}, new IODescriptor[]{outputDescriptor}, options, executorClassName, executionEndpoint);
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

    public String getExecutorClassName() {
        return executorClassName;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("ServiceDescriptor[")
                .append("id:").append(getId())
                .append(" name:").append(getName())
                .append(" executor:").append(executorClassName)
                .append(" endpoint:").append(executionEndpoint)
                .append("]");
        return b.toString();
    }


}
