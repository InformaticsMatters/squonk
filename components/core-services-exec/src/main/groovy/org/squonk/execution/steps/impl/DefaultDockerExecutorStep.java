package org.squonk.execution.steps.impl;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Volume;
import org.apache.camel.CamelContext;
import org.squonk.core.DockerServiceDescriptor;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.util.GroovyUtils;
import org.squonk.execution.variable.VariableManager;
import org.squonk.execution.variable.impl.FilesystemReadContext;
import org.squonk.execution.variable.impl.FilesystemWriteContext;
import org.squonk.io.IODescriptor;
import org.squonk.util.IOUtils;

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
    protected Integer DEBUG_MODE = new Integer(IOUtils.getConfiguration("SQUONK_DEBUG_MODE", "0"));

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

        // this executes this cell
        doExecute(varman, context, descriptor);
        LOG.info(varman.getTmpVariableInfo());

    }

    protected void doExecute(VariableManager varman, CamelContext context, DockerServiceDescriptor descriptor) throws Exception {

        LOG.info("Debug Mode: " + DEBUG_MODE);

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
        LOG.info("Template: " + command);
        String expandedCommand = GroovyUtils.expandTemplate(command, args);
        LOG.fine("Command: " + expandedCommand);
        
        String localWorkDir = "/source";

        DockerRunner runner = createDockerRunner(image, localWorkDir);
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
                LOG.info("Handling " + inputDescriptors.length + " inputs");
                for (IODescriptor d : inputDescriptors) {
                    LOG.info("Writing input for " + d.getName() + " " + d.getMediaType());
                    handleInput(varman, runner, d);
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
                LOG.info("Handling " + outputDescriptors.length + " outputs");
                for (IODescriptor d : outputDescriptors) {
                    handleOutput(varman, runner, d);
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
            if (DEBUG_MODE < 2) {
                runner.cleanup();
                LOG.info("Results cleaned up");
            }
        }
    }

    protected <P,Q> void handleInput(VariableManager varman, DockerRunner runner, IODescriptor<P,Q> descriptor) throws Exception {
        P value = fetchMappedInput(descriptor.getName(), descriptor.getPrimaryType(), varman, true);
        FilesystemWriteContext context = new FilesystemWriteContext(runner.getHostWorkDir(), descriptor.getName());
        varman.putValue(descriptor.getPrimaryType(), value, context);
    }

    protected <P, Q> void handleOutput(VariableManager varman, DockerRunner runner, IODescriptor<P, Q> descriptor) throws Exception {
        FilesystemReadContext context = new FilesystemReadContext(runner.getHostWorkDir(), descriptor.getName());
        P value = varman.getValue(descriptor.getPrimaryType(), context);
        createMappedOutput(descriptor.getName(), descriptor.getPrimaryType(), value, varman);
    }
}
