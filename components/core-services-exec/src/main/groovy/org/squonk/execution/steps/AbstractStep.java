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
import org.squonk.execution.ExecutableService;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptor;
import org.squonk.notebook.api.VariableKey;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
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

/**
 * @author timbo
 */
public abstract class AbstractStep extends ExecutableService implements Step, StatusUpdatable {

    private static final Logger LOG = Logger.getLogger(AbstractStep.class.getName());


    protected Long outputProducerId;

    protected final Map<String, VariableKey> inputVariableMappings = new HashMap<>();
    protected final Map<String, String> outputVariableMappings = new HashMap<>();


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

    protected <T> T fetchMappedInput(String internalName, Class<T> primaryType, Class secondaryType, VariableManager varman) throws Exception {
        return fetchMappedInput(internalName, primaryType, secondaryType, varman, false);
    }

    protected <T> T fetchMappedInput(String internalName, Class<T> type, VariableManager varman) throws Exception {
        return fetchMappedInput(internalName, type, null, varman);
    }


    /**
     * Map the variable name using the variable mappings and fetch the
     * corresponding value.
     *
     * @param <T>
     * @param internalName
     * @param primaryType
     * @param secondaryType
     * @param varman
     * @param required     Whether a value is required
     * @return
     * @throws IOException
     * @throws IllegalStateException If required is true and no value is present
     */
    protected <T> T fetchMappedInput(String internalName, Class<T> primaryType, Class secondaryType, VariableManager varman, boolean required) throws Exception {
        VariableKey mappedVar = mapInputVariable(internalName);
        LOG.info("VariableKey mapped to " + internalName + " is " + mappedVar);
        if (mappedVar == null) {
            if (required) {
                throw new IllegalStateException(buildVariableNotFoundMessage(internalName));
            } else {
                return null;
            }
        }
        T input = fetchInput(mappedVar, primaryType, secondaryType, varman);
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
     * @param primaryType
     * @param secondaryType
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> T fetchInput(VariableKey var, Class<T> primaryType, Class secondaryType, VariableManager varman) throws Exception {
        //System.out.println("Getting value for variable " + externalName);
        T value = (T) varman.getValue(var, primaryType, secondaryType);
        return value;
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


    /**
     * Fetch the input using the default name for the input variable
     *
     * @param varman
     * @param runner
     * @param mediaType
     * @return
     * @throws Exception
     */
    protected DatasetMetadata handleDockerInput(VariableManager varman, ContainerRunner runner, String mediaType) throws Exception {
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
    protected DatasetMetadata handleDockerInput(VariableManager varman, ContainerRunner runner, String mediaType, String varName) throws Exception {

        if (mediaType == null) {
            mediaType = CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON;
        }

        switch (mediaType) {
            case CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON:
                Dataset basicDataset = fetchMappedInput(varName, Dataset.class, BasicObject.class, varman, true);
                writeAsDataset(basicDataset, runner);
                return basicDataset.getMetadata();
            case CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON:
                Dataset molDataset = fetchMappedInput(varName, Dataset.class, MoleculeObject.class, varman, true);
                writeAsDataset(molDataset, runner);
                return molDataset.getMetadata();
            case CommonMimeTypes.MIME_TYPE_MDL_SDF:
                InputStream sdf = fetchMappedInput(varName, InputStream.class, null, varman, true);
                writeAsSDF(sdf, runner);
                return null; // TODO can we getServiceDescriptors the metadata somehow?
            default:
                throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }
    }

    protected DatasetMetadata handleDockerOutput(DatasetMetadata inputMetadata, VariableManager varman, ContainerRunner runner, String mediaType) throws Exception {

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

    protected void writeAsDataset(Dataset input, ContainerRunner runner) throws IOException {
        LOG.info("Writing metadata input.meta");
        runner.writeInput("input.meta", JsonHandler.getInstance().objectToJson(input.getMetadata()));
        LOG.info("Writing data input.data.gz");
        runner.writeInput("input.data.gz", input.getInputStream(true));
    }

    protected void writeAsSDF(InputStream sdf, ContainerRunner runner) throws IOException {
        LOG.fine("Writing SDF");
        //runner.writeInput("input.sdf.gz", IOUtils.getGzippedInputStream(sdf));

        String data = IOUtils.convertStreamToString(sdf);
        //LOG.info("DATA: " + data);
        LOG.info("Writing SDF input.sdf.gz");
        runner.writeInput("input.sdf.gz", IOUtils.getGzippedInputStream(new ByteArrayInputStream(data.getBytes())));
    }

    protected DatasetMetadata readAsDataset(DatasetMetadata inputMetadata, VariableManager varman, ContainerRunner runner) throws Exception {
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


    protected DatasetMetadata readAsSDF(DatasetMetadata inputMetadata, VariableManager varman, ContainerRunner runner) throws Exception {

        try (InputStream is = runner.readOutput("output.sdf.gz")) {
            createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, InputStream.class, is, varman);
        }
        // TODO can we get the metadata somehow?
        return null;
    }

}
