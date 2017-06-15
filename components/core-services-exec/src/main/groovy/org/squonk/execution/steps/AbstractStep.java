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
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptor;
import org.squonk.core.ServiceDescriptor;
import org.squonk.notebook.api.VariableKey;
import org.squonk.types.TypeResolver;
import org.squonk.util.IOUtils;
import org.squonk.util.Metrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
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
    protected IODescriptor[] inputs;
    protected IODescriptor[] outputs;
    protected final Map<String, VariableKey> inputVariableMappings = new HashMap<>();
    protected final Map<String, String> outputVariableMappings = new HashMap<>();
    protected String statusMessage = null;
    protected ServiceDescriptor serviceDescriptor;

    protected Map<String,Integer> usageStats = new HashMap<>();

    public Map<String, Integer> getUsageStats() {
        return usageStats;
    }


    @Override
    public Long getOutputProducerId() {
        return outputProducerId;
    }

    public void updateStatus(String status)  {
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
        for (Map.Entry<String,VariableKey> e : inputVariableMappings.entrySet()) {
            if (count > 0) {
                b.append(" ");
            }
            count++;
            b.append(e.getKey()).append(" -> ").append(e.getValue());
        }
        b.append("] outputs:[");
        count = 0;
        for (Map.Entry<String,String> e : outputVariableMappings.entrySet()) {
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

    @Override
    public void configure(
            Long outputProducerId,
            String jobId,
            Map<String, Object> options,
            IODescriptor[] inputs,
            IODescriptor[] outputs,
            Map<String, VariableKey> inputVariableMappings,
            Map<String, String> outputVariableMappings,
            ServiceDescriptor serviceDescriptor) {
        this.outputProducerId = outputProducerId;
        this.jobId = jobId;
        this.options = options;
        this.inputs = inputs;
        this.outputs = outputs;
        this.inputVariableMappings.putAll(inputVariableMappings);
        this.outputVariableMappings.putAll(outputVariableMappings);
        this.serviceDescriptor = serviceDescriptor;
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
     * @param required Whether a value is required
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

    /** Get the option value, performing a type conversion if needed
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
                return (T)val;
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

    protected HttpServiceDescriptor getHttpServiceDescriptor() {
        if (serviceDescriptor == null) {
            throw new IllegalStateException("Service descriptor not found");
        } else if (!(serviceDescriptor instanceof HttpServiceDescriptor)) {
            throw new IllegalStateException("Invalid service descriptor. Expected HttpServiceDescriptor but found " + serviceDescriptor.getClass().getSimpleName());
        }
        return (HttpServiceDescriptor)serviceDescriptor;
    }

    protected String getHttpExecutionEndpoint() {
        return getHttpServiceDescriptor().getExecutionEndpoint();
    }

    protected IODescriptor getSingleInputDescriptor() {
        ServiceConfig serviceConfig = getHttpServiceDescriptor().getServiceConfig();
        IODescriptor[] inputDescriptors = serviceConfig.getInputDescriptors();
        IODescriptor inputDescriptor;
        if (inputDescriptors != null && inputDescriptors.length == 1) {
            inputDescriptor = inputDescriptors[0];
        } else if (inputDescriptors == null || inputDescriptors.length == 0 ) {
            throw new IllegalStateException("Expected one input IODescriptor. Found none");
        } else {
            throw new IllegalStateException("Expected one input IODescriptor. Found " + inputDescriptors.length);
        }
        return inputDescriptor;
    }

    protected ThinDescriptor getThinDescriptor(IODescriptor inputDescriptor) {
        ThinDescriptor[] tds = getHttpServiceDescriptor().getThinDescriptors();
        ServiceConfig serviceConfig = getHttpServiceDescriptor().getServiceConfig();
        ThinDescriptor td;
        if (tds == null || tds.length == 0) {
            if (inputDescriptor.getPrimaryType() == Dataset.class) {
                td = new ThinDescriptor(inputDescriptor.getName(), serviceConfig.getOutputDescriptors()[0].getName());
            } else {
                throw new IllegalStateException("Thin execution only suppported for Dataset. Found " + inputDescriptor.getPrimaryType().getName());
            }
        } else if (tds.length == 1) {
            if (tds[0] == null) {
                LOG.warning("ThinDescriptor array provided including a null element. This is bad practice and can lead to problems.");
                td = new ThinDescriptor(inputDescriptor.getName(), serviceConfig.getOutputDescriptors()[0].getName());
            } else {
                td = tds[0];
            }
        } else {
            throw new IllegalStateException("Expected single ThinDescriptor but found " + tds.length);
        }
        return td;
    }

    /** Derrive a new IODescriptor of the specified media type using the specified IODescriptor as the base.
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


    /** Converts the input value of the type specified by the from IODescriptor to the format specified by the to IODescriptor.
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


    /** Generate a standard status message describing the outcome of execution
     *
     * @param total The total number processed. -1 means unknown
     * @param results The number of results. -1 means unknown
     * @param errors The number of errors. -1 means unknown
     * @return
     */
    protected String generateStatusMessage(int total, int results, int errors) {
        LOG.info(String.format("Generating status msg: %s %s %s", total, results, errors));
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
}
