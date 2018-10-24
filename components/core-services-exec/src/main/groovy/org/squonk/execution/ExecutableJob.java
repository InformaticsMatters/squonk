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

import org.apache.camel.TypeConverter;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.runners.DockerRunner;
import org.squonk.execution.runners.OpenShiftRunner;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.util.GroovyUtils;
import org.squonk.io.IODescriptor;
import org.squonk.util.IOUtils;
import org.squonk.util.Metrics;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Base class that defines core functionality for job execution including options, inputs/outputs
 * and managing the status of the job.
 */
public class ExecutableJob {

    private Logger LOG = Logger.getLogger(ExecutableJob.class.getName());

    protected static final String MSG_FETCHING_INPUT = "Fetching input ...";
    protected static final String MSG_PREPARING_INPUT = "Preparing input ...";
    protected static final String MSG_PREPARING_OUTPUT = "Writing output ...";
    protected static final String MSG_PROCESSED = "%s processed";
    protected static final String MSG_RESULTS = "%s results";
    protected static final String MSG_ERRORS = "%s errors";
    protected static final String MSG_PROCESSING_COMPLETE = "Processing complete";
    protected static final String MSG_PROCESSING_RESULTS_READY = "Results ready";
    protected static final String MSG_PREPARING_CONTAINER = "Preparing Docker container";
    protected static final String MSG_RUNNING_CONTAINER = "Running Docker container";
    protected static final String MSG_JOB_TOOK_TOO_LONG = "Job took too long to complete - terminating";
    protected static final String MSG_RESULTS_NOT_FETCHED = "Job completed but results were never fetched";

    protected static final String OPTION_DOCKER_COMMAND = StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND;

    protected final String CONTAINER_RUNNER_TYPE = IOUtils.getConfiguration("SQUONK_CONTAINER_RUNNER_TYPE",
            "docker").toLowerCase();

    protected final String DOCKER_SERVICES_DIR = IOUtils.getConfiguration("SQUONK_DOCKER_SERVICES_DIR", "../../data/testfiles/docker-services");
    protected final Integer DEBUG_MODE = new Integer(IOUtils.getConfiguration("SQUONK_DEBUG_MODE", "0"));

    protected String jobId;
    protected Map<String, Object> options;

    protected IODescriptor[] inputs;
    protected IODescriptor[] outputs;
    protected ServiceDescriptor serviceDescriptor;

    protected Map<String, Integer> usageStats = new HashMap<>();

    protected int numProcessed = -1;
    protected int numResults = -1;
    protected int numErrors = -1;

    protected String statusMessage = null;

    public String getJobId() {
        return jobId;
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    public IODescriptor[] getInputs() {
        return inputs;
    }

    public IODescriptor[] getOutputs() {
        return outputs;
    }

    public int getNumProcessed() {
        return numProcessed;
    }

    public int getNumResults() {
        return numResults;
    }

    public int getNumErrors() {
        return numErrors;
    }

    /**
     * Usage stats that will be recorded against the execution of the job
     *
     * @return
     */
    public Map<String, Integer> getUsageStats() {
        return usageStats;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    protected Object getOption(String name) {
        return (options == null ? null : options.get(name));
    }

    protected <T> T getOption(String name, Class<T> type) {
        if (options != null) {
            return (T) options.get(name);
        }
        return null;
    }

    protected <T> T getOption(String name, Class<T> type, T defaultValue) {
        T val = getOption(name, type);
        if (val == null) {
            return defaultValue;
        } else {
            return val;
        }
    }

    protected <T> T getOption(String name, Class<T> type, TypeConverter converter) {
        return getOption(name, type, converter, null);
    }

    /**
     * Get the option value, performing a type conversion if needed
     *
     * @param name
     * @param type
     * @param converter
     * @param defaultValue
     * @param <T>
     * @return
     */
    protected <T> T getOption(String name, Class<T> type, TypeConverter converter, T defaultValue) {
        Object val = getOption(name);
        if (val == null) {
            return defaultValue;
        } else {
            if (type.isAssignableFrom(val.getClass())) {
                return (T) val;
            } else {
                LOG.info("Unexpected option type. Trying to convert from " + val.getClass().getName() + " to " + type.getName());
                return converter.convertTo(type, val);
            }
        }
    }

    /**
     * Take the command template and substitute it with the user specified options.
     * The options are those that begin with 'arg.'
     * The template is a Groovy GString.
     *
     * @param cmdTemplate
     * @param options
     * @return
     */
    protected String expandCommand(String cmdTemplate, Map<String, Object> options) {
        Map<String, Object> args = new LinkedHashMap<>();
        // Inject magical variables that are used to define locations of inputs and outputs.
        // For execution these are set to the empty string.
        args.put("PIN", "");
        args.put("POUT", "");
        options.forEach((k, v) -> {
            if (k.startsWith("arg.")) {
                LOG.fine("Found argument " + k + " = " + v);
                args.put(k.substring(4), v);
            }
        });

        // replace windows line end characters
        String command = cmdTemplate.replaceAll("\\r\\n", "\n");
        LOG.fine("Template: " + command);
        String expandedCommand = GroovyUtils.expandTemplate(command, args);
        LOG.info("Command: " + expandedCommand);
        return expandedCommand;
    }

    protected ContainerRunner createContainerRunner(String image) throws IOException {
        return createContainerRunner(image, null);
    }

    protected ContainerRunner createContainerRunner(String image, String workdir) throws IOException {
        // The CONTAINER_RUNNER_TYPE (environment variable) defines what
        // type of ContainerRunner we produce...

        ContainerRunner runner = null;
        if (CONTAINER_RUNNER_TYPE.equals("docker")) {

            LOG.fine("Creating DockerRunner instance...");
            runner = new DockerRunner(image, workdir, jobId);

        } else if (CONTAINER_RUNNER_TYPE.equals("openshift")) {

            LOG.fine("Creating OpenShiftRunner instance...");
            runner = new OpenShiftRunner(image, workdir, workdir, jobId);

        } else {
            throw new IOException("Unsupported ContainerRunner type: '" + CONTAINER_RUNNER_TYPE + "'");
        }

        return runner;
    }

    /**
     * Generate a standard status message describing the outcome of execution
     *
     * @param total   The total number processed. -1 means unknown
     * @param results The number of results. -1 means unknown
     * @param errors  The number of errors. -1 means unknown
     * @return
     */
    protected String generateStatusMessage(int total, int results, int errors) {
        LOG.fine(String.format("Generating status msg: %s %s %s", total, results, errors));
        List<String> msgs = new ArrayList<>();
        if (total >= 0) {
            msgs.add(String.format(MSG_PROCESSED, total));
        }
        if (results >= 0) {
            msgs.add(String.format(MSG_RESULTS, results));
        }
        if (errors > 0) {
            msgs.add(String.format(MSG_ERRORS, errors));
        }
        if (msgs.isEmpty()) {
            return MSG_PROCESSING_COMPLETE;
        } else {
            return msgs.stream().collect(Collectors.joining(", "));
        }
    }

    protected void generateMetricsAndStatus(Properties props, float executionTimeSeconds) throws IOException {

        statusMessage = generateMetrics(props, executionTimeSeconds);
        if (statusMessage == null) {
            statusMessage = generateStatusMessage(numProcessed, numResults, numErrors);
            LOG.fine("Using generic status message: " + statusMessage);
        }
    }

    /**
     * @param props                The Properties object from which to read the metrics keys and values
     * @param executionTimeSeconds
     * @return The custom status message, if one is specified using the key __StatusMessage__
     */
    protected String generateMetrics(Properties props, float executionTimeSeconds) {

        if (props == null) {
            LOG.warning("Properties is null. Cannot generate metrics.");
            return null;
        }

        String status = null;

        for (String key : props.stringPropertyNames()) {

            if ("__StatusMessage__".equals(key)) {
                String sm = props.getProperty(key);
                if (sm != null) {
                    status = sm;
                    LOG.fine("Custom status message: " + sm);
                }
            } else if ("__InputCount__".equals(key)) {
                numProcessed = tryGetAsInt(props, key);
                LOG.finer("InputCount: " + numProcessed);
            } else if ("__OutputCount__".equals(key)) {
                numResults = tryGetAsInt(props, key);
                LOG.finer("OutputCount: " + numResults);
            } else if ("__ErrorCount__".equals(key)) {
                numErrors = tryGetAsInt(props, key);
                LOG.finer("ErrorCount: " + numErrors);
            } else if (key.startsWith("__") && key.endsWith("__")) {
                LOG.warning("Unexpected magical key: " + key);
            } else {
                int c = tryGetAsInt(props, key);
                if (c >= 0) {
                    usageStats.put(key, c);
                }
            }
        }

        generateExecutionTimeMetrics(executionTimeSeconds);
        return status;
    }

    protected void generateExecutionTimeMetrics(float executionTimeSeconds) {
        float mins = executionTimeSeconds / 60f;
        if (mins > 1) {
            usageStats.put(Metrics.generate(Metrics.PROVIDER_SQUONK, Metrics.METRICS_CPU_MINUTES), Math.round(mins));
        }
    }


    protected int tryGetAsInt(Properties props, String key) {
        String val = props.getProperty(key);
        if (val == null) {
            return -1;
        }
        try {
            return new Integer(val);
        } catch (NumberFormatException nfe) {
            LOG.warning("Failed to read value for " + key + " as integer: " + val);
            return -1;
        }
    }

}
