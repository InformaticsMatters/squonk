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

    public static final String OPTION_DOCKER_IMAGE = StepDefinitionConstants.OPTION_DOCKER_IMAGE;
    public static final String OPTION_DOCKER_COMMAND = StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        String image = getOption(OPTION_DOCKER_IMAGE, String.class);
        String command = getOption(OPTION_DOCKER_COMMAND, String.class);
        if (image == null) {
            throw new IllegalStateException("Docker image not defined. Should be present as option named " + OPTION_DOCKER_IMAGE);
        }
        if (command == null) {
            throw new IllegalStateException("Command to run is not defined. Should be present as option named " + OPTION_DOCKER_COMMAND);
        }
        String hostWorkDir = "/tmp/work";
        String localWorkDir = "/source";
        LOG.info("Docker image: " + image + ", script: " + command);
        DockerRunner runner = createDockerRunner(image, hostWorkDir, localWorkDir);
        try {
            // create input files
            DatasetMetadata inputMetadata = handleInput(varman, runner);

            LOG.info("Writing command file");
            runner.writeInput("run.sh", command);

            // run the command
            LOG.info("Executing ...");
            int retval = runner.execute("/bin/sh", localWorkDir + "/run.sh");
            LOG.info("Command executed with return status of " + retval);

            // handle the output
            handleOutput(inputMetadata, varman, runner);

        } finally {
            // cleanup
            runner.cleanWorkDir();
            LOG.info("Results cleaned up");
        }
    }



}
