package org.squonk.execution.steps.impl;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Volume;
import org.apache.camel.CamelContext;
import org.squonk.api.MimeTypeResolver;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import java.util.logging.Logger;

/**
 * Created by timbo on 29/12/15.
 */
public class UntrustedGroovyDatasetScriptStep extends AbstractDockerStep {

    private static final Logger LOG = Logger.getLogger(UntrustedGroovyDatasetScriptStep.class.getName());

    public static final String OPTION_DOCKER_IMAGE = StepDefinitionConstants.OPTION_DOCKER_IMAGE;
    public static final String OPTION_SCRIPT = StepDefinitionConstants.TrustedGroovyDataset.OPTION_SCRIPT;


    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_CONTAINER;

        String image = getOption(OPTION_DOCKER_IMAGE, String.class);
        String script = getOption(OPTION_SCRIPT, String.class);
        if (image == null) {
            image = "squonk/groovy";
        }
        if (script == null) {
            statusMessage = "Error: Script to execute is not defined";
            throw new IllegalStateException("Script to execute is not defined. Should be present as option named " + OPTION_SCRIPT);
        }
        LOG.info("Docker image: " + image + ", script: " + script);
        String hostWorkDir = "/tmp/work";
        String localWorkDir = "/source";
        DockerRunner runner = createDockerRunner(image, hostWorkDir, localWorkDir);
        Volume maven = runner.addVolume("/var/maven_repo");
        runner.addBind("/var/maven_repo", maven, AccessMode.ro);
        statusMessage = MSG_PREPARING_INPUT;

        try {
            // create input files
            DatasetMetadata inputMetadata = handleInput(varman, runner, MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON);

            LOG.info("Writing script file");
            runner.writeInput("run.groovy", script);

            // run the command
            LOG.info("Executing ...");
            statusMessage = MSG_RUNNING_CONTAINER;

            int status = runner.execute("run.groovy");
            LOG.info("Script executed with return status of " + status);
            if (status != 0) {
                String log = runner.getLog();
                statusMessage = "Error: " + log;
                LOG.warning("Execution errors: " + log);
                throw new RuntimeException("Container execution failed:\n" + log);
            } else {
                // handle the output
                statusMessage = MSG_PREPARING_OUTPUT;
                DatasetMetadata meta = handleOutput(inputMetadata, varman, runner, MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON);
                statusMessage = String.format(MSG_RECORDS_PROCESSED, meta.getSize());
            }

        } finally {
            // cleanup
            runner.cleanWorkDir();
            LOG.info("Results cleaned up");
        }
    }
}
