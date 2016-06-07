package org.squonk.execution.steps.impl;

import com.im.lac.types.MoleculeObject;
import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * Created by timbo on 29/12/15.
 */
public class DockerProcessDatasetStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(DockerProcessDatasetStep.class.getName());

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    public static final String OPTION_DOCKER_IMAGE = StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_IMAGE;
    public static final String OPTION_DOCKER_COMMAND = StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        Dataset input = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, varman, true);
        LOG.info("Input Dataset: " + input);
        String image = getOption(OPTION_DOCKER_IMAGE, String.class);
        String command = getOption(OPTION_DOCKER_COMMAND, String.class);
        if (image == null) {
            throw new IllegalStateException("Docker image not defined. Should be present as option named " + OPTION_DOCKER_IMAGE);
        }
        if (command == null) {
            throw new IllegalStateException("Command to run is not defined. Should be present as option named " + OPTION_DOCKER_COMMAND);
        }
        LOG.info("Docker image: " + image + ", script: " + command);
        DockerRunner runner = new DockerRunner(image, "/tmp/work");
        runner.init();
        LOG.info("Using work dir of " + runner.getWorkDir().getPath());
        try {
            JsonHandler jh = JsonHandler.getInstance();

            // create input files
            LOG.info("Writing metadata");
            runner.writeInput("input.meta", jh.objectToJson(input.getMetadata()));
            LOG.info("Writing data");
            runner.writeInput("input.data.gz", input.getInputStream(true));
            LOG.info("Writing command file");
            runner.writeInput("run.sh", command);

            // run the command
            LOG.info("Executing ...");
            int retval = runner.execute("/bin/sh", "/tmp/work/run.sh");
            LOG.info("Command executed with return status of " + retval);

            // handle the output
            DatasetMetadata meta;
            try (InputStream is = runner.readOutput("output.meta")) {
                if (is == null) {
                    meta = input.getMetadata();
                } else {
                    meta = jh.objectFromJson(is, DatasetMetadata.class);
                }
            }

            try (InputStream is = runner.readOutput("output.data.gz")) {
                Dataset<MoleculeObject> dataset = new Dataset(MoleculeObject.class, IOUtils.getGunzippedInputStream(is), meta);
                createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, dataset, varman);
                LOG.info("Results: " + dataset.getMetadata());
            }

        } finally {
            // cleanup
            runner.clean();
            LOG.info("Results cleaned up");
        }
    }
}
