/*
 * Copyright (c) 2018 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.api.VariableHandler;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.steps.AbstractThinStep;
import org.squonk.execution.variable.impl.FilesystemReadContext;
import org.squonk.execution.variable.impl.FilesystemWriteContext;
import org.squonk.io.IODescriptor;
import org.squonk.io.SquonkDataSource;
import org.squonk.types.DefaultHandler;
import org.squonk.types.TypeHandlerUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for steps that run in containers, typically Docker containers.
 */
public abstract class AbstractContainerStep extends AbstractThinStep {

    private static final Logger LOG = Logger.getLogger(AbstractContainerStep.class.getName());

    protected Float containerExecutionTime = null;

    @Override
    public Map<String, Object> doExecute(Map<String, Object> inputs, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_CONTAINER;
        DefaultServiceDescriptor dsd = getDefaultServiceDescriptor();
        ContainerRunner containerRunner = prepareContainerRunner();

        Map<String, List<SquonkDataSource>> dataSourcesMap = doExecuteForDataSources(inputs, context, containerRunner, dsd);
        LOG.info("Execution generated " + dataSourcesMap.size() + " outputs");
        int i = 1;
        if (LOG.isLoggable(Level.FINE)) {
            for (Map.Entry<String, List<SquonkDataSource>> e : dataSourcesMap.entrySet()) {
                StringBuilder b = new StringBuilder("Output ")
                        .append(i)
                        .append(": ")
                        .append(e.getKey())
                        .append(" -> [");
                for (SquonkDataSource sds : e.getValue()) {
                    b.append(" ")
                            .append(sds.getName())
                            .append(":")
                            .append(sds.getContentType());
                }
                b.append(" ]");
                LOG.fine(b.toString());
                i++;
            }
        }
        Map<String, Object> results = new LinkedHashMap<>();
        for (IODescriptor iod : serviceDescriptor.getServiceConfig().getOutputDescriptors()) {
            List<SquonkDataSource> dataSources = dataSourcesMap.get(iod.getName());
            if (dataSources == null || dataSources.isEmpty()) {
                LOG.warning("No dataSources found for variable " + iod.getName());
            } else {
                Object variable = TypeHandlerUtils.convertDataSourcesToVariable(dataSources, iod.getPrimaryType(), iod.getSecondaryType());
                results.put(iod.getName(), variable);
            }
        }
        return results;
    }

    protected Map<String, List<SquonkDataSource>> doExecuteForDataSources(
            Map<String, Object> inputs,
            CamelContext context,
            ContainerRunner containerRunner,
            DefaultServiceDescriptor descriptor) throws Exception {

        // create input files
        statusMessage = MSG_PREPARING_INPUT;

        // write the input data
        writeInputs(inputs, descriptor, containerRunner);

        handleExecute(containerRunner);

        // handle the outputs
        statusMessage = MSG_PREPARING_OUTPUT;
        Map<String, List<SquonkDataSource>> results = readOutputs(descriptor, containerRunner.getHostWorkDir());

        handleMetrics(containerRunner);

        return results;
    }

    protected abstract ContainerRunner prepareContainerRunner() throws IOException;

    protected void handleExecute(ContainerRunner containerRunner) {
        // run the command
        statusMessage = MSG_RUNNING_CONTAINER;
        LOG.info("Executing ...");
        long t0 = System.currentTimeMillis();
        int status = executeContainerRunner(containerRunner);
        long t1 = System.currentTimeMillis();
        containerExecutionTime = (t1 - t0) / 1000.0f;
        LOG.info(String.format("Executed in %s seconds with return status of %s", containerExecutionTime, status));

        if (status != 0) {
            String log = containerRunner.getLog();
            LOG.warning("Execution errors: " + log);
            statusMessage = "Container execution failed";
            throw new RuntimeException("Container execution failed:\n" + log);
        }
    }

    protected int executeContainerRunner(ContainerRunner containerRunner) {
        return containerRunner.execute(containerRunner.getLocalWorkDir() + "/execute");
    }

    protected void handleMetrics(ContainerRunner containerRunner) throws IOException {
        statusMessage = MSG_PROCESSING_RESULTS_READY;
        Properties props = containerRunner.getFileAsProperties("output_metrics.txt");
        generateMetricsAndStatus(props, containerExecutionTime);
    }

    protected void writeInputs(
            Map<String, Object> data,
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
                    doWriteInput(value, runner, iod);
                }
            }
        }
    }


    protected <P, Q> void doWriteInput(
            P input,
            ContainerRunner runner,
            IODescriptor<P, Q> iod) throws Exception {

        LOG.info("Handling input for " + iod.getName());
        VariableHandler<P> vh = DefaultHandler.createVariableHandler(iod.getPrimaryType(), iod.getSecondaryType());
        File dir = runner.getHostWorkDir();
        FilesystemWriteContext writeContext = new FilesystemWriteContext(dir, iod.getName());
        vh.writeVariable(input, writeContext);
    }


    protected Map<String, List<SquonkDataSource>> readOutputs(DefaultServiceDescriptor serviceDescriptor, File workdir) throws Exception {

        IODescriptor[] outputDescriptors = serviceDescriptor.resolveOutputIODescriptors();
        Map<String, List<SquonkDataSource>> results = new LinkedHashMap<>();
        if (outputDescriptors != null) {
            LOG.info("Handling " + outputDescriptors.length + " outputs");

            for (IODescriptor iod : outputDescriptors) {
                LOG.info("Reading output for " + iod.getName() + " " + iod.getMediaType());
                List<SquonkDataSource> result = doReadOutput(workdir, iod);
                results.put(iod.getName(), result);
            }
        }
        return results;
    }

    protected <P, Q> List<SquonkDataSource> doReadOutput(File workdir, IODescriptor<P, Q> iod) throws Exception {
        List<SquonkDataSource> outputs = buildOutputs(workdir, iod);
        return outputs;
    }

    private <P, Q> List<SquonkDataSource> buildOutputs(File workdir, IODescriptor<P, Q> iod) throws Exception {
        VariableHandler<P> vh = DefaultHandler.createVariableHandler(iod.getPrimaryType(), iod.getSecondaryType());
        VariableHandler.ReadContext readContext = new FilesystemReadContext(workdir, iod.getName());
        List<SquonkDataSource> dataSources = vh.readDataSources(readContext);
        return dataSources;
    }
}
