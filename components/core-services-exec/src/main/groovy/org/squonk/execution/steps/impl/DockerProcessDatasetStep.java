package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.DatasetMetadata;;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import java.util.logging.Logger;

/**
 * Created by timbo on 29/12/15.
 */
public class DockerProcessDatasetStep extends AbstractDockerStep {

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
        String hostWorkDir = "/tmp/work";
        String localWorkDir = "/source";

        DockerRunner runner = createDockerRunner(image, hostWorkDir, localWorkDir);
        LOG.info("Docker image: " + image + ", hostWorkDir: " + runner.getHostWorkDir() + ", script: " + command);
        try {
            // create input files
            statusMessage = MSG_PREPARING_INPUT;
            DatasetMetadata inputMetadata = handleInput(varman, runner, inputMediaType);

            LOG.info("Writing command file");
            // replace windows line end characters
            command = command.replaceAll("\\r\\n", "\n");
            runner.writeInput("execute", command, true);

            // run the command
            statusMessage = MSG_RUNNING_CONTAINER;
            LOG.info("Executing ...");
            int status = runner.execute(localWorkDir + "/execute");
            LOG.info("Executed with return status of " + status);
            if (status != 0) {
                String log = runner.getLog();
                statusMessage = "Error: " + log;
                LOG.warning("Execution errors: " + log);
                throw new RuntimeException("Container execution failed:\n" + log);
            }

            // handle the output
            statusMessage = MSG_PREPARING_OUTPUT;
            DatasetMetadata meta = handleOutput(inputMetadata, varman, runner, outputMediaType);
            if (meta == null) {
                statusMessage = MSG_PROCESSING_COMPLETE;
            } else {
                statusMessage = String.format(MSG_RECORDS_PROCESSED, meta.getSize());
            }

        } finally {
            // cleanup
            runner.cleanWorkDir();
            LOG.info("Results cleaned up");
        }
    }

}
