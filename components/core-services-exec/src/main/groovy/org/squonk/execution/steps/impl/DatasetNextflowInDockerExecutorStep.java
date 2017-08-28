package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.core.NextflowServiceDescriptor;
import org.squonk.execution.runners.DockerRunner;
import org.squonk.execution.steps.AbstractServiceStep;
import org.squonk.execution.variable.VariableManager;
import org.squonk.util.IOUtils;

import java.util.Properties;
import java.util.logging.Logger;

/** Step that executes a <a href="http://nextflow.io">Nextflow</a> workflow, executing it inside a Docker container.
 *
 * IMPORTANT: Details of this step are subject to change.
 *
 * Created by timbo on 28/07/17.
 */
public class DatasetNextflowInDockerExecutorStep extends AbstractServiceStep {

    private static final Logger LOG = Logger.getLogger(DatasetNextflowInDockerExecutorStep.class.getName());
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

        statusMessage = MSG_PREPARING_CONTAINER;

        String command =  descriptor.getNextflowParams();
        String expandedCommand;
        if (command != null) {
            expandedCommand = expandCommand(command, options);
        } else {
            expandedCommand = "";
        }
        String fullCommand = "nextflow run nextflow.nf " + expandedCommand + " -with-docker debian";

        String image = "informaticsmatters/nextflow";
        DockerRunner runner = new DockerRunner(image);
        runner.init();
        LOG.info("Docker image: " + image + ", hostWorkDir: " + runner.getHostWorkDir() + ", command: " + fullCommand);

        try {
            // create input files
            statusMessage = MSG_PREPARING_INPUT;

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
                LOG.info("Writing nextflow.config as:\n" + nextflowConfigContents);
                runner.writeInput("nextflow.config", nextflowConfigContents, false);
            } else {
                LOG.info("No nextflow.config");
            }

            runner.includeDockerSocket();

            // write the input data
            handleInputs(camelContext, descriptor, varman, runner);

            // run the command
            statusMessage = MSG_RUNNING_CONTAINER;
            LOG.info("Executing ...");
            long t0 = System.currentTimeMillis();
            int status = runner.execute("./execute");
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
            handleOutputs(camelContext, descriptor, varman, runner);

            Properties props = runner.getFileAsProperties("output_metrics.txt");
            generateMetricsAndStatus(props, duration);

        } finally {
            // cleanup
            if (DEBUG_MODE < 2) {
                runner.cleanup();
                LOG.info("Results cleaned up");
            }
        }
    }

}
