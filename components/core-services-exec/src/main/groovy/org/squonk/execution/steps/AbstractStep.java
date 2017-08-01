/*
 * Copyright (c) 2017 Informatics Matters Ltd.
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

package org.squonk.execution.steps;

import org.apache.camel.CamelContext;
import org.apache.camel.TypeConverter;
import org.apache.camel.spi.TypeConverterRegistry;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptor;
import org.squonk.notebook.api.VariableKey;
import org.squonk.types.BasicObject;
import org.squonk.types.TypeResolver;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;
import org.squonk.util.Metrics;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author timbo
 */
public abstract class AbstractStep implements Step, StatusUpdatable {

    private static final Logger LOG = Logger.getLogger(AbstractStep.class.getName());

    protected Integer DEBUG_MODE = new Integer(IOUtils.getConfiguration("SQUONK_DEBUG_MODE", "0"));

    protected static final String MSG_PREPARING_INPUT = "Preparing input ...";
    protected static final String MSG_PREPARING_OUTPUT = "Writing output ...";
    protected static final String MSG_PROCESSED = "%s processed";
    protected static final String MSG_RESULTS = "%s results";
    protected static final String MSG_ERRORS = "%s errors";
    protected static final String MSG_PROCESSING_COMPLETE = "Processing complete";
    protected static final String MSG_PREPARING_CONTAINER = "Preparing Docker container";
    protected static final String MSG_RUNNING_CONTAINER = "Running Docker container";

    protected Long outputProducerId;
    protected String jobId;
    protected Map<String, Object> options;

    protected final Map<String, VariableKey> inputVariableMappings = new HashMap<>();
    protected final Map<String, String> outputVariableMappings = new HashMap<>();
    protected String statusMessage = null;


    protected Map<String, Integer> usageStats = new HashMap<>();

    protected int numRecordsProcessed = -1;
    protected int numRecordsOutput = -1;
    protected int numErrors = -1;

    public Map<String, Integer> getUsageStats() {
        return usageStats;
    }

    @Override
    public Long getOutputProducerId() {
        return outputProducerId;
    }

    @Override
    public Map<String, VariableKey> getInputVariableMappings() {
        return inputVariableMappings;
    }

    @Override
    public Map<String, String> getOutputVariableMappings() {
        return outputVariableMappings;
    }

    public void updateStatus(String status) {
        statusMessage = status;
    }

    public void dumpConfig(Level level) {
        if (LOG.isLoggable(level)) {
            LOG.log(level, this.toString());
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("Step configuration: [class:").append(this.getClass().getName())
                .append(" producerID:").append(outputProducerId)
                .append(" inputs:[");
        int count = 0;
        for (Map.Entry<String, VariableKey> e : inputVariableMappings.entrySet()) {
            if (count > 0) {
                b.append(" ");
            }
            count++;
            b.append(e.getKey()).append(" -> ").append(e.getValue());
        }
        b.append("] outputs:[");
        count = 0;
        for (Map.Entry<String, String> e : outputVariableMappings.entrySet()) {
            if (count > 0) {
                b.append(" ");
            }
            count++;
            b.append(e.getKey()).append(" -> ").append(e.getValue());
        }
        b.append("]");
        if (options != null) {
            b.append(" options:[");
            count = 0;
            for (Map.Entry<String, Object> e : options.entrySet()) {
                if (count > 0) {
                    b.append(" ");
                }
                count++;
                b.append(e.getKey()).append(" -> ").append(e.getValue());
            }
            b.append("]");
        }
        b.append("]");
        return b.toString();
    }


    protected VariableKey mapInputVariable(String name) {
        VariableKey mapped = inputVariableMappings.get(name);
        return mapped;
    }

    protected String mapOutputVariable(String name) {
        String mapped = outputVariableMappings.get(name);
        return (mapped == null) ? name : mapped;
    }

    protected <T> T fetchMappedInput(String internalName, Class<T> type, VariableManager varman) throws Exception {
        return fetchMappedInput(internalName, type, varman, false);
    }


    /**
     * Map the variable name using the variable mappings and fetch the
     * corresponding value.
     *
     * @param <T>
     * @param internalName
     * @param type
     * @param varman
     * @param required     Whether a value is required
     * @return
     * @throws IOException
     * @throws IllegalStateException If required is true and no value is present
     */
    protected <T> T fetchMappedInput(String internalName, Class<T> type, VariableManager varman, boolean required) throws Exception {
        VariableKey mappedVar = mapInputVariable(internalName);
        LOG.info("VariableKey mapped to " + internalName + " is " + mappedVar);
        if (mappedVar == null) {
            if (required) {
                throw new IllegalStateException(buildVariableNotFoundMessage(internalName));
            } else {
                return null;
            }
        }
        T input = fetchInput(mappedVar, type, varman);
        if (input == null && required) {
            throw new IllegalStateException(buildVariableNotFoundMessage(internalName));
        }
        return input;
    }


    private String buildVariableNotFoundMessage(String internalName) {
        StringBuilder b = new StringBuilder("Mandatory input variable not found: ");
        b.append(internalName).append(". Mappings present:");
        inputVariableMappings.forEach((k, v) -> {
            b.append(" ").append(k).append(" -> ").append(v);
        });
        return b.toString();
    }

    /**
     * Fetch the value with this name
     *
     * @param <T>
     * @param var
     * @param type
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> T fetchInput(VariableKey var, Class<T> type, VariableManager varman) throws Exception {
        //System.out.println("Getting value for variable " + externalName);
        T value = (T) varman.getValue(var, type);
        return value;
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


    //

    /**
     * Map the variable name and then submit it. See {@link #createVariable} for
     * details.
     *
     * @param <T>
     * @param localName
     * @param value
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> void createMappedOutput(String localName, Class<T> type, T value, VariableManager varman) throws Exception {
        String outFldName = mapOutputVariable(localName);
        createVariable(outFldName, type, value, varman);
    }

    /**
     * Creates a variable with the specified name. If the name starts with an
     * underscore (_) then a temporary variable (PersistenceType.NONE) is
     * created, otherwise the provided persistence type is used.
     *
     * @param <T>
     * @param mappedName
     * @param value
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> void createVariable(String mappedName, Class<T> type, T value, VariableManager varman) throws Exception {
        LOG.fine("Creating variable " + mappedName + "  for producer " + getOutputProducerId());
        VariableKey key = new VariableKey(getOutputProducerId(), mappedName);
        varman.putValue(key, type, value);
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    protected void generateExecutionTimeMetrics(float executionTimeSeconds) {
        float mins = executionTimeSeconds / 60f;
        if (mins > 1) {
            usageStats.put(Metrics.generate(Metrics.PROVIDER_SQUONK, Metrics.METRICS_CPU_MINUTES), Math.round(mins));
        }
    }


    /**
     * Derrive a new IODescriptor of the specified media type using the specified IODescriptor as the base.
     * If the media types are identical the base is returned. If they are different then an IODescriptor corresponding to the
     * media type is created (if possible) with the same name as the base.
     *
     * @param mediaType
     * @param base
     * @return
     */
    protected IODescriptor generateIODescriptorForMediaType(String mediaType, IODescriptor base) {
        IODescriptor result;
        if (!base.getMediaType().equals(mediaType)) {
            LOG.info("Required media type is " + mediaType);
            result = TypeResolver.getInstance().createIODescriptor(base.getName(), mediaType);
            if (result.getPrimaryType() == null) {
                throw new IllegalStateException("Don't know how to handle " + mediaType);
            }
        } else {
            result = base;
        }
        return result;
    }


    /**
     * Converts the input value of the type specified by the from IODescriptor to the format specified by the to IODescriptor.
     *
     * @param camelContext
     * @param from
     * @param to
     * @param value
     * @return
     */
    protected Object convertValue(CamelContext camelContext, IODescriptor from, IODescriptor to, Object value) {

        if (from.getMediaType().equals(to.getMediaType())) {
            return value;
        }

        TypeConverterRegistry registry = camelContext.getTypeConverterRegistry();
        TypeConverter typeConverter = registry.lookup(to.getPrimaryType(), from.getPrimaryType());
        if (typeConverter == null) {
            throw new IllegalStateException("No TypeConverter registered for " + from.getMediaType() + " to " + to.getMediaType());
        }
        return typeConverter.convertTo(to.getPrimaryType(), value);
    }

    protected DockerRunner createDockerRunner(String image, String hostWorkDir, String localWorkDir) throws IOException {
        DockerRunner runner = new DockerRunner(image, hostWorkDir, localWorkDir);
        runner.init();
        LOG.info("Using host work dir of " + runner.getHostWorkDir().getPath());
        LOG.info("Using local work dir of " + runner.getLocalWorkDir());
        return runner;
    }

    protected DockerRunner createDockerRunner(String image, String localWorkDir) throws IOException {
        return createDockerRunner(image, null, localWorkDir);
    }

    /**
     * Fetch the input using the default name for the input variable
     *
     * @param varman
     * @param runner
     * @param mediaType
     * @return
     * @throws Exception
     */
    protected DatasetMetadata handleDockerInput(VariableManager varman, DockerRunner runner, String mediaType) throws Exception {
        return handleDockerInput(varman, runner, mediaType, StepDefinitionConstants.VARIABLE_INPUT_DATASET);
    }

    /**
     * Fetch the input in the case that the input has been renamed from the default name
     *
     * @param varman
     * @param runner
     * @param mediaType
     * @param varName
     * @return
     * @throws Exception
     */
    protected DatasetMetadata handleDockerInput(VariableManager varman, DockerRunner runner, String mediaType, String varName) throws Exception {

        if (mediaType == null) {
            mediaType = CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON;
        }

        switch (mediaType) {
            case CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON:
            case CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON:
                Dataset dataset = fetchMappedInput(varName, Dataset.class, varman, true);
                writeAsDataset(dataset, runner);
                return dataset.getMetadata();
            case CommonMimeTypes.MIME_TYPE_MDL_SDF:
                InputStream sdf = fetchMappedInput(varName, InputStream.class, varman, true);
                writeAsSDF(sdf, runner);
                return null; // TODO can we getServiceDescriptors the metadata somehow?
            default:
                throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }
    }

    protected DatasetMetadata handleDockerOutput(DatasetMetadata inputMetadata, VariableManager varman, DockerRunner runner, String mediaType) throws Exception {

        if (mediaType == null) {
            mediaType = CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON;
        }

        switch (mediaType) {
            case CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON:
            case CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON:
                return readAsDataset(inputMetadata, varman, runner);
            case CommonMimeTypes.MIME_TYPE_MDL_SDF:
                return readAsSDF(inputMetadata, varman, runner);
            default:
                throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }
    }

    protected void writeAsDataset(Dataset input, DockerRunner runner) throws IOException {
        LOG.info("Writing metadata input.meta");
        runner.writeInput("input.meta", JsonHandler.getInstance().objectToJson(input.getMetadata()));
        LOG.info("Writing data input.data.gz");
        runner.writeInput("input.data.gz", input.getInputStream(true));
    }

    protected void writeAsSDF(InputStream sdf, DockerRunner runner) throws IOException {
        LOG.fine("Writing SDF");
        //runner.writeInput("input.sdf.gz", IOUtils.getGzippedInputStream(sdf));

        String data = IOUtils.convertStreamToString(sdf);
        //LOG.info("DATA: " + data);
        LOG.info("Writing SDF input.sdf.gz");
        runner.writeInput("input.sdf.gz", IOUtils.getGzippedInputStream(new ByteArrayInputStream(data.getBytes())));
    }

    protected DatasetMetadata readAsDataset(DatasetMetadata inputMetadata, VariableManager varman, DockerRunner runner) throws Exception {
        DatasetMetadata meta;
        try (InputStream is = runner.readOutput("output.meta")) {
            if (is == null) {
                meta = inputMetadata;
            } else {
                meta = JsonHandler.getInstance().objectFromJson(is, DatasetMetadata.class);
            }
        }

        try (InputStream is = runner.readOutput("output.data.gz")) {
            Dataset<? extends BasicObject> dataset = new Dataset(IOUtils.getGunzippedInputStream(is), meta);
            createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, Dataset.class, dataset, varman);
            LOG.fine("Results: " + dataset.getMetadata());
            return dataset.getMetadata();
        }
    }


    protected DatasetMetadata readAsSDF(DatasetMetadata inputMetadata, VariableManager varman, DockerRunner runner) throws Exception {

        try (InputStream is = runner.readOutput("output.sdf.gz")) {
            createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, InputStream.class, is, varman);
        }
        // TODO can we get the metadata somehow?
        return null;
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
            statusMessage = generateStatusMessage(numRecordsProcessed, numRecordsOutput, numErrors);
            LOG.fine("Using generic status message: " + statusMessage);
        }
    }

        /**
     *
     * @param props The Properties object from which to read the metrics keys and values
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
                numRecordsProcessed = tryGetAsInt(props, key);
            } else if ("__OutputCount__".equals(key)) {
                numRecordsOutput = tryGetAsInt(props, key);
            } else if ("__ErrorCount__".equals(key)) {
                numErrors = tryGetAsInt(props, key);
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


    private int tryGetAsInt(Properties props, String key) {
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
