/*
 * Copyright (c) 2017 Informatics Matters Ltd.
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

package org.squonk.execution.steps.impl;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Volume;
import org.apache.camel.CamelContext;
import org.squonk.core.DockerServiceDescriptor;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.SquonkDataSource;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/** Default executor step for Docker execution.
 *
 * Created by timbo on 29/12/15.
 */
public class DefaultDockerExecutorStep extends AbstractDockerStep {

    private static final Logger LOG = Logger.getLogger(DefaultDockerExecutorStep.class.getName());

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        if (serviceDescriptor == null) {
            throw new IllegalStateException("No service descriptor present ");
        } else if (!(serviceDescriptor instanceof DockerServiceDescriptor)) {
            throw new IllegalStateException("Expected service descriptor to be a " +
                    DockerServiceDescriptor.class.getName() +
                    "  but it was a " + serviceDescriptor.getClass().getName());
        }
        DockerServiceDescriptor descriptor = (DockerServiceDescriptor) serviceDescriptor;

        LOG.info("Input types are " + IOUtils.joinArray(descriptor.getServiceConfig().getInputDescriptors(), ","));
        LOG.info("Output types are " + IOUtils.joinArray(descriptor.getServiceConfig().getOutputDescriptors(), ","));

        // this executes this cell
        doExecute(varman, context, descriptor);
        //LOG.info(varman.getTmpVariableInfo());

    }


    protected void doExecute(VariableManager varman, CamelContext camelContext, DockerServiceDescriptor descriptor) throws Exception {

        statusMessage = MSG_PREPARING_CONTAINER;

        ContainerRunner containerRunner = prepareContainerRunner(descriptor);

        try {
            // create input files
            statusMessage = MSG_PREPARING_INPUT;

            // fetch the input data
            Map<String,Object> inputs = fetchInputs(camelContext, descriptor, varman, containerRunner);
            // write the input data
            handleInputs(inputs, descriptor, containerRunner);
            //handleInputs(camelContext, descriptor, varman, containerRunner);
            // run the command
            float duration = handleExecute(containerRunner);

            // handle the output
            statusMessage = MSG_PREPARING_OUTPUT;
            handleOutputs(camelContext, descriptor, varman, containerRunner);

            handleMetrics(containerRunner, duration);

        } finally {
            // cleanup
            if (containerRunner != null && DEBUG_MODE < 2) {
                containerRunner.cleanup();
                LOG.info("Results cleaned up");
            }
        }
    }

    @Override
    public Map<String, List<SquonkDataSource>> executeForDataSources(Map<String, Object> inputs, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_CONTAINER;
        DockerServiceDescriptor descriptor = getDockerServiceDescriptor();

        ContainerRunner containerRunner = prepareContainerRunner(descriptor);

        Map<String, List<SquonkDataSource>> outputs = doExecuteForDataSources(inputs, context, containerRunner, descriptor);
        return outputs;
    }

    private ContainerRunner prepareContainerRunner(DockerServiceDescriptor descriptor) throws IOException {

        String image = getOption(StepDefinitionConstants.OPTION_DOCKER_IMAGE, String.class);
        if (image == null || image.isEmpty()) {
            image = descriptor.getImageName();
        }
        if (image == null || image.isEmpty()) {
            throw new IllegalStateException(
                    "Docker image not defined. Must be set as value of the imageName property of the ServiceDescriptor or as an option named "
                            + StepDefinitionConstants.OPTION_DOCKER_IMAGE);
        }

        String imageVersion = getOption(StepDefinitionConstants.OPTION_DOCKER_IMAGE_VERSION, String.class);
        if (imageVersion != null && !imageVersion.isEmpty()) {
            image = image + ":" + imageVersion;
        }

        String command = getOption(OPTION_DOCKER_COMMAND, String.class);
        if (command == null || command.isEmpty()) {
            command = descriptor.getCommand();
        }
        if (command == null || command.isEmpty()) {
            throw new IllegalStateException(
                    "Docker run command is not defined. Must be set as value of the command property of the ServiceDescriptor as option named "
                            + OPTION_DOCKER_COMMAND);
        }
        // command will be something like:
        // screen.py 'c1(c2c(oc1)ccc(c2)OCC(=O)O)C(=O)c1ccccc1' 0.3 --d morgan2
        // screen.py '${query}' ${threshold} --d ${descriptor}
        String expandedCommand = expandCommand(command, options);

        ContainerRunner containerRunner = createContainerRunner(image);
        containerRunner.init();
        LOG.info("Docker image: " + image + ", hostWorkDir: " + containerRunner.getHostWorkDir() + ", command: " + expandedCommand);

        // add the resources
        if (descriptor.getVolumes() != null) {
            for (Map.Entry<String, String> e : descriptor.getVolumes().entrySet()) {
                String dirToMount = e.getKey();
                String mountAs = e.getValue();
                Volume v = containerRunner.addVolume(mountAs);
                containerRunner.addBind(DOCKER_SERVICES_DIR + "/" + dirToMount, v, AccessMode.ro);
                LOG.info("Volume " + DOCKER_SERVICES_DIR + "/" + dirToMount + " mounted as " + mountAs);
            }
        }

        // write the command that executes everything
        LOG.info("Writing command file");
        containerRunner.writeInput("execute", "#!/bin/sh\n" + expandedCommand + "\n", true);

        return containerRunner;
    }

}
