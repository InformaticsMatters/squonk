package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.docker.DockerExecutorDescriptor;
import org.squonk.execution.docker.DockerExecutorDescriptorRegistry;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.Step;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.util.GroovyUtils;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.Resource;
import org.squonk.notebook.api.VariableKey;
import org.squonk.util.CommonMimeTypes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;



/**
 * Created by timbo on 29/12/15.
 */
public class CannedDockerProcessDatasetStep extends AbstractDockerStep {

    private static final Logger LOG = Logger.getLogger(CannedDockerProcessDatasetStep.class.getName());

    private static final String OPTION_DOCKER_IMAGE = StepDefinitionConstants.OPTION_DOCKER_IMAGE;
    private static final String OPTION_DOCKER_COMMAND = StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        String id = getOption("docker.executor.id", String.class);
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("docker.executor.id must be defined in the options");
        }
        DockerExecutorDescriptor descriptor = fetchDescriptor(id);

        String inputMediaType = descriptor.getInputMediaType();
        String outputMediaType = descriptor.getOutputMediaType();

        // first handle any conversions
        // currently these are hard coded and only SDF is supported
        // TODO - provide these conversions through a registry
        if (inputMediaType == null || CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON.equals(inputMediaType)) {
            // do nothing
        } else if (CommonMimeTypes.MIME_TYPE_MDL_SDF.equals(inputMediaType)) {
            // convert to SDF
            Step sdfConvertStep = createSdfGeneratorStep("_converted_sdf_input");
            LOG.info("Executing SDF converter step");
            sdfConvertStep.execute(varman, context);
            //LOG.info(varman.getTmpVariableInfo());

        } else {
            throw new IllegalStateException("Unsupported format conversion: " + inputMediaType);
        }

        Step readerStep = null;
        if (outputMediaType == null || CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON.equals(outputMediaType)) {
            // do nothing
        } else if (CommonMimeTypes.MIME_TYPE_MDL_SDF.equals(outputMediaType)) {
            // convert from SDF
            readerStep = createSdfReaderStep("_converted_sdf_output");
        } else {
            throw new IllegalStateException("Unsupported format conversion: " + inputMediaType);
        }

        // this executes this cell
        doExecute(varman, context, descriptor);
        //LOG.info(varman.getTmpVariableInfo());

        // and now if we created a reader to convert the format then execute it
        if (readerStep != null) {
            LOG.info("Executing SDF reader step");
            readerStep.execute(varman, context);
            //LOG.info(varman.getTmpVariableInfo());
        }
    }

    private DockerExecutorDescriptor fetchDescriptor(String id) {
        DockerExecutorDescriptor d = DockerExecutorDescriptorRegistry.getInstance().fetch(id);
        if (d == null) {
            throw new IllegalStateException("ID " + id + " not found");
        }
        return d;
    }

    private Step createSdfGeneratorStep(String tmpVarName) {

        DatasetServiceExecutorStep step = new DatasetServiceExecutorStep();

        Map<String,Object> opts = new HashMap<>();
        opts.put("header.Content-Encoding", "gzip");
        opts.put("header.Accept-Encoding", "gzip");
        opts.put("header.Content-Type", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON);
        opts.put("header.Accept", CommonMimeTypes.MIME_TYPE_MDL_SDF);
        // TODO - allow user to define which impl to use
        // TODO - avoid hardcoding the URL - look it up from the service descriptors?
        opts.put(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, "http://chemservices:8080/chem-services-cdk-basic/rest/v1/converters/convert_to_sdf");

        Map<String,VariableKey> inputs = new HashMap<>();
        VariableKey origInput = inputVariableMappings.get(StepDefinitionConstants.VARIABLE_INPUT_DATASET);
        inputs.put(StepDefinitionConstants.VARIABLE_INPUT_DATASET, origInput);

        // tell the main cell to read its input to the tmp variable name
        inputVariableMappings.put(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(outputProducerId, tmpVarName));

        Map<String,String> outputs = new HashMap<>();
        outputs.put(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, tmpVarName);

        step.configure(outputProducerId, jobId, opts, inputs, outputs);
        return step;
    }

    private Step createSdfReaderStep(String tmpVarName) {

        SDFReaderStep step = new SDFReaderStep();

        Map<String,VariableKey> inputs = new HashMap<>();
        // tell the converter cell to read its data from the tmp variable
        VariableKey origInput = inputVariableMappings.get(StepDefinitionConstants.VARIABLE_INPUT_DATASET);
        inputs.put(StepDefinitionConstants.VARIABLE_FILE_INPUT, new VariableKey(outputProducerId, tmpVarName));

        Map<String,String> outputs = new HashMap<>();
        // tell the converter cell to write its output to whatever the original cell was to use
        String origOutput = outputVariableMappings.get(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET);
        outputs.put(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, origOutput);

        // tell the main cell to write its output to the tmp variable
        outputVariableMappings.put(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, tmpVarName);

        step.configure(outputProducerId, jobId, null, inputs, outputs);
        return step;
    }

    protected void doExecute(VariableManager varman, CamelContext context, DockerExecutorDescriptor descriptor) throws Exception {

        statusMessage = MSG_PREPARING_CONTAINER;

        String image = getOption(OPTION_DOCKER_IMAGE, String.class);
        String command = descriptor.getCommand();
        String inputMediaType = descriptor.getInputMediaType();
        String outputMediaType = descriptor.getOutputMediaType();

        if (image == null || image.isEmpty()) {
            statusMessage = "Error: Docker image not defined";
            throw new IllegalStateException("Docker image not defined. Should be present as option named " + OPTION_DOCKER_IMAGE);
        }
        if (command == null || command.isEmpty()) {
            statusMessage = "Error: Docker command not defined";
            throw new IllegalStateException("Command to run is not defined. Should be present as option named " + OPTION_DOCKER_COMMAND);
        }

        // command will be something like:
        // screen.py 'c1(c2c(oc1)ccc(c2)OCC(=O)O)C(=O)c1ccccc1' 0.3 --d morgan2
        // screen.py '${query}' ${threshold} --d ${descriptor}

        Map<String,Object> args = new LinkedHashMap<>();
        options.forEach((k,v) -> {
            if (k.startsWith("arg.")) {
                LOG.fine("Found argument " + k + " = " + v);
                args.put(k.substring(4), v);
            }
        });

        // replace windows line end characters
        command = command.replaceAll("\\r\\n", "\n");
        String expandedCommand = GroovyUtils.expandTemplate(command, args);

        String hostWorkDir = "/tmp/work";
        String localWorkDir = "/source";

        DockerRunner runner = createDockerRunner(image, hostWorkDir, localWorkDir);
        LOG.info("Docker image: " + image + ", hostWorkDir: " + runner.getHostWorkDir() + ", command: " + expandedCommand);
        try {
            // create input files
            statusMessage = MSG_PREPARING_INPUT;

            // write the command that executes everything
            LOG.fine("Writing command file");
            runner.writeInput("execute", "#!/bin/sh\n" + expandedCommand, true);

            // add the resources
            for (Map.Entry<String,Resource> e : descriptor.getResources().entrySet()) {
                Resource r = e.getValue();
                runner.writeInput(e.getKey(), r.get());
            }

            // write the input data
            // TODO - handle multiple inputs and don't assume names of input and output
            DatasetMetadata inputMetadata = handleInput(varman, runner, inputMediaType);

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
            handleOutput(inputMetadata, varman, runner, outputMediaType);

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

}
