package org.squonk.execution.steps.impl;

import org.squonk.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Created by timbo on 08/06/16.
 */
public abstract class AbstractDockerStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(AbstractDockerStep.class.getName());


    protected DockerRunner createDockerRunner(String image, String hostWorkDir, String localWorkDir) throws IOException {
        DockerRunner runner = new DockerRunner(image, hostWorkDir, localWorkDir);
        runner.init();
        LOG.info("Using host work dir of " + runner.getHostWorkDir().getPath());
        LOG.info("Using local work dir of " + runner.getLocalWorkDir());
        return runner;
    }

    protected DatasetMetadata handleInput(VariableManager varman, DockerRunner runner, String mediaType) throws Exception {

        if (mediaType == null) {
            mediaType = CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON;
        }

        switch (mediaType) {
            case CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON:
                Dataset dataset = fetchMappedInput(StepDefinitionConstants.VARIABLE_INPUT_DATASET, Dataset.class, varman, true);
                writeAsMoleculeObjectDataset(dataset, runner);
                return dataset.getMetadata();
            case CommonMimeTypes.MIME_TYPE_MDL_SDF:
                InputStream sdf = fetchMappedInput(StepDefinitionConstants.VARIABLE_INPUT_DATASET, InputStream.class, varman, true);
                writeAsSDF(sdf, runner);
                return null; // TODO can we get the metadata somehow?
            default:
                throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }
    }

    protected DatasetMetadata handleOutput(DatasetMetadata inputMetadata, VariableManager varman, DockerRunner runner, String mediaType) throws Exception {

        if (mediaType == null) {
            mediaType = CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON;
        }

        switch (mediaType) {
            case CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON:
                return readAsDataset(inputMetadata, varman, runner);
            case CommonMimeTypes.MIME_TYPE_MDL_SDF:
                return readAsSDF(inputMetadata, varman, runner);
            default:
                throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }
    }

    protected void writeAsMoleculeObjectDataset(Dataset input, DockerRunner runner) throws IOException {
        LOG.info("Writing metadata");
        runner.writeInput("input.meta", JsonHandler.getInstance().objectToJson(input.getMetadata()));
        LOG.info("Writing data");
        runner.writeInput("input.data.gz", input.getInputStream(true));
    }

    protected void writeAsSDF(InputStream sdf, DockerRunner runner) throws IOException {
        LOG.info("Writing SDF");
        runner.writeInput("input.sdf.gz", IOUtils.getGzippedInputStream(sdf));
    }

    protected DatasetMetadata readAsDataset(DatasetMetadata inputMetadata, VariableManager varman, DockerRunner runner) throws Exception {
        DatasetMetadata meta;
        try (InputStream is = runner.readOutput("output.meta")) {
            if (is == null) {
                meta = inputMetadata;
            } else {
                meta = JsonHandler.getInstance().objectFromJson(is, DatasetMetadata.class);
            }
        }

        try (InputStream is = runner.readOutput("output.data.gz")) {
            Dataset<MoleculeObject> dataset = new Dataset(MoleculeObject.class, IOUtils.getGunzippedInputStream(is), meta);
            createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, Dataset.class, dataset, varman);
            LOG.info("Results: " + dataset.getMetadata());
            return dataset.getMetadata();
        }
    }


    protected DatasetMetadata readAsSDF(DatasetMetadata inputMetadata, VariableManager varman, DockerRunner runner) throws Exception {

        try (InputStream is = runner.readOutput("output.sdf.gz")) {
            createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, InputStream.class, is, varman);
        }
        // TODO can we get the metadata somehow?
        return null;
    }

}
