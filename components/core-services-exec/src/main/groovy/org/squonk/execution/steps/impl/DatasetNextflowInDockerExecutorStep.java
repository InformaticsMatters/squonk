package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.core.NextflowServiceDescriptor;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.runners.DockerRunner;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.SquonkDataSource;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/** Step that executes a <a href="http://nextflow.io">Nextflow</a> workflow, executing it inside a Docker container.
 *
 * IMPORTANT: Details of this step are subject to change.
 *
 * Created by timbo on 28/07/17.
 */
public class DatasetNextflowInDockerExecutorStep extends AbstractDockerStep {

    private static final Logger LOG = Logger.getLogger(DatasetNextflowInDockerExecutorStep.class.getName());
    private static final String NEXTFLOW_IMAGE = IOUtils.getConfiguration("SQUONK_NEXTFLOW_IMAGE", "informaticsmatters/nextflow-docker:0.30.2");
    private static final String NEXTFLOW_OPTIONS = IOUtils.getConfiguration("SQUONK_NEXTFLOW_OPTIONS", "-with-docker centos:7 -with-trace");

    protected static final String MSG_RUNNING_NEXTFLOW = "Running Nextflow";


    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        if (serviceDescriptor == null) {
            throw new IllegalStateException("No service descriptor present ");
        } else if (!(serviceDescriptor instanceof NextflowServiceDescriptor)) {
            throw new IllegalStateException("Expected service descriptor to be a " +
                    NextflowServiceDescriptor.class.getName() +
                    "  but it was a " + serviceDescriptor.getClass().getName());
        }
        NextflowServiceDescriptor descriptor = (NextflowServiceDescriptor) serviceDescriptor;

        LOG.info("Input types are " + IOUtils.joinArray(descriptor.getServiceConfig().getInputDescriptors(), ","));
        LOG.info("Output types are " + IOUtils.joinArray(descriptor.getServiceConfig().getOutputDescriptors(), ","));

        // this executes this cell
        doExecute(varman, context, descriptor);
        //LOG.info(varman.getTmpVariableInfo());
    }

    protected void doExecute(VariableManager varman, CamelContext camelContext, NextflowServiceDescriptor descriptor) throws Exception {

        ContainerRunner containerRunner = prepareContainerRunner(descriptor);
        try {
            // create input files
            statusMessage = MSG_PREPARING_INPUT;
            // write the input data
            // fetch the input data
            Map<String,Object> inputs = fetchInputs(camelContext, descriptor, varman, containerRunner);
            // write the input data
            handleInputs(inputs, descriptor, containerRunner);
            //handleInputs(camelContext, descriptor, varman, runner);

            // run the command
            float duration = handleExecute(containerRunner);
            // handle the output
            statusMessage = MSG_PREPARING_OUTPUT;
            handleOutputs(camelContext, descriptor, varman, containerRunner);

            handleMetrics(containerRunner, duration);

        } finally {
            // cleanup
            if (DEBUG_MODE < 2) {
                containerRunner.cleanup();
                LOG.info("Results cleaned up");
            }
        }
    }


    @Override
    public Map<String, List<SquonkDataSource>> executeForDataSources(Map<String, Object> inputs, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_CONTAINER;
        NextflowServiceDescriptor descriptor = getNextflowServiceDescriptor();
        ContainerRunner containerRunner = prepareContainerRunner(descriptor);

        Map<String, List<SquonkDataSource>> outputs = doExecuteForDataSources(inputs, context, containerRunner, descriptor);
        return outputs;
    }

    private ContainerRunner prepareContainerRunner(NextflowServiceDescriptor descriptor) throws IOException {
        statusMessage = MSG_PREPARING_CONTAINER;

        String command =  descriptor.getNextflowParams();
        String expandedCommand;
        if (command != null) {
            expandedCommand = expandCommand(command, options);
        } else {
            expandedCommand = "";
        }
        String fullCommand = "nextflow run nextflow.nf " + NEXTFLOW_OPTIONS + " " + expandedCommand;
        ContainerRunner runner = createContainerRunner(NEXTFLOW_IMAGE);
        runner.init();
        LOG.info("Docker Nextflow executor image: " + NEXTFLOW_IMAGE + ", hostWorkDir: " + runner.getHostWorkDir() + ", command: " + fullCommand);

        // write the command that executes everything
        LOG.info("Writing command file");
        runner.writeInput("execute", "#!/bin/sh\n" + fullCommand + "\n", true);

        // write the nextflow file that executes everything
        LOG.info("Writing nextflow.nf");
        String nextflowFileContents = descriptor.getNextflowFile();
        runner.writeInput("nextflow.nf", nextflowFileContents, false);

        // write the nextflow config file if one is defined
        String nextflowConfigContents = descriptor.getNextflowConfig();
        if (nextflowConfigContents != null && !nextflowConfigContents.isEmpty()) {
            // An opportunity for the runner to provide extra configuration.
            // There may be nothing to add but the returned string
            // will be valid.
            nextflowConfigContents = runner.addExtraNextflowConfig(nextflowConfigContents);
            LOG.info("Writing nextflow.config as:\n" + nextflowConfigContents);
            runner.writeInput("nextflow.config", nextflowConfigContents, false);
        } else {
            LOG.info("No nextflow.config");
        }

        // The runner's either a plain Docker runner
        // or it's an OpenShift runner.
        if (runner instanceof DockerRunner){
            ((DockerRunner)runner).includeDockerSocket();
        }

        return runner;
    }

}