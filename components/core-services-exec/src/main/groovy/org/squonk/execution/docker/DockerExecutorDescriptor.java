package org.squonk.execution.docker;

import org.squonk.core.ServiceDescriptor;
import org.squonk.io.Resource;
import org.squonk.options.OptionDescriptor;

import java.util.Map;

/**
 * Created by timbo on 05/12/16.
 */
public class DockerExecutorDescriptor {

    private final ServiceDescriptor serviceDescriptor;
    private final String command;
    private final String inputName;
    private final String outputName;
    private final String inputMediaType;
    private final String outputMediaType;
    private final Map<String, Resource> resources;

    public DockerExecutorDescriptor(
            // these are for the service descriptor
            String id, String name, String description, String[] tags, String resourceUrl,
            Class inputClass, Class outputClass, ServiceDescriptor.DataType inputType, ServiceDescriptor.DataType outputType,
            String icon, OptionDescriptor[] optionDescriptors,
            // these are the other properties
            String command,
            String inputName, String outputName,
            String inputMediaType, String outputMediaType,
            Map<String, Resource> resources) {

        this.serviceDescriptor = new ServiceDescriptor(id, name, description, tags, resourceUrl,
                inputClass, outputClass, inputType, outputType,
                icon, null, false, optionDescriptors, "org.squonk.execution.steps.impl.CannedDockerProcessDatasetStep");

        this.command = command;
        this.inputName = inputName;
        this.outputName = outputName;
        this.inputMediaType = inputMediaType;
        this.outputMediaType = outputMediaType;
        this.resources = resources;
    }

    public String getId() {
        return serviceDescriptor.getId();
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    public String getCommand() {
        return command;
    }

    public String getInputName() {
        return inputName;
    }

    public String getOutputName() {
        return outputName;
    }

    public String getInputMediaType() {
        return inputMediaType;
    }

    public String getOutputMediaType() {
        return outputMediaType;
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

}
