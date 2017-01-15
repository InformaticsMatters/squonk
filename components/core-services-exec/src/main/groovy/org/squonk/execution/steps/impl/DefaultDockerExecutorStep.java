package org.squonk.execution.steps.impl;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Volume;
import org.apache.camel.CamelContext;
import org.squonk.core.DockerServiceDescriptor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.util.GroovyUtils;
import org.squonk.execution.variable.VariableManager;
import org.squonk.execution.variable.impl.FilesystemReadContext;
import org.squonk.execution.variable.impl.FilesystemWriteContext;
import org.squonk.io.IODescriptor;
import org.squonk.types.BasicObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Created by timbo on 29/12/15.
 */
public class DefaultDockerExecutorStep extends AbstractDockerStep {

    private static final Logger LOG = Logger.getLogger(DefaultDockerExecutorStep.class.getName());

    private static final String OPTION_DOCKER_COMMAND = StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND;
    protected String DOCKER_SERVICES_DIR = IOUtils.getConfiguration("SQUONK_DOCKER_SERVICES_DIR", "../../data/testfiles/docker-services");

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        if (serviceDescriptor == null) {
            throw new IllegalStateException("No service descriptor present ");
        } else if (!(serviceDescriptor instanceof DockerServiceDescriptor)) {
            throw new IllegalStateException("Expected service descriptor to be a " +
                    DockerServiceDescriptor.class.getName() +
                    "  but it was a " + serviceDescriptor.getClass().getName());
        }
        DockerServiceDescriptor descriptor = (DockerServiceDescriptor) serviceDescriptor;

        IODescriptor[] inputDescriptors = descriptor.getServiceConfig().getInputDescriptors();
        IODescriptor[] outputDescriptors = descriptor.getServiceConfig().getOutputDescriptors();
        LOG.info("Input types are " + IOUtils.joinArray(inputDescriptors, ","));
        LOG.info("Output types are " + IOUtils.joinArray(outputDescriptors, ","));

        // first handle any conversions
        // currently these are hard coded and only SDF is supported
        //
        // input conversions:
        // TODO - provide these conversions through a registry
//        if (outputDescriptors != null) {
//            for (IODescriptor iod : inputDescriptors) {
//                if (CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON.equals(iod.getMediaType())) {
//                    // do nothing
//                } else if (CommonMimeTypes.MIME_TYPE_MDL_SDF.equals(iod.getMediaType())) {
//                    // convert to SDF
//                    Step sdfConvertStep = createSdfGeneratorStep(iod.getName(), "_" + iod.getName() + "_converted_sdf");
//                    LOG.info("Executing SDF converter step");
//                    sdfConvertStep.execute(varman, context);
//                    LOG.info(varman.getTmpVariableInfo());
//                } else {
//                    throw new IllegalStateException("Unsupported format conversion: " + inputDescriptors[0]);
//                }
//            }
//        }

        // output conversions:
//        Step readerStep = null;
//        if (outputDescriptors != null) {
//            for (IODescriptor iod : outputDescriptors) {
//                if (CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON.equals(iod.getMediaType())) {
//                    // do nothing
//                } else if (CommonMimeTypes.MIME_TYPE_MDL_SDF.equals(iod.getMediaType())) {
//                    // convert from SDF
//                    readerStep = createSdfReaderStep(iod.getName(), "_" + iod.getName() + "_converted_sdf");
//                } else {
//                    throw new IllegalStateException("Unsupported format conversion: " + iod);
//                }
//            }
//        }

        // this executes this cell
        doExecute(varman, context, descriptor);
        LOG.info(varman.getTmpVariableInfo());

        // and now if we created a reader to convert the format then execute it
//        if (readerStep != null) {
//            LOG.info("Executing SDF reader step");
//            readerStep.execute(varman, context);
//            LOG.info(varman.getTmpVariableInfo());
//        }
    }

//    private Step createSdfGeneratorStep(String origInputName, String origOutputName, String tmpVarName) {
//
//        DatasetServiceExecutorStep step = new DatasetServiceExecutorStep();
//
//        Map<String, Object> opts = new HashMap<>();
//        opts.put("header.Content-Encoding", "gzip");
//        opts.put("header.Accept-Encoding", "gzip");
//        opts.put("header.Content-Type", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON);
//        opts.put("header.Accept", CommonMimeTypes.MIME_TYPE_MDL_SDF);
//        // TODO - allow user to define which impl to use
//        // TODO - avoid hardcoding the URL - look it up from the service descriptors?
//        opts.put(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, "http://chemservices:8080/chem-services-cdk-basic/rest/v1/converters/convert_to_sdf");
//
//        Map<String, VariableKey> inputs = new HashMap<>();
//        VariableKey origInputKey = inputVariableMappings.getServiceDescriptors(origInputName);
//        inputs.put(origInputName, origInputKey);
//
//        // tell the main cell to read its input to the tmp variable name
//        inputVariableMappings.put(origInputName, new VariableKey(outputProducerId, tmpVarName));
//
//        Map<String, String> outputs = new HashMap<>();
//        outputs.put(origOutputName, tmpVarName);
//
//        step.configure(outputProducerId, jobId, opts, inputs, outputs);
//        return step;
//    }
//
//    private Step createSdfReaderStep(String origInputName, String origOutputName, String tmpVarName) {
//
//        SDFReaderStep step = new SDFReaderStep();
//
//        Map<String, VariableKey> inputs = new HashMap<>();
//        // tell the converter cell to read its data from the tmp variable
//        //VariableKey origInputKey = inputVariableMappings.getServiceDescriptors(origInputName);
//        inputs.put(origInputName, new VariableKey(outputProducerId, tmpVarName));
//
//        Map<String, String> outputs = new HashMap<>();
//        // tell the converter cell to write its output to whatever the original cell was to use
//        String origOutput = outputVariableMappings.getServiceDescriptors(origOutputName);
//        outputs.put(origOutputName, origOutput);
//
//        // tell the main cell to write its output to the tmp variable
//        outputVariableMappings.put(origOutputName, tmpVarName);
//
//        step.configure(outputProducerId, jobId, null, inputs, outputs);
//        return step;
//    }

    protected void doExecute(VariableManager varman, CamelContext context, DockerServiceDescriptor descriptor) throws Exception {

        statusMessage = MSG_PREPARING_CONTAINER;

        String image = descriptor.getImageName();
        if (image == null || image.isEmpty()) {
            statusMessage = "Error: Docker image not defined";
            throw new IllegalStateException("Docker image not defined. Must be set as value of the executionEndpoint property of the ServiceDescriptor");
        }

        String imageVersion = getOption(StepDefinitionConstants.OPTION_DOCKER_IMAGE_VERSION, String.class);
        if (imageVersion != null) {
            image = image + ":" + imageVersion;
        }

        String command = descriptor.getCommand();
        IODescriptor[] inputDescriptors = descriptor.getServiceConfig().getInputDescriptors();
        IODescriptor[] outputDescriptors = descriptor.getServiceConfig().getOutputDescriptors();

        if (command == null || command.isEmpty()) {
            statusMessage = "Error: Docker command not defined";
            throw new IllegalStateException("Run command is not defined. Should be present as option named " + OPTION_DOCKER_COMMAND);
        }
        // command will be something like:
        // screen.py 'c1(c2c(oc1)ccc(c2)OCC(=O)O)C(=O)c1ccccc1' 0.3 --d morgan2
        // screen.py '${query}' ${threshold} --d ${descriptor}

        Map<String, Object> args = new LinkedHashMap<>();
        options.forEach((k, v) -> {
            if (k.startsWith("arg.")) {
                LOG.info("Found argument " + k + " = " + v);
                args.put(k.substring(4), v);
            }
        });

        // replace windows line end characters
        command = command.replaceAll("\\r\\n", "\n");
        String expandedCommand = GroovyUtils.expandTemplate(command, args);
        LOG.fine("Command: " + expandedCommand);

        String hostWorkDir = "/tmp/work";
        String localWorkDir = "/source";

        DockerRunner runner = createDockerRunner(image, hostWorkDir, localWorkDir);
        LOG.info("Docker image: " + image + ", hostWorkDir: " + runner.getHostWorkDir() + ", command: " + expandedCommand);
        try {
            // create input files
            statusMessage = MSG_PREPARING_INPUT;

            // write the command that executes everything
            LOG.info("Writing command file");
            runner.writeInput("execute", "#!/bin/sh\n" + expandedCommand, true);

            // add the resources
            for (Map.Entry<String, String> e : descriptor.getVolumes().entrySet()) {
                String dirToMount = e.getKey();
                String mountAs = e.getValue();
                Volume v = runner.addVolume(mountAs);
                runner.addBind(DOCKER_SERVICES_DIR + "/" + dirToMount, v, AccessMode.ro);
                LOG.info("Volume " + DOCKER_SERVICES_DIR + "/" + dirToMount + " mounted as " + mountAs);
            }

            // write the input data
            if (inputDescriptors != null) {
                for (IODescriptor d : inputDescriptors) {
                    LOG.info("Writing input for " + d.getName() + " " + d.getMediaType());
                    writeInput(varman, runner, d);
                }
            }

            // run the command
            statusMessage = MSG_RUNNING_CONTAINER;
            LOG.info("Executing ...");
            long t0 = System.currentTimeMillis();
            int status = runner.execute(localWorkDir + "/execute");
            long t1 = System.currentTimeMillis();
            float duration = (t1 - t0) / 1000.0f;
            LOG.info(String.format("Executed in %s seconds with return status of %s", duration, status));

            if (status != 0) {
                String log = runner.getLog();
                statusMessage = "Error: " + log;
                LOG.warning("Execution errors: " + log);
                throw new RuntimeException("Container execution failed:\n" + log);
            }

            // handle the output
            statusMessage = MSG_PREPARING_OUTPUT;
            if (outputDescriptors != null) {
                for (IODescriptor d : outputDescriptors) {
                    readOutput(varman, runner, d);
                }
            }

            generateMetrics(runner, "output_metrics.txt", duration);

            if (numRecordsProcessed < 0 && numRecordsOutput < 0) {
                statusMessage = MSG_PROCESSING_COMPLETE;
            } else {
                String s1 = numRecordsProcessed < 0 ? "?" : "" + numRecordsProcessed;
                String s2 = numRecordsOutput < 0 ? "?" : "" + numRecordsOutput;
                statusMessage = String.format(MSG_RECORDS_PROCESSED_AND_OUTPUT, s1, s2);
            }

        } finally {
            // cleanup
            runner.cleanup();
            LOG.info("Results cleaned up");
        }
    }

    protected <P,Q> void writeInput(VariableManager varman, DockerRunner runner, IODescriptor<P,Q> descriptor) throws Exception {
        P value = fetchMappedInput(descriptor.getName(), descriptor.getPrimaryType(), varman, true);
        FilesystemWriteContext context = new FilesystemWriteContext(runner.getHostWorkDir(), descriptor.getName());
        varman.putValue(descriptor.getPrimaryType(), value, context);
    }


    protected void writeInputX(VariableManager varman, DockerRunner runner, IODescriptor descriptor) throws Exception {

        // conversions?

        String mediaType;
        if (descriptor.getMediaType() == null) {
            mediaType = CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON;
        } else {
            mediaType = descriptor.getMediaType();
        }

        if (CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON.equals(mediaType) || CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON.equals(mediaType)) {
            Dataset dataset = fetchMappedInput(descriptor.getName(), Dataset.class, varman, true);
            writeDataset(dataset, runner, descriptor);
        } else if (CommonMimeTypes.MIME_TYPE_MDL_SDF.equals(mediaType)) {
            InputStream sdf = fetchMappedInput(descriptor.getName(), InputStream.class, varman, true);
            writeSDF(sdf, runner, descriptor);
        } else {
            throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }
    }

    protected void writeDataset(Dataset input, DockerRunner runner, IODescriptor descriptor) throws IOException {
        LOG.info("Writing metadata " + descriptor.getName() + ".meta");
        runner.writeInput(descriptor.getName() + ".meta", JsonHandler.getInstance().objectToJson(input.getMetadata()));
        LOG.info("Writing data " + descriptor.getName() + ".data.gz");
        runner.writeInput(descriptor.getName() + ".data.gz", input.getInputStream(true));
    }

    protected void writeSDF(InputStream sdf, DockerRunner runner, IODescriptor descriptor) throws IOException {
        LOG.info("Writing SDF " + descriptor.getName() + ".sdf.gz");
        runner.writeInput(descriptor.getName() + ".sdf.gz", IOUtils.getGzippedInputStream(sdf));
    }

    protected <P, Q> void readOutput(VariableManager varman, DockerRunner runner, IODescriptor<P, Q> descriptor) throws Exception {
        FilesystemReadContext context = new FilesystemReadContext(runner.getHostWorkDir(), descriptor.getName());
        P value = varman.getValue(descriptor.getPrimaryType(), context);
        createMappedOutput(descriptor.getName(), descriptor.getPrimaryType(), value, varman);
    }

    protected void readOutput2(VariableManager varman, DockerRunner runner, IODescriptor descriptor) throws Exception {

        String mediaType;
        if (descriptor.getMediaType() == null) {
            mediaType = CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON;
        } else {
            mediaType = descriptor.getMediaType();
        }

        if (CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON.equals(mediaType) || CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON.equals(mediaType)) {
            readDataset(varman, descriptor, runner);
        } else if (CommonMimeTypes.MIME_TYPE_MDL_SDF.equals(mediaType)) {
            readSDF(varman, descriptor, runner);
        } else {
            throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }
    }


    protected DatasetMetadata readDataset(VariableManager varman, IODescriptor descriptor, DockerRunner runner) throws Exception {
        DatasetMetadata meta;
        LOG.info("Reading metadata " + descriptor.getName() + ".meta");
        try (InputStream is = runner.readOutput(descriptor.getName() + ".meta")) {
            if (is == null) {
                meta = new DatasetMetadata(descriptor.getSecondaryType());
            } else {
                meta = JsonHandler.getInstance().objectFromJson(is, DatasetMetadata.class);
            }
        }

        LOG.info("Reading data " + descriptor.getName() + ".data.gz");
        try (InputStream is = runner.readOutput(descriptor.getName() + ".data.gz")) {
            Dataset<? extends BasicObject> dataset = new Dataset(meta.getType(), IOUtils.getGunzippedInputStream(is), meta);
            createMappedOutput(descriptor.getName(), Dataset.class, dataset, varman);
            LOG.fine("Results: " + dataset.getMetadata());
            return dataset.getMetadata();
        }
    }

    protected void readSDF(VariableManager varman, IODescriptor descriptor, DockerRunner runner) throws Exception {
        LOG.info("Reading SDF " + descriptor.getName() + ".sdf.gz");
        try (InputStream is = runner.readOutput(descriptor.getName() + ".sdf.gz")) {
            createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, InputStream.class, is, varman);
        }
    }

}
