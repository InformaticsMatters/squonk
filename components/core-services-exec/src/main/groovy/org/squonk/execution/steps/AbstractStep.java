/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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
import org.squonk.core.*;
import org.squonk.dataset.Dataset;
import org.squonk.execution.ExecutableJob;
import org.squonk.io.IODescriptor;
import org.squonk.notebook.api.VariableKey;
import org.squonk.types.BasicObject;
import org.squonk.types.TypeResolver;
import org.squonk.util.Metrics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/** Base class for steps. See {@link Step} for the basics of how steps work.
 *
 * @author timbo
 */
public abstract class AbstractStep extends ExecutableJob implements Step, StatusUpdatable {

    private static final Logger LOG = Logger.getLogger(AbstractStep.class.getName());

    protected final Map<String, VariableKey> inputVariableMappings = new HashMap<>();
    protected final Map<String, String> outputVariableMappings = new HashMap<>();

    @Override
    public Map<String, VariableKey> getInputVariableMappings() {
        return inputVariableMappings;
    }

    @Override
    public Map<String, String> getOutputVariableMappings() {
        return outputVariableMappings;
    }

    /** {@inheritDoc}
     *
     * @param jobId
     * @param options
     * @param serviceDescriptor
     * @param camelContext
     * @param auth
     */
    @Override
    public void configure(
            String jobId,
            Map<String, Object> options,
            ServiceDescriptor serviceDescriptor,
            CamelContext camelContext,
            String auth) {
        this.jobId = jobId;
        this.options = options;
        this.inputs = serviceDescriptor == null ? null : serviceDescriptor.resolveInputIODescriptors();
        this.outputs = serviceDescriptor == null ? null :serviceDescriptor.resolveOutputIODescriptors();
        this.serviceDescriptor = serviceDescriptor;
        this.camelContext = camelContext;
        this.auth = auth;
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


    public Map<String,Object> execute(Map<String,Object> inputs) throws Exception {
        Map<String,Object> preparedInputs = prepareInputs(inputs);
        Map<String,Object> outputs = doExecute(preparedInputs);
        Map<String,Object> preparedOutputs = prepareOutputs(outputs);
        return preparedOutputs;
    }

    public void cleanup() {
        doCleanup();
    }

    protected Map<String,Object> prepareInputs(Map<String,Object> inputs) throws Exception {
        return inputs;
    }

    protected Map<String,Object> prepareOutputs(Map<String,Object> outputs) throws Exception {
        return outputs;
    }

    protected abstract Map<String,Object> doExecute(Map<String,Object> inputs) throws Exception;

    protected void doCleanup() {
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

    protected TypeConverter findTypeConverter() {
        if (camelContext == null) {
            return null;
        } else {
            return camelContext.getTypeConverter();
        }
    }


    /**
     * Converts the input value of the type specified by the from IODescriptor to the format specified by the to IODescriptor.
     *
     * @param from
     * @param to
     * @param value
     * @return
     */
    protected Object convertValue(IODescriptor from, IODescriptor to, Object value) {

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

    protected DefaultServiceDescriptor getDefaultServiceDescriptor() {
        if (serviceDescriptor == null) {
            throw new IllegalStateException("Service descriptor not found");
        } else if (!(serviceDescriptor instanceof DefaultServiceDescriptor)) {
            throw new IllegalStateException("Invalid service descriptor. Expected DefaultServiceDescriptor but found " + serviceDescriptor.getClass().getSimpleName());
        }
        return (DefaultServiceDescriptor)serviceDescriptor;
    }


    protected HttpServiceDescriptor getHttpServiceDescriptor() {
        if (serviceDescriptor == null) {
            throw new IllegalStateException("Service descriptor not found");
        } else if (!(serviceDescriptor instanceof HttpServiceDescriptor)) {
            throw new IllegalStateException("Invalid service descriptor. Expected HttpServiceDescriptor but found " + serviceDescriptor.getClass().getSimpleName());
        }
        return (HttpServiceDescriptor)serviceDescriptor;
    }

    protected DockerServiceDescriptor getDockerServiceDescriptor() {
        if (serviceDescriptor == null) {
            throw new IllegalStateException("Service descriptor not found");
        } else if (!(serviceDescriptor instanceof DockerServiceDescriptor)) {
            throw new IllegalStateException("Invalid service descriptor. Expected DockerServiceDescriptor but found " + serviceDescriptor.getClass().getSimpleName());
        }
        return (DockerServiceDescriptor)serviceDescriptor;
    }

    protected NextflowServiceDescriptor getNextflowServiceDescriptor() {
        if (serviceDescriptor == null) {
            throw new IllegalStateException("Service descriptor not found");
        } else if (!(serviceDescriptor instanceof NextflowServiceDescriptor)) {
            throw new IllegalStateException("Invalid service descriptor. Expected NextflowServiceDescriptor but found " + serviceDescriptor.getClass().getSimpleName());
        }
        return (NextflowServiceDescriptor)serviceDescriptor;
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

    /** The the value as a Dataset. There must be only one item in the map and it must be a Dataset
     *
     * @param inputs
     * @return
     */
    protected Dataset getSingleDatasetFromMap(Map<String, Object> inputs) {
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("Map must have only one value");
        }
        Object value = inputs.values().iterator().next();
        if (value instanceof Dataset) {
            return (Dataset)value;
        } else {
            throw new IllegalArgumentException("Value is not a dataset");
        }
    }

    /** Adds a counter to the stream and when the stream is closed will write a status message as defined by the
     * message param which must be in message format syntax, with the count being passed in as the sole parameter.
     * e.g. use a value like "Processed %s molecules".
     * The contents of the stream are not modified in any way and you must use the updated stream that is returned,
     * not the one that is passed in as a parameter.
     * NOTE: the counting only happens once the stream starts getting consumed, and the status message only gets set
     * once the stream is closed.
     *
     * @param stream
     * @param message
     * @param <T>
     * @return
     */
    protected <T> Stream<T> addStreamCounter(Stream<T> stream, String message) {
        final AtomicInteger count = new AtomicInteger(0);
        return stream.peek((o) -> {
            count.incrementAndGet();
        }).onClose(() -> {
            statusMessage = String.format(message, count.intValue());
        });
    }

    protected <T extends BasicObject> void addResultsCounter(Dataset<T> dataset) throws IOException {
        Stream<T> stream = dataset.getStream();
        final AtomicInteger count = new AtomicInteger(0);
        stream = stream.peek((o) -> {
            count.incrementAndGet();
        }).onClose(() -> {
            numResults = count.intValue();
            statusMessage = generateStatusMessage(numProcessed, numResults, numErrors);
        });
        dataset.replaceStream(stream);
    }
}
