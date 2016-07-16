package org.squonk.execution.steps.impl;

import org.squonk.execution.docker.DockerRunner;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by timbo on 29/12/15.
 */
public class UntrustedPythonDatasetScriptStep extends AbstractDockerScriptRunnerStep {

    private static final Logger LOG = Logger.getLogger(UntrustedPythonDatasetScriptStep.class.getName());


    protected int executeRunner() {
        return runner.execute("run.py");
    }

    protected DockerRunner createRunner() throws IOException {
        statusMessage = MSG_PREPARING_CONTAINER;

        String image = getOption(OPTION_DOCKER_IMAGE, String.class);
        if (image == null) {
            image = "python:2.7";
        }

        String script = getOption(OPTION_SCRIPT, String.class);
        if (script == null) {
            statusMessage = "Error: Script to execute is not defined";
            throw new IllegalStateException("Script to execute is not defined. Should be present as option named " + OPTION_SCRIPT);
        }
        LOG.fine("Docker image: " + image + ", Script: " + script);

        String hostWorkDir = "/tmp/work";
        String localWorkDir = "/source";
        DockerRunner runner = createDockerRunner(image, hostWorkDir, localWorkDir)
                .withNetwork(ISOLATED_NETWORK_NAME);

        LOG.info("Writing script file");
        runner.writeInput("run.py", script);

        return runner;
    }

}
