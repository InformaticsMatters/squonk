package org.squonk.execution.steps.impl;

import com.im.lac.types.MoleculeObject;
import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;

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
        LOG.info("Docker image: " + image);
        DockerRunner runner = new DockerRunner(image, "/tmp/");

        // create files
        File inputDir = runner.getInputDir();
        DatasetMetadata meta = input.getMetadata();
        File metaF = new File(inputDir, "input.meta");
        boolean success = metaF.createNewFile();
        if (!success) {
            throw new IOException("Could not create input file for metadata");
        }
        File dataF = new File(inputDir, "input.data");
        success = dataF.createNewFile();
        if (!success) {
            throw new IOException("Could not create input file for data");
        }
        // copy metadata
        JsonHandler.getInstance().objectToFile(meta, metaF);

        // copy data
        try (InputStream is = input.getInputStream(false)) {
            Files.copy(is, dataF.toPath());
        }

        // run the command
        runner.execute(command);
        LOG.info("Results found in " + runner.getOutputDir().getPath());

        // grab output
        File outputDir = runner.getOutputDir();
        File resultsF = new File(inputDir, "output.data");
        if (!resultsF.exists()) {
            throw new IOException("Results file not found. Expected file named output.data in directory output");
        }
        File resultsMF = new File(inputDir, "output.meta");
        DatasetMetadata<MoleculeObject> resultsMeta = null;
        if (resultsMF.exists()) {
            try (InputStream mis = new FileInputStream(resultsMF)) {
                resultsMeta = JsonHandler.getInstance().objectFromJson(mis, DatasetMetadata.class);
            }
        } else {
            resultsMeta = new DatasetMetadata(MoleculeObject.class);
        }
        try (FileInputStream fis = new FileInputStream(resultsF)) {
            Dataset<MoleculeObject> dataset = new Dataset(MoleculeObject.class, fis, resultsMeta);
            createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, dataset, varman);
            LOG.info("Results: " + dataset.getMetadata());
        }

        // cleanup
        runner.clean();
        LOG.info("Results cleaned up");
    }
}
