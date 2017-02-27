package org.squonk.execution.steps.impl;

import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.BasicObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by timbo on 08/06/16.
 */
public abstract class AbstractDockerStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(AbstractDockerStep.class.getName());
    protected int numRecordsProcessed = -1;
    protected int numRecordsOutput = -1;


    protected DockerRunner createDockerRunner(String image, String hostWorkDir, String localWorkDir) throws IOException {
        DockerRunner runner = new DockerRunner(image, hostWorkDir, localWorkDir);
        runner.init();
        LOG.info("Using host work dir of " + runner.getHostWorkDir().getPath());
        LOG.info("Using local work dir of " + runner.getLocalWorkDir());
        return runner;
    }

    protected DockerRunner createDockerRunner(String image, String localWorkDir) throws IOException {
        return createDockerRunner(image, null, localWorkDir);
    }

    /** Fetch the input using the default name for the input variable
     *
     * @param varman
     * @param runner
     * @param mediaType
     * @return
     * @throws Exception
     */
    protected DatasetMetadata handleInput(VariableManager varman, DockerRunner runner, String mediaType) throws Exception {
        return handleInput(varman, runner, mediaType, StepDefinitionConstants.VARIABLE_INPUT_DATASET);
    }

    /** Fetch the input in the case that the input has been renamed from the default name
     *
     * @param varman
     * @param runner
     * @param mediaType
     * @param varName
     * @return
     * @throws Exception
     */
    protected DatasetMetadata handleInput(VariableManager varman, DockerRunner runner, String mediaType, String varName) throws Exception {

        if (mediaType == null) {
            mediaType = CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON;
        }

        switch (mediaType) {
            case CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON:
            case CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON:
                Dataset dataset = fetchMappedInput(varName, Dataset.class, varman, true);
                writeAsDataset(dataset, runner);
                return dataset.getMetadata();
            case CommonMimeTypes.MIME_TYPE_MDL_SDF:
                InputStream sdf = fetchMappedInput(varName, InputStream.class, varman, true);
                writeAsSDF(sdf, runner);
                return null; // TODO can we getServiceDescriptors the metadata somehow?
            default:
                throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }
    }

    protected DatasetMetadata handleOutput(DatasetMetadata inputMetadata, VariableManager varman, DockerRunner runner, String mediaType) throws Exception {

        if (mediaType == null) {
            mediaType = CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON;
        }

        switch (mediaType) {
            case CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON:
            case CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON:
                return readAsDataset(inputMetadata, varman, runner);
            case CommonMimeTypes.MIME_TYPE_MDL_SDF:
                return readAsSDF(inputMetadata, varman, runner);
            default:
                throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }
    }

    protected void writeAsDataset(Dataset input, DockerRunner runner) throws IOException {
        LOG.fine("Writing metadata");
        runner.writeInput("input.meta", JsonHandler.getInstance().objectToJson(input.getMetadata()));
        LOG.fine("Writing data");
        runner.writeInput("input.data.gz", input.getInputStream(true));
    }

    protected void writeAsSDF(InputStream sdf, DockerRunner runner) throws IOException {
        LOG.fine("Writing SDF");
        //runner.writeInput("input.sdf.gz", IOUtils.getGzippedInputStream(sdf));

        String data = IOUtils.convertStreamToString(sdf);
        LOG.info("DATA: " + data);

        runner.writeInput("input.sdf.gz", IOUtils.getGzippedInputStream(new ByteArrayInputStream(data.getBytes())));
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
            Dataset<? extends BasicObject> dataset = new Dataset(meta.getType(), IOUtils.getGunzippedInputStream(is), meta);
            createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, Dataset.class, dataset, varman);
            LOG.fine("Results: " + dataset.getMetadata());
            return dataset.getMetadata();
        }
    }


    protected DatasetMetadata readAsSDF(DatasetMetadata inputMetadata, VariableManager varman, DockerRunner runner) throws Exception {

        try (InputStream is = runner.readOutput("output.sdf.gz")) {
            createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, InputStream.class, is, varman);
        }
        // TODO can we getServiceDescriptors the metadata somehow?
        return null;
    }

    protected void generateMetrics(DockerRunner runner, String filename, float executionTimeSeconds) throws IOException {
        try (InputStream is = runner.readOutput(filename)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                for (String key : props.stringPropertyNames()) {
                    int c = new Integer(props.getProperty(key));
                    if ("__InputCount__".equals(key)) {
                        numRecordsProcessed = c;
                    } else  if ("__OutputCount__".equals(key)) {
                        numRecordsOutput = c;
                    } else {
                        usageStats.put(key, c);
                    }
                }
            }
        }
        generateExecutionTimeMetrics(executionTimeSeconds);
    }

}
