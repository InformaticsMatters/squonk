package org.squonk.execution.steps.impl;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Volume;
import org.apache.camel.CamelContext;
import org.squonk.api.MimeTypeResolver;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by timbo on 29/12/15.
 */
public class UntrustedGroovyDatasetScriptStep extends AbstractDockerStep {

    private static final Logger LOG = Logger.getLogger(UntrustedGroovyDatasetScriptStep.class.getName());

    public static final String OPTION_DOCKER_IMAGE = StepDefinitionConstants.OPTION_DOCKER_IMAGE;
    public static final String OPTION_SCRIPT = StepDefinitionConstants.TrustedGroovyDataset.OPTION_SCRIPT;

    private static String ISOLATED_NETWORK_NAME = IOUtils.getConfiguration("ISOLATED_NETWORK_NAME", "deploy_squonk_isolated");

    DockerRunner runner = null;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {


        try {
            runner = createRunner();

            statusMessage = MSG_PREPARING_INPUT;
            // create input files
            DatasetMetadata inputMetadata = handleInput(varman, runner, MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON);

            // run the command
            LOG.info("Executing ...");
            statusMessage = MSG_RUNNING_CONTAINER;
            long t0 = System.currentTimeMillis();
            int status = runner.execute("run.groovy");
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
                DatasetMetadata outputMetadata = handleOutput(inputMetadata, varman, runner, MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON);
                statusMessage = String.format(MSG_RECORDS_PROCESSED, outputMetadata.getSize());
            }

            generateMetrics(runner, duration);

        } finally {
            // cleanup
            if (runner != null) {
                runner.cleanup();
                LOG.info("Results cleaned up");
            }
        }
    }

    DockerRunner createRunner() throws IOException {
        statusMessage = MSG_PREPARING_CONTAINER;

        String image = getOption(OPTION_DOCKER_IMAGE, String.class);
        if (image == null) {
            image = "squonk/groovy";
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
        Volume maven = runner.addVolume("/var/maven_repo");
        runner.addBind("/var/maven_repo", maven, AccessMode.ro);

        LOG.info("Writing script file");
        runner.writeInput("run.groovy", script);

        return runner;
    }

}
