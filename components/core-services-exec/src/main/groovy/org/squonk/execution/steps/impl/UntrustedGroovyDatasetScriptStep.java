package org.squonk.execution.steps.impl;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Volume;
import org.apache.camel.CamelContext;
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

        String image = getOption(OPTION_DOCKER_IMAGE, String.class);
        String script = getOption(OPTION_SCRIPT, String.class);
        if (image == null) {
            image = "squonk/groovy";
        }
        if (script == null) {
            throw new IllegalStateException("Script to execute is not defined. Should be present as option named " + OPTION_SCRIPT);
        }
        LOG.info("Docker image: " + image + ", script: " + script);
        String hostWorkDir = "/tmp/work";
        String localWorkDir = "/source";
        DockerRunner runner = createDockerRunner(image, hostWorkDir, localWorkDir);
        Volume maven = runner.addVolume("/var/maven_repo");
        runner.addBind("/var/maven_repo", maven, AccessMode.ro);
        try {
            // create input files
            DatasetMetadata inputMetadata = handleInput(varman, runner);

            LOG.info("Writing script file");
            runner.writeInput("run.groovy", script);

            // run the command
            LOG.info("Executing ...");
            int retval = runner.execute("run.groovy");
            LOG.info("Script executed with return status of " + retval);

            // handle the output
            handleOutput(inputMetadata, varman, runner);

        } finally {
            // cleanup
            runner.cleanWorkDir();
            LOG.info("Results cleaned up");
        }
    }
}
