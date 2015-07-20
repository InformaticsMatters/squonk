package com.im.lac.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.im.lac.dataset.Metadata;

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
public class ServiceDescriptor {

    /**
     * A license token the user must have to be able to use the service. Should this really be an
     * enum? - probably need to be able to define new token types on the fly so it might be better
     * as and interface.
     *
     */
    public enum LicenseToken {

        CHEMAXON
    }

    private final String name;
    private final String description;
    private final String[] tags;
    private final String resourceUrl;
    private final String[] paths;
    private final String owner;
    private final String ownerUrl;
    private final String[] layers;
    private final Class[] inputClasses;
    private final Class[] outputClasses;
    private final Metadata.Type[] inputTypes;
    private final Metadata.Type[] outputTypes;
    private final AccessMode[] accessModes;

    public ServiceDescriptor(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("tags") String[] tags,
            @JsonProperty("resourceUrl") String resourceUrl,
            @JsonProperty("paths") String[] paths,
            @JsonProperty("owner") String owner,
            @JsonProperty("ownerUrl") String ownerUrl,
            @JsonProperty("layers") String[] layers,
            @JsonProperty("inputClasses") Class[] inputClasses,
            @JsonProperty("outputClasses") Class[] outputClasses,
            @JsonProperty("inputTypes") Metadata.Type[] inputTypes,
            @JsonProperty("outputTypes") Metadata.Type[] outputTypes,
            @JsonProperty("accessModes") AccessMode[] accessModes
    ) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.resourceUrl = resourceUrl;
        this.paths = paths;
        this.owner = owner;
        this.ownerUrl = ownerUrl;
        this.layers = layers;
        this.inputClasses = inputClasses;
        this.outputClasses = outputClasses;
        this.inputTypes = inputTypes;
        this.outputTypes = outputTypes;
        this.accessModes = accessModes;
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
    public Class[] getInputClasses() {
        return inputClasses;
    }

    /**
     * Often the same as the inputClass, but not always
     *
     * @return
     */
    public Class[] getOutputClasses() {
        return outputClasses;
    }

    /**
     * Single Item or Stream of multiple Items
     *
     * @return
     */
    public Metadata.Type[] getInputTypes() {
        return inputTypes;
    }

    /**
     * Usually the same as the inputType, but not always
     *
     * @return
     */
    public Metadata.Type[] getOutputTypes() {
        return outputTypes;
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

}
