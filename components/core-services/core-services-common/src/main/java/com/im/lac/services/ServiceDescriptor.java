package com.im.lac.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.im.lac.dataset.Metadata;
import java.io.Serializable;

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

    /**
     * A license token the user must have to be able to use the service. Should this really be an
     * enum? - probably need to be able to define new token types on the fly so it might be better
     * as and interface.
     *
     */
    public enum LicenseToken {

        CHEMAXON
    }

    private final String id;
    private final String name;
    private final String description;
    private final String[] tags;
    private final String resourceUrl;
    private final String[] paths;
    private final String owner;
    private final String ownerUrl;
    private final String[] layers;
    private final Class inputClass;
    private final Class outputClass;
    private final Metadata.Type inputType;
    private final Metadata.Type outputType;
    private final AccessMode[] accessModes;

    @JsonCreator
    public ServiceDescriptor(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("tags") String[] tags,
            @JsonProperty("resourceUrl") String resourceUrl,
            @JsonProperty("paths") String[] paths,
            @JsonProperty("owner") String owner,
            @JsonProperty("ownerUrl") String ownerUrl,
            @JsonProperty("layers") String[] layers,
            @JsonProperty("inputClass") Class inputClass,
            @JsonProperty("outputClass") Class outputClass,
            @JsonProperty("inputType") Metadata.Type inputType,
            @JsonProperty("outputType") Metadata.Type outputType,
            @JsonProperty("accessModes") AccessMode[] accessModes
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.resourceUrl = resourceUrl;
        this.paths = paths;
        this.owner = owner;
        this.ownerUrl = ownerUrl;
        this.layers = layers;
        this.inputClass = inputClass;
        this.outputClass = outputClass;
        this.inputType = inputType;
        this.outputType = outputType;
        this.accessModes = accessModes;
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
     * Get the location(s) where this service will appear in the UI. The path is modelled as a
     * absolute unix path. e.g. "/services/rdkit/clustering/my cluster service"
     *
     * @return
     */
    public String[] getPaths() {
        return paths;
    }

    /**
     * The owner of this service
     *
     * @return
     */
    public String getOwner() {
        return owner;
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
     * The URL of the owner of this service. e.g. the owners "home page"
     *
     * @return
     */
    public String getOwnerUrl() {
        return ownerUrl;
    }

    /**
     * The layers this service should belong to
     *
     * @return
     */
    public String[] getLayers() {
        return layers;
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
    public Metadata.Type getInputType() {
        return inputType;
    }

    /**
     * Usually the same as the inputType, but not always
     *
     * @return
     */
    public Metadata.Type getOutputType() {
        return outputType;
    }

    /**
     * One of more modes through which this service can be accessed. e.g. a service can support
     * direct and batch modes.
     *
     * @return
     */
    public AccessMode[] getAccessModes() {
        return accessModes;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("ServiceDescriptor[")
                .append("id:").append(id)
                .append(" name:").append(name)
                .append("]");
        return b.toString();
    }

}
