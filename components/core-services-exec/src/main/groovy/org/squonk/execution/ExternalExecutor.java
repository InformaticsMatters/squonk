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
package org.squonk.execution;

import org.squonk.api.VariableHandler;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.DockerServiceDescriptor;
import org.squonk.core.NextflowServiceDescriptor;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.impl.FilesystemReadContext;
import org.squonk.execution.variable.impl.FilesystemWriteContext;
import org.squonk.io.IODescriptor;
import org.squonk.jobdef.ExternalJobDefinition;
import org.squonk.jobdef.JobStatus;
import org.squonk.jobdef.JobStatus.Status;
import org.squonk.types.StreamType;
import org.squonk.types.TypeResolver;
import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExternalExecutor extends ExecutableService {

    private final Logger LOG = Logger.getLogger(ExternalExecutor.class.getName());
    private static final TypeResolver typeResolver = TypeResolver.getInstance();

    private final ExternalJobDefinition jobDefinition;
    private final ExecutorCallback callback;
    private final LinkedHashMap<String, InputStream> data = new LinkedHashMap<>();
    private final Map<String, Object> results = new LinkedHashMap<>();

    private ContainerRunner runner;
    protected Status status;

    public ExternalExecutor(ExternalJobDefinition jobDefinition, ExecutorCallback callback) {
        super(jobDefinition.getJobId(), jobDefinition.getOptions());
        this.jobDefinition = jobDefinition;
        this.callback = callback;
    }

    public ServiceDescriptor getServiceDescriptor() {
        return jobDefinition.getServiceDescriptor();
    }

    /**
     * Add the data to be executed. The name property must match the name in the IODescriptor for an input (in the case
     * of a value being comprised of a single InputStream, or must start with the name from the IODescriptor followed by
     * and underscore followed by the identifier for the sub type in the case of datasets being comprised of multiple
     * InputStreams.
     *
     * @param name
     * @param data
     */
    protected void addData(String name, InputStream data) {
        this.data.put(name, data);
    }

    /**
     * Add the data to be executed.
     *
     * @param name  Must match the name of an IODescriptor for an input
     * @param value The value to add, probably an instance of @{link StreamType}. e.g. DataSet or SDFile
     */
    public void addData(String name, Object value) throws IOException {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        if (value == null) {
            LOG.warning("Adding null data value. This should be avoided");
            return;
        }
        if (value instanceof StreamType) {
            StreamType streamType = (StreamType) value;
            InputStream[] outputs = streamType.getInputStreams();
            String[] names = streamType.getStreamNames();
            if (names.length == 1) {
                // single output - we can just use the name from the IOD
                addData(name, outputs[0]);
            } else {
                // multiple outputs - we need to append the name to the one from the IOD
                for (int i = 0; i < names.length; i++) {
                    if (outputs[i] != null) {
                        addData(name + "_" + names[i], outputs[i]);
                    }
                }
            }
        } else {
            // hope this never happens, but would at least handle simple types
            String txt = value.toString();
            data.put(name, new ByteArrayInputStream(txt.getBytes()));
        }
    }

    /** Fetch the results. This should only be called when the status has changed to @{link JobStatus.Status.RESULTS_READY}.
     * The results are a keyed by the name of the result, with the name being significant when there are multiple results.
     * The result value will be one of the supported data types such as DataSet for SDFile, and in most cases will be an
     * implementation of @{link org.squonk.types.StreamType} or in other cases something that can be reconstructed from
     * its .toString() representation using a single argument constructor of type String.
     *
     * @return
     */
    public Map<String, Object> getResults() {
        if (Status.RESULTS_READY != status) {
            throw new IllegalStateException("Results not available");
        }
        return results;
    }

    public Map<String, Integer> getUsageStats() {
        return usageStats;
    }

    private JobStatus updateStatus(Status status) {
        this.status = status;
        try {
            return callback == null ? null : callback.updateStatus(getJobId(), status, statusMessage, numProcessed, numErrors);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to update status", e);
        }
        return null;
    }

    public void execute() {

        ServiceDescriptor serviceDescriptor = getServiceDescriptor();
        LOG.info("Input types are " + IOUtils.joinArray(serviceDescriptor.getServiceConfig().getInputDescriptors(), ","));
        LOG.info("Output types are " + IOUtils.joinArray(serviceDescriptor.getServiceConfig().getOutputDescriptors(), ","));

        try {
            if (serviceDescriptor instanceof DockerServiceDescriptor) {
                // handle as a Docker service
                DockerServiceDescriptor descriptor = (DockerServiceDescriptor) serviceDescriptor;
                doExecuteDocker(descriptor);
                return;
            } else if (serviceDescriptor instanceof NextflowServiceDescriptor) {
                // handle as a Nexflow service
                NextflowServiceDescriptor descriptor = (NextflowServiceDescriptor) serviceDescriptor;
                doExecuteNextflow(descriptor);
                return;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to execute job", e);
            statusMessage = "Execution failed: " + e.getMessage();
            updateStatus(Status.ERROR);
            return;
        }

        throw new IllegalStateException("Expected service descriptor to be a " +
                DockerServiceDescriptor.class.getName() +
                "  but it was a " + serviceDescriptor.getClass().getName());
    }

    private void doExecuteNextflow(NextflowServiceDescriptor descriptor) throws Exception {
        throw new IllegalStateException("NYI");
    }

    private void doExecuteDocker(DockerServiceDescriptor descriptor) throws Exception {

        statusMessage = MSG_PREPARING_CONTAINER;

        String image = getOption(StepDefinitionConstants.OPTION_DOCKER_IMAGE, String.class);
        if (image == null || image.isEmpty()) {
            image = descriptor.getImageName();
        }
        if (image == null || image.isEmpty()) {
            throw new IllegalStateException(
                    "Docker image not defined. Must be set as value of the executionEndpoint property of the ServiceDescriptor or as an option named "
                            + StepDefinitionConstants.OPTION_DOCKER_IMAGE);
        }

        String imageVersion = getOption(StepDefinitionConstants.OPTION_DOCKER_IMAGE_VERSION, String.class);
        if (imageVersion != null && !imageVersion.isEmpty()) {
            image = image + ":" + imageVersion;
        }

        String command = getOption(OPTION_DOCKER_COMMAND, String.class);
        if (command == null || command.isEmpty()) {
            command = descriptor.getCommand();
        }
        if (command == null || command.isEmpty()) {
            throw new IllegalStateException(
                    "Docker run command is not defined. Must be set as value of the command property of the ServiceDescriptor as option named "
                            + OPTION_DOCKER_COMMAND);
        }
        // command will be something like:
        // screen.py 'c1(c2c(oc1)ccc(c2)OCC(=O)O)C(=O)c1ccccc1' 0.3 --d morgan2
        // screen.py '${query}' ${threshold} --d ${descriptor}
        String expandedCommand = expandCommand(command, options);

        String localWorkDir = "/source";

        runner = createContainerRunner(image, localWorkDir);
        runner.init();
        LOG.info("Docker image: " + image + ", hostWorkDir: " + runner.getHostWorkDir() + ", command: " + expandedCommand);

        // create input files
        statusMessage = MSG_PREPARING_INPUT;

        // write the command that executes everything
        LOG.info("Writing command file");
        runner.writeInput("execute", "#!/bin/sh\n" + expandedCommand + "\n", true);

        // write the input data
        handleInputs(descriptor, runner);

        // run the command
        statusMessage = MSG_RUNNING_CONTAINER;
        LOG.info("Executing ...");
        updateStatus(Status.RUNNING);
        long t0 = System.currentTimeMillis();
        int status = runner.execute(localWorkDir + "/execute");
        long t1 = System.currentTimeMillis();
        float duration = (t1 - t0) / 1000.0f;
        LOG.info(String.format("Executed in %s seconds with return status of %s", duration, status));

        if (status != 0) {
            String log = runner.getLog();
            LOG.warning("Execution errors: " + log);
            throw new RuntimeException("Container execution failed:\n" + log);
        }

        // handle the output
        statusMessage = MSG_PREPARING_OUTPUT;
        handleOutputs(descriptor, runner);

        Properties props = runner.getFileAsProperties("output_metrics.txt");
        generateMetricsAndStatus(props, duration);

        statusMessage = MSG_PROCESSING_RESULTS_READY;
        updateStatus(Status.RESULTS_READY);
    }

    public void terminate() throws Exception {

        // TODO - implement terminating the runner

        cleanup();
    }

    public void cleanup() {
        if (runner != null && DEBUG_MODE < 2) {
            runner.cleanup();
            LOG.info("Results cleaned up");
        }
    }

    protected void handleOutputs(
            DefaultServiceDescriptor serviceDescriptor,
            ContainerRunner runner) throws Exception {

        IODescriptor[] outputDescriptors = serviceDescriptor.resolveOutputIODescriptors();
        if (outputDescriptors != null) {
            LOG.info("Handling " + outputDescriptors.length + " inputs");

            for (IODescriptor iod : outputDescriptors) {
                LOG.info("Writing output for " + iod.getName() + " " + iod.getMediaType());
                doHandleOutput(runner, iod);
            }
        }
    }

    protected <P, Q> void doHandleOutput(
            ContainerRunner runner,
            IODescriptor<P, Q> iod) throws Exception {

        VariableHandler<P> vh = typeResolver.createVariableHandler(iod.getPrimaryType(), iod.getSecondaryType());
        File dir = runner.getHostWorkDir();
        VariableHandler.ReadContext readContext = new FilesystemReadContext(dir, iod.getName());
        P value = vh.readVariable(readContext);
        results.put(iod.getName(), value);
//        if (value != null) {
//            if (value instanceof StreamType) {
//                StreamType streamType = (StreamType) value;
//                InputStream[] outputs = streamType.getGzippedInputStreams();
//                String[] names = streamType.getStreamNames();
//                if (names.length == 1) {
//                    // single output - we can just use the name from the IOD
//                    results.put(iod.getName(), outputs[0]);
//                } else {
//                    // multiple output - we need to append the name to the one from the IOD
//                    for (int i = 0; i < names.length; i++) {
//                        if (outputs[i] != null) {
//                            results.put(iod.getName() + "_" + names[i], outputs[i]);
//                        }
//                    }
//                }
//            } else {
//                // hope this never happens, but would at least handle simple types
//                String txt = value.toString();
//                results.put(iod.getName(), new ByteArrayInputStream(txt.getBytes()));
//            }
//        }
    }

    protected void handleInputs(
            DefaultServiceDescriptor serviceDescriptor,
            ContainerRunner runner) throws Exception {

        IODescriptor[] inputDescriptors = serviceDescriptor.resolveInputIODescriptors();
        if (inputDescriptors != null) {
            LOG.info("Handling " + inputDescriptors.length + " inputs");
            for (IODescriptor iod : inputDescriptors) {
                LOG.info("Writing input for " + iod.getName() + " " + iod.getMediaType());
                Map<String, InputStream> inputs = new HashMap<>();
                data.forEach((name, is) -> {
                    if (name.equalsIgnoreCase(iod.getName())) {
                        inputs.put(name, is);
                    } else if (name.toLowerCase().startsWith(iod.getName().toLowerCase() + "_")) {
                        String part = name.substring(iod.getName().length() + 1);
                        inputs.put(part, is);

                    }
                });
                doHandleInput(inputs, runner, iod);
            }
        }
    }

    protected <P, Q> void doHandleInput(
            Map<String, InputStream> inputs,
            ContainerRunner runner,
            IODescriptor<P, Q> iod) throws Exception {

        LOG.info("Handling input for " + iod.getName() + " with " + inputs.size() + " values");

        // TODO - handle type conversion

        VariableHandler<P> vh = typeResolver.createVariableHandler(iod.getPrimaryType(), iod.getSecondaryType());
        P value = vh.create(inputs);
        File dir = runner.getHostWorkDir();
        FilesystemWriteContext writeContext = new FilesystemWriteContext(dir, iod.getName());
        vh.writeVariable(value, writeContext);
    }
}
