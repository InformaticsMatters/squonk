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
import org.squonk.api.MimeTypeResolver;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.AbstractStandardStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by timbo on 16/07/16.
 */
public abstract class AbstractDockerScriptRunnerStep extends AbstractStandardStep {

    private static final Logger LOG = Logger.getLogger(AbstractDockerScriptRunnerStep.class.getName());

    public static final String OPTION_DOCKER_IMAGE = StepDefinitionConstants.OPTION_DOCKER_IMAGE;
    public static final String OPTION_SCRIPT = StepDefinitionConstants.TrustedGroovyDataset.OPTION_SCRIPT;

    protected static String ISOLATED_NETWORK_NAME = IOUtils.getConfiguration("ISOLATED_NETWORK_NAME", "deploy_squonk_isolated");

    protected DockerRunner runner = null;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {


        try {
            runner = createRunner();

            statusMessage = MSG_PREPARING_INPUT;
            // create input files

            DatasetMetadata inputMetadata = handleDockerInput(varman, runner, MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON);

            // run the command
            LOG.info("Executing ...");
            statusMessage = MSG_RUNNING_CONTAINER;
            long t0 = System.currentTimeMillis();
            int status = executeRunner();
            long t1 = System.currentTimeMillis();
            float duration = (t1 - t0) / 1000.0f;
            LOG.info(String.format("Script executed in %s seconds with return status of %s", duration, status));

            if (status != 0) {
                String log = runner.getLog();
                statusMessage = "Error: " + log;
                LOG.warning("Execution errors: " + log);
                throw new Exception("Container execution failed:\n" + log);
            } else {
                // handle the output
                statusMessage = MSG_PREPARING_OUTPUT;
                DatasetMetadata outputMetadata = handleDockerOutput(inputMetadata, varman, runner, MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON);
                statusMessage = generateStatusMessage(inputMetadata.getSize(), outputMetadata.getSize(), -1);
            }

            generateMetrics(runner, "metrics.txt", duration);

        } finally {
            // cleanup
            if (runner != null) {
                runner.cleanup();
                LOG.info("Results cleaned up");
            }
        }
    }


    protected abstract int executeRunner();

    protected abstract  DockerRunner createRunner() throws IOException;
}
