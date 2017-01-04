package org.squonk.io;

import org.squonk.core.Descriptor;
import org.squonk.options.OptionDescriptor;

/**
 * Created by timbo on 30/12/16.
 */
public abstract class ExecutableDescriptor implements Descriptor {

    private final String id;
    private final String name;
    private final String description;
    private final String[] tags;
    private final String resourceUrl;
    private final String icon;
    private final IODescriptor[] inputDescriptors;
    private final IODescriptor[] outputDescriptors;
    private final OptionDescriptor[] optionDescriptors;

    protected ExecutableDescriptor(
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            IODescriptor[] inputDescriptors,
            IODescriptor[] outputDescriptors,
            OptionDescriptor[] optionDescriptors
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.resourceUrl = resourceUrl;
        this.icon = icon;
        this.inputDescriptors = inputDescriptors;
        this.outputDescriptors = outputDescriptors;
        this.optionDescriptors = optionDescriptors;
    }

    @Override
    public String getId() {
        return id;
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

    public String getResourceUrl() {
        return resourceUrl;
    }

    public String getIcon() {
        return icon;
    }

    public IODescriptor[] getInputDescriptors() {
        return inputDescriptors;
    }

    public IODescriptor[] getOutputDescriptors() {
        return outputDescriptors;
    }

    public OptionDescriptor[] getOptionDescriptors() {
        return optionDescriptors;
    }
}
