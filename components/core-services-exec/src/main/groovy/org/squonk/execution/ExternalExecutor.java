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

import org.apache.camel.CamelContext;
import org.squonk.api.VariableHandler;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.DockerServiceDescriptor;
import org.squonk.core.NextflowServiceDescriptor;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.runners.DefaultServiceRunner;
import org.squonk.execution.runners.DockerRunner;
import org.squonk.execution.runners.ServiceRunner;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.impl.FilesystemReadContext;
import org.squonk.execution.variable.impl.FilesystemWriteContext;
import org.squonk.io.IODescriptor;
import org.squonk.io.SquonkDataSource;
import org.squonk.io.StringDataSource;
import org.squonk.jobdef.ExternalJobDefinition;
import org.squonk.jobdef.JobStatus;
import org.squonk.jobdef.JobStatus.Status;
import org.squonk.types.DefaultHandler;
import org.squonk.types.StreamType;
import org.squonk.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Manages the execution of jobs. Each execution should have its own instance of this class.
 * The procedure is as follows:
 * <ol>
 *     <li>Create an instance of this class, including passing in an instance to received callback calls.</li>
 *     <li>Set the input(s) using one of the addDataAs*() methods.</li>
 *     <li>Use the execute() method to start the job. This returns a JobStatus with the job's ID.</li>
 *     <li>Use the callback instance to be notified of status changes.</li>
 *     <li>When the status changes to {@link org.squonk.jobdef.JobStatus.Status#RESULTS_READY} you can fetch the results
 *     using one of the getResultsAs*() methods.</li>
 *     <li>One you have the results call the {@link #cleanup} method.</li>
 * </ol>
 *
 */
public class ExternalExecutor extends ExecutableJob {

    private static final Logger LOG = Logger.getLogger(ExternalExecutor.class.getName());

    private static final String NEXTFLOW_IMAGE = IOUtils.
            getConfiguration("SQUONK_NEXTFLOW_IMAGE",
                    "informaticsmatters/nextflow-docker:0.30.2");
    private static final String NEXTFLOW_OPTIONS = IOUtils.
            getConfiguration("SQUONK_NEXTFLOW_OPTIONS",
                    "-with-docker ubuntu");

    private final ExternalJobDefinition jobDefinition;
    private final ExecutorCallback callback;
    private final Map<String, Object> data;
    private final CamelContext camelContext;
    //private final Map<String, Object> results = new LinkedHashMap<>();
    private Map<String,List<SquonkDataSource>> results = new LinkedHashMap<>();
    private File resultsDir;

    private ServiceRunner runner;
    protected Status status;

    public ExternalExecutor(
            ExternalJobDefinition jobDefinition,
            Map<String, Object> data,
            Map<String, Object> options,
            ServiceDescriptor serviceDescriptor,
            CamelContext camelContext,
            ExecutorCallback callback) {
        this.jobDefinition = jobDefinition;
        this.jobId = jobDefinition.getJobId();
        this.data = data;
        this.options = options;
        this.serviceDescriptor = serviceDescriptor;
        this.camelContext = camelContext;
        this.callback = callback;

    }

    /** used in testing */
    public ExternalExecutor(
            ExternalJobDefinition jobDefinition,
            Map<String, Object> data,
            Map<String, Object> options,
            ServiceDescriptor serviceDescriptor) {
        this.jobDefinition = jobDefinition;
        this.jobId = jobDefinition.getJobId();
        this.data = data;
        this.options = options;
        this.serviceDescriptor = serviceDescriptor;

        this.camelContext = null;
        this.callback = null;
    }

    public ServiceDescriptor getServiceDescriptor() {
        return jobDefinition.getServiceDescriptor();
    }

    /**
     * Fetch the results. This should only be called when the status has changed to @{link JobStatus.Status.RESULTS_READY}.
     * The results are a keyed by the name of the result, with the name being significant when there are multiple results.
     * The result value will be one of the supported data types such as DataSet for SDFile, and in most cases will be an
     * implementation of @{link org.squonk.types.StreamType} or in other cases something that can be reconstructed from
     * its .toString() representation using a single argument constructor of type String.
     *
     * @return
     */
    public Map<String, Object> getResultsAsObjects() throws Exception {
//        if (Status.RESULTS_READY != status) {
//            throw new IllegalStateException("Results not available");
//        }
//        return results;

        Map<String,List<SquonkDataSource>> r = getResultsAsDataSources();
        Map<String, Object> map = new HashMap<>();
        IODescriptor[] outputDescriptors = serviceDescriptor.resolveOutputIODescriptors();
        if (outputDescriptors != null) {
            LOG.info("Handling " + outputDescriptors.length + " outputs");

            for (IODescriptor iod : outputDescriptors) {
                LOG.info("Getting output for " + iod.getName() + " " + iod.getMediaType());
                List<SquonkDataSource> dataSources = r.get(iod.getName());
                if (dataSources != null && !dataSources.isEmpty()) {
                    VariableHandler vh = DefaultHandler.createVariableHandler(iod.getPrimaryType(), iod.getSecondaryType());
                    Object value = vh.create(dataSources);
                    map.put(iod.getName(), value);
                } else {
                    LOG.warning("Not datasources found for output " + iod.getName());
                }
            }
        }
        return map;
    }

//    /**
//     * Fetch results as InputStreams ready to transfer.
//     * For each output there will be one or more InputStreams.
//     * Where there is a single InputStreams (e.g. with SDFile)
//     * the key in the map will be the name of the output (e.g. "output").
//     * Where there are multiple InputStreams (e.g. with Dataset)
//     * the keys in the map will be the name of the output appended with the type of output
//     * (e.g. "output_data" and "output_metadata").
//     *
//     * @return
//     * @throws IOException
//     */
//    public Map<String, InputStream> getResultsAsInputStreams() throws IOException {
////        List<SquonkDataSource> dataSources = getResultsAsDataSources();
////        Map<String, InputStream> results = new HashMap<>();
////        for (SquonkDataSource dataSource: dataSources) {
////            results.put(dataSource.getName(), dataSource.getInputStream());
////        }
////        return results;
//
//        Map<String,List<SquonkDataSource> r = getResultsAsDataSources();
//        Map<String, InputStream> map = new HashMap<>();
//        for (SquonkDataSource dataSource: r) {
//            map.put(dataSource.getName(), dataSource.getInputStream());
//        }
//        return map;
//    }

    /**
     * Fetch results as SquonkDataSource ready to transfer.
     * For each output there will be one or more SquonkDataSource.
     * Where there is a single SquonkDataSource (e.g. with SDFile)
     * the name of the SquonkDataSource will be the name of the output (e.g. "output").
     * Where there are multiple SquonkDataSource (e.g. with Dataset)
     * the name will be the name of the output appended with the type of output
     * (e.g. "output_data" and "output_metadata").
     *
     * @return
     * @throws IOException
     */
    public Map<String,List<SquonkDataSource>> getResultsAsDataSources() throws IOException {
//        Map<String, Object> objects = getResultsAsObjects();
//        return convertObjectToDataSources(objects);
        if (Status.RESULTS_READY != status) {
            throw new IllegalStateException("Results not yet available");
        }
        if (results == null || results.isEmpty()) {
            throw new IllegalStateException("Results not found");
        }
        return results;
    }

//    private static List<SquonkDataSource> convertObjectToDataSources(Map<String, Object> objects) throws IOException {
//        List<SquonkDataSource> inputs = new ArrayList<>();
//        for (Map.Entry<String, Object> e : objects.entrySet()) {
//            String name = e.getKey();
//            Object value = e.getValue();
//            LOG.fine("Found value " + value + " for variable " + name);
//            if (value != null) {
//                if (value instanceof StreamType) {
//                    StreamType streamType = (StreamType) value;
//                    SquonkDataSource[] dataSources = streamType.getDataSources();
//                    if (dataSources.length == 1) {
//                        // single datasource - can just uses the output name
//                        dataSources[0].setGzipContent(false);
//                        inputs.add(dataSources[0]);
//                    } else {
//                        for (SquonkDataSource ds : dataSources) {
//                            LOG.fine("Adding DataHandler for " + ds.getName());
//                            String dsName = ds.getName();
//                            ds.setName(name + "_" + dsName);
//                            ds.setGzipContent(false);
//                            inputs.add(ds);
//                        }
//                    }
//                } else {
//                    // hope this never happens, but would at least handle simple types
//                    String txt = value.toString();
//                    SquonkDataSource ds = new StringDataSource(name, "text/plain", txt, false);
//                    inputs.add(ds);
//                }
//            }
//        }
//        return inputs;
//    }

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

    /** Execute the job.
     * Use the Callback instance to track the execution of the job.
     *
     */
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
            } else if (serviceDescriptor instanceof DefaultServiceDescriptor){
                DefaultServiceDescriptor descriptor = (DefaultServiceDescriptor)serviceDescriptor;
                doExecuteDefault(descriptor);
                return;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to execute job", e);
            statusMessage = "Execution failed: " + e.getMessage();
            updateStatus(Status.ERROR);
            return;
        }

        throw new IllegalStateException("Unsupported service descriptor type " +
                serviceDescriptor.getClass().getName());
    }

    private void doExecuteDefault(DefaultServiceDescriptor serviceDescriptor) throws Exception {
        String execClsName = serviceDescriptor.getServiceConfig().getExecutorClassName();
        LOG.info("Executor class name is " + execClsName);
        Class cls = Class.forName(execClsName);
        if (AbstractStep.class.isAssignableFrom(cls)) {
            AbstractStep step = (AbstractStep)cls.newInstance();
            step.configure(jobId, options, serviceDescriptor);
            DefaultServiceRunner serviceRunner = new DefaultServiceRunner(jobId, step, camelContext);
            this.runner = serviceRunner;
            updateStatus(Status.RUNNING);
            serviceRunner.execute(data);
            resultsDir = serviceRunner.getHostWorkDir();
            if (serviceRunner.isResultsReady()) {
                updateStatus(Status.RESULTS_READY);

                handleOutputs(serviceDescriptor, serviceRunner.getHostWorkDir());

                statusMessage = MSG_PROCESSING_RESULTS_READY;
                updateStatus(Status.RESULTS_READY);
            } else {
                LOG.warning("Execution did not complete successfully");
                statusMessage = "Execution did not complete successfully";
                updateStatus(Status.ERROR);
            }
        } else {
            throw new IllegalArgumentException("Executor " + execClsName + " not supported");
        }
    }

    private void doExecuteNextflow(NextflowServiceDescriptor descriptor) throws Exception {

        statusMessage = MSG_PREPARING_CONTAINER;

        String command =  descriptor.getNextflowParams();
        String expandedCommand;
        if (command != null) {
            expandedCommand = expandCommand(command, options);
        } else {
            expandedCommand = "";
        }
        String fullCommand = "nextflow run nextflow.nf " + NEXTFLOW_OPTIONS + " " + expandedCommand;
        ContainerRunner containerRunner = createContainerRunner(NEXTFLOW_IMAGE);
        this.runner = containerRunner;
        containerRunner.init();
        LOG.info("Docker Nextflow executor image: " + NEXTFLOW_IMAGE + ",hostWorkDir: " + containerRunner.getHostWorkDir() + ", command: " + fullCommand);
        resultsDir = containerRunner.getHostWorkDir();

        // create input files
        statusMessage = MSG_PREPARING_INPUT;

        // write the command that executes everything
        LOG.info("Writing command file");
        containerRunner.writeInput("execute", "#!/bin/sh\n" + fullCommand + "\n", true);

        // write the nextflow file that executes everything
        LOG.info("Writing nextflow.nf");
        String nextflowFileContents = descriptor.getNextflowFile();
        containerRunner.writeInput("nextflow.nf", nextflowFileContents, false);

        // write the nextflow config file if one is defined
        String nextflowConfigContents = descriptor.getNextflowConfig();
        if (nextflowConfigContents != null && !nextflowConfigContents.isEmpty()) {
            // An opportunity for the runner to provide extra configuration.
            // There may be nothing to add but the returned string
            // will be valid.
            nextflowConfigContents = containerRunner.addExtraNextflowConfig(nextflowConfigContents);
            LOG.info("Writing nextflow.config as:\n" + nextflowConfigContents);
            containerRunner.writeInput("nextflow.config", nextflowConfigContents, false);
        } else {
            LOG.info("No nextflow.config");
        }

        // The runner's either a plain Docker runner
        // or it's an OpenShift runner.
        if (containerRunner instanceof DockerRunner){
            ((DockerRunner)containerRunner).includeDockerSocket();
        }

        // write the input data
        handleInputs(descriptor, containerRunner);

        // run the command
        statusMessage = MSG_RUNNING_CONTAINER;
        LOG.info("Executing ...");
        long t0 = System.currentTimeMillis();
        int status = containerRunner.execute("./execute");
        long t1 = System.currentTimeMillis();
        float duration = (t1 - t0) / 1000.0f;
        LOG.info(String.format("Executed in %s seconds with return status of %s", duration, status));

        if (status != 0) {
            String log = containerRunner.getLog();
            statusMessage = "Error: " + log;
            LOG.warning("Execution errors: " + log);
            throw new RuntimeException("Container execution failed:\n" + log);
        }

        // handle the output
        statusMessage = MSG_PREPARING_OUTPUT;
        handleOutputs(descriptor, containerRunner.getHostWorkDir());

        Properties props = containerRunner.getFileAsProperties("output_metrics.txt");
        generateMetricsAndStatus(props, duration);

        statusMessage = MSG_PROCESSING_RESULTS_READY;
        updateStatus(Status.RESULTS_READY);
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

        ContainerRunner containerRunner = createContainerRunner(image);
        this.runner = containerRunner;
        containerRunner.init();
        LOG.info("Docker image: " + image + ", hostWorkDir: " + containerRunner.getHostWorkDir() + ", command: " + expandedCommand);
        resultsDir = containerRunner.getHostWorkDir();

        // create input files
        statusMessage = MSG_PREPARING_INPUT;

        // write the command that executes everything
        LOG.info("Writing command file");
        containerRunner.writeInput("execute", "#!/bin/sh\n" + expandedCommand + "\n", true);

        // write the input data
        handleInputs(descriptor, containerRunner);

        // run the command
        statusMessage = MSG_RUNNING_CONTAINER;
        LOG.info("Executing ...");
        updateStatus(Status.RUNNING);
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

        // handle the output
        statusMessage = MSG_PREPARING_OUTPUT;
        handleOutputs(descriptor, containerRunner.getHostWorkDir());

        Properties props = containerRunner.getFileAsProperties("output_metrics.txt");
        generateMetricsAndStatus(props, duration);

        statusMessage = MSG_PROCESSING_RESULTS_READY;
        updateStatus(Status.RESULTS_READY);
    }

    /** Cancel the job if it is running.
     * Cancelling also cleans up the job.
     *
     * @throws Exception
     */
    public void cancel() throws Exception {

        // TODO - implement terminating the runner

        // should cleanup really be done or left to a separate call?
        cleanup();
    }

    /** Cleanup the job removing any resources that it created (files, containers etc.)
     *
     */
    public void cleanup() {
        if (runner != null && DEBUG_MODE < 2) {
            runner.cleanup();
            LOG.info("Results cleaned up");
        }
    }

    protected void handleOutputs(DefaultServiceDescriptor serviceDescriptor, File workdir) throws Exception {

        IODescriptor[] outputDescriptors = serviceDescriptor.resolveOutputIODescriptors();
        if (outputDescriptors != null) {
            LOG.info("Handling " + outputDescriptors.length + " outputs");

            for (IODescriptor iod : outputDescriptors) {
                LOG.info("Reading output for " + iod.getName() + " " + iod.getMediaType());
                doHandleOutput(workdir, iod);
            }
        }
    }

    protected <P, Q> void doHandleOutput(
            File workdir,
            IODescriptor<P, Q> iod) throws Exception {

        List<SquonkDataSource> outputs = buildOutputs(workdir, iod);
        results.put(iod.getName(), outputs);
    }

//    private <P, Q> P doBuildOutput(File workdir, IODescriptor<P, Q> iod) throws Exception {
//
//        VariableHandler<P> vh = DefaultHandler.createVariableHandler(iod.getPrimaryType(), iod.getSecondaryType());
//        VariableHandler.ReadContext readContext = new FilesystemReadContext(workdir, iod.getName());
//        P value = vh.readVariable(readContext);
//        return value;
//    }

    private <P,Q> List<SquonkDataSource> buildOutputs(File workdir, IODescriptor<P, Q> iod) throws Exception {

        VariableHandler<P> vh = DefaultHandler.createVariableHandler(iod.getPrimaryType(), iod.getSecondaryType());
        VariableHandler.ReadContext readContext = new FilesystemReadContext(workdir, iod.getName());
        List<SquonkDataSource> dataSources = vh.readDataSources(readContext);
        return dataSources;
    }

    protected void handleInputs(
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

}
