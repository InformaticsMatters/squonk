package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.core.AbstractServiceDescriptor;
import org.squonk.io.IODescriptor;
import org.squonk.io.IORoute;
import org.squonk.options.OptionDescriptor;

import java.util.Date;
import java.util.Map;

/**
 * Describes a Docker execution.
 * Created by timbo on 05/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DockerServiceDescriptor extends AbstractServiceDescriptor {

    private final IORoute[] inputRoutes;
    private final IORoute[] outputRoutes;

    /**
     * The command to execute when running the container
     */
    private final String command;

    /**
     * The name of the docker image to use
     */
    private final String imageName;

    /**
     * Any volumes that need to be mounted in the container before execution. Examples would be volumes contained script files and license files.
     */
    private final Map<String, String> volumes;

    /**
     * @param id                The ID that will be used for this and the service descriptor that is generated
     * @param name              The of the service
     * @param description       A description for the service
     * @param tags              Keywords that allow the service to be browsed
     * @param resourceUrl       URL of further information (e.g. web page) on the service
     * @param icon              A icon that can be used to depict the service
     * @param inputDescriptors  Descriptors for the inputs this service consumes. Often a single source that e.g. can be written to a file that the container reads
     * @param inputRoutes       The route for providing the inputs (file, stdin etc). Must match the number of inputDescriptors.
     * @param outputDescriptors Descriptors for the outputs this service produces. Often a single source that e.g. can be read from a file that the container produces
     * @param outputRoutes      The route for reading the outputs (file, stdout etc). Must match the number of outputDescriptors.
     * @param optionDescriptors Option descriptors that define the UI for the user.
     * @param executorClassName The class name of the executor
     * @param imageName         Docker image to use if not overriden by one of the user defined options (e.g. if there is a choice of images to use).
     * @param command           The command to run when executing the container.
     * @param volumes           Volumes that need to be mounted. Primarily the volume that contains the scripts to execute. The key is the directory to
     *                          mount (relative to the configured directory that contains mountable volumes), the value is where to mount it in
     *                          the container.
     */
    public DockerServiceDescriptor(
            // these relate to the ServiceDescriptor
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("tags") String[] tags,
            @JsonProperty("resourceUrl") String resourceUrl,
            @JsonProperty("icon") String icon,
            @JsonProperty("status") ServiceConfig.Status status,
            @JsonProperty("statusLastChecked") Date statusLastChecked,
            @JsonProperty("inputDescriptors") IODescriptor[] inputDescriptors,
            @JsonProperty("inputRoutes") IORoute[] inputRoutes, // docker specific
            @JsonProperty("outputDescriptors") IODescriptor[] outputDescriptors,
            @JsonProperty("outputRoutes") IORoute[] outputRoutes, // docker specific
            @JsonProperty("optionDescriptors") OptionDescriptor[] optionDescriptors,
            // these are specific to docker execution
            @JsonProperty("executorClassName") String executorClassName,
            @JsonProperty("imageName") String imageName,
            @JsonProperty("command") String command,
            @JsonProperty("volumes") Map<String, String> volumes) {

        super(id, name, description, tags, resourceUrl, icon, status, statusLastChecked, inputDescriptors, outputDescriptors, optionDescriptors, executorClassName);
        this.inputRoutes = inputRoutes;
        this.outputRoutes = outputRoutes;
        this.imageName = imageName;
        this.command = command;
        this.volumes = volumes;
    }

    public IORoute[] getInputRoutes() {
        return inputRoutes;
    }

    public IORoute[] getOutputRoutes() {
        return outputRoutes;
    }

    public String getImageName() {
        return imageName;
    }

    public String getCommand() {
        return command;
    }

    public Map<String, String> getVolumes() {
        return volumes;
    }
}
