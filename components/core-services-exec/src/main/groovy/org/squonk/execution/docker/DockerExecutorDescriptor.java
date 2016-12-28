package org.squonk.execution.docker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.io.IODescriptor;
import org.squonk.core.ServiceDescriptor;
import org.squonk.options.OptionDescriptor;

import java.util.Map;

/** Describes a Docker execution.
 * Created by timbo on 05/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DockerExecutorDescriptor {

    /** The Service descriptor for the service. This is used on the client to build the UI etc.
     */
    private final String id;
    private final String name;
    private final String description;

    private final IODescriptor[] inputDescriptors;
    private final IODescriptor[] outputDescriptors;
    private final String[] tags;
    private final String resourceUrl;
    private final String icon;

    private final String executor;

    /** The command to execute when running the container
     */
    private final String command;

    private final OptionDescriptor[] optionDescriptors;

    private final String imageName;

    /** Any resources that need to be copied to the container before execution. Examples would be script files and license files.
     * The key is used as the file name, and the resource provides the data to copy. The resource location can be an absolute or
     * relative URL.
     */
    private final Map<String, String> resources;

    /**
     *
     * @param id The ID that will be used for this and the service descriptor that is generated
     * @param name The of the service
     * @param description A description for the service
     * @param tags Keywords that allow the service to be browsed
     * @param resourceUrl URL of further information (e.g. web page) on the service
     * @param icon A icon that can be used to depict the service
     * @param inputDescriptors Descriptors for the inputs this service consumes. Often a single source that e.g. can be written to a file that the container reads
     * @param outputDescriptors Descriptors for the outputs this service produces. Often a single source that e.g. can be read from a file that the container produces
     * @param optionDescriptors Option descriptors that define the UI for the user.
     * @param imageName Docker image to use if not overriden by one of the user defined options (e.g. if there is a choice of images to use).
     * @param command The command to run when executing the container.
     * @param resources A set of resources that will be copied to the container as files before execution.
     */
    public DockerExecutorDescriptor(
            // these are for the service descriptor
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("tags") String[] tags,
            @JsonProperty("resourceUrl") String resourceUrl,
            @JsonProperty("icon") String icon,
            @JsonProperty("inputDescriptors") IODescriptor[] inputDescriptors,
            @JsonProperty("outputDescriptors") IODescriptor[] outputDescriptors,
            @JsonProperty("optionDescriptors") OptionDescriptor[] optionDescriptors,
            // these are the other properties
            @JsonProperty("executor") String executor,
            @JsonProperty("imageName") String imageName,
            @JsonProperty("command") String command,
            @JsonProperty("resources") Map<String, String> resources) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.resourceUrl = resourceUrl;
        this.icon = icon;
        this.inputDescriptors = inputDescriptors;
        this.outputDescriptors = outputDescriptors;
        this.optionDescriptors = optionDescriptors;
        this.executor = executor;
        this.imageName = imageName;
        this.command = command;
        this.resources = resources;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

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

    public String getExecutor() {
        return executor;
    }

    public String getImageName() {
        return imageName;
    }

    public String getCommand() {
        return command;
    }

    public Map<String, String> getResources() {
        return resources;
    }

    @JsonIgnore
    public ServiceDescriptor getServiceDescriptor() {
        return createServiceDescriptor();
    }


    protected ServiceDescriptor createServiceDescriptor() {
        return new ServiceDescriptor(id, name, description, tags, resourceUrl, icon,
                inputDescriptors, outputDescriptors,
                optionDescriptors, executor, imageName);
    //"org.squonk.execution.steps.impl.CannedDockerProcessDatasetStep"
    }

}
