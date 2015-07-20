package com.im.lac.services.impl;

import com.im.lac.dataset.Metadata;
import com.im.lac.services.ServiceDescriptor;

/**
 *
 * @author timbo
 */
public class ServiceDescriptorImpl implements ServiceDescriptor {

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
    private final Mode[] modes;

    public ServiceDescriptorImpl(
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String[] paths,
            String owner,
            String ownerUrl,
            String[] layers,
            Class[] inputClasses,
            Class[] outputClasses,
            Metadata.Type[] inputTypes,
            Metadata.Type[] outputTypes,
            Mode[] modes
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
        this.modes = modes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String[] getTags() {
        return tags;
    }

    @Override
    public String[] getPaths() {
        return paths;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getOwnerUrl() {
        return ownerUrl;
    }

    @Override
    public String[] getLayers() {
        return layers;
    }

    @Override
    public Class[] getInputClasses() {
        return inputClasses;
    }

    @Override
    public Class[] getOutputClasses() {
        return outputClasses;
    }

    @Override
    public Metadata.Type[] getInputTypes() {
        return inputTypes;
    }

    @Override
    public Metadata.Type[] getOutputTypes() {
        return outputTypes;
    }

    @Override
    public Mode[] getModes() {
        return modes;
    }

    @Override
    public String getResourceUrl() {
        return resourceUrl;
    }

}
