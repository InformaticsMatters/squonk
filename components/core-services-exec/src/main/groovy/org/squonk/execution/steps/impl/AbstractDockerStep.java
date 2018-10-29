package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.api.VariableHandler;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.steps.AbstractThinDatasetStep;
import org.squonk.execution.variable.impl.FilesystemReadContext;
import org.squonk.execution.variable.impl.FilesystemWriteContext;
import org.squonk.io.IODescriptor;
import org.squonk.io.SquonkDataSource;
import org.squonk.types.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class AbstractDockerStep extends AbstractThinDatasetStep {

    private static final Logger LOG = Logger.getLogger(AbstractDockerStep.class.getName());

    @Override
    public Map<String, Object> executeForVariables(Map<String, Object> inputs, CamelContext context) throws Exception {

        Map<String, List<SquonkDataSource>> dataSourcesMap = executeForDataSources(inputs, context);
        Map<String, Object> results = new LinkedHashMap<>();
        for (IODescriptor iod : serviceDescriptor.getServiceConfig().getOutputDescriptors()) {
            VariableHandler vh = DefaultHandler.createVariableHandler(iod.getPrimaryType(), iod.getSecondaryType());
            List<SquonkDataSource> dataSources = dataSourcesMap.get(iod.getName());
            if (dataSources == null || dataSources.isEmpty()) {
                LOG.warning("No dataSources found for variable " + iod.getName());
            } else {
                Object variable = vh.create(dataSources);
                results.put(iod.getName(), variable);
            }
        }
        return results;
    }

    protected Map<String, List<SquonkDataSource>> doExecuteForDataSources(Map<String, Object> inputs, CamelContext context, ContainerRunner containerRunner, DefaultServiceDescriptor descriptor) throws Exception {

        // create input files
        statusMessage = MSG_PREPARING_INPUT;

        // write the input data
        handleInputs(inputs, descriptor, containerRunner);

        float duration = handleExecute(containerRunner);

        // handle the outputs
        statusMessage = MSG_PREPARING_OUTPUT;
        Map<String,List<SquonkDataSource>> results = handleOutputs(descriptor, containerRunner.getHostWorkDir());

        handleMetrics(containerRunner, duration);

        return results;
    }

    protected float handleExecute(ContainerRunner containerRunner) {
        // run the command
        statusMessage = MSG_RUNNING_CONTAINER;
        LOG.info("Executing ...");
        long t0 = System.currentTimeMillis();
        int status = containerRunner.execute(containerRunner.getLocalWorkDir() + "/execute");
        long t1 = System.currentTimeMillis();
        float duration = (t1 - t0) / 1000.0f;
        LOG.info(String.format("Executed in %s seconds with return status of %s", duration, status));

        if (status != 0) {
            String log = containerRunner.getLog();
            LOG.warning("Execution errors: " + log);
            throw new RuntimeException("Container execution failed:\n" + log);
        }
        return duration;
    }

    protected void handleMetrics(ContainerRunner containerRunner, float duration) throws IOException {
        statusMessage = MSG_PROCESSING_RESULTS_READY;
        Properties props = containerRunner.getFileAsProperties("output_metrics.txt");
        generateMetricsAndStatus(props, duration);
    }

    protected void handleInputs(
            Map<String,Object> data,
            DefaultServiceDescriptor serviceDescriptor,
            ContainerRunner runner) throws Exception {

        IODescriptor[] inputDescriptors = serviceDescriptor.resolveInputIODescriptors();
        if (inputDescriptors != null) {
            LOG.info("Handling " + inputDescriptors.length + " inputs");
            for (IODescriptor iod : inputDescriptors) {
                Object value = data.get(iod.getName());
                if (value == null) {
                    LOG.warning("No input found for " + iod.getName());
                } else {
                    LOG.info("Writing input for " + iod.getName() + " " + iod.getMediaType());
                    doHandleInput(value, runner, iod);
                }
            }
        }
    }


    protected <P, Q> void doHandleInput(
            P input,
            ContainerRunner runner,
            IODescriptor<P, Q> iod) throws Exception {

        LOG.info("Handling input for " + iod.getName());

        // TODO - handle type conversion

        VariableHandler<P> vh = DefaultHandler.createVariableHandler(iod.getPrimaryType(), iod.getSecondaryType());
        File dir = runner.getHostWorkDir();
        FilesystemWriteContext writeContext = new FilesystemWriteContext(dir, iod.getName());
        vh.writeVariable(input, writeContext);
    }


    protected Map<String,List<SquonkDataSource>> handleOutputs(DefaultServiceDescriptor serviceDescriptor, File workdir) throws Exception {

        IODescriptor[] outputDescriptors = serviceDescriptor.resolveOutputIODescriptors();
        Map<String,List<SquonkDataSource>> results = new LinkedHashMap<>();
        if (outputDescriptors != null) {
            LOG.info("Handling " + outputDescriptors.length + " outputs");

            for (IODescriptor iod : outputDescriptors) {
                LOG.info("Reading output for " + iod.getName() + " " + iod.getMediaType());
                List<SquonkDataSource> result = doHandleOutput(workdir, iod);
                results.put(iod.getName(), result);
            }
        }
        return results;
    }

    protected <P, Q> List<SquonkDataSource> doHandleOutput(File workdir, IODescriptor<P, Q> iod) throws Exception {
        List<SquonkDataSource> outputs = buildOutputs(workdir, iod);
        return outputs;
    }

    private <P,Q> List<SquonkDataSource> buildOutputs(File workdir, IODescriptor<P, Q> iod) throws Exception {
        VariableHandler<P> vh = DefaultHandler.createVariableHandler(iod.getPrimaryType(), iod.getSecondaryType());
        VariableHandler.ReadContext readContext = new FilesystemReadContext(workdir, iod.getName());
        List<SquonkDataSource> dataSources = vh.readDataSources(readContext);
        return dataSources;
    }
}
