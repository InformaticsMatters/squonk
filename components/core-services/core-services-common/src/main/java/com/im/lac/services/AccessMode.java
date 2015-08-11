package com.im.lac.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.im.lac.job.jobdef.JobDefinition;
import java.io.Serializable;

/**
 * Defines a way to access a service
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccessMode implements Serializable {

    final String id;
    final String name;
    final String description;
    final String executionEndpoint;
    final boolean endpointRelative;
    final Class<? extends JobDefinition> jobType;
    final Integer minSize;
    final Integer maxSize;
    final Float cost;
    final ServiceDescriptor.LicenseToken[] requiredLicenseTokens;
    final ServicePropertyDescriptor[] parameters;

    @JsonCreator
    public AccessMode(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("executionEndpoint") String executionEndpoint,
            @JsonProperty("endpointRelative") boolean endpointRelative,
            @JsonProperty("jobType") Class<? extends JobDefinition> jobType,
            @JsonProperty("minSize") Integer minSize,
            @JsonProperty("maxSize") Integer maxSize,
            @JsonProperty("cost") Float cost,
            @JsonProperty("requiredLicenseTokens") ServiceDescriptor.LicenseToken[] requiredLicenseTokens,
            @JsonProperty("parameters") ServicePropertyDescriptor[] parameters) {

        if (cost != null && cost < 0) {
            throw new IllegalStateException("Cannot specify negative cost");
        }

        this.id = id;
        this.name = name;
        this.description = description;
        this.executionEndpoint = executionEndpoint;
        this.endpointRelative = endpointRelative;
        this.jobType = jobType;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.cost = cost;
        this.requiredLicenseTokens = requiredLicenseTokens;
        this.parameters = parameters;
    }

    /**
     * The ID of this mode
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * The short name of this mode
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * A meaningful description of the mode
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * The endpoint (often a URL) to call to execute the service
     *
     * @return
     */
    public String getExecutionEndpoint() {
        return executionEndpoint;
    }

    /**
     * Is the definition of the endpoint relative to the URL from which the ServiceDescriptor was
     * obtained
     *
     * @return
     */
    public boolean isEndpointRelative() {
        return endpointRelative;
    }

    /**
     * The JobDefinition class for this mode. This is used to submit a job to this service.
     *
     * @return
     */
    public Class<? extends JobDefinition> getJobType() {
        return jobType;
    }

    /**
     * The minimum number of items that can be sent to this service mode. Usually this would be 1.
     *
     * @return
     */
    public Integer getMinSize() {
        return minSize;
    }

    /**
     * The maximum number of items that can be sent to this service mode. Usually this would be
     * Integer.MAX_VALUE, but this allows certain service modes to be limited in terms of input e.g.
     * for slow running jobs only a small number of records can be sent in "direct" mode with a
     * batch mode supporting larger, or unlimited number of records.
     *
     * @return
     */
    public Integer getMaxSize() {
        return maxSize;
    }

    /**
     * The cost of using this service for a single item. e.g. for processing 1000 items is costs
     * 1000 * cost. Many services will have a cost of zero. Cost is in arbitrary units and deducted
     * from the users account.
     * <p>
     * <b>Important</b>: ensure negative costs are not permitted!
     *
     * @return
     */
    public Float getCost() {
        return cost;
    }

    /**
     * To use this service mode you must possess ALL of these tokens.
     *
     * @return
     */
    public ServiceDescriptor.LicenseToken[] getRequiredLicenseTokens() {
        return requiredLicenseTokens;
    }

    /**
     * Get the parameters accepted by this service mode. This is used to build the UI for the user
     * to fill, and those parameters submitted with the JobDefintion. These should be modelled as
     * name/value pairs that can handled as HTTP header parameters (not a Map as HTTP supports
     * multiple parameters with the same name)
     *
     * TODO - spec this out in more detail
     *
     * @return
     */
    public ServicePropertyDescriptor[] getParameters() {
        return parameters;
    }

}
