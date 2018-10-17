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

import org.apache.camel.CamelContext;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

;

/** TODO REMOVE THIS CLASS
 * Created by timbo on 29/12/15.
 *
 */
public class DockerProcessDatasetStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(DockerProcessDatasetStep.class.getName());

    private static final String OPTION_DOCKER_IMAGE = StepDefinitionConstants.OPTION_DOCKER_IMAGE;
    private static final String OPTION_DOCKER_COMMAND = StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND;
    private static final String OPTION_MEDIA_TYPE_INPUT = StepDefinitionConstants.OPTION_MEDIA_TYPE_INPUT;
    private static final String OPTION_MEDIA_TYPE_OUTPUT = StepDefinitionConstants.OPTION_MEDIA_TYPE_OUTPUT;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_CONTAINER;

        String image = getOption(OPTION_DOCKER_IMAGE, String.class);
        String command = getOption(OPTION_DOCKER_COMMAND, String.class);
        String inputMediaType = getOption(OPTION_MEDIA_TYPE_INPUT, String.class);
        String outputMediaType = getOption(OPTION_MEDIA_TYPE_OUTPUT, String.class);


        if (image == null) {
            statusMessage = "Error: Docker image not defined";
            throw new IllegalStateException("Docker image not defined. Should be present as option named " + OPTION_DOCKER_IMAGE);
        }
        if (command == null) {
            statusMessage = "Error: Docker command not defined";
            throw new IllegalStateException("Command to run is not defined. Should be present as option named " + OPTION_DOCKER_COMMAND);
        }

        ContainerRunner runner = createContainerRunner(image);
        runner.init();
        LOG.info("Docker image: " + image + ", hostWorkDir: " + runner.getHostWorkDir() + ", script: " + command);
        try {
            // create input files
            statusMessage = MSG_PREPARING_INPUT;
            DatasetMetadata inputMetadata = handleDockerInput(varman, runner, inputMediaType);

            LOG.info("Writing command file");
            // replace windows line end characters
            command = command.replaceAll("\\r\\n", "\n");
            runner.writeInput("execute", command, true);

            // run the command
            statusMessage = MSG_RUNNING_CONTAINER;
            LOG.info("Executing ...");
            long t0 = System.currentTimeMillis();
            int status = runner.execute(runner.getLocalWorkDir() + "/execute");
            long t1 = System.currentTimeMillis();
            float duration = (t1 - t0) / 1000.0f;
            LOG.info(String.format("Executed in %s seconds with return status of %s", duration, status));

            if (status != 0) {
                String log = runner.getLog();
                statusMessage = "Error: " + log;
                LOG.warning("Execution errors: " + log);
                throw new RuntimeException("Container execution failed:\n" + log);
            }

            // handle the output
            statusMessage = MSG_PREPARING_OUTPUT;
            DatasetMetadata meta = handleDockerOutput(inputMetadata, varman, runner, outputMediaType);
            Properties props = runner.getFileAsProperties("metrics.txt");
            generateMetricsAndStatus(props, duration);

        } finally {
            // cleanup
            if (DEBUG_MODE < 2) {
                runner.cleanup();
                LOG.info("Results cleaned up");
            }
        }
    }

    @Override
    public Map<String, Object> executeWithData(Map<String, Object> inputs, CamelContext context) throws Exception {
        // TODO - remove the need for this
        throw new RuntimeException("Not implementable");
    }

}
