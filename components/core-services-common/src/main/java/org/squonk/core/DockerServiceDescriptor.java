/*
 * Copyright (c) 2020 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.dataset.ThinDescriptor;
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
public class DockerServiceDescriptor extends DefaultServiceDescriptor {


    /**
     * The name of the docker image to use
     */
    private final String imageName;

    /**The command to execute
     *
     */
    private final String command;

    /**
     * Any volumes that need to be mounted in the container before execution. Examples would be volumes contained script files and license files.
     */
    private final Map<String, String> volumes;

    /**
     *
     * @param serviceConfig
     * @param inputRoutes
     * @param outputRoutes
     * @param imageName
     * @param command
     * @param volumes
     */
    @JsonCreator
    public DockerServiceDescriptor(
            @JsonProperty("serviceConfig") ServiceConfig serviceConfig,
            @JsonProperty("thinDescriptors") ThinDescriptor[] thinDescriptors,
            @JsonProperty("inputRoutes") IORoute[] inputRoutes,
            @JsonProperty("outputRoutes") IORoute[] outputRoutes,
            @JsonProperty("imageName") String imageName,
            @JsonProperty("command") String command,
            @JsonProperty("volumes") Map<String, String> volumes) {
        super(serviceConfig, thinDescriptors, inputRoutes, outputRoutes);
        this.imageName = imageName;
        this.command = command;
        this.volumes = volumes;
    }

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
     * @param thinDescriptors   Descriptors for thin execution
     * @param executorClassName The class name of the executor
     * @param imageName         Docker image to use if not overriden by one of the user defined options (e.g. if there is a choice of images to use).
     * @param imagePullSecret   The name of a secret required to pull the Docker image, empty if not required (used for Kubernetes deployments).
     * @param command           The command to run when executing the container.
     * @param volumes           Volumes that need to be mounted. Primarily the volume that contains the scripts to execute. The key is the directory to
     *                          mount (relative to the configured directory that contains mountable volumes), the value is where to mount it in
     *                          the container.
     */
    public DockerServiceDescriptor(
            // these relate to the ServiceConfig
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            ServiceConfig.Status status,
            Date statusLastChecked,
            IODescriptor[] inputDescriptors,
            IORoute[] inputRoutes,
            IODescriptor[] outputDescriptors,
            IORoute[] outputRoutes,
            OptionDescriptor[] optionDescriptors,
            ThinDescriptor[] thinDescriptors,
            // these are specific to docker execution
            String executorClassName,
            String imageName,
            String imagePullSecret,
            String command,
            Map<String, String> volumes) {

        super(new ServiceConfig(id, name, description, tags, resourceUrl, icon,
                        inputDescriptors, outputDescriptors, optionDescriptors, status, statusLastChecked, executorClassName),
                thinDescriptors, inputRoutes, outputRoutes);

        this.imageName = imageName;
        this.imagePullSecret = imagePullSecret;
        this.command = command;
        this.volumes = volumes;
    }


    /** Used for testing only
     *
     * @param id
     * @param name
     * @param inputDescriptors
     * @param outputDescriptors
     */
    public DockerServiceDescriptor(
            String id,
            String name,
            IODescriptor[] inputDescriptors,
            IODescriptor[] outputDescriptors
            ) {
        this(id, name, null, null, null, null, null, null, inputDescriptors, null, outputDescriptors, null, null, null, null, null, null, null);
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
