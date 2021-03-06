/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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
import org.squonk.core.NextflowServiceDescriptor;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.runners.DockerRunner;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.SquonkDataSource;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/** Step that executes a <a href="http://nextflow.io">Nextflow</a> workflow, executing it inside a Docker container.
 *
 * IMPORTANT: Details of this step are subject to change.
 *
 * Created by timbo on 28/07/17.
 */
public class DatasetNextflowInDockerExecutorStep extends AbstractContainerStep {

    private static final Logger LOG = Logger.getLogger(DatasetNextflowInDockerExecutorStep.class.getName());
    private static final String NEXTFLOW_IMAGE = IOUtils.getConfiguration("SQUONK_NEXTFLOW_IMAGE", "informaticsmatters/nextflow:19.10.0");

    public Map<String, List<SquonkDataSource>> executeForDataSources(Map<String, Object> inputs, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_CONTAINER;
        NextflowServiceDescriptor descriptor = getNextflowServiceDescriptor();
        ContainerRunner containerRunner = prepareContainerRunner();

        Map<String, List<SquonkDataSource>> outputs = doExecuteForDataSources(inputs, containerRunner, descriptor);
        return outputs;
    }

    protected ContainerRunner prepareContainerRunner() throws IOException {

        NextflowServiceDescriptor descriptor = getNextflowServiceDescriptor();

        statusMessage = MSG_PREPARING_CONTAINER;

        String command = descriptor.getNextflowParams();
        String expandedCommand;
        if (command != null) {
            expandedCommand = expandCommand(command, options);
        } else {
            expandedCommand = "";
        }
        ContainerRunner runner = createContainerRunner(NEXTFLOW_IMAGE);
        runner.init();
        LOG.info("Nextflow executor image: " + NEXTFLOW_IMAGE +
                 " hostWorkDir: " + runner.getHostWorkDir());

        // write the nextflow file that executes everything
        LOG.info("Writing main.nf file with content...");
        String nextflowFileContents = descriptor.getNextflowFile();
        runner.writeInput("main.nf", nextflowFileContents, false);

        // Construct nextflow configuration
        String nextflowConfigContents = "";
        if (!expandedCommand.isEmpty()) {
            // Add the configured parameters.
            // the expanded command should be a series of 'params'
            // settings, one per line.
            nextflowConfigContents += expandedCommand + "\n";
        }
        // Add the nextflow config file (if one is defined)
        nextflowConfigContents += descriptor.getNextflowConfig();
        // An opportunity for the runner to provide extra configuration.
        // There may be nothing to add but the returned string
        // will be valid.
        nextflowConfigContents = runner.addExtraNextflowConfig(nextflowConfigContents);
        LOG.info("Writing nextflow.config file...\n" + nextflowConfigContents);
        runner.writeInput("nextflow.config", nextflowConfigContents, false);

        // The runner's either a plain Docker runner
        // or it's an OpenShift runner.
        if (runner instanceof DockerRunner){
            ((DockerRunner)runner).includeDockerSocket();
        }

        return runner;
    }

}