package org.squonk.execution.steps.impl;

import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;
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

    protected DatasetMetadata handleInput(VariableManager varman, DockerRunner runner) throws Exception {
        Dataset input = fetchMappedInput(StepDefinitionConstants.VARIABLE_INPUT_DATASET, Dataset.class, varman, true);
        LOG.info("Writing metadata");
        runner.writeInput("input.meta", JsonHandler.getInstance().objectToJson(input.getMetadata()));
        LOG.info("Writing data");
        runner.writeInput("input.data.gz", input.getInputStream(true));
        return input.getMetadata();
    }

    protected DatasetMetadata handleOutput(DatasetMetadata inputMetadata, VariableManager varman, DockerRunner runner) throws Exception {
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
}
